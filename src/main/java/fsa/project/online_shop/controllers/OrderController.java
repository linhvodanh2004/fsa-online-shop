package fsa.project.online_shop.controllers;

import fsa.project.online_shop.models.Cart;
import fsa.project.online_shop.models.Order;
import fsa.project.online_shop.models.User;
import fsa.project.online_shop.services.*;
import fsa.project.online_shop.utils.SessionUtil;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.support.SessionStatus;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class OrderController {
    private final CartService cartService;
    private final SessionUtil sessionUtil;
    private final VnPayService vnPayService;
    private final OrderService orderService;
    private final EmailSenderService emailSenderService;


    @GetMapping("/cart-detail")
    public String showUserCart(Model model) {
        User user = sessionUtil.getUserFromSession();
        System.out.println(user);
        Cart cart = user.getCart();
        model.addAttribute("userCart", (cart.getCartItems().isEmpty()) ? null : cart.getCartItems());
        return "user/cart-detail";
    }

    @GetMapping("/cart-detail/checkout-vnpay-success")
    public String checkoutSuccess(HttpServletRequest request, Model model) {
        int result = vnPayService.vnPayReturn(request);
        String vnp_TxnRef = request.getParameter("vnp_TxnRef");
        String vnp_TransactionNo = request.getParameter("vnp_TransactionNo");
        Order order = orderService.getOrderById(Long.parseLong(vnp_TxnRef));
        try {
            if (result == 1) {
                orderService.handlePaymentSuccess(order.getId(), vnp_TransactionNo);
                emailSenderService.notifyOrderPending(order);
            }
            else {
                orderService.handlePaymentFailed(Long.parseLong(vnp_TxnRef));
                emailSenderService.notifyOrderCancelled(order);
            }
            model.addAttribute("status", result);
            model.addAttribute("bankCode", request.getParameter("vnp_BankCode"));
            model.addAttribute("amount", parseCurrencyToLong(request.getParameter("vnp_Amount")));
            model.addAttribute("orderId", request.getParameter("vnp_TxnRef"));
            model.addAttribute("transactionNo", request.getParameter("vnp_TransactionNo"));
            model.addAttribute("payDate", parseVnpayDate(request.getParameter("vnp_PayDate")));
            return "user/checkout-vnpay-success"; // Trả về HTML
        }
        catch (MessagingException e) {
            return "redirect:/?error=email-failed";
        }
    }

    private Double parseCurrencyToLong(String input) {
        if (input == null || input.isBlank()) return 0D;
        String cleaned = input.replaceAll("[.,]", "");
        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format: " + input, e);
        }
    }
    private LocalDateTime parseVnpayDate(String raw) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return LocalDateTime.parse(raw, formatter);
    }
}
