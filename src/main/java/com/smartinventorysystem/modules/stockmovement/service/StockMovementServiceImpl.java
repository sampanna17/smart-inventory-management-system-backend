package com.smartinventorysystem.modules.stockmovement.service;

import com.smartinventorysystem.common.dto.PageResponse;
import com.smartinventorysystem.constants.MessageConstants;
import com.smartinventorysystem.enums.MovementType;
import com.smartinventorysystem.exceptions.BadRequestException;
import com.smartinventorysystem.exceptions.InsufficientStockException;
import com.smartinventorysystem.exceptions.ResourceNotFoundException;
import com.smartinventorysystem.modules.product.entity.Product;
import com.smartinventorysystem.modules.product.repository.ProductRepository;
import com.smartinventorysystem.modules.stockmovement.dto.request.CreateStockMovementRequest;
import com.smartinventorysystem.modules.stockmovement.dto.request.StockMovementFilterRequest;
import com.smartinventorysystem.modules.stockmovement.dto.response.StockMovementResponse;
import com.smartinventorysystem.modules.stockmovement.entity.StockMovement;
import com.smartinventorysystem.modules.stockmovement.mapper.StockMovementMapper;
import com.smartinventorysystem.modules.stockmovement.repository.StockMovementRepository;
import com.smartinventorysystem.modules.stockmovement.specification.StockMovementSpecification;
import com.smartinventorysystem.modules.user.entity.User;
import com.smartinventorysystem.modules.user.repository.UserRepository;
import com.smartinventorysystem.modules.user.service.UserService;
import com.smartinventorysystem.utils.AuthenticatedUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartinventorysystem.enums.NotificationType;
import com.smartinventorysystem.modules.notification.service.NotificationService;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional("simsTransactionManager")
public class StockMovementServiceImpl implements StockMovementService {

    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final StockMovementMapper stockMovementMapper;
    private final Clock clock;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public StockMovementResponse createStockMovement(CreateStockMovementRequest request) {
        User authenticatedUser = authenticatedUserProvider.getCurrentUser();

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with ID: " + request.getProductId()
                ));

        validateMovementQuantity(request.getQuantity());
        validateMovementType(request.getMovementType());

        int currentStock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;

        if (request.getMovementType() == MovementType.PURCHASE || request.getMovementType() == MovementType.RETURN) {
            product.setStockQuantity(currentStock + request.getQuantity());
            productRepository.save(product);
        } else if (request.getMovementType() == MovementType.SALE) {
            if (currentStock < request.getQuantity()) {
                throw new InsufficientStockException("Insufficient stock for product: " + product.getProductName());
            }
            product.setStockQuantity(currentStock - request.getQuantity());
            productRepository.save(product);
        } else if (request.getMovementType() == MovementType.ADJUSTMENT) {
            product.setStockQuantity(currentStock + request.getQuantity());
            productRepository.save(product);
        }

        // Check stock thresholds if stock decreased
        if (request.getMovementType() == MovementType.SALE || request.getMovementType() == MovementType.ADJUSTMENT) {
            int remainingStock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
            if (remainingStock <= 0) {
                notificationService.notifyUserAndAdmins(
                        authenticatedUser.getUserID(),
                        "Out of Stock Alert",
                        "Product '" + product.getProductName() + "' is out of stock (Quantity: 0).",
                        NotificationType.OUT_OF_STOCK
                );
            } else if (product.getReorderLevel() != null && remainingStock <= product.getReorderLevel()) {
                notificationService.notifyUserAndAdmins(
                        authenticatedUser.getUserID(),
                        "Low Stock Alert",
                        "Product '" + product.getProductName() + "' is running low on stock (Remaining: " + remainingStock + ", Reorder Level: " + product.getReorderLevel() + ").",
                        NotificationType.LOW_STOCK
                );
            }
        }

        StockMovement stockMovement = new StockMovement();
        stockMovement.setProduct(product);
        stockMovement.setUserID(authenticatedUser.getUserID());
        stockMovement.setMovementType(request.getMovementType());
        stockMovement.setQuantity(request.getQuantity());
        stockMovement.setMovementDate(LocalDateTime.now(clock));
        stockMovement.setRemarks(request.getRemarks());

        StockMovement savedMovement = stockMovementRepository.save(stockMovement);

        StockMovementResponse response = stockMovementMapper.toResponse(savedMovement);
        response.setUserName(authenticatedUser.getFullName());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public StockMovementResponse getStockMovementById(Integer movementId) {
        StockMovement stockMovement = stockMovementRepository.findByIdWithProduct(movementId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstants.STOCK_MOVEMENT_NOT_FOUND + movementId));

        StockMovementResponse response = stockMovementMapper.toResponse(stockMovement);
        response.setUserName(getUserFullName(stockMovement.getUserID()));
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockMovementResponse> getAllStockMovements() {
        List<StockMovement> movements = stockMovementRepository.findAllWithProduct();
        List<StockMovementResponse> responses = stockMovementMapper.toResponseList(movements);

        populateUserNames(responses);
        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockMovementResponse> getMovementsByProduct(Integer productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException(MessageConstants.PRODUCT_NOT_FOUND_MSG + productId);
        }

        List<StockMovement> movements = stockMovementRepository.findByProductWithProduct(productId);
        List<StockMovementResponse> responses = stockMovementMapper.toResponseList(movements);

        populateUserNames(responses);
        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockMovementResponse> getMovementsByUser(Integer userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException(MessageConstants.USER_NOT_FOUND_WITH_ID + userId);
        }

        List<StockMovement> movements = stockMovementRepository.findByUserWithProduct(userId);
        List<StockMovementResponse> responses = stockMovementMapper.toResponseList(movements);

        populateUserNames(responses);
        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockMovementResponse> getMovementsByMovementType(String movementType) {
        MovementType type = parseMovementType(movementType);

        List<StockMovement> movements = stockMovementRepository.findByMovementTypeWithProduct(type);
        List<StockMovementResponse> responses = stockMovementMapper.toResponseList(movements);

        populateUserNames(responses);
        return responses;
    }

    @Override
    @Transactional
    public void deleteStockMovement(Integer movementId) {
        StockMovement stockMovement = stockMovementRepository.findById(movementId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstants.STOCK_MOVEMENT_NOT_FOUND + movementId));

        if (stockMovement.getMovementType() == MovementType.PURCHASE
                || stockMovement.getMovementType() == MovementType.SALE) {
            throw new BadRequestException("Purchase and sale stock movements cannot be deleted.");
        }

        stockMovementRepository.delete(stockMovement);
    }

    @Override
    @Transactional
    public void recordMovement(Product product,
                               Integer quantity,
                               MovementType movementType,
                               Integer userId,
                               String remarks) {

        StockMovement movement = new StockMovement();
        movement.setProduct(product);
        movement.setUserID(userId);
        movement.setMovementType(movementType);
        movement.setQuantity(quantity);
        movement.setMovementDate(LocalDateTime.now(clock));
        movement.setRemarks(remarks);

        stockMovementRepository.save(movement);
    }

    private void validateMovementQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new BadRequestException("Quantity must be greater than zero.");
        }
    }

    private void validateMovementType(MovementType movementType) {
        if (movementType == null) {
            throw new BadRequestException("Movement type is required.");
        }
    }

    private MovementType parseMovementType(String movementType) {
        if (movementType == null || movementType.isBlank()) {
            throw new BadRequestException("Movement type is required.");
        }

        try {
            return MovementType.valueOf(movementType.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid movement type: " + movementType);
        }
    }

    private void populateUserNames(List<StockMovementResponse> responses) {
        List<Integer> userIds = responses.stream()
                .map(StockMovementResponse::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (userIds.isEmpty()) {
            return;
        }

        Map<Integer, String> userNames = userService.getUserFullNames(userIds);

        responses.forEach(response ->
                response.setUserName(userNames.get(response.getUserId()))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StockMovementResponse> getStockMovements(StockMovementFilterRequest request) {
        Pageable pageable = createPageable(request);
        Specification<StockMovement> specification = StockMovementSpecification.withFilters(request);

        Page<StockMovement> movementPage = stockMovementRepository.findAll(specification, pageable);
        List<StockMovementResponse> content = stockMovementMapper.toResponseList(movementPage.getContent());
        populateUserNames(content);

        return PageResponse.<StockMovementResponse>builder()
                .content(content)
                .pageNumber(movementPage.getNumber())
                .pageSize(movementPage.getSize())
                .totalElements(movementPage.getTotalElements())
                .totalPages(movementPage.getTotalPages())
                .first(movementPage.isFirst())
                .last(movementPage.isLast())
                .hasNext(movementPage.hasNext())
                .hasPrevious(movementPage.hasPrevious())
                .build();
    }

    private Pageable createPageable(StockMovementFilterRequest request) {
        int page = (request != null && request.getPage() != null) ? request.getPage() : 0;
        int size = (request != null && request.getSize() != null) ? request.getSize() : 10;

        String sortBy = (request != null && request.getSortBy() != null) ? request.getSortBy() : "movementDate";
        String sortDir = (request != null && request.getSortDir() != null) ? request.getSortDir() : "desc";

        String targetProperty = mapSortProperty(sortBy);
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;

        Sort sort = Sort.by(direction, targetProperty);
        return PageRequest.of(page, size, sort);
    }

    private String mapSortProperty(String sortBy) {
        if (sortBy == null) {
            return "movementDate";
        }
        return switch (sortBy.trim().toLowerCase()) {
            case "product", "productname" -> "product.productName";
            case "type", "movementtype" -> "movementType";
            case "quantity" -> "quantity";
            case "remarks" -> "remarks";
            case "date", "movementdate" -> "movementDate";
            case "id", "movementid" -> "movementID";
            default -> "movementDate";
        };
    }

    private String getUserFullName(Integer userId) {
        return userService.getUserFullName(userId);
    }

}