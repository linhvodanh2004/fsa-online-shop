package fsa.project.online_shop.services;

import fsa.project.online_shop.dtos.MonthlyRevenueDto;
import fsa.project.online_shop.models.Cart;
import fsa.project.online_shop.models.Order;
import fsa.project.online_shop.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {
    public Order createOrder(Cart cart);
    public Order handleSaveOrder(Order order);
    public void handlePaymentSuccess(Long orderId, String transactionId);
    public void handlePaymentFailed(Long orderId);

    /**
     * Get all orders with pagination
     */
    Page<Order> getAllOrders(Pageable pageable, String orderStatus);

    /**
     * Get orders by user with pagination
     */
    Page<Order> getOrdersByUser(User user, Pageable pageable);

    /**
     * Get order by ID
     */
    Order getOrderById(Long orderId);

    /**
     * Create new order
     */
    Order createOrder(Order order);

    /**
     * Update order
     */
    Order updateOrder(Order order);

    /**
     * Cancel order
     */
    void cancelOrder(Long orderId);

    /**
     * Update order status
     */
    void updateOrderStatus(Long orderId, String status);

    /**
     * Get orders by status
     */
    List<Order> getOrdersByStatus(String status);

    /**
     * Get user's recent orders
     */
    List<Order> getRecentOrdersByUser(User user, int limit);

    /**
     * Count orders by user
     */
    long countOrdersByUser(User user);

    /**
     * Get total order value by user
     */
    Double getTotalOrderValueByUser(User user);

    public void updateOrderStatusInBulk(String orderStatus);

    long countAll();

    double getTotalEarnings();

    public List<MonthlyRevenueDto> getRevenueLast6Months();

    public Order getOrderByIdWithItems(Long orderId);
}
