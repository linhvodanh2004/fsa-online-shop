package fsa.project.online_shop.services.implement;

import fsa.project.online_shop.models.Cart;
import fsa.project.online_shop.models.Order;
import fsa.project.online_shop.models.OrderItem;
import fsa.project.online_shop.models.Product;
import fsa.project.online_shop.models.constant.OrderStatus;
import fsa.project.online_shop.repositories.*;
import fsa.project.online_shop.models.User;
import fsa.project.online_shop.repositories.OrderRepository;
import fsa.project.online_shop.services.OrderService;
import fsa.project.online_shop.utils.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    @Override
    public Page<Order> getAllOrders(Pageable pageable) {
        try {
            return orderRepository.findAllByOrderByCreationTimeDesc(pageable);
        } catch (Exception e) {
            log.error("Error getting all orders: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get orders", e);
        }
    }

    @Override
    public Page<Order> getOrdersByUser(User user, Pageable pageable) {
        try {
            if (user == null) {
                throw new IllegalArgumentException("User cannot be null");
            }
            return orderRepository.findByUser(user, pageable);
        } catch (Exception e) {
            log.error("Error getting orders for user {}: {}", user.getEmail(), e.getMessage(), e);
            throw new RuntimeException("Failed to get user orders", e);
        }
    }
    @Override
    public Order handleSaveOrder(Order order) {
        return orderRepository.save(order);
    }
    @Override
    public Order getOrderById(Long orderId) {
        try {
            if (orderId == null) {
                throw new IllegalArgumentException("Order ID cannot be null");
            }
            return orderRepository.findById(orderId).orElse(null);
        } catch (Exception e) {
            log.error("Error getting order by ID {}: {}", orderId, e.getMessage(), e);
            throw new RuntimeException("Failed to get order", e);
        }
    }

    @Override
    @Transactional
    public Order createOrder(Order order) {
        try {
            if (order == null) {
                throw new IllegalArgumentException("Order cannot be null");
            }

            // Set creation time if not set
            if (order.getCreationTime() == null) {
                order.setCreationTime(LocalDateTime.now());
            }

            // Set default status if not set
            if (order.getStatus() == null) {
                order.setStatus("PENDING");
            }

            return orderRepository.save(order);

        } catch (Exception e) {
            log.error("Error creating order: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create order", e);
        }
    }

    @Override
    @Transactional
    public Order updateOrder(Order order) {
        try {
            if (order == null || order.getId() == null) {
                throw new IllegalArgumentException("Order and Order ID cannot be null");
            }

            Order savedOrder = orderRepository.save(order);
            log.info("✅ Updated order with ID: {}", savedOrder.getId());
            return savedOrder;

        } catch (Exception e) {
            log.error("Error updating order {}: {}", order.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to update order", e);
        }
    }
    @Override
    @Transactional
    public void handlePaymentSuccess(Long orderId, String transactionId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        order.setPaymentStatus(true);
        order.setPaymentTransactionId(transactionId);
        order.setPaymentDate(LocalDateTime.now());
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId) {
        try {
            if (orderId == null) {
                throw new IllegalArgumentException("Order ID cannot be null");
            }

            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("Order not found"));

            // Only allow cancellation for pending orders
            if (!"PENDING".equals(order.getStatus())) {
                throw new IllegalStateException("Cannot cancel order in status: " + order.getStatus());
            }

            order.setStatus("CANCELLED");
            order.setCancellationTime(LocalDateTime.now());

            orderRepository.save(order);
            log.info("❌ Cancelled order with ID: {}", orderId);

        } catch (Exception e) {
            log.error("Error cancelling order {}: {}", orderId, e.getMessage(), e);
            throw new RuntimeException("Failed to cancel order", e);
        }
    }

    @Override
    @Transactional
    public void updateOrderStatus(Long orderId, String status) {
        try {
            if (orderId == null || status == null) {
                throw new IllegalArgumentException("Order ID and status cannot be null");
            }

            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("Order not found"));

            String oldStatus = order.getStatus();
            order.setStatus(status);

            // Set appropriate timestamps based on status
            switch (status.toUpperCase()) {
                case "TRANSIT":
                    order.setTransitTime(LocalDateTime.now());
                    break;
                case "DELIVERED":
                    order.setDeliveryTime(LocalDateTime.now());
                    break;
                case "CANCELLED":
                    order.setCancellationTime(LocalDateTime.now());
                    break;
            }

            orderRepository.save(order);

        } catch (Exception e) {
            log.error("Error updating order status {}: {}", orderId, e.getMessage(), e);
            throw new RuntimeException("Failed to update order status", e);
        }
    }
    @Override
    @Transactional
    public void handlePaymentFailed(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        order.setPaymentStatus(false);
        order.setPaymentTransactionId(null);
        order.setPaymentDate(null);
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancellationTime(LocalDateTime.now());
        orderRepository.save(order);

        Set<OrderItem> orderItems = order.getOrderItems();
        orderItems.forEach(orderItem -> {
            Product product = productRepository.findById(orderItem.getProduct().getId()).orElse(null);
            if(product != null){
                product.setQuantity(product.getQuantity() + orderItem.getQuantity());
                product.setSold(product.getSold() - orderItem.getQuantity());
                productRepository.save(product);
            }
        });
    }
    @Override
    public List<Order> getOrdersByStatus(String status) {
        try {
            if (status == null) {
                throw new IllegalArgumentException("Status cannot be null");
            }
            return orderRepository.findByStatus(status);
        } catch (Exception e) {
            log.error("Error getting orders by status {}: {}", status, e.getMessage(), e);
            throw new RuntimeException("Failed to get orders by status", e);
        }
    }

    @Override
    public List<Order> getRecentOrdersByUser(User user, int limit) {
        try {
            if (user == null) {
                throw new IllegalArgumentException("User cannot be null");
            }
            Pageable pageable = PageRequest.of(0, limit);
            return orderRepository.findRecentOrdersByUser(user, pageable);
        } catch (Exception e) {
            log.error("Error getting recent orders for user {}: {}", user.getEmail(), e.getMessage(), e);
            throw new RuntimeException("Failed to get recent orders", e);
        }
    }

    @Override
    public long countOrdersByUser(User user) {
        try {
            if (user == null) {
                throw new IllegalArgumentException("User cannot be null");
            }
            return orderRepository.countByUser(user);
        } catch (Exception e) {
            log.error("Error counting orders for user {}: {}", user.getEmail(), e.getMessage(), e);
            return 0;
        }
    }

    @Override
    public Double getTotalOrderValueByUser(User user) {
        try {
            if (user == null) {
                throw new IllegalArgumentException("User cannot be null");
            }
            Double total = orderRepository.getTotalOrderValueByUser(user);
            return total != null ? total : 0.0;
        } catch (Exception e) {
            log.error("Error getting total order value for user {}: {}", user.getEmail(), e.getMessage(), e);
            return 0.0;
        }
    }
    @Override
    @Transactional
    public Order createOrder(Cart cart) {
        if(!cartRepository.areAllCartItemsValid(cart.getId()) || cart.getCartItems().isEmpty()){
            throw new IllegalArgumentException("Cart is not valid");
        }
        Order order = OrderMapper.fromCartToOrder(cart);
        orderRepository.save(order);
        Set<OrderItem> orderItems = order.getOrderItems();
        orderItems.forEach(orderItem -> {
            orderItem.setOrder(order);
            orderItemRepository.save(orderItem);
            Product product = productRepository.findById(orderItem.getProduct().getId()).orElse(null);
            if(product != null){
                product.setQuantity(product.getQuantity() - orderItem.getQuantity());
                product.setSold(product.getSold() + orderItem.getQuantity());
                productRepository.save(product);
            }
            else{
                throw new IllegalArgumentException("Product is not available");
            }
        });
        order.setOrderItems(orderItems);
        order.setCreationTime(LocalDateTime.now());
        order.setPaymentMethod("COD");
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentStatus(false);

        cartItemRepository.deleteByCartId(cart.getId());
        cart.setCartItems(null);
        cartRepository.save(cart);

        return orderRepository.save(order);
    }
}
