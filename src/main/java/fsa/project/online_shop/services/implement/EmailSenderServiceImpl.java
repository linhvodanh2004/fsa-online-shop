package fsa.project.online_shop.services.implement;

import fsa.project.online_shop.models.OrderItem;
import fsa.project.online_shop.models.Order;
import fsa.project.online_shop.services.EmailSenderService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;

@Service
@RequiredArgsConstructor
public class EmailSenderServiceImpl implements EmailSenderService {
    private final JavaMailSender javaMailSender;

    @Override
    public void sendVerificationCode(String email, String username, String code) throws MessagingException {
        String subject = "🔐 Reset Your Password - Verification Code";
        String content = "<div style='font-family: Arial, sans-serif; padding: 20px;'>"
                + "<h2 style='color: #333;'>Forgot your password?</h2>"
                + "<p>Hi <strong>" + username + "</strong>,</p>"
                + "<p>We received a request to reset your password. Use the verification code below to proceed:</p>"
                + "<div style='background-color: #f3f4f6; padding: 10px; text-align: center; font-size: 24px; "
                + "letter-spacing: 2px; font-weight: bold; color: #1d4ed8; border-radius: 5px;'>"
                + code + "</div>"
                + "<p>This code will expire in 90 seconds. If you didn't request a password reset, please ignore this email.</p>"
                + "<hr style='margin-top: 30px;'>"
                + "<p style='font-size: 12px; color: gray;'>Gaming Online Shop Team</p>"
                + "</div>";

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom("linhvodanh2004@gmail.com");
        helper.setTo(email);
        helper.setSubject(subject);
        helper.setText(content, true); // true = isHtml
        javaMailSender.send(message);
    }

    @Override
    public void notifyOrderPending(Order order) throws MessagingException {
        String subject = "📋 Order Confirmation - Order Received";
        String content = buildOrderEmailContent(
                "Order Confirmation",
                "Thank you for your order! We have received your order and it is currently being processed.",
                order,
                "#ffcc00",
                "🎮 Gaming Online Shop"
        );

        sendOrderEmail(order.getReceiverEmail(), subject, content);
    }

    @Override
    public void notifyOrderTransit(Order order) throws MessagingException {
        String subject = "🚚 Order Update - Your Order is On The Way";
        String content = buildOrderEmailContent(
                "Order In Transit",
                "Great news! Your order has been shipped and is on its way to you.",
                order,
                "#007bff",
                "🚚 Your Gaming Gear is Coming!"
        );

        sendOrderEmail(order.getReceiverEmail(), subject, content);
    }

    @Override
    public void notifyOrderDelivered(Order order) throws MessagingException {
        String subject = "✅ Order Delivered - Enjoy Your Gaming Gear!";
        String content = buildOrderEmailContent(
                "Order Delivered",
                "Your order has been successfully delivered! We hope you enjoy your new gaming gear.",
                order,
                "#28a745",
                "🎉 Happy Gaming!"
        );

        sendOrderEmail(order.getReceiverEmail(), subject, content);
    }

    @Override
    public void notifyOrderCancelled(Order order) throws MessagingException {
        String subject = "❌ Order Cancelled - Refund Processing";
        String content = buildOrderEmailContent(
                "Order Cancelled",
                "Your order has been cancelled as requested. If you made a payment, the refund will be processed within 3-5 business days.",
                order,
                "#dc3545",
                "💰 Refund Processing"
        );

        sendOrderEmail(order.getReceiverEmail(), subject, content);
    }

