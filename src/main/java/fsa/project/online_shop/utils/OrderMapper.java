package fsa.project.online_shop.utils;

import fsa.project.online_shop.dtos.CartItemResponse;
import fsa.project.online_shop.dtos.CartResponse;
import fsa.project.online_shop.dtos.OrderItemResponse;
import fsa.project.online_shop.dtos.OrderResponse;
import fsa.project.online_shop.models.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class OrderMapper {
    public static Order fromCartToOrder(Cart cart) {
        return Order.builder()
                .sum(cart.getSum())
                .user(cart.getUser())
                .orderItems(fromCartToOrderItem(cart))
                .build();
    }

    private static Set<OrderItem> fromCartToOrderItem(Cart cart) {
        return cart.getCartItems().stream()
                .map(cartItem ->
                        OrderItem.builder()
                                .price(cartItem.getPrice())
                                .quantity(cartItem.getQuantity())
                                .product(cartItem.getProduct())
                                .build()
                ).collect(Collectors.toSet());
    }


    public static OrderResponse toOrderResponse(Order order) {
        return OrderResponse.builder()
                .orderId(order.getId())
                .username(order.getUser().getUsername())
                .receiverName(order.getReceiverName())
                .receiverPhone(order.getReceiverPhone())
                .receiverEmail(order.getReceiverEmail())
                .receiverAddress(order.getReceiverAddress())
                .note(order.getNote())
                .status(order.getStatus())
                .sum(order.getSum())
                .creationTime(order.getCreationTime())
                .transitTime(order.getTransitTime())
                .deliveryTime(order.getDeliveryTime())
                .cancellationTime(order.getCancellationTime())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .paymentTransactionId(order.getPaymentTransactionId())
                .paymentDate(order.getPaymentDate())
                .orderItems(order.getOrderItems()
                        .stream()
                        .map(OrderMapper::toOrderItemResponse)
                        .sorted(Comparator.comparing(OrderItemResponse::getProductId))
                        .toList())
                .build();
    }

    private static OrderItemResponse toOrderItemResponse(OrderItem item) {
        Product product = item.getProduct();
        return OrderItemResponse.builder()
                .productId(product.getId())
                .productName(product.getName())
                .productImage(product.getImage())
                .price(item.getPrice())
                .unitPrice(item.getPrice() / item.getQuantity())
                .quantity(item.getQuantity())
                .build();
    }
}
