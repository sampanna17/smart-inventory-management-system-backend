package com.smartinventorysystem.modules.sale.service;

import com.smartinventorysystem.constants.MessageConstants;
import com.smartinventorysystem.enums.MovementType;
import com.smartinventorysystem.enums.SaleStatus;
import com.smartinventorysystem.exceptions.ResourceNotFoundException;
import com.smartinventorysystem.exceptions.InsufficientStockException;
import com.smartinventorysystem.exceptions.InvalidSaleStatusException;
import com.smartinventorysystem.modules.customer.entity.Customer;
import com.smartinventorysystem.modules.customer.repository.CustomerRepository;
import com.smartinventorysystem.modules.product.entity.Product;
import com.smartinventorysystem.modules.product.repository.ProductRepository;
import com.smartinventorysystem.modules.sale.dto.request.CreateSaleDetailRequest;
import com.smartinventorysystem.modules.sale.dto.request.CreateSaleRequest;
import com.smartinventorysystem.modules.sale.dto.request.UpdateSaleDetailRequest;
import com.smartinventorysystem.modules.sale.dto.request.UpdateSaleRequest;
import com.smartinventorysystem.modules.sale.dto.request.UpdateSaleStatusRequest;
import com.smartinventorysystem.modules.sale.dto.response.SaleResponse;
import com.smartinventorysystem.modules.sale.dto.response.SaleSummaryResponse;
import com.smartinventorysystem.modules.sale.entity.Sale;
import com.smartinventorysystem.modules.sale.entity.SaleDetail;
import com.smartinventorysystem.modules.sale.mapper.SaleMapper;
import com.smartinventorysystem.modules.sale.repository.SaleRepository;
import com.smartinventorysystem.modules.sale.repository.SaleDetailRepository;
import com.smartinventorysystem.modules.stockmovement.service.StockMovementService;
import com.smartinventorysystem.modules.user.entity.User;
import com.smartinventorysystem.modules.user.service.UserService;
import com.smartinventorysystem.common.dto.PageResponse;
import com.smartinventorysystem.modules.sale.dto.request.SaleFilterRequest;
import com.smartinventorysystem.modules.sale.specification.SaleSpecification;
import com.smartinventorysystem.enums.NotificationType;
import com.smartinventorysystem.modules.notification.service.NotificationService;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
@Transactional("simsTransactionManager")
public class SaleServiceImpl implements SaleService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final SaleRepository saleRepository;
    private final SaleDetailRepository saleDetailRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final StockMovementService stockMovementService;
    private final UserService userService;
    private final SaleMapper saleMapper;
    private final Clock clock;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final NotificationService notificationService;

    @Override
    @Transactional("simsTransactionManager")
    public SaleResponse createSale(CreateSaleRequest request) {
        User user = authenticatedUserProvider.getCurrentUser();
        Customer customer = getCustomerIfPresent(request.getCustomerId());

        Sale sale = buildNewSale(request, user, customer);
        ProcessedSaleItems processed = processSaleItems(sale, request.getItems(), user.getUserID());

        sale.setTotalAmount(processed.totalAmount());
        Sale savedSale = saleRepository.save(sale);
        List<SaleDetail> savedDetails = saleDetailRepository.saveAll(processed.details());

        sendSaleOrderNotification(user, savedSale);
        checkAndNotifyStockAlerts(user, savedDetails);

        SaleResponse response = saleMapper.toResponse(savedSale, savedDetails);
        response.setUserName(user.getFullName());
        return response;
    }

    private record ProcessedSaleItems(List<SaleDetail> details, BigDecimal totalAmount) {}

    private Sale buildNewSale(CreateSaleRequest request, User user, Customer customer) {
        Sale sale = new Sale();
        sale.setCustomer(customer);
        sale.setUserID(user.getUserID());
        sale.setSaleDate(request.getSaleDate());
        sale.setStatus(SaleStatus.COMPLETED);
        sale.setInvoiceNumber(generateInvoiceNumber());
        sale.setCreatedAt(LocalDateTime.now(clock));
        return sale;
    }

    private ProcessedSaleItems processSaleItems(Sale sale, List<CreateSaleDetailRequest> items, Integer userId) {
        List<SaleDetail> details = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CreateSaleDetailRequest itemReq : items) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageConstants.PRODUCT_NOT_FOUND_MSG + itemReq.getProductId()));

            validateAndDeductStock(product, itemReq.getQuantity());
            stockMovementService.recordMovement(
                    product,
                    itemReq.getQuantity(),
                    MovementType.SALE,
                    userId,
                    "Stock deducted for sale " + sale.getInvoiceNumber()
            );

            SaleDetail detail = new SaleDetail();
            detail.setSale(sale);
            detail.setProduct(product);
            detail.setQuantity(itemReq.getQuantity());
            detail.setUnitPrice(product.getSellingPrice());

            BigDecimal subTotal = product.getSellingPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            detail.setSubTotal(subTotal);

            totalAmount = totalAmount.add(subTotal);
            details.add(detail);
        }

        return new ProcessedSaleItems(details, totalAmount);
    }

    private void sendSaleOrderNotification(User user, Sale savedSale) {
        notificationService.notifyUserAndAdmins(
                user.getUserID(),
                "Sales Order Placed",
                "Sales invoice " + savedSale.getInvoiceNumber() + " for NPR " + savedSale.getTotalAmount() + " has been placed.",
                NotificationType.ORDER_PLACED
        );
    }

    private void checkAndNotifyStockAlerts(User user, List<SaleDetail> details) {
        for (SaleDetail detail : details) {
            Product product = detail.getProduct();
            int remainingStock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
            if (remainingStock <= 0) {
                notificationService.notifyUserAndAdmins(
                    user.getUserID(),
                    "Out of Stock Alert",
                    "Product '" + product.getProductName() + "' is out of stock (Quantity: 0).",
                    NotificationType.OUT_OF_STOCK
                );
            } else if (product.getReorderLevel() != null && remainingStock <= product.getReorderLevel()) {
                notificationService.notifyUserAndAdmins(
                    user.getUserID(),
                    "Low Stock Alert",
                    "Product '" + product.getProductName() + "' is running low on stock (Remaining: " + remainingStock + ", Reorder Level: " + product.getReorderLevel() + ").",
                    NotificationType.LOW_STOCK
                );
            }
        }
    }

    @Override
    @Transactional("simsTransactionManager")
    public SaleResponse updateSale(Integer saleId, UpdateSaleRequest request) {
        Sale sale = saleRepository.findByIdWithCustomer(saleId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstants.SALE_NOT_FOUND_MSG + saleId));

        if (sale.getStatus() != SaleStatus.COMPLETED) {
            throw new InvalidSaleStatusException("Only COMPLETED sales can be edited.");
        }

        Integer effectiveUserId = sale.getUserID() != null 
                ? sale.getUserID() 
                : authenticatedUserProvider.getCurrentUserId();

        Customer customer = getCustomerIfPresent(request.getCustomerId());
        sale.setCustomer(customer);
        sale.setSaleDate(request.getSaleDate());

        List<SaleDetail> oldDetails = saleDetailRepository.findBySaleIdWithProduct(saleId);
        restoreStock(
                oldDetails,
                effectiveUserId,
                "Sale stock restored for update " + sale.getInvoiceNumber()
        );

        saleDetailRepository.deleteAll(oldDetails);
        saleDetailRepository.flush();

        List<SaleDetail> newDetails = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (UpdateSaleDetailRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageConstants.PRODUCT_NOT_FOUND_MSG + itemReq.getProductId()));

            validateAndDeductStock(product, itemReq.getQuantity());
            stockMovementService.recordMovement(
                    product,
                    itemReq.getQuantity(),
                    MovementType.SALE,
                    effectiveUserId,
                    "Stock deducted for sale update " + sale.getInvoiceNumber()
            );

            SaleDetail detail = new SaleDetail();
            detail.setSale(sale);
            detail.setProduct(product);
            detail.setQuantity(itemReq.getQuantity());
            detail.setUnitPrice(product.getSellingPrice());

            BigDecimal subTotal = product.getSellingPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            detail.setSubTotal(subTotal);

            totalAmount = totalAmount.add(subTotal);
            newDetails.add(detail);
        }

        sale.setTotalAmount(totalAmount);

        List<SaleDetail> savedDetails = saleDetailRepository.saveAll(newDetails);
        Sale updatedSale = saleRepository.save(sale);

        SaleResponse response = saleMapper.toResponse(updatedSale, savedDetails);
        response.setUserName(userService.getUserFullName(updatedSale.getUserID()));
        return response;
    }

    @Override
    @Transactional("simsTransactionManager")
    public void deleteSale(Integer saleId) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstants.SALE_NOT_FOUND_MSG + saleId));

        List<SaleDetail> details = saleDetailRepository.findBySaleIdWithProduct(saleId);

        if (sale.getStatus() == SaleStatus.COMPLETED) {
            Integer effectiveUserId = sale.getUserID() != null 
                    ? sale.getUserID() 
                    : authenticatedUserProvider.getCurrentUserId();

            restoreStock(
                    details,
                    effectiveUserId,
                    "Sale deleted and stock restored " + sale.getInvoiceNumber()
            );
        }

        saleDetailRepository.deleteAll(details);
        saleRepository.delete(sale);
    }

    @Override
    @Transactional("simsTransactionManager")
    public SaleResponse updateStatus(Integer saleId, UpdateSaleStatusRequest request) {
        Sale sale = saleRepository.findByIdWithCustomer(saleId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstants.SALE_NOT_FOUND_MSG + saleId));

        SaleStatus oldStatus = sale.getStatus();
        SaleStatus newStatus = request.getStatus();

        if (oldStatus == newStatus) {
            List<SaleDetail> details = saleDetailRepository.findBySaleIdWithProduct(saleId);
            SaleResponse response = saleMapper.toResponse(sale, details);
            response.setUserName(userService.getUserFullName(sale.getUserID()));
            return response;
        }

        List<SaleDetail> details = saleDetailRepository.findBySaleIdWithProduct(saleId);

        if (oldStatus == SaleStatus.COMPLETED && (newStatus == SaleStatus.CANCELLED || newStatus == SaleStatus.REFUNDED)) {
            restoreStock(
                    details,
                    sale.getUserID(),
                    "Sale stock restored for status update " + sale.getInvoiceNumber()
            );
        } else if ((oldStatus == SaleStatus.CANCELLED || oldStatus == SaleStatus.REFUNDED) && newStatus == SaleStatus.COMPLETED) {
            deductStock(
                    details,
                    sale.getUserID(),
                    "Stock deducted for sale status update " + sale.getInvoiceNumber()
            );
        }

        sale.setStatus(newStatus);
        Sale updatedSale = saleRepository.save(sale);

        SaleResponse response = saleMapper.toResponse(updatedSale, details);
        response.setUserName(userService.getUserFullName(updatedSale.getUserID()));
        return response;
    }

    @Override
    @Transactional(value = "simsTransactionManager", readOnly = true)
    public SaleResponse getSale(Integer saleId) {
        Sale sale = saleRepository.findByIdWithCustomer(saleId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstants.SALE_NOT_FOUND_MSG + saleId));

        List<SaleDetail> details = saleDetailRepository.findBySaleIdWithProduct(saleId);

        SaleResponse response = saleMapper.toResponse(sale, details);
        response.setUserName(userService.getUserFullName(sale.getUserID()));
        return response;
    }

    @Override
    @Transactional(value = "simsTransactionManager", readOnly = true)
    public List<SaleSummaryResponse> getAllSales() {
        List<Sale> sales = saleRepository.findAllWithCustomer();
        return mapToSummaryResponses(sales);
    }

    @Override
    @Transactional(value = "simsTransactionManager", readOnly = true)
    public PageResponse<SaleSummaryResponse> getSales(SaleFilterRequest request) {
        Pageable pageable = createPageable(request);
        Specification<Sale> specification = SaleSpecification.withFilters(request);

        Page<Sale> salePage = saleRepository.findAll(specification, pageable);
        if (salePage.isEmpty()) {
            return PageResponse.<SaleSummaryResponse>builder()
                    .content(new ArrayList<>())
                    .pageNumber(salePage.getNumber())
                    .pageSize(salePage.getSize())
                    .totalElements(salePage.getTotalElements())
                    .totalPages(salePage.getTotalPages())
                    .first(salePage.isFirst())
                    .last(salePage.isLast())
                    .hasNext(salePage.hasNext())
                    .hasPrevious(salePage.hasPrevious())
                    .build();
        }

        List<Integer> saleIds = salePage.getContent().stream()
                .map(Sale::getSaleID)
                .toList();

        List<Sale> detailedSales = saleRepository.findAllByIdInWithCustomer(saleIds);

        Map<Integer, Sale> saleMap = new HashMap<>();
        detailedSales.forEach(s -> saleMap.put(s.getSaleID(), s));

        List<Sale> orderedSales = saleIds.stream()
                .map(saleMap::get)
                .filter(Objects::nonNull)
                .toList();

        List<SaleSummaryResponse> content = mapToSummaryResponses(orderedSales);

        return PageResponse.<SaleSummaryResponse>builder()
                .content(content)
                .pageNumber(salePage.getNumber())
                .pageSize(salePage.getSize())
                .totalElements(salePage.getTotalElements())
                .totalPages(salePage.getTotalPages())
                .first(salePage.isFirst())
                .last(salePage.isLast())
                .hasNext(salePage.hasNext())
                .hasPrevious(salePage.hasPrevious())
                .build();
    }

    private Pageable createPageable(SaleFilterRequest request) {
        int page = (request != null && request.getPage() != null) ? request.getPage() : 0;
        int size = (request != null && request.getSize() != null) ? request.getSize() : 10;

        String sortBy = (request != null && request.getSortBy() != null) ? request.getSortBy() : "saleDate";
        String sortDir = (request != null && request.getSortDir() != null) ? request.getSortDir() : "desc";

        String targetProperty = mapSortProperty(sortBy);
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;

        Sort sort = Sort.by(direction, targetProperty);
        return PageRequest.of(page, size, sort);
    }

    private String mapSortProperty(String sortBy) {
        if (sortBy == null) {
            return "saleDate";
        }
        return switch (sortBy.trim().toLowerCase()) {
            case "invoicenumber", "invoice", "number" -> "invoiceNumber";
            case "date", "saledate" -> "saleDate";
            case "amount", "totalamount" -> "totalAmount";
            case "status" -> "status";
            case "createdat" -> "createdAt";
            case "id", "saleid" -> "saleID";
            default -> "saleDate";
        };
    }

    @Override
    @Transactional(value = "simsTransactionManager", readOnly = true)
    public List<SaleSummaryResponse> getSalesByCustomer(Integer customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer not found with ID: " + customerId);
        }
        List<Sale> sales = saleRepository.findByCustomerWithCustomer(customerId);
        return mapToSummaryResponses(sales);
    }

    @Override
    @Transactional(value = "simsTransactionManager", readOnly = true)
    public List<SaleSummaryResponse> getSalesByStatus(SaleStatus status) {
        List<Sale> sales = saleRepository.findByStatusWithCustomer(status);
        return mapToSummaryResponses(sales);
    }

    private Customer getCustomerIfPresent(Integer customerId) {
        if (customerId == null) {
            return null;
        }
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));
    }

    private String generateInvoiceNumber() {
        String dateStr = DateTimeFormatter.ofPattern("yyyyMMdd")
                .format(LocalDateTime.now(clock));

        String invoiceNumber;

        do {
            String randomSuffix = String.format("%05d", RANDOM.nextInt(100000));
            invoiceNumber = "INV-" + dateStr + "-" + randomSuffix;

        } while (saleRepository.findByInvoiceNumber(invoiceNumber).isPresent());

        return invoiceNumber;
    }

    private void validateAndDeductStock(Product product, int quantity) {
        int currentStock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
        if (currentStock < quantity) {
            throw new InsufficientStockException("Insufficient stock for product: " + product.getProductName() + ". Available: " + currentStock + ", Requested: " + quantity);
        }
        product.setStockQuantity(currentStock - quantity);
        productRepository.save(product);
    }

    private void restoreStock(List<SaleDetail> details, Integer userId, String remarks) {
        for (SaleDetail detail : details) {
            Product product = detail.getProduct();
            int currentStock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
            product.setStockQuantity(currentStock + detail.getQuantity());
            productRepository.save(product);

            stockMovementService.recordMovement(
                    product,
                    detail.getQuantity(),
                    MovementType.ADJUSTMENT,
                    userId,
                    remarks
            );
        }
    }

    private void deductStock(List<SaleDetail> details, Integer userId, String remarks) {
        for (SaleDetail detail : details) {
            Product product = detail.getProduct();
            int currentStock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
            if (currentStock < detail.getQuantity()) {
                throw new InsufficientStockException("Insufficient stock for product: " + product.getProductName() + ". Available: " + currentStock + ", Requested: " + detail.getQuantity());
            }
        }

        for (SaleDetail detail : details) {
            Product product = detail.getProduct();
            int currentStock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
            product.setStockQuantity(currentStock - detail.getQuantity());
            productRepository.save(product);

            stockMovementService.recordMovement(
                    product,
                    detail.getQuantity(),
                    MovementType.SALE,
                    userId,
                    remarks
            );
        }
    }

    private List<SaleSummaryResponse> mapToSummaryResponses(List<Sale> sales) {
        List<SaleSummaryResponse> responses = saleMapper.toSummaryResponseList(sales);
        List<Integer> userIds = responses.stream()
                .map(SaleSummaryResponse::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (!userIds.isEmpty()) {
            Map<Integer, String> userNames = userService.getUserFullNames(userIds);
            responses.forEach(response -> response.setUserName(userNames.get(response.getUserId())));
        }
        return responses;
    }
}