    private void sendOrderEmail(String email, String subject, String content) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom("linhvodanh2004@gmail.com");
        helper.setTo(email);
        helper.setSubject(subject);
        helper.setText(content, true);
        javaMailSender.send(message);
    }

    private String buildOrderEmailContent(String title, String message, Order order, String color, String footer) {
        DecimalFormat df = new DecimalFormat("#,###.##");
        StringBuilder content = new StringBuilder();

        content.append("<div style='font-family: Arial, sans-serif; padding: 20px; background-color: #f8f9fa;'>")
                .append("<div style='max-width: 600px; margin: 0 auto; background-color: white; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1);'>")

                // Header
                .append("<div style='background-color: ").append(color).append("; color: white; padding: 20px; text-align: center;'>")
                .append("<h1 style='margin: 0; font-size: 24px;'>").append(title).append("</h1>")
                .append("</div>")

                // Content
                .append("<div style='padding: 30px;'>")
                .append("<p style='font-size: 16px; color: #333; margin-bottom: 20px;'>Hi <strong>").append(order.getReceiverName()).append("</strong>,</p>")
                .append("<p style='font-size: 14px; color: #666; line-height: 1.6; margin-bottom: 30px;'>").append(message).append("</p>")

                // Order Details
                .append("<div style='background-color: #f8f9fa; padding: 20px; border-radius: 8px; margin-bottom: 20px;'>")
                .append("<h3 style='color: #333; margin-top: 0; margin-bottom: 15px;'>📦 Order Details</h3>")
                .append("<div style='display: flex; justify-content: space-between; margin-bottom: 10px;'>")
                .append("<span style='color: #666;'>Order ID: </span>")
                .append("<span style='font-weight: bold; color: #333;'>#").append(order.getId()).append("</span>")
                .append("</div>")
                .append("<div style='display: flex; justify-content: space-between; margin-bottom: 10px;'>")
                .append("<span style='color: #666;'>Receiver: </span>")
                .append("<span style='color: #333;'>").append(order.getReceiverName()).append("</span>")
                .append("</div>")
                .append("<div style='display: flex; justify-content: space-between; margin-bottom: 10px;'>")
                .append("<span style='color: #666;'>Phone: </span>")
                .append("<span style='color: #333;'>").append(order.getReceiverPhone()).append("</span>")
                .append("</div>")
                .append("</div>")

                // Order Items
                .append("<div style='margin-bottom: 20px;'>")
                .append("<h3 style='color: #333; margin-bottom: 15px;'>🛒 Items Ordered</h3>")
                .append("<table style='width: 100%; border-collapse: collapse; background-color: white;'>")
                .append("<thead>")
                .append("<tr style='background-color: #f8f9fa;'>")
                .append("<th style='padding: 12px; text-align: left; border-bottom: 1px solid #dee2e6; color: #666;'>Product</th>")
                .append("<th style='padding: 12px; text-align: center; border-bottom: 1px solid #dee2e6; color: #666;'>Qty</th>")
                .append("<th style='padding: 12px; text-align: right; border-bottom: 1px solid #dee2e6; color: #666;'>Price</th>")
                .append("<th style='padding: 12px; text-align: right; border-bottom: 1px solid #dee2e6; color: #666;'>Total</th>")
                .append("</tr>")
                .append("</thead>")
                .append("<tbody>");

        // Add order items
        for (OrderItem item : order.getOrderItems()) {
            double itemTotal = item.getQuantity() * item.getPrice();
            content.append("<tr>")
                    .append("<td style='padding: 12px; border-bottom: 1px solid #f1f3f4; color: #333;'>")
                    .append(item.getProduct().getName()).append("</td>")
                    .append("<td style='padding: 12px; text-align: center; border-bottom: 1px solid #f1f3f4; color: #333;'>")
                    .append(item.getQuantity()).append("</td>")
                    .append("<td style='padding: 12px; text-align: right; border-bottom: 1px solid #f1f3f4; color: #333;'>")
                    .append(df.format(item.getPrice())).append(" VND</td>")
                    .append("<td style='padding: 12px; text-align: right; border-bottom: 1px solid #f1f3f4; color: #333; font-weight: bold;'>")
                    .append(df.format(itemTotal)).append(" VND</td>")
                    .append("</tr>");
        }

        content.append("</tbody>")
                .append("</table>")
                .append("</div>")

                // Total Amount
                .append("<div style='background-color: ").append(color).append("; color: white; padding: 15px; border-radius: 8px; text-align: right;'>")
                .append("<h3 style='margin: 0; font-size: 20px;'>Total: ").append(df.format(order.getSum())).append(" VND</h3>")
                .append("</div>")

                .append("</div>") // End content padding

                // Footer
                .append("<div style='background-color: #f8f9fa; padding: 20px; text-align: center; border-top: 1px solid #dee2e6;'>")
                .append("<p style='margin: 0; color: #666; font-size: 14px;'>").append(footer).append("</p>")
                .append("<p style='margin: 10px 0 0 0; color: #999; font-size: 12px;'>Gaming Online Shop Team</p>")
                .append("</div>")

                .append("</div>") // End container
                .append("</div>"); // End wrapper

        return content.toString();
    }
}