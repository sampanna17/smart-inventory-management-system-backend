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
import com.smartinventorysystem.modules.product.repository.ProductRepository;
import com.smartinventorysystem.modules.purchase.dto.response.PurchaseResponse;
import com.smartinventorysystem.modules.purchase.entity.Purchase;
import com.smartinventorysystem.modules.purchase.mapper.PurchaseMapper;
import com.smartinventorysystem.modules.purchase.repository.PurchaseRepository;
import com.smartinventorysystem.modules.sale.dto.response.SaleSummaryResponse;
import com.smartinventorysystem.modules.sale.entity.Sale;
import com.smartinventorysystem.modules.sale.entity.SaleDetail;
import com.smartinventorysystem.modules.sale.mapper.SaleMapper;
import com.smartinventorysystem.modules.sale.repository.SaleDetailRepository;
import com.smartinventorysystem.modules.sale.repository.SaleRepository;
import com.smartinventorysystem.modules.stockmovement.dto.response.StockMovementResponse;
import com.smartinventorysystem.modules.stockmovement.entity.StockMovement;
import com.smartinventorysystem.modules.stockmovement.mapper.StockMovementMapper;
import com.smartinventorysystem.modules.stockmovement.repository.StockMovementRepository;
import com.smartinventorysystem.modules.supplier.repository.SupplierRepository;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

	private static final int DASHBOARD_PERIOD_DAYS = 7;
	private static final int TOP_SELLING_LIMIT = 5;
	private static final int RECENT_LIMIT = 5;

	private final ProductRepository productRepository;
	private final CategoryRepository categoryRepository;
	private final SupplierRepository supplierRepository;
	private final CustomerRepository customerRepository;
	private final SaleRepository saleRepository;
	private final SaleDetailRepository saleDetailRepository;
	private final PurchaseRepository purchaseRepository;
	private final NotificationRepository notificationRepository;
	private final StockMovementRepository stockMovementRepository;

	private final SaleMapper saleMapper;
	private final NotificationMapper notificationMapper;
	private final PurchaseMapper purchaseMapper;
	private final StockMovementMapper stockMovementMapper;

	private final AuthenticatedUserProvider authenticatedUserProvider;
	private final Clock clock;

	@Override
	public AdminDashboardResponse getAdminDashboard() {

		AdminDashboardResponse response = new AdminDashboardResponse();

		response.setSummary(buildAdminSummary());
		response.setCharts(buildDashboardCharts());
		response.setTopSellingProducts(buildTopSellingProducts());
		response.setRecentActivities(buildRecentActivities());

		return response;
	}

	@Override
	public StaffDashboardResponse getStaffDashboard() {

		Integer userId = authenticatedUserProvider.getCurrentUserId();

		StaffDashboardResponse response = new StaffDashboardResponse();

		response.setSummary(buildStaffSummary(userId));
		response.setRecentSales(buildRecentSales(userId));
		response.setNotifications(
				notificationMapper.toResponseList(
						notificationRepository.findTop10ByUserIDOrderByCreatedAtDesc(userId)
				)
		);

		return response;
	}

	private AdminDashboardSummaryResponse buildAdminSummary() {

		AdminDashboardSummaryResponse summary = new AdminDashboardSummaryResponse();

		summary.setTotalProducts(productRepository.count());
		summary.setTotalCategories(categoryRepository.count());
		summary.setTotalSuppliers(supplierRepository.count());
		summary.setTotalCustomers(customerRepository.count());

		summary.setTotalSales(
				saleRepository.countByStatus(SaleStatus.COMPLETED)
		);

		summary.setTotalRevenue(
				saleRepository.sumRevenueByStatus(SaleStatus.COMPLETED)
		);

		summary.setTotalPurchases(
				purchaseRepository.count()
		);

		summary.setLowStockProducts(
				productRepository.countLowStockProducts()
		);

		summary.setOutOfStockProducts(
				productRepository.countOutOfStockProducts()
		);

		summary.setUnreadNotifications(
				notificationRepository.countByUserIDAndIsReadFalse(
						authenticatedUserProvider.getCurrentUserId()
				)
		);

		return summary;
	}

	private StaffDashboardSummaryResponse buildStaffSummary(Integer userId) {

		LocalDate today = LocalDate.now(clock);

		LocalDateTime start = today.atStartOfDay();
		LocalDateTime end = today.plusDays(1).atStartOfDay().minusNanos(1);

		StaffDashboardSummaryResponse summary = new StaffDashboardSummaryResponse();

		summary.setTodaySales(
				saleRepository.countByUserIDAndStatusAndSaleDateBetween(
						userId,
						SaleStatus.COMPLETED,
						start,
						end
				)
		);

		summary.setTodayRevenue(
				saleRepository.sumRevenueByUserAndDateBetween(
						userId,
						SaleStatus.COMPLETED,
						start,
						end
				)
		);

		summary.setProductsSoldToday(
				saleDetailRepository.countProductsSoldToday(
						userId,
						SaleStatus.COMPLETED,
						start,
						end
				)
		);

		summary.setLowStockProducts(
				productRepository.countLowStockProducts()
		);

		return summary;
	}

	private DashboardChartsResponse buildDashboardCharts() {

		List<Sale> completedSales = saleRepository
				.findByStatusWithCustomer(SaleStatus.COMPLETED);

		LocalDate today = LocalDate.now(clock);

		List<TrendDataPointResponse> salesTrend = new ArrayList<>();
		List<TrendDataPointResponse> revenueTrend = new ArrayList<>();

		for (int i = DASHBOARD_PERIOD_DAYS - 1; i >= 0; i--) {

			LocalDate date = today.minusDays(i);

			long count = completedSales.stream()
					.filter(s -> getSaleDateTime(s).toLocalDate().equals(date))
					.count();

			BigDecimal revenue = completedSales.stream()
					.filter(s -> getSaleDateTime(s).toLocalDate().equals(date))
					.map(Sale::getTotalAmount)
					.reduce(BigDecimal.ZERO, BigDecimal::add);

			TrendDataPointResponse salesPoint = new TrendDataPointResponse();
			salesPoint.setPeriod(date.toString());
			salesPoint.setCount(count);
			salesPoint.setAmount(revenue);

			TrendDataPointResponse revenuePoint = new TrendDataPointResponse();
			revenuePoint.setPeriod(date.toString());
			revenuePoint.setCount(count);
			revenuePoint.setAmount(revenue);

			salesTrend.add(salesPoint);
			revenueTrend.add(revenuePoint);
		}

		DashboardChartsResponse charts = new DashboardChartsResponse();
		charts.setSalesTrend(salesTrend);
		charts.setRevenueTrend(revenueTrend);

		return charts;
	}

	private List<TopSellingProductResponse> buildTopSellingProducts() {

		return saleDetailRepository.findAll().stream()
				.filter(detail -> detail.getSale() != null)
				.filter(detail -> detail.getSale().getStatus() == SaleStatus.COMPLETED)
				.filter(detail -> detail.getProduct() != null)
				.collect(Collectors.toMap(
						detail -> detail.getProduct().getProductId(),
						this::toAggregate,
						ProductSalesAggregate::merge
				))
				.values()
				.stream()
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

	private DashboardRecentActivitiesResponse buildRecentActivities() {

		DashboardRecentActivitiesResponse activities = new DashboardRecentActivitiesResponse();

		// Recent Sales
		List<SaleSummaryResponse> recentSales = saleMapper.toSummaryResponseList(
				saleRepository.findAllWithCustomer().stream()
						.sorted(Comparator.comparing(this::getSaleDateTime).reversed())
						.limit(RECENT_LIMIT)
						.toList()
		);

		recentSales.forEach(sale ->
				sale.setUserName(authenticatedUserProvider.getCurrentUser().getFullName())
		);

		// Recent Purchases
		List<PurchaseResponse> recentPurchases = purchaseMapper.toResponseList(
				purchaseRepository.findAllWithDetails().stream()
						.sorted(Comparator.comparing(Purchase::getPurchaseDate).reversed())
						.limit(RECENT_LIMIT)
						.toList()
		);

		recentPurchases.forEach(purchase ->
				purchase.setUserName(authenticatedUserProvider.getCurrentUser().getFullName())
		);

		// Recent Stock Movements
		List<StockMovementResponse> recentStockMovements = stockMovementMapper.toResponseList(
				stockMovementRepository.findAllWithProduct().stream()
						.sorted(Comparator.comparing(StockMovement::getMovementDate).reversed())
						.limit(RECENT_LIMIT)
						.toList()
		);

		recentStockMovements.forEach(movement ->
				movement.setUserName(authenticatedUserProvider.getCurrentUser().getFullName())
		);

		activities.setRecentSales(recentSales);
		activities.setRecentPurchases(recentPurchases);
		activities.setRecentStockMovements(recentStockMovements);

		return activities;
	}

	private List<SaleSummaryResponse> buildRecentSales(Integer userId) {

		List<SaleSummaryResponse> responses = saleMapper.toSummaryResponseList(
				saleRepository.findTop5ByUserIDAndStatusOrderBySaleDateDesc(
						userId,
						SaleStatus.COMPLETED
				)
		);

		String userName = authenticatedUserProvider.getCurrentUser().getFullName();

		responses.forEach(response -> response.setUserName(userName));

		return responses;
	}

	private LocalDateTime getSaleDateTime(Sale sale) {
		return sale.getSaleDate() != null
				? sale.getSaleDate()
				: sale.getCreatedAt();
	}

	private ProductSalesAggregate toAggregate(SaleDetail detail) {

		long quantity = detail.getQuantity() == null
				? 0L
				: detail.getQuantity();

		BigDecimal revenue = detail.getSubTotal() != null
				? detail.getSubTotal()
				: BigDecimal.ZERO;

		return new ProductSalesAggregate(
				detail.getProduct().getProductId(),
				detail.getProduct().getProductName(),
				quantity,
				revenue
		);
	}

	private record ProductSalesAggregate(
			Integer productId,
			String productName,
			long totalQuantitySold,
			BigDecimal totalRevenue
	) {

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
