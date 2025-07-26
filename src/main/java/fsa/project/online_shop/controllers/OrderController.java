package fsa.project.online_shop.controllers;

import fsa.project.online_shop.dtos.OrderResponse;
import fsa.project.online_shop.models.Cart;
import fsa.project.online_shop.models.Order;
import fsa.project.online_shop.models.User;
import fsa.project.online_shop.models.constant.OrderStatus;
import fsa.project.online_shop.services.*;
import fsa.project.online_shop.utils.OrderMapper;
import fsa.project.online_shop.utils.SessionUtil;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
        model.addAttribute("receiverName", (user.getReceiverName() != null) ? user.getReceiverName()
                : (user.getFullname() != null) ? user.getFullname() : "");
        model.addAttribute("receiverEmail", (user.getEmail()  == null) ? "" : user.getEmail());
        model.addAttribute("receiverPhone", (user.getReceiverPhone() != null) ? user.getReceiverPhone()
                : (user.getPhone() != null) ? user.getPhone() : "");
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
            } else {
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
        } catch (MessagingException e) {
            return "redirect:/?error=email-failed";
        }
    }

    @PostMapping("/cart-detail/checkout")
    public String codOrderCheckout(
            @RequestParam String receiverName,
            @RequestParam String receiverPhone,
            @RequestParam String receiverEmail,
            @RequestParam String receiverAddress,
            @RequestParam(required = false) String note
    ) {
        try {
            User user = sessionUtil.getUserFromSession();
            Cart cart = user.getCart();
            Order order = orderService.createOrder(cart);
            order.setReceiverName(receiverName);
            order.setReceiverPhone(receiverPhone);
            order.setReceiverEmail(receiverEmail);
            order.setReceiverAddress(receiverAddress);
            order.setNote(note);
            order.setPaymentMethod("COD");
            orderService.handleSaveOrder(order);
            emailSenderService.notifyOrderPending(order);
            return "redirect:/?success=checkout-success";
        } catch (MessagingException e) {
            return "redirect:/?error=email-failed";
        }
    }

    @GetMapping("/admin/orders")
    public String showOrders(
            Model model,
            @RequestParam(defaultValue = OrderStatus.PENDING) String orderStatus,
            @RequestParam(defaultValue = "1") Integer page
    ) {
        PageRequest pageRequest = PageRequest.of(page - 1, 10);
        Page<Order> orders = orderService.getAllOrders(pageRequest, orderStatus);
        model.addAttribute("orders", orders.isEmpty() ? null : orders);
        model.addAttribute("orderStatus", orderStatus);
        model.addAttribute("page", page);
        model.addAttribute("totalPages", orders.getTotalPages());
        return "admin/admin-order-manager";
    }

    @GetMapping("/admin/orders/update-status/{orderId}")
    public String updateOrderStatus(
            @RequestParam String orderStatus,
            @PathVariable("orderId") Long orderId, Model model) {
        Order order = orderService.getOrderById(orderId);
        if (order != null) {
            try {
                orderService.updateOrderStatus(orderId, orderStatus.toUpperCase());
                switch (orderStatus.toUpperCase()) {
                    case OrderStatus.IN_TRANSIT -> emailSenderService.notifyOrderTransit(order);
                    case OrderStatus.DELIVERED -> emailSenderService.notifyOrderDelivered(order);
                    case OrderStatus.CANCELLED -> emailSenderService.notifyOrderCancelled(order);
                }
                return "redirect:/admin/orders?success=update-order-status-successfully";
            } catch (MessagingException e) {
                return "redirect:/admin/orders?error=email-failed";
            }
        }
        return "redirect:/admin/orders?error=fail-to-update-order-status";
    }

    @GetMapping("/admin/orders/update-bulk-status")
    public String updateBulkOrderStatus(
            @RequestParam String orderStatus) {
        try {
            List<Order> orders;
            switch (orderStatus) {
                case OrderStatus.IN_TRANSIT:
                    orders = orderService.getOrdersByStatus(OrderStatus.PENDING);
                    orderService.updateOrderStatusInBulk(orderStatus);
                    for (Order order : orders) {
                        emailSenderService.notifyOrderTransit(order);
                    }
                    break;
                case OrderStatus.DELIVERED:
                    orders = orderService.getOrdersByStatus(OrderStatus.IN_TRANSIT);
                    orderService.updateOrderStatusInBulk(orderStatus);
                    for (Order order : orders) {
                        emailSenderService.notifyOrderDelivered(order);
                    }
                    break;
            }
        }
        catch (MessagingException e){
            return "redirect:/admin/orders?error=email-failed";
        }
        return "redirect:/admin/orders?success=update-order-status-successfully";
    }

    @ResponseBody
    @GetMapping("/orders/api/order-detail/{orderId}")
    public ResponseEntity<?> getOrderDetail(@PathVariable("orderId") Long orderId) {
        Order order = orderService.getOrderById(orderId);
        OrderResponse orderResponse = OrderMapper.toOrderResponse(order);
        return ResponseEntity.ok(orderResponse);
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
