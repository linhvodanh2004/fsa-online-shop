package fsa.project.online_shop.utils;

import fsa.project.online_shop.dtos.CartItemResponse;
import fsa.project.online_shop.dtos.CartResponse;
import fsa.project.online_shop.models.Cart;
import fsa.project.online_shop.models.CartItem;
import fsa.project.online_shop.models.Product;

import java.util.Comparator;

public class CartMapper {
    public static CartResponse toCartResponse(Cart cart) {
        return CartResponse.builder()
                .cartId(cart.getId())
                .sum(cart.getSum())
                .cartItems(cart.getCartItems().stream()
                        .map(CartMapper::toCartItemResponse)
                        .sorted(Comparator.comparing(CartItemResponse::getProductId))
                        .toList())
                .build();
    }

    private static CartItemResponse toCartItemResponse(CartItem item) {
        Product product = item.getProduct();
        return CartItemResponse.builder()
                .productId(product.getId())
                .productName(product.getName())
                .productImage(product.getImage())
                .productStatus(product.getStatus())
                .productQuantity(product.getQuantity())
                .unitPrice(product.getPrice())
                .price(item.getPrice())
                .quantity(item.getQuantity())
                .build();
    }
}

