package fsa.project.online_shop.services.implement;

import fsa.project.online_shop.models.Cart;
import fsa.project.online_shop.models.CartItem;
import fsa.project.online_shop.models.Product;
import fsa.project.online_shop.repositories.CartItemRepository;
import fsa.project.online_shop.repositories.CartRepository;
import fsa.project.online_shop.repositories.ProductRepository;
import fsa.project.online_shop.repositories.UserRepository;
import fsa.project.online_shop.services.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@RequiredArgsConstructor
@Service
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    public Integer getCartItemCount(Long cartId) {
        Optional<Cart> cart = cartRepository.findById(cartId);
        return cart.map(value -> value.getCartItems().size()).orElse(0);
    }

    @Override
    public void createCart(Long userId) {
        Cart cart = Cart.builder()
                .sum(0D)
                .user(userRepository.findById(userId).orElse(null))
                .build();
        cartRepository.save(cart);
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
            CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cartId, productId).get();
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
    public void addProductToAnonymousCart(Map<Long, Integer> cart, Long productId, Integer quantity) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null || product.getStatus() == false) {
            throw new IllegalArgumentException("Product is not available");
        }
        if ((cart.containsKey(productId) && cart.get(productId) + quantity > product.getQuantity())
                || (quantity > product.getQuantity())) {
            throw new IllegalArgumentException("Not enough quantity in stock");
        }
        cart.merge(productId, quantity, Integer::sum);
//        if (cart.containsKey(productId)) {
//            cart.put(productId, cart.get(productId) + quantity);
//        } else {
//            cart.put(productId, quantity);
//        }
    }

    public Cart generateCartFromMap(Map<Long, Integer> map){
        Cart cart = Cart.builder()
                .sum(0D)
                .build();
        Set<CartItem> cartItems = new HashSet<>();
        for (Map.Entry<Long, Integer> entry : map.entrySet()) {
            Product product = productRepository.findById(entry.getKey()).orElse(null);
            CartItem cartItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(entry.getValue())
                    .price(product.getPrice() * entry.getValue())
                    .build();
            cartItems.add(cartItem);
            cart.setSum(cart.getSum() + cartItem.getPrice());
        }
        cart.setCartItems(cartItems);
        return cart;
    }
}
