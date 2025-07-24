package fsa.project.online_shop.services;

import fsa.project.online_shop.models.Cart;
import fsa.project.online_shop.models.User;

public interface CartService {
    public Integer getCartItemCount(Long cartId);

    public void addProductToCart(Long cartId, Long productId, Integer quantity);
    public void removeProductFromCart(User user, Long productId);
    public void increaseQuantity(Cart cart, Long productId);
    public void decreaseQuantity(Cart cart, Long productId);

}
