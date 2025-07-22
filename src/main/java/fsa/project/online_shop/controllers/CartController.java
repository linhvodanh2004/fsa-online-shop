package fsa.project.online_shop.controllers;

import fsa.project.online_shop.models.User;
import fsa.project.online_shop.services.CartService;
import fsa.project.online_shop.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;
    private final UserService userService;

    @ResponseBody
    @GetMapping("/cart/api/count")
    public Integer getCartItemCount(Principal principal, HttpServletRequest request) {
        if (principal == null) {
            HttpSession session = request.getSession(false);
            Map<Long, Integer> cart = (Map<Long, Integer>) session.getAttribute("cart");
            if (cart == null) {return 0;}
            return cart.values().stream().mapToInt(i -> i).sum();
        }
        String username = principal.getName();
        User user = userService.getUserByUsername(username);
        if (user == null) {return 0;}
        return cartService.getCartItemCount(user.getCart().getId());
    }
}
