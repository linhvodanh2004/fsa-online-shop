package fsa.project.online_shop.services.implement;

import fsa.project.online_shop.models.Cart;
import fsa.project.online_shop.models.CartItem;
import fsa.project.online_shop.models.Product;
import fsa.project.online_shop.models.User;
import fsa.project.online_shop.repositories.CartItemRepository;
import fsa.project.online_shop.repositories.CartRepository;
import fsa.project.online_shop.repositories.ProductRepository;
import fsa.project.online_shop.services.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    @Override
    public Integer getCartItemCount(Long cartId) {
        Optional<Cart> cart = cartRepository.findById(cartId);
        return cart.map(value -> value.getCartItems().size()).orElse(0);
    }

    @Override
    @Transactional
    public void addProductToCart(Long cartId, Long productId, Integer quantity) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product.getStatus() == false) {
            throw new IllegalArgumentException("Product is not available");
        }
        Cart cart = cartRepository.findById(cartId).orElse(null);
        if (cartItemRepository.existsByCartIdAndProductId(cartId, productId)) {
            CartItem cartItem = cartItemRepository.findByCart_IdAndProduct_Id(cartId, productId).get();
            if (cartItem.getQuantity() + quantity > product.getQuantity()) {
                throw new IllegalArgumentException("Not enough quantity in stock");
            }
            Integer newQuantity = cartItem.getQuantity() + quantity;
            cartItem.setQuantity(newQuantity);
            cartItem.setPrice(product.getPrice() * newQuantity);
            cartItemRepository.save(cartItem);
        } else {
            if (quantity > product.getQuantity()) {
                throw new IllegalArgumentException("Not enough quantity in stock");
            }
            CartItem cartItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(quantity)
                    .price(product.getPrice() * quantity)
                    .build();
            cartItemRepository.save(cartItem);
        }
        cartRepository.updateCartSumById(cartId);
    }

    @Override
    @Transactional
    public void removeProductFromCart(User user, Long productId) {
        Cart cart = cartRepository.findByUser(user);
        Set<CartItem> items = cart.getCartItems();
        CartItem itemToRemove = cartItemRepository.findByCart_IdAndProduct_Id(cart.getId(), productId).get();
        items.remove(itemToRemove);
        cartItemRepository.delete(itemToRemove);
        cart.setCartItems(items);
        cartRepository.updateCartSumById(cart.getId());
        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void increaseQuantity(Cart cart, Long productId) {
        CartItem item = cartItemRepository.findByCart_IdAndProduct_Id(cart.getId(), productId).get();
        Set<CartItem> items = cart.getCartItems();
        Product product = item.getProduct();
        if (item.getQuantity() >= product.getQuantity()) {
            throw new IllegalArgumentException("Not enough quantity in stock");
        }
        item.setQuantity(item.getQuantity() + 1);
        item.setPrice(product.getPrice() * item.getQuantity());
        items.stream().filter(cartItem -> cartItem.getProduct().getId().equals(productId)).findFirst().ifPresent(cartItem -> {
            cartItem.setQuantity(item.getQuantity());
            cartItem.setPrice(item.getPrice());
        });
        cartItemRepository.save(item);
        cart.setCartItems(items);
        cartRepository.updateCartSumById(cart.getId());
        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void decreaseQuantity(Cart cart, Long productId) {
        CartItem item = cartItemRepository.findByCart_IdAndProduct_Id(cart.getId(), productId).get();
        Set<CartItem> items = cart.getCartItems();
        Product product = item.getProduct();
        if (item.getQuantity() == 1) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        Integer newQuantity = (item.getQuantity() > product.getQuantity()) ? product.getQuantity() : item.getQuantity() - 1;
        item.setQuantity(newQuantity);
        item.setPrice(product.getPrice() * item.getQuantity());
        items.stream().filter(cartItem -> cartItem.getProduct().getId().equals(productId)).findFirst().ifPresent(cartItem -> {
            cartItem.setQuantity(item.getQuantity());
            cartItem.setPrice(item.getPrice());
        });
        cartItemRepository.save(item);
        cart.setCartItems(items);
        cartRepository.updateCartSumById(cart.getId());
        cartRepository.save(cart);
    }
}
