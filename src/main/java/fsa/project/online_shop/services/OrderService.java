package fsa.project.online_shop.services;

import fsa.project.online_shop.models.Cart;
import fsa.project.online_shop.models.Order;

public interface OrderService {
    public Order createOrder(Cart cart);
    public Order handleSaveOrder(Order order);
    public Order getOrderById(Long orderId);
    public void handlePaymentSuccess(Long orderId, String transactionId);
    public void handlePaymentFailed(Long orderId);
}
