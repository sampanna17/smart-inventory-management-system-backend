package com.smartinventorysystem.modules.purchase.service;

import com.smartinventorysystem.enums.PurchaseStatus;
import com.smartinventorysystem.exceptions.BadRequestException;
import com.smartinventorysystem.exceptions.ResourceNotFoundException;
import com.smartinventorysystem.modules.product.entity.Product;
import com.smartinventorysystem.modules.product.repository.ProductRepository;
import com.smartinventorysystem.modules.productsupplier.repository.ProductSupplierRepository;
import com.smartinventorysystem.modules.purchase.dto.request.CreatePurchaseRequest;
import com.smartinventorysystem.modules.purchase.dto.request.PurchaseItemRequest;
import com.smartinventorysystem.modules.purchase.dto.request.UpdatePurchaseRequest;
import com.smartinventorysystem.modules.purchase.dto.request.UpdatePurchaseStatusRequest;
import com.smartinventorysystem.modules.purchase.dto.response.PurchaseResponse;
import com.smartinventorysystem.modules.purchase.entity.Purchase;
import com.smartinventorysystem.modules.purchase.entity.PurchaseDetail;
import com.smartinventorysystem.modules.purchase.mapper.PurchaseMapper;
import com.smartinventorysystem.modules.purchase.repository.PurchaseRepository;
import com.smartinventorysystem.modules.stockmovement.service.StockMovementService;
import com.smartinventorysystem.modules.supplier.entity.Supplier;
import com.smartinventorysystem.modules.supplier.repository.SupplierRepository;
import com.smartinventorysystem.modules.user.entity.User;
import com.smartinventorysystem.modules.user.service.UserService;
import com.smartinventorysystem.common.dto.PageResponse;
import com.smartinventorysystem.enums.MovementType;
import com.smartinventorysystem.enums.NotificationType;
import com.smartinventorysystem.modules.notification.service.NotificationService;
import com.smartinventorysystem.modules.purchase.dto.request.PurchaseFilterRequest;
import com.smartinventorysystem.modules.purchase.specification.PurchaseSpecification;
import lombok.RequiredArgsConstructor;
import com.smartinventorysystem.utils.AuthenticatedUserProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional("simsTransactionManager")
public class PurchaseServiceImpl implements PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final ProductSupplierRepository productSupplierRepository;
    private final StockMovementService stockMovementService;
    private final UserService userService;
    private final PurchaseMapper purchaseMapper;
    private final Clock clock;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final NotificationService notificationService;

    private static final String PURCHASE_NOT_FOUND = "Purchase not found with ID: ";

    @Override
    @Transactional("simsTransactionManager")
    public PurchaseResponse createPurchase(CreatePurchaseRequest request) {
        User user = authenticatedUserProvider.getCurrentUser();

        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with ID: " + request.getSupplierId()));

        Purchase purchase = new Purchase();
        purchase.setSupplier(supplier);
        purchase.setUserID(user.getUserID());
        purchase.setPurchaseDate(request.getPurchaseDate());
        purchase.setStatus(PurchaseStatus.PENDING);

        // Generate unique purchase number (length <= 30)
        String purchaseNumber = "PO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        while (purchaseRepository.findByPurchaseNumber(purchaseNumber).isPresent()) {
            purchaseNumber = "PO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        purchase.setPurchaseNumber(purchaseNumber);
        purchase.setCreatedAt(LocalDateTime.now(clock));
        purchase.setUpdatedAt(LocalDateTime.now(clock));

        List<PurchaseDetail> details = new ArrayList<>();
        for (PurchaseItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + itemReq.getProductId()));

            validateProductSupplier(product, supplier);

            PurchaseDetail detail = new PurchaseDetail();
            detail.setPurchase(purchase);
            detail.setProduct(product);
            detail.setQuantity(itemReq.getQuantity());
            detail.setUnitPrice(itemReq.getUnitPrice());
            detail.setSubTotal(itemReq.getUnitPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())));
            details.add(detail);
        }
        purchase.getPurchaseDetails().addAll(details);

        BigDecimal totalAmount = purchase.getPurchaseDetails().stream()
                .map(PurchaseDetail::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        purchase.setTotalAmount(totalAmount);

        Purchase savedPurchase = purchaseRepository.save(purchase);

        notificationService.notifyUserAndAdmins(
                user.getUserID(),
                "Purchase Order Placed",
                "Purchase order " + savedPurchase.getPurchaseNumber() + " for NPR " + totalAmount + " has been placed.",
                NotificationType.ORDER_PLACED
        );

        PurchaseResponse response = purchaseMapper.toResponse(savedPurchase);
        response.setUserName(user.getFullName());

        return response;
    }

    @Override
    @Transactional("simsTransactionManager")
    public PurchaseResponse updatePurchase(Integer purchaseId, UpdatePurchaseRequest request) {
        Purchase purchase = purchaseRepository.findByIdWithDetails(purchaseId)
                .orElseThrow(() -> new ResourceNotFoundException(PURCHASE_NOT_FOUND + purchaseId));

        if (purchase.getStatus() != PurchaseStatus.PENDING) {
            throw new BadRequestException("Only pending purchases can be updated.");
        }

        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with ID: " + request.getSupplierId()));

        purchase.setSupplier(supplier);
        purchase.setPurchaseDate(request.getPurchaseDate());

        // Update purchase details
        purchase.getPurchaseDetails().clear();

        List<PurchaseDetail> details = new ArrayList<>();
        for (PurchaseItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + itemReq.getProductId()));

            validateProductSupplier(product, supplier);

            PurchaseDetail detail = new PurchaseDetail();
            detail.setPurchase(purchase);
            detail.setProduct(product);
            detail.setQuantity(itemReq.getQuantity());
            detail.setUnitPrice(itemReq.getUnitPrice());
            detail.setSubTotal(itemReq.getUnitPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())));
            details.add(detail);
        }
        purchase.getPurchaseDetails().addAll(details);

        BigDecimal totalAmount = purchase.getPurchaseDetails().stream()
                .map(PurchaseDetail::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        purchase.setTotalAmount(totalAmount);
        purchase.setUpdatedAt(LocalDateTime.now(clock));

        Purchase updatedPurchase = purchaseRepository.save(purchase);

        PurchaseResponse response = purchaseMapper.toResponse(updatedPurchase);

        response.setUserName(
                userService.getUserFullName(purchase.getUserID())
        );

        return response;
    }

    @Override
    @Transactional("simsTransactionManager")
    public void deletePurchase(Integer purchaseId) {
        Purchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new ResourceNotFoundException(PURCHASE_NOT_FOUND + purchaseId));

        if (purchase.getStatus() == PurchaseStatus.RECEIVED) {
            throw new BadRequestException("Cannot delete a received purchase.");
        }

        purchaseRepository.delete(purchase);
    }

    @Override
    public PurchaseResponse getPurchaseById(Integer purchaseId) {
        Purchase purchase = purchaseRepository.findByIdWithDetails(purchaseId)
                .orElseThrow(() -> new ResourceNotFoundException(PURCHASE_NOT_FOUND + purchaseId));

        PurchaseResponse response = purchaseMapper.toResponse(purchase);

        response.setUserName(
                userService.getUserFullName(purchase.getUserID())
        );

        return response;
    }

    @Override
    @Transactional(value = "simsTransactionManager", readOnly = true)
    public List<PurchaseResponse> getAllPurchases() {
        List<Purchase> purchases = purchaseRepository.findAllWithDetails();
        List<PurchaseResponse> responses = purchaseMapper.toResponseList(purchases);
        if (responses == null) {
            return new ArrayList<>();
        }

        List<Integer> userIds = responses.stream()
                .map(PurchaseResponse::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Integer, String> userNames = userService.getUserFullNames(userIds);
        if (userNames != null) {
            responses.forEach(response -> {
                if (response.getUserId() != null) {
                    response.setUserName(userNames.get(response.getUserId()));
                }
            });
        }
        return responses;
    }

    @Override
    @Transactional(value = "simsTransactionManager", readOnly = true)
    public PageResponse<PurchaseResponse> getPurchases(PurchaseFilterRequest request) {
        Pageable pageable = createPageable(request);
        Specification<Purchase> specification = PurchaseSpecification.withFilters(request);

        Page<Purchase> purchasePage = purchaseRepository.findAll(specification, pageable);
        if (purchasePage.isEmpty()) {
            return PageResponse.<PurchaseResponse>builder()
                    .content(new ArrayList<>())
                    .pageNumber(purchasePage.getNumber())
                    .pageSize(purchasePage.getSize())
                    .totalElements(purchasePage.getTotalElements())
                    .totalPages(purchasePage.getTotalPages())
                    .first(purchasePage.isFirst())
                    .last(purchasePage.isLast())
                    .hasNext(purchasePage.hasNext())
                    .hasPrevious(purchasePage.hasPrevious())
                    .build();
        }

        List<Integer> purchaseIds = purchasePage.getContent().stream()
                .map(Purchase::getPurchaseId)
                .toList();

        List<Purchase> detailedPurchases = purchaseRepository.findAllByIdInWithDetails(purchaseIds);

        Map<Integer, Purchase> purchaseMap = new HashMap<>();
        detailedPurchases.forEach(p -> purchaseMap.put(p.getPurchaseId(), p));

        List<Purchase> orderedPurchases = purchaseIds.stream()
                .map(purchaseMap::get)
                .filter(Objects::nonNull)
                .toList();

        List<PurchaseResponse> content = purchaseMapper.toResponseList(orderedPurchases);
        if (content == null) {
            content = new ArrayList<>();
        }

        List<Integer> userIds = content.stream()
                .map(PurchaseResponse::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Integer, String> userNames = userService.getUserFullNames(userIds);
        if (userNames != null) {
            content.forEach(res -> {
                if (res.getUserId() != null) {
                    res.setUserName(userNames.get(res.getUserId()));
                }
            });
        }

        return PageResponse.<PurchaseResponse>builder()
                .content(content)
                .pageNumber(purchasePage.getNumber())
                .pageSize(purchasePage.getSize())
                .totalElements(purchasePage.getTotalElements())
                .totalPages(purchasePage.getTotalPages())
                .first(purchasePage.isFirst())
                .last(purchasePage.isLast())
                .hasNext(purchasePage.hasNext())
                .hasPrevious(purchasePage.hasPrevious())
                .build();
    }

    private Pageable createPageable(PurchaseFilterRequest request) {
        int page = (request != null && request.getPage() != null) ? request.getPage() : 0;
        int size = (request != null && request.getSize() != null) ? request.getSize() : 10;

        String sortBy = (request != null && request.getSortBy() != null) ? request.getSortBy() : "purchaseDate";
        String sortDir = (request != null && request.getSortDir() != null) ? request.getSortDir() : "desc";

        String targetProperty = mapSortProperty(sortBy);
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;

        Sort sort = Sort.by(direction, targetProperty);
        return PageRequest.of(page, size, sort);
    }

    private String mapSortProperty(String sortBy) {
        if (sortBy == null) {
            return "purchaseDate";
        }
        return switch (sortBy.trim().toLowerCase()) {
            case "purchasenumber", "number" -> "purchaseNumber";
            case "date", "purchasedate" -> "purchaseDate";
            case "amount", "totalamount" -> "totalAmount";
            case "status" -> "status";
            case "createdat" -> "createdAt";
            case "id", "purchaseid" -> "purchaseId";
            default -> "purchaseDate";
        };
    }

    @Override
    @Transactional("simsTransactionManager")
    public PurchaseResponse updatePurchaseStatus(Integer purchaseId, UpdatePurchaseStatusRequest request) {
        Purchase purchase = purchaseRepository.findByIdWithDetails(purchaseId)
                .orElseThrow(() -> new ResourceNotFoundException(PURCHASE_NOT_FOUND+ purchaseId));

        PurchaseStatus oldStatus = purchase.getStatus();
        PurchaseStatus newStatus = request.getStatus();

        if (oldStatus == newStatus) {
            return purchaseMapper.toResponse(purchase);
        }

        // Adjust stock quantities based on status transitions
        if (newStatus == PurchaseStatus.RECEIVED) {
            for (PurchaseDetail detail : purchase.getPurchaseDetails()) {
                Product product = detail.getProduct();
                int currentStock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
                product.setStockQuantity(currentStock + detail.getQuantity());
                productRepository.save(product);
                stockMovementService.recordMovement(
                        product,
                        detail.getQuantity(),
                        MovementType.PURCHASE,
                        purchase.getUserID(),
                        "Stock received from purchase " + purchase.getPurchaseNumber()
                );
            }
        } else if (oldStatus == PurchaseStatus.RECEIVED) {
            for (PurchaseDetail detail : purchase.getPurchaseDetails()) {
                Product product = detail.getProduct();
                int currentStock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
                product.setStockQuantity(currentStock - detail.getQuantity());
                productRepository.save(product);
                stockMovementService.recordMovement(
                        product,
                        detail.getQuantity(),
                        MovementType.ADJUSTMENT,
                        purchase.getUserID(),
                        "Purchase receipt reversed for " + purchase.getPurchaseNumber()
                );
            }
        }

        purchase.setStatus(newStatus);
        purchase.setUpdatedAt(LocalDateTime.now(clock));

        Purchase updatedPurchase = purchaseRepository.save(purchase);

        if (newStatus == PurchaseStatus.RECEIVED) {
            notificationService.notifyUserAndAdmins(
                    purchase.getUserID(),
                    "Purchase Order Received",
                    "Purchase order " + purchase.getPurchaseNumber() + " items have been received into inventory.",
                    NotificationType.GENERAL
            );
        }

        PurchaseResponse response = purchaseMapper.toResponse(updatedPurchase);

        response.setUserName(
                userService.getUserFullName(purchase.getUserID())
        );

        return response;
    }

    @Override
    public List<PurchaseResponse> getPurchasesBySupplier(Integer supplierId) {

        if (!supplierRepository.existsById(supplierId)) {
            throw new ResourceNotFoundException(
                    "Supplier not found with ID: " + supplierId
            );
        }

        List<Purchase> purchases =
                purchaseRepository.findBySupplierWithDetails(supplierId);

        List<PurchaseResponse> responses =
                purchaseMapper.toResponseList(purchases);

        List<Integer> userIds = responses.stream()
                .map(PurchaseResponse::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Integer, String> userNames = userService.getUserFullNames(userIds);

        responses.forEach(response ->
                response.setUserName(userNames.get(response.getUserId()))
        );

        return responses;
    }

    private void validateProductSupplier(Product product, Supplier supplier) {
        if (!productSupplierRepository.existsByProductProductIdAndSupplierSupplierId(
                product.getProductId(),
                supplier.getSupplierId()
        )) {
            throw new BadRequestException(
                    "Supplier '" + supplier.getSupplierName() +
                            "' is not assigned to product '" +
                            product.getProductName() + "'"
            );
        }
    }
}

