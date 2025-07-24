package fsa.project.online_shop.controllers;

import fsa.project.online_shop.dtos.VnPayRequest;
import fsa.project.online_shop.dtos.VnPayResponse;
import fsa.project.online_shop.models.Cart;
import fsa.project.online_shop.models.Order;
import fsa.project.online_shop.models.User;
import fsa.project.online_shop.services.OrderService;
import fsa.project.online_shop.services.VnPayService;
import fsa.project.online_shop.utils.SessionUtil;
import fsa.project.online_shop.utils.VnPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/payment/api")
@CrossOrigin("*")
@RequiredArgsConstructor
public class VnPayController {
    private final VnPayService vnPayService;
    private final OrderService orderService;
    private final SessionUtil sessionUtil;
    @PostMapping("/create")
    public ResponseEntity<?> createPayment(HttpServletRequest request,
                                           @RequestBody VnPayRequest paymentRequest) {
        try {
            User user = sessionUtil.getUserFromSession();
            Cart cart = user.getCart();
            Order order = orderService.createOrder(cart);
            order.setReceiverEmail(paymentRequest.getReceiverEmail());
            order.setReceiverName(paymentRequest.getReceiverName());
            order.setReceiverPhone(paymentRequest.getReceiverPhone());
            order.setReceiverAddress(paymentRequest.getReceiverAddress());
            order.setNote(paymentRequest.getNote());
            order.setPaymentMethod("VNPAY");
            orderService.handleSaveOrder(order);
            String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
            String vnpayUrl = vnPayService.createVnPayPayment(paymentRequest,
                    VnPayUtil.getIpAddress(request.getRemoteAddr()), String.valueOf(order.getId()));

            VnPayResponse response = VnPayResponse.builder()
                    .status("success")
                    .message("Payment URL generated successfully")
                    .url(vnpayUrl)
                    .build();
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            VnPayResponse response = VnPayResponse.builder()
                    .status("error")
                    .message("Error creating payment: " + e.getMessage())
                    .url(null)
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/return")
    public ResponseEntity<Map<String, Object>> paymentReturn(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            int paymentStatus = vnPayService.vnPayReturn(request);
            String vnp_TxnRef = request.getParameter("vnp_TxnRef");
            String vnp_Amount = request.getParameter("vnp_Amount");
            String vnp_OrderInfo = request.getParameter("vnp_OrderInfo");
            String vnp_TransactionStatus = request.getParameter("vnp_TransactionStatus");
            String vnp_ResponseCode = request.getParameter("vnp_ResponseCode");
            String vnp_TransactionNo = request.getParameter("vnp_TransactionNo");
            String vnp_BankCode = request.getParameter("vnp_BankCode");
            String vnp_PayDate = request.getParameter("vnp_PayDate");
            response.put("orderId", vnp_TxnRef);
            response.put("amount", vnp_Amount != null ? Long.parseLong(vnp_Amount) / 100 : 0);
            response.put("orderInfo", vnp_OrderInfo);
            response.put("transactionNo", vnp_TransactionNo);
            response.put("bankCode", vnp_BankCode);
            response.put("payDate", vnp_PayDate);
            response.put("responseCode", vnp_ResponseCode);
            response.put("transactionStatus", vnp_TransactionStatus);
            switch (paymentStatus) {
                case 1:
                    orderService.handlePaymentSuccess(Long.parseLong(vnp_TxnRef), vnp_TransactionNo);
                    response.put("status", "success");
                    response.put("message", "Payment successful");
                    break;
                case 0:
                    orderService.handlePaymentFailed(Long.parseLong(vnp_TxnRef));
                    response.put("status", "failed");
                    response.put("message", "Payment failed");
                    break;
                default:
                    orderService.handlePaymentFailed(Long.parseLong(vnp_TxnRef));
                    response.put("status", "invalid");
                    response.put("message", "Invalid signature");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error processing payment return: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/ipn")
    public ResponseEntity<Map<String, String>> paymentIPN(HttpServletRequest request) {
        Map<String, String> response = new HashMap<>();
        try {
            int paymentStatus = vnPayService.vnPayReturn(request);
            if (paymentStatus == 1) {
                // Xử lý logic khi thanh toán thành công
                // Ví dụ: cập nhật database, gửi email, etc.
                String vnp_TxnRef = request.getParameter("vnp_TxnRef");
                String vnp_Amount = request.getParameter("vnp_Amount");
                // TODO: Implement your business logic here

                response.put("RspCode", "00");
                response.put("Message", "Confirm Success");
            } else {
                response.put("RspCode", "97");
                response.put("Message", "Invalid Signature");
            }
        } catch (Exception e) {
            response.put("RspCode", "99");
            response.put("Message", "Unknown error");
        }
        return ResponseEntity.ok(response);
    }

}
