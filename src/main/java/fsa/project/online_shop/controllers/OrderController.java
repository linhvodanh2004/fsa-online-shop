package fsa.project.online_shop.controllers;

import fsa.project.online_shop.models.Cart;
import fsa.project.online_shop.models.User;
import fsa.project.online_shop.services.CartService;
import fsa.project.online_shop.services.UserService;
import fsa.project.online_shop.utils.SessionUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.support.SessionStatus;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class OrderController {
    private final UserService userService;
    private final CartService cartService;
    private final SessionUtil sessionUtil;

    @GetMapping("/cart/anonymous")
    public String showAnonymousCart(Model model) {
        Map<Long, Integer> cart = sessionUtil.getCartFromSession();
        Cart cartObj = cartService.generateCartFromMap(cart);
        model.addAttribute("anonymousCart", (cartObj.getCartItems().isEmpty()) ? null : cartObj);
        return "user/cart-detail";
    }

    @GetMapping("/cart/user-cart")
    public String showUserCart( Model model) {
        User user = sessionUtil.getUserFromSession();
        System.out.println(user);
        Cart cart = user.getCart();
        model.addAttribute("userCart", (cart.getCartItems().isEmpty()) ? null : cart.getCartItems());
        return "user/cart-detail";
    }
}
