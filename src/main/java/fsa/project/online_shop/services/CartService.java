package fsa.project.online_shop.services;

import fsa.project.online_shop.models.Cart;

import java.util.Map;

public interface CartService {
    public Integer getCartItemCount(Long cartId);

    public void addProductToCart(Long cartId, Long productId, Integer quantity);
    public void addProductToAnonymousCart(Map<Long, Integer> cart, Long productId, Integer quantity);
    public void createCart(Long userId);
    public Cart generateCartFromMap(Map<Long, Integer> map);
    }
