package fsa.project.online_shop.controllers;

import fsa.project.online_shop.dtos.AddToCartRequest;
import fsa.project.online_shop.dtos.CartItemResponse;
import fsa.project.online_shop.dtos.CartResponse;
import fsa.project.online_shop.models.Cart;
import fsa.project.online_shop.models.Product;
import fsa.project.online_shop.models.User;
import fsa.project.online_shop.services.CartService;
import fsa.project.online_shop.services.ProductService;
import fsa.project.online_shop.services.UserService;
import fsa.project.online_shop.utils.CartMapper;
import fsa.project.online_shop.utils.SessionUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cart/api")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;
    private final ProductService productService;
    private final SessionUtil sessionUtil;

    @GetMapping("/count")
    public Integer getCartItemCount(HttpServletRequest request) {
        User user = sessionUtil.getUserFromSession();
        return cartService.getCartItemCount(user.getCart().getId());
    }

    @PostMapping("/add-to-cart")
    public Map<String, Object> addToCart(
            @RequestBody AddToCartRequest addToCartRequest) {
        Map<String, Object> response = new HashMap<>();
        Long productId = addToCartRequest.getProductId();
        Integer quantity = addToCartRequest.getQuantity();
        Product product = productService.getProductById(productId);
        User user = sessionUtil.getUserFromSession();
        try {
            if (product == null) {
                response.put("success", false);
                response.put("message", "Product not found");
                return response;
            }
            cartService.addProductToCart(user.getCart().getId(), productId, quantity);
            int totalItems = cartService.getCartItemCount(user.getCart().getId());
            response.put("success", true);
            response.put("message", "Product added to cart successfully");
            response.put("cartItemCount", totalItems);
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "An error occurred while adding product to cart: " + e.getMessage());
            return response;
        }
    }

    @GetMapping("/get-cart")
    public ResponseEntity<?> getCart() {
        User user = sessionUtil.getUserFromSession();
        Cart cart = user.getCart();
        CartResponse response = CartMapper.toCartResponse(cart);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/{productId}")
    public ResponseEntity<?> deleteCartItem(@PathVariable Long productId) {
        User user = sessionUtil.getUserFromSession();
        cartService.removeProductFromCart(user, productId);
        return ResponseEntity.ok().body("Deleted successfully");
    }

    @PostMapping("/increase-quantity/{productId}")
    public ResponseEntity<?> increaseQuantity(@PathVariable Long productId) {
        try {
            User user = sessionUtil.getUserFromSession();
            Cart cart = user.getCart();
            cartService.increaseQuantity(cart, productId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Increased quantity successfully"));
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "An error occurred while increasing quantity: " + e.getMessage()));
        }
    }

    @PostMapping("/decrease-quantity/{productId}")
    public ResponseEntity<?> decreaseQuantity(@PathVariable Long productId) {

        try {
            User user = sessionUtil.getUserFromSession();
            Cart cart = user.getCart();
            cartService.decreaseQuantity(cart, productId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Decreased quantity successfully"
            ));
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "An error occurred while decreasing quantity: " + e.getMessage()));
        }
    }
}
