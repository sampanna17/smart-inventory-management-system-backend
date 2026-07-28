package com.smartinventorysystem.modules.dashboard.service;

import com.smartinventorysystem.enums.SaleStatus;
import com.smartinventorysystem.modules.category.repository.CategoryRepository;
import com.smartinventorysystem.modules.customer.repository.CustomerRepository;
import com.smartinventorysystem.modules.dashboard.dto.response.AdminDashboardResponse;
import com.smartinventorysystem.modules.dashboard.dto.response.AdminDashboardSummaryResponse;
import com.smartinventorysystem.modules.dashboard.dto.response.DashboardChartsResponse;
import com.smartinventorysystem.modules.dashboard.dto.response.DashboardRecentActivitiesResponse;
import com.smartinventorysystem.modules.dashboard.dto.response.StaffDashboardResponse;
import com.smartinventorysystem.modules.dashboard.dto.response.StaffDashboardSummaryResponse;
import com.smartinventorysystem.modules.dashboard.dto.response.TopSellingProductResponse;
import com.smartinventorysystem.modules.dashboard.dto.response.TrendDataPointResponse;
import com.smartinventorysystem.modules.notification.mapper.NotificationMapper;
import com.smartinventorysystem.modules.notification.repository.NotificationRepository;
import com.smartinventorysystem.modules.product.entity.Product;
import com.smartinventorysystem.modules.product.repository.ProductRepository;
import com.smartinventorysystem.modules.purchase.repository.PurchaseRepository;
import com.smartinventorysystem.modules.sale.dto.response.SaleSummaryResponse;
import com.smartinventorysystem.modules.sale.entity.Sale;
import com.smartinventorysystem.modules.sale.entity.SaleDetail;
import com.smartinventorysystem.modules.sale.mapper.SaleMapper;
import com.smartinventorysystem.modules.sale.repository.SaleDetailRepository;
import com.smartinventorysystem.modules.sale.repository.SaleRepository;
import com.smartinventorysystem.modules.supplier.repository.SupplierRepository;
import com.smartinventorysystem.modules.user.service.UserService;
import com.smartinventorysystem.utils.AuthenticatedUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

	private static final int DASHBOARD_PERIOD_DAYS = 7;
	private static final int TOP_SELLING_LIMIT = 5;
	private static final int RECENT_SALES_LIMIT = 5;

	private final ProductRepository productRepository;
	private final CategoryRepository categoryRepository;
	private final SupplierRepository supplierRepository;
	private final CustomerRepository customerRepository;
	private final SaleRepository saleRepository;
	private final SaleDetailRepository saleDetailRepository;
	private final PurchaseRepository purchaseRepository;
	private final NotificationRepository notificationRepository;
	private final SaleMapper saleMapper;
	private final NotificationMapper notificationMapper;
	private final UserService userService;
	private final AuthenticatedUserProvider authenticatedUserProvider;
	private final Clock clock;

	@Override
	public AdminDashboardResponse getAdminDashboard() {
		AdminDashboardResponse response = new AdminDashboardResponse();
		response.setSummary(buildAdminSummary());
		response.setCharts(buildDashboardCharts());
		response.setTopSellingProducts(buildTopSellingProducts());
		response.setRecentActivities(new DashboardRecentActivitiesResponse());
		return response;
	}

	@Override
	public StaffDashboardResponse getStaffDashboard() {
		Integer currentUserId = authenticatedUserProvider.getCurrentUserId();

		StaffDashboardResponse response = new StaffDashboardResponse();
		response.setSummary(buildStaffSummary(currentUserId));
		response.setRecentSales(buildRecentSales(currentUserId));
		response.setNotifications(notificationMapper.toResponseList(
				notificationRepository.findAllByUserIDOrderByCreatedAtDesc(currentUserId)
		));
		return response;
	}

	private AdminDashboardSummaryResponse buildAdminSummary() {
		List<Product> products = productRepository.findAll();
		List<Sale> sales = saleRepository.findAll();

		AdminDashboardSummaryResponse summary = new AdminDashboardSummaryResponse();
		summary.setTotalProducts((long) products.size());
		summary.setTotalCategories(categoryRepository.count());
		summary.setTotalSuppliers(supplierRepository.count());
		summary.setTotalCustomers(customerRepository.count());
		summary.setTotalSales(countCompletedSales(sales));
		summary.setTotalRevenue(sumCompletedSalesRevenue(sales));
		summary.setTotalPurchases(purchaseRepository.count());
		summary.setLowStockProducts(countLowStockProducts(products));
		summary.setOutOfStockProducts(countOutOfStockProducts(products));
		summary.setUnreadNotifications(notificationRepository.countByUserIDAndIsReadFalse(
				authenticatedUserProvider.getCurrentUserId()
		));
		return summary;
	}

	private StaffDashboardSummaryResponse buildStaffSummary(Integer userId) {
		List<Sale> sales = saleRepository.findAll();
		LocalDate today = LocalDate.now(clock);

		List<Sale> todaySales = sales.stream()
				.filter(sale -> Objects.equals(sale.getUserID(), userId))
				.filter(this::isCompletedSale)
				.filter(sale -> getSaleDateTime(sale).toLocalDate().equals(today))
				.toList();

		long productsSoldToday = saleDetailRepository.findAll().stream()
				.filter(detail -> detail.getSale() != null)
				.filter(detail -> Objects.equals(detail.getSale().getUserID(), userId))
				.filter(detail -> isCompletedSale(detail.getSale()))
				.filter(detail -> getSaleDateTime(detail.getSale()).toLocalDate().equals(today))
				.mapToLong(detail -> detail.getQuantity() == null ? 0L : detail.getQuantity())
				.sum();

		StaffDashboardSummaryResponse summary = new StaffDashboardSummaryResponse();
		summary.setTodaySales((long) todaySales.size());
		summary.setTodayRevenue(sumSalesRevenue(todaySales));
		summary.setProductsSoldToday(productsSoldToday);
		summary.setLowStockProducts(countLowStockProducts(productRepository.findAll()));
		return summary;
	}

	private DashboardChartsResponse buildDashboardCharts() {
		List<Sale> sales = saleRepository.findAll().stream()
				.filter(this::isCompletedSale)
				.toList();

		LocalDate today = LocalDate.now(clock);
		List<TrendDataPointResponse> salesTrend = new ArrayList<>();
		List<TrendDataPointResponse> revenueTrend = new ArrayList<>();

		for (int daysBack = DASHBOARD_PERIOD_DAYS - 1; daysBack >= 0; daysBack--) {
			LocalDate periodDate = today.minusDays(daysBack);
			List<Sale> salesForPeriod = sales.stream()
					.filter(sale -> getSaleDateTime(sale).toLocalDate().equals(periodDate))
					.toList();

			TrendDataPointResponse salesPoint = new TrendDataPointResponse();
			salesPoint.setPeriod(periodDate.toString());
			salesPoint.setCount((long) salesForPeriod.size());
			salesPoint.setAmount(sumSalesRevenue(salesForPeriod));
			salesTrend.add(salesPoint);

			TrendDataPointResponse revenuePoint = new TrendDataPointResponse();
			revenuePoint.setPeriod(periodDate.toString());
			revenuePoint.setCount((long) salesForPeriod.size());
			revenuePoint.setAmount(sumSalesRevenue(salesForPeriod));
			revenueTrend.add(revenuePoint);
		}

		DashboardChartsResponse charts = new DashboardChartsResponse();
		charts.setSalesTrend(salesTrend);
		charts.setRevenueTrend(revenueTrend);
		return charts;
	}

	private List<TopSellingProductResponse> buildTopSellingProducts() {
		Map<Integer, ProductSalesAggregate> aggregates = saleDetailRepository.findAll().stream()
				.filter(detail -> detail.getSale() != null)
				.filter(detail -> isCompletedSale(detail.getSale()))
				.filter(detail -> detail.getProduct() != null)
				.collect(Collectors.toMap(
						detail -> detail.getProduct().getProductId(),
						this::toAggregate,
						ProductSalesAggregate::merge
				));

		return aggregates.values().stream()
				.sorted(Comparator
						.comparingLong(ProductSalesAggregate::totalQuantitySold).reversed()
						.thenComparing(ProductSalesAggregate::totalRevenue, Comparator.reverseOrder()))
				.limit(TOP_SELLING_LIMIT)
				.map(aggregate -> {
					TopSellingProductResponse response = new TopSellingProductResponse();
					response.setProductId(aggregate.productId());
					response.setProductName(aggregate.productName());
					response.setTotalQuantitySold(aggregate.totalQuantitySold());
					response.setTotalRevenue(aggregate.totalRevenue());
					return response;
				})
				.toList();
	}

	private List<SaleSummaryResponse> buildRecentSales(Integer userId) {
		List<Sale> recentSales = saleRepository.findAllWithCustomer().stream()
				.filter(sale -> Objects.equals(sale.getUserID(), userId))
				.filter(this::isCompletedSale)
				.sorted(Comparator.comparing(this::getSaleDateTime).reversed())
				.limit(RECENT_SALES_LIMIT)
				.toList();

		List<SaleSummaryResponse> responses = saleMapper.toSummaryResponseList(recentSales);
		String userFullName = authenticatedUserProvider.getCurrentUser().getFullName();
		responses.forEach(response -> response.setUserName(userFullName));
		return responses;
	}

	private long countCompletedSales(List<Sale> sales) {
		return sales.stream()
				.filter(this::isCompletedSale)
				.count();
	}

	private BigDecimal sumCompletedSalesRevenue(List<Sale> sales) {
		return sumSalesRevenue(sales.stream()
				.filter(this::isCompletedSale)
				.toList());
	}

	private BigDecimal sumSalesRevenue(List<Sale> sales) {
		return sales.stream()
				.map(Sale::getTotalAmount)
				.filter(Objects::nonNull)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	private long countLowStockProducts(List<Product> products) {
		return products.stream()
				.filter(product -> product.getReorderLevel() != null)
				.filter(product -> getStockQuantity(product) > 0)
				.filter(product -> getStockQuantity(product) <= product.getReorderLevel())
				.count();
	}

	private long countOutOfStockProducts(List<Product> products) {
		return products.stream()
				.filter(product -> getStockQuantity(product) <= 0)
				.count();
	}

	private ProductSalesAggregate toAggregate(SaleDetail detail) {
		Integer productId = detail.getProduct().getProductId();
		String productName = detail.getProduct().getProductName();
		long quantity = detail.getQuantity() == null ? 0L : detail.getQuantity();
		BigDecimal revenue = detail.getSubTotal() != null
				? detail.getSubTotal()
				: safeUnitPrice(detail).multiply(BigDecimal.valueOf(quantity));
		return new ProductSalesAggregate(productId, productName, quantity, revenue);
	}

	private BigDecimal safeUnitPrice(SaleDetail detail) {
		return detail.getUnitPrice() == null ? BigDecimal.ZERO : detail.getUnitPrice();
	}

	private boolean isCompletedSale(Sale sale) {
		return sale.getStatus() == SaleStatus.COMPLETED;
	}

	private LocalDateTime getSaleDateTime(Sale sale) {
		return sale.getSaleDate() != null ? sale.getSaleDate() : sale.getCreatedAt();
	}

	private int getStockQuantity(Product product) {
		return product.getStockQuantity() == null ? 0 : product.getStockQuantity();
	}

	private record ProductSalesAggregate(
			Integer productId,
			String productName,
			long totalQuantitySold,
			BigDecimal totalRevenue) {

		private ProductSalesAggregate merge(ProductSalesAggregate other) {
			return new ProductSalesAggregate(
					productId,
					productName,
					totalQuantitySold + other.totalQuantitySold,
					totalRevenue.add(other.totalRevenue)
			);
		}
	}
}
