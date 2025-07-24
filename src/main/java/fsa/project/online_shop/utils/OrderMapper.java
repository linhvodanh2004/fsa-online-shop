package fsa.project.online_shop.utils;

import fsa.project.online_shop.models.Cart;
import fsa.project.online_shop.models.Order;
import fsa.project.online_shop.models.OrderItem;

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
}
