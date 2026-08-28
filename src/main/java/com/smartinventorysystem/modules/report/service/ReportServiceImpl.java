package com.smartinventorysystem.modules.report.service;

import com.smartinventorysystem.enums.MovementType;
import com.smartinventorysystem.enums.Role;
import com.smartinventorysystem.enums.SaleStatus;
import com.smartinventorysystem.modules.customer.entity.Customer;
import com.smartinventorysystem.modules.customer.repository.CustomerRepository;
import com.smartinventorysystem.modules.product.entity.Product;
import com.smartinventorysystem.modules.product.repository.ProductRepository;
import com.smartinventorysystem.modules.purchase.entity.Purchase;
import com.smartinventorysystem.modules.purchase.repository.PurchaseRepository;
import com.smartinventorysystem.modules.report.dto.request.AnalyticsFilterRequest;
import com.smartinventorysystem.modules.report.dto.request.InventoryReportFilterRequest;
import com.smartinventorysystem.modules.report.dto.request.PurchaseReportFilterRequest;
import com.smartinventorysystem.modules.report.dto.request.SalesReportFilterRequest;
import com.smartinventorysystem.modules.report.dto.response.*;
import com.smartinventorysystem.modules.sale.entity.Sale;
import com.smartinventorysystem.modules.sale.entity.SaleDetail;
import com.smartinventorysystem.modules.sale.repository.SaleDetailRepository;
import com.smartinventorysystem.modules.sale.repository.SaleRepository;
import com.smartinventorysystem.modules.stockmovement.entity.StockMovement;
import com.smartinventorysystem.modules.stockmovement.repository.StockMovementRepository;
import com.smartinventorysystem.modules.supplier.entity.Supplier;
import com.smartinventorysystem.modules.supplier.repository.SupplierRepository;
import com.smartinventorysystem.modules.user.entity.User;
import com.smartinventorysystem.modules.user.repository.UserRepository;
import com.smartinventorysystem.modules.user.service.UserService;
import com.smartinventorysystem.utils.AuthenticatedUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final SaleRepository saleRepository;
    private final SaleDetailRepository saleDetailRepository;
    private final PurchaseRepository purchaseRepository;
    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;
    private final CustomerRepository customerRepository;
    private final SupplierRepository supplierRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter YEAR_FORMATTER = DateTimeFormatter.ofPattern("yyyy");

    @Override
    public SalesReportResponse generateSalesReport(SalesReportFilterRequest filter) {
        User currentUser = authenticatedUserProvider.getCurrentUser();
        if (currentUser.getRole() == Role.STAFF && filter.getUserId() == null) {
            filter.setUserId(currentUser.getUserID());
        }

        List<Sale> allSales = saleRepository.findAllWithCustomer();

        List<Sale> filteredSales = allSales.stream()
                .filter(s -> matchesSaleFilter(s, filter))
                .sorted(Comparator.comparing(this::getSaleDateTime).reversed())
                .toList();

        List<SaleDetail> allDetails = saleDetailRepository.findAll();
        Map<Integer, List<SaleDetail>> detailsBySaleId = allDetails.stream()
                .filter(d -> d.getSale() != null && d.getSale().getSaleID() != null)
                .collect(Collectors.groupingBy(d -> d.getSale().getSaleID()));

        List<Integer> userIds = filteredSales.stream()
                .map(Sale::getUserID)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Integer, String> userNames = userService.getUserFullNames(userIds);

        List<SalesReportItem> reportItems = filteredSales.stream()
                .map(sale -> {
                    List<SaleDetail> details = detailsBySaleId.getOrDefault(sale.getSaleID(), Collections.emptyList());
                    int totalItems = details.stream().mapToInt(d -> d.getQuantity() != null ? d.getQuantity() : 0).sum();
                    return SalesReportItem.builder()
                            .saleId(sale.getSaleID())
                            .invoiceNumber(sale.getInvoiceNumber())
                            .customerId(sale.getCustomer() != null ? sale.getCustomer().getCustomerID() : null)
                            .customerName(sale.getCustomer() != null ? sale.getCustomer().getCustomerName() : "Walk-in Customer")
                            .userId(sale.getUserID())
                            .userName(userNames.getOrDefault(sale.getUserID(), "Unknown"))
                            .saleDate(getSaleDateTime(sale))
                            .totalAmount(sale.getTotalAmount() != null ? sale.getTotalAmount() : BigDecimal.ZERO)
                            .status(sale.getStatus())
                            .totalItems(totalItems)
                            .build();
                })
                .toList();

        long completedSalesCount = reportItems.stream().filter(i -> i.getStatus() == SaleStatus.COMPLETED).count();
        long refundedSalesCount = reportItems.stream().filter(i -> i.getStatus() == SaleStatus.REFUNDED).count();
        long cancelledSalesCount = reportItems.stream().filter(i -> i.getStatus() == SaleStatus.CANCELLED).count();

        BigDecimal totalRevenue = reportItems.stream()
                .filter(i -> i.getStatus() == SaleStatus.COMPLETED)
                .map(SalesReportItem::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalUnitsSold = reportItems.stream()
                .filter(i -> i.getStatus() == SaleStatus.COMPLETED)
                .mapToLong(SalesReportItem::getTotalItems)
                .sum();

        BigDecimal averageOrderValue = completedSalesCount > 0
                ? totalRevenue.divide(BigDecimal.valueOf(completedSalesCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        SalesReportSummary summary = SalesReportSummary.builder()
                .totalRevenue(totalRevenue)
                .totalSalesCount(reportItems.size())
                .totalUnitsSold(totalUnitsSold)
                .averageOrderValue(averageOrderValue)
                .completedSalesCount(completedSalesCount)
                .refundedSalesCount(refundedSalesCount)
                .cancelledSalesCount(cancelledSalesCount)
                .build();

        List<SalesPeriodData> periodBreakdown = buildSalesPeriodBreakdown(reportItems, filter.getGroupBy());

        return SalesReportResponse.builder()
                .summary(summary)
                .periodBreakdown(periodBreakdown)
                .sales(reportItems)
                .build();
    }

    private boolean matchesSaleFilter(Sale sale, SalesReportFilterRequest filter) {
        if (filter == null) return true;
        LocalDateTime saleDate = getSaleDateTime(sale);

        if (filter.getStartDate() != null && saleDate.isBefore(filter.getStartDate())) return false;
        if (filter.getEndDate() != null && saleDate.isAfter(filter.getEndDate())) return false;
        if (filter.getCustomerId() != null) {
            if (sale.getCustomer() == null || !filter.getCustomerId().equals(sale.getCustomer().getCustomerID())) return false;
        }
        if (filter.getUserId() != null && !filter.getUserId().equals(sale.getUserID())) return false;
        if (filter.getStatus() != null && sale.getStatus() != filter.getStatus()) return false;

        return true;
    }

    private List<SalesPeriodData> buildSalesPeriodBreakdown(List<SalesReportItem> items, String groupBy) {
        DateTimeFormatter formatter = "MONTH".equalsIgnoreCase(groupBy)
                ? MONTH_FORMATTER
                : "YEAR".equalsIgnoreCase(groupBy) ? YEAR_FORMATTER : DATE_FORMATTER;

        Map<String, List<SalesReportItem>> grouped = items.stream()
                .filter(i -> i.getStatus() == SaleStatus.COMPLETED && i.getSaleDate() != null)
                .collect(Collectors.groupingBy(i -> formatter.format(i.getSaleDate())));

        return grouped.entrySet().stream()
                .map(entry -> {
                    String period = entry.getKey();
                    List<SalesReportItem> periodItems = entry.getValue();
                    long count = periodItems.size();
                    BigDecimal revenue = periodItems.stream().map(SalesReportItem::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                    long units = periodItems.stream().mapToLong(SalesReportItem::getTotalItems).sum();
                    return new SalesPeriodData(period, count, revenue, units);
                })
                .sorted(Comparator.comparing(SalesPeriodData::getPeriod))
                .toList();
    }

    @Override
    public InventoryReportResponse generateInventoryReport(InventoryReportFilterRequest filter) {
        List<Product> allProducts = productRepository.findAll();

        List<Product> filteredProducts = allProducts.stream()
                .filter(p -> matchesProductFilter(p, filter))
                .sorted(Comparator.comparing(Product::getProductName, String.CASE_INSENSITIVE_ORDER))
                .toList();

        List<InventoryReportItem> items = filteredProducts.stream()
                .map(p -> {
                    int stock = p.getStockQuantity() != null ? p.getStockQuantity() : 0;
                    int reorder = p.getReorderLevel() != null ? p.getReorderLevel() : 0;
                    BigDecimal cost = p.getCostPrice() != null ? p.getCostPrice() : BigDecimal.ZERO;
                    BigDecimal selling = p.getSellingPrice() != null ? p.getSellingPrice() : BigDecimal.ZERO;

                    BigDecimal totalCost = cost.multiply(BigDecimal.valueOf(stock));
                    BigDecimal totalRetail = selling.multiply(BigDecimal.valueOf(stock));
                    BigDecimal profit = totalRetail.subtract(totalCost);

                    String status = stock <= 0 ? "OUT_OF_STOCK" : (stock <= reorder ? "LOW_STOCK" : "IN_STOCK");

                    return InventoryReportItem.builder()
                            .productId(p.getProductId())
                            .productName(p.getProductName())
                            .categoryId(p.getCategory() != null ? p.getCategory().getCategoryId() : null)
                            .categoryName(p.getCategory() != null ? p.getCategory().getCategoryName() : "Uncategorized")
                            .unitName(p.getUnit() != null ? p.getUnit().getUnitName() : "")
                            .costPrice(cost)
                            .sellingPrice(selling)
                            .stockQuantity(stock)
                            .reorderLevel(reorder)
                            .totalCostValue(totalCost)
                            .totalRetailValue(totalRetail)
                            .potentialProfit(profit)
                            .stockStatus(status)
                            .build();
                })
                .toList();

        long totalUnits = items.stream().mapToLong(InventoryReportItem::getStockQuantity).sum();
        BigDecimal totalCostVal = items.stream().map(InventoryReportItem::getTotalCostValue).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalRetailVal = items.stream().map(InventoryReportItem::getTotalRetailValue).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPotentialProfit = totalRetailVal.subtract(totalCostVal);
        long lowStockCount = items.stream().filter(i -> "LOW_STOCK".equals(i.getStockStatus())).count();
        long outOfStockCount = items.stream().filter(i -> "OUT_OF_STOCK".equals(i.getStockStatus())).count();

        InventoryReportSummary summary = InventoryReportSummary.builder()
                .totalProductsCount(items.size())
                .totalUnitsInStock(totalUnits)
                .totalCostValue(totalCostVal)
                .totalRetailValue(totalRetailVal)
                .totalPotentialProfit(totalPotentialProfit)
                .lowStockCount(lowStockCount)
                .outOfStockCount(outOfStockCount)
                .build();

        // Category breakdown
        Map<String, List<InventoryReportItem>> byCategory = items.stream()
                .collect(Collectors.groupingBy(InventoryReportItem::getCategoryName));

        List<CategoryInventorySummary> categoryBreakdown = byCategory.entrySet().stream()
                .map(entry -> {
                    List<InventoryReportItem> catItems = entry.getValue();
                    Integer catId = catItems.isEmpty() ? null : catItems.get(0).getCategoryId();
                    long pCount = catItems.size();
                    long units = catItems.stream().mapToLong(InventoryReportItem::getStockQuantity).sum();
                    BigDecimal costVal = catItems.stream().map(InventoryReportItem::getTotalCostValue).reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal retailVal = catItems.stream().map(InventoryReportItem::getTotalRetailValue).reduce(BigDecimal.ZERO, BigDecimal::add);
                    return CategoryInventorySummary.builder()
                            .categoryId(catId)
                            .categoryName(entry.getKey())
                            .productCount(pCount)
                            .totalUnitsInStock(units)
                            .totalCostValue(costVal)
                            .totalRetailValue(retailVal)
                            .build();
                })
                .sorted(Comparator.comparing(CategoryInventorySummary::getTotalCostValue).reversed())
                .toList();

        // Stock movement summary
        List<StockMovement> movements = stockMovementRepository.findAll();
        if (filter != null && (filter.getMovementStartDate() != null || filter.getMovementEndDate() != null)) {
            movements = movements.stream().filter(m -> {
                LocalDateTime date = m.getMovementDate();
                if (date == null) return false;
                if (filter.getMovementStartDate() != null && date.isBefore(filter.getMovementStartDate())) return false;
                if (filter.getMovementEndDate() != null && date.isAfter(filter.getMovementEndDate())) return false;
                return true;
            }).toList();
        }

        long purchasedQty = movements.stream().filter(m -> m.getMovementType() == MovementType.PURCHASE).mapToLong(m -> m.getQuantity() != null ? m.getQuantity() : 0).sum();
        long soldQty = movements.stream().filter(m -> m.getMovementType() == MovementType.SALE).mapToLong(m -> m.getQuantity() != null ? m.getQuantity() : 0).sum();
        long adjustedQty = movements.stream().filter(m -> m.getMovementType() == MovementType.ADJUSTMENT).mapToLong(m -> m.getQuantity() != null ? m.getQuantity() : 0).sum();
        long returnedQty = movements.stream().filter(m -> m.getMovementType() == MovementType.RETURN).mapToLong(m -> m.getQuantity() != null ? m.getQuantity() : 0).sum();

        MovementSummaryResponse movementSummary = MovementSummaryResponse.builder()
                .totalPurchasedQty(purchasedQty)
                .totalSoldQty(soldQty)
                .totalAdjustedQty(adjustedQty)
                .totalReturnedQty(returnedQty)
                .build();

        return InventoryReportResponse.builder()
                .summary(summary)
                .categoryBreakdown(categoryBreakdown)
                .movementSummary(movementSummary)
                .items(items)
                .build();
    }

    private boolean matchesProductFilter(Product product, InventoryReportFilterRequest filter) {
        if (filter == null) return true;

        if (filter.getCategoryId() != null) {
            if (product.getCategory() == null || !filter.getCategoryId().equals(product.getCategory().getCategoryId())) return false;
        }

        int stock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
        int reorder = product.getReorderLevel() != null ? product.getReorderLevel() : 0;

        if (filter.getStockStatus() != null && !filter.getStockStatus().isBlank() && !"ALL".equalsIgnoreCase(filter.getStockStatus())) {
            switch (filter.getStockStatus().toUpperCase()) {
                case "IN_STOCK" -> { if (stock <= reorder) return false; }
                case "LOW_STOCK" -> { if (stock <= 0 || stock > reorder) return false; }
                case "OUT_OF_STOCK" -> { if (stock > 0) return false; }
            }
        }

        if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
            String query = filter.getSearch().trim().toLowerCase();
            boolean matchesName = product.getProductName() != null && product.getProductName().toLowerCase().contains(query);
            boolean matchesCategory = product.getCategory() != null && product.getCategory().getCategoryName() != null && product.getCategory().getCategoryName().toLowerCase().contains(query);
            if (!matchesName && !matchesCategory) return false;
        }

        return true;
    }

    @Override
    public PurchaseReportResponse generatePurchaseReport(PurchaseReportFilterRequest filter) {
        List<Purchase> allPurchases = purchaseRepository.findAllWithDetails();

        List<Purchase> filteredPurchases = allPurchases.stream()
                .filter(p -> matchesPurchaseFilter(p, filter))
                .sorted(Comparator.comparing(Purchase::getPurchaseDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        List<Integer> userIds = filteredPurchases.stream()
                .map(Purchase::getUserID)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Integer, String> userNames = userService.getUserFullNames(userIds);

        List<PurchaseReportItem> items = filteredPurchases.stream()
                .map(p -> {
                    int totalItems = p.getPurchaseDetails() != null
                            ? p.getPurchaseDetails().stream().mapToInt(d -> d.getQuantity() != null ? d.getQuantity() : 0).sum()
                            : 0;
                    return PurchaseReportItem.builder()
                            .purchaseId(p.getPurchaseId())
                            .purchaseNumber(p.getPurchaseNumber())
                            .supplierId(p.getSupplier() != null ? p.getSupplier().getSupplierId() : null)
                            .supplierName(p.getSupplier() != null ? p.getSupplier().getSupplierName() : "Unknown Supplier")
                            .userId(p.getUserID())
                            .userName(userNames.getOrDefault(p.getUserID(), "Unknown"))
                            .purchaseDate(p.getPurchaseDate() != null ? p.getPurchaseDate() : p.getCreatedAt())
                            .totalAmount(p.getTotalAmount() != null ? p.getTotalAmount() : BigDecimal.ZERO)
                            .status(p.getStatus())
                            .totalItems(totalItems)
                            .build();
                })
                .toList();

        BigDecimal totalExpenditure = items.stream()
                .map(PurchaseReportItem::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long receivedCount = items.stream().filter(i -> i.getStatus() != null && "RECEIVED".equalsIgnoreCase(i.getStatus().name())).count();
        long pendingCount = items.stream().filter(i -> i.getStatus() != null && "PENDING".equalsIgnoreCase(i.getStatus().name())).count();
        long cancelledCount = items.stream().filter(i -> i.getStatus() != null && "CANCELLED".equalsIgnoreCase(i.getStatus().name())).count();

        PurchaseReportSummary summary = PurchaseReportSummary.builder()
                .totalPurchasesCount(items.size())
                .totalExpenditure(totalExpenditure)
                .receivedCount(receivedCount)
                .pendingCount(pendingCount)
                .cancelledCount(cancelledCount)
                .build();

        // Group by supplier
        Map<String, List<PurchaseReportItem>> bySupplier = items.stream()
                .collect(Collectors.groupingBy(PurchaseReportItem::getSupplierName));

        List<SupplierPurchaseSummary> supplierBreakdown = bySupplier.entrySet().stream()
                .map(entry -> {
                    List<PurchaseReportItem> supItems = entry.getValue();
                    Integer supId = supItems.isEmpty() ? null : supItems.get(0).getSupplierId();
                    Supplier sup = supId != null ? supplierRepository.findById(supId).orElse(null) : null;
                    String contactInfo = sup != null ? (sup.getEmail() != null ? sup.getEmail() : sup.getPhone()) : "";
                    long count = supItems.size();
                    BigDecimal amount = supItems.stream().map(PurchaseReportItem::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                    long recCount = supItems.stream().filter(i -> i.getStatus() != null && "RECEIVED".equalsIgnoreCase(i.getStatus().name())).count();
                    return SupplierPurchaseSummary.builder()
                            .supplierId(supId)
                            .supplierName(entry.getKey())
                            .contactPerson(contactInfo != null ? contactInfo : "")
                            .purchaseCount(count)
                            .totalAmount(amount)
                            .receivedCount(recCount)
                            .build();
                })
                .sorted(Comparator.comparing(SupplierPurchaseSummary::getTotalAmount).reversed())
                .toList();

        return PurchaseReportResponse.builder()
                .summary(summary)
                .supplierBreakdown(supplierBreakdown)
                .purchases(items)
                .build();
    }

    private boolean matchesPurchaseFilter(Purchase purchase, PurchaseReportFilterRequest filter) {
        if (filter == null) return true;
        LocalDateTime date = purchase.getPurchaseDate() != null ? purchase.getPurchaseDate() : purchase.getCreatedAt();
        if (date == null) return false;

        if (filter.getStartDate() != null && date.isBefore(filter.getStartDate())) return false;
        if (filter.getEndDate() != null && date.isAfter(filter.getEndDate())) return false;
        if (filter.getSupplierId() != null) {
            if (purchase.getSupplier() == null || !filter.getSupplierId().equals(purchase.getSupplier().getSupplierId())) return false;
        }
        if (filter.getStatus() != null && purchase.getStatus() != filter.getStatus()) return false;

        return true;
    }

    @Override
    public ProductAnalyticsResponse generateProductAnalytics(AnalyticsFilterRequest filter) {
        List<SaleDetail> allDetails = saleDetailRepository.findAll();

        List<SaleDetail> validDetails = allDetails.stream()
                .filter(d -> d.getSale() != null && d.getSale().getStatus() == SaleStatus.COMPLETED && d.getProduct() != null)
                .filter(d -> {
                    if (filter == null) return true;
                    LocalDateTime date = getSaleDateTime(d.getSale());
                    if (filter.getStartDate() != null && date.isBefore(filter.getStartDate())) return false;
                    if (filter.getEndDate() != null && date.isAfter(filter.getEndDate())) return false;
                    return true;
                })
                .toList();

        int limit = (filter != null && filter.getLimit() != null && filter.getLimit() > 0) ? filter.getLimit() : 10;

        Map<Integer, List<SaleDetail>> byProduct = validDetails.stream()
                .collect(Collectors.groupingBy(d -> d.getProduct().getProductId()));

        List<ProductPerformanceData> performances = byProduct.values().stream()
                .map(details -> {
                    Product p = details.get(0).getProduct();
                    long qty = details.stream().mapToLong(d -> d.getQuantity() != null ? d.getQuantity() : 0).sum();
                    BigDecimal rev = details.stream().map(d -> d.getSubTotal() != null ? d.getSubTotal() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal stock = p.getStockQuantity() != null ? BigDecimal.valueOf(p.getStockQuantity()) : BigDecimal.ZERO;
                    String category = p.getCategory() != null ? p.getCategory().getCategoryName() : "General";

                    return ProductPerformanceData.builder()
                            .productId(p.getProductId())
                            .productName(p.getProductName())
                            .categoryName(category)
                            .totalQuantitySold(qty)
                            .totalRevenue(rev)
                            .currentStock(stock)
                            .build();
                })
                .toList();

        List<ProductPerformanceData> topByQty = performances.stream()
                .sorted(Comparator.comparingLong(ProductPerformanceData::getTotalQuantitySold).reversed())
                .limit(limit)
                .toList();

        List<ProductPerformanceData> topByRev = performances.stream()
                .sorted(Comparator.comparing(ProductPerformanceData::getTotalRevenue, Comparator.reverseOrder()))
                .limit(limit)
                .toList();

        BigDecimal grandRevenue = performances.stream().map(ProductPerformanceData::getTotalRevenue).reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, List<ProductPerformanceData>> byCat = performances.stream()
                .collect(Collectors.groupingBy(ProductPerformanceData::getCategoryName));

        List<CategoryPerformanceData> catPerformances = byCat.entrySet().stream()
                .map(entry -> {
                    List<ProductPerformanceData> list = entry.getValue();
                    long totalQty = list.stream().mapToLong(ProductPerformanceData::getTotalQuantitySold).sum();
                    BigDecimal catRev = list.stream().map(ProductPerformanceData::getTotalRevenue).reduce(BigDecimal.ZERO, BigDecimal::add);
                    double pct = (grandRevenue.compareTo(BigDecimal.ZERO) > 0)
                            ? catRev.divide(grandRevenue, 4, RoundingMode.HALF_UP).doubleValue() * 100
                            : 0.0;
                    return CategoryPerformanceData.builder()
                            .categoryName(entry.getKey())
                            .totalItemsSold(totalQty)
                            .totalRevenue(catRev)
                            .revenuePercentage(Math.round(pct * 100.0) / 100.0)
                            .build();
                })
                .sorted(Comparator.comparing(CategoryPerformanceData::getTotalRevenue).reversed())
                .toList();

        return ProductAnalyticsResponse.builder()
                .topSellingByQuantity(topByQty)
                .topSellingByRevenue(topByRev)
                .categoryPerformance(catPerformances)
                .build();
    }

    @Override
    public CustomerAnalyticsResponse generateCustomerAnalytics(AnalyticsFilterRequest filter) {
        List<Sale> completedSales = saleRepository.findByStatusWithCustomer(SaleStatus.COMPLETED);

        if (filter != null) {
            completedSales = completedSales.stream().filter(s -> {
                LocalDateTime date = getSaleDateTime(s);
                if (filter.getStartDate() != null && date.isBefore(filter.getStartDate())) return false;
                if (filter.getEndDate() != null && date.isAfter(filter.getEndDate())) return false;
                return true;
            }).toList();
        }

        Map<Integer, List<Sale>> byCustomer = completedSales.stream()
                .filter(s -> s.getCustomer() != null && s.getCustomer().getCustomerID() != null)
                .collect(Collectors.groupingBy(s -> s.getCustomer().getCustomerID()));

        List<CustomerPerformanceData> customerPerformances = byCustomer.entrySet().stream()
                .map(entry -> {
                    List<Sale> sales = entry.getValue();
                    Customer c = sales.get(0).getCustomer();
                    long orders = sales.size();
                    BigDecimal spend = sales.stream().map(s -> s.getTotalAmount() != null ? s.getTotalAmount() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal avgSpend = orders > 0 ? spend.divide(BigDecimal.valueOf(orders), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
                    LocalDateTime lastOrder = sales.stream().map(this::getSaleDateTime).max(LocalDateTime::compareTo).orElse(null);

                    return CustomerPerformanceData.builder()
                            .customerId(c.getCustomerID())
                            .customerName(c.getCustomerName())
                            .email(c.getEmail())
                            .phone(c.getPhone())
                            .totalOrders(orders)
                            .totalSpend(spend)
                            .averageOrderValue(avgSpend)
                            .lastOrderDate(lastOrder)
                            .build();
                })
                .sorted(Comparator.comparing(CustomerPerformanceData::getTotalSpend).reversed())
                .toList();

        long totalCustomersInDb = customerRepository.count();
        long activeCustomers = customerPerformances.size();
        BigDecimal totalSpendAll = customerPerformances.stream().map(CustomerPerformanceData::getTotalSpend).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avgCustomerSpend = activeCustomers > 0 ? totalSpendAll.divide(BigDecimal.valueOf(activeCustomers), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        CustomerAnalyticsSummary summary = CustomerAnalyticsSummary.builder()
                .totalCustomers(totalCustomersInDb)
                .activeCustomers(activeCustomers)
                .averageCustomerSpend(avgCustomerSpend)
                .build();

        int limit = (filter != null && filter.getLimit() != null && filter.getLimit() > 0) ? filter.getLimit() : 20;
        List<CustomerPerformanceData> topCustomers = customerPerformances.stream().limit(limit).toList();

        return CustomerAnalyticsResponse.builder()
                .summary(summary)
                .topCustomers(topCustomers)
                .build();
    }

    @Override
    public StaffPerformanceReportResponse generateStaffPerformanceReport(AnalyticsFilterRequest filter) {
        List<User> users = userRepository.findAll();
        List<Sale> completedSales = saleRepository.findByStatusWithCustomer(SaleStatus.COMPLETED);

        if (filter != null) {
            completedSales = completedSales.stream().filter(s -> {
                LocalDateTime date = getSaleDateTime(s);
                if (filter.getStartDate() != null && date.isBefore(filter.getStartDate())) return false;
                if (filter.getEndDate() != null && date.isAfter(filter.getEndDate())) return false;
                return true;
            }).toList();
        }

        List<SaleDetail> allDetails = saleDetailRepository.findAll();
        Map<Integer, List<SaleDetail>> detailsBySaleId = allDetails.stream()
                .filter(d -> d.getSale() != null && d.getSale().getSaleID() != null)
                .collect(Collectors.groupingBy(d -> d.getSale().getSaleID()));

        Map<Integer, List<Sale>> salesByUser = completedSales.stream()
                .filter(s -> s.getUserID() != null)
                .collect(Collectors.groupingBy(Sale::getUserID));

        List<StaffPerformanceData> staffDataList = users.stream()
                .map(u -> {
                    List<Sale> userSales = salesByUser.getOrDefault(u.getUserID(), Collections.emptyList());
                    long salesCount = userSales.size();
                    BigDecimal totalRev = userSales.stream().map(s -> s.getTotalAmount() != null ? s.getTotalAmount() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);
                    long unitsSold = userSales.stream()
                            .flatMap(s -> detailsBySaleId.getOrDefault(s.getSaleID(), Collections.emptyList()).stream())
                            .mapToLong(d -> d.getQuantity() != null ? d.getQuantity() : 0)
                            .sum();

                    BigDecimal avgSale = salesCount > 0 ? totalRev.divide(BigDecimal.valueOf(salesCount), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
                    LocalDateTime lastSale = userSales.stream().map(this::getSaleDateTime).max(LocalDateTime::compareTo).orElse(null);

                    return StaffPerformanceData.builder()
                            .staffId(u.getUserID())
                            .fullName(u.getFullName() != null ? u.getFullName() : u.getEmail())
                            .email(u.getEmail())
                            .role(u.getRole())
                            .status(u.getStatus())
                            .totalSalesCount(salesCount)
                            .totalRevenue(totalRev)
                            .totalUnitsSold(unitsSold)
                            .averageSaleValue(avgSale)
                            .lastSaleDate(lastSale)
                            .build();
                })
                .sorted(Comparator.comparing(StaffPerformanceData::getTotalRevenue).reversed())
                .toList();

        return StaffPerformanceReportResponse.builder()
                .staffMembers(staffDataList)
                .build();
    }

    @Override
    public byte[] exportSalesReportCsv(SalesReportFilterRequest filter) {
        SalesReportResponse report = generateSalesReport(filter);
        StringBuilder sb = new StringBuilder();
        sb.append("Invoice Number,Customer Name,Date,Staff Name,Status,Items Count,Total Amount\n");

        for (SalesReportItem item : report.getSales()) {
            sb.append(escapeCsv(item.getInvoiceNumber())).append(",")
                    .append(escapeCsv(item.getCustomerName())).append(",")
                    .append(item.getSaleDate() != null ? item.getSaleDate().format(DATE_FORMATTER) : "").append(",")
                    .append(escapeCsv(item.getUserName())).append(",")
                    .append(item.getStatus()).append(",")
                    .append(item.getTotalItems()).append(",")
                    .append(item.getTotalAmount()).append("\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] exportInventoryReportCsv(InventoryReportFilterRequest filter) {
        InventoryReportResponse report = generateInventoryReport(filter);
        StringBuilder sb = new StringBuilder();
        sb.append("Product ID,Product Name,Category,Unit,Stock Quantity,Reorder Level,Cost Price,Selling Price,Total Cost Value,Total Retail Value,Stock Status\n");

        for (InventoryReportItem item : report.getItems()) {
            sb.append(item.getProductId()).append(",")
                    .append(escapeCsv(item.getProductName())).append(",")
                    .append(escapeCsv(item.getCategoryName())).append(",")
                    .append(escapeCsv(item.getUnitName())).append(",")
                    .append(item.getStockQuantity()).append(",")
                    .append(item.getReorderLevel()).append(",")
                    .append(item.getCostPrice()).append(",")
                    .append(item.getSellingPrice()).append(",")
                    .append(item.getTotalCostValue()).append(",")
                    .append(item.getTotalRetailValue()).append(",")
                    .append(item.getStockStatus()).append("\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] exportPurchaseReportCsv(PurchaseReportFilterRequest filter) {
        PurchaseReportResponse report = generatePurchaseReport(filter);
        StringBuilder sb = new StringBuilder();
        sb.append("Purchase Number,Supplier Name,Date,Staff Name,Status,Items Count,Total Amount\n");

        for (PurchaseReportItem item : report.getPurchases()) {
            sb.append(escapeCsv(item.getPurchaseNumber())).append(",")
                    .append(escapeCsv(item.getSupplierName())).append(",")
                    .append(item.getPurchaseDate() != null ? item.getPurchaseDate().format(DATE_FORMATTER) : "").append(",")
                    .append(escapeCsv(item.getUserName())).append(",")
                    .append(item.getStatus()).append(",")
                    .append(item.getTotalItems()).append(",")
                    .append(item.getTotalAmount()).append("\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private LocalDateTime getSaleDateTime(Sale sale) {
        return sale.getSaleDate() != null ? sale.getSaleDate() : sale.getCreatedAt();
    }

    private String escapeCsv(String value) {
        if (value == null) return "\"\"";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
