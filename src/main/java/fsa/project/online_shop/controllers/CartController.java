package fsa.project.online_shop.controllers;

import fsa.project.online_shop.dtos.AddToCartRequest;
import fsa.project.online_shop.models.Cart;
import fsa.project.online_shop.models.Product;
import fsa.project.online_shop.models.User;
import fsa.project.online_shop.services.CartService;
import fsa.project.online_shop.services.ProductService;
import fsa.project.online_shop.services.UserService;
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
import java.util.Map;

@RestController
@RequestMapping("/cart/api")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;
    private final UserService userService;
    private final ProductService productService;
    private final SessionUtil sessionUtil;

    @GetMapping("/count")
    public Integer getCartItemCount(HttpServletRequest request) {
        User user = sessionUtil.getUserFromSession();
        if (user == null) {
            Map<Long, Integer> cart = sessionUtil.getCartFromSession();
            return (cart.isEmpty()) ? 0 : cart.size();
        }
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
            if (user == null) {
                Map<Long, Integer> cart = sessionUtil.getCartFromSession();
                cartService.addProductToAnonymousCart(cart, productId, quantity);
                sessionUtil.setCartToSession(cart);
                int totalItems = cart.size();
                System.out.println(cart.toString());
                response.put("success", true);
                response.put("message", "Product added to cart successfully");
                response.put("cartItemCount", totalItems);
            } else {
                cartService.addProductToCart(user.getCart().getId(), productId, quantity);
                int totalItems = cartService.getCartItemCount(user.getCart().getId());
                response.put("success", true);
                response.put("message", "Product added to cart successfully");
                response.put("cartItemCount", totalItems);
            }
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "An error occurred while adding product to cart: " + e.getMessage());
            return response;
        }
    }
//
//    @GetMapping("/cart-detail")
//    public ResponseEntity<?> getCart(HttpServletRequest request) {
//        User user = sessionUtil.getUserFromSession();
//        Cart cart;
//        if(user != null){
//            cart = user.getCart();
//        }
//        else{
//            Map<Long, Integer> cartMap = sessionUtil.getCartFromSession();
//            cart = cartService.generateCartFromMap(cartMap);
//        }
//        return ResponseEntity.ok(cart);
//    }



}
