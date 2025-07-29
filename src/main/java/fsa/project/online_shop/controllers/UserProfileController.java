package fsa.project.online_shop.controllers;

import fsa.project.online_shop.models.Order;
import fsa.project.online_shop.models.User;
import fsa.project.online_shop.models.constant.OrderStatus;
import fsa.project.online_shop.services.EmailSenderService;
import fsa.project.online_shop.services.OrderService;
import fsa.project.online_shop.services.UserService;
import fsa.project.online_shop.utils.SessionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class UserProfileController {

    private final UserService userService;
    private final OrderService orderService;
    private final SessionUtil sessionUtil;
    private final EmailSenderService emailSenderService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Show user profile page
     */
    @GetMapping("/my-profile")
    public String showMyProfile(Model model, Authentication authentication) {
        try {
//            User user = getCurrentUser(authentication);
            User user = sessionUtil.getUserFromSession();
            if (user == null) {
                log.warn("User not found for profile page");
                return "redirect:/login";
            }
            model.addAttribute("user", user);
            log.info("👤 User {} accessed profile page", user.getEmail());
            return "user/my-profile";

        } catch (Exception e) {
            log.error("Error loading profile page: {}", e.getMessage(), e);
            return "redirect:/";
        }
    }

    /**
     * Show user orders page with pagination
     */
    @GetMapping("/my-orders")
    public String showMyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "creationTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Model model,
            Authentication authentication) {
        try {
//            User user = getCurrentUser(authentication);
            User user = sessionUtil.getUserFromSession();
            if (user == null) {
                log.warn("User not found for orders page");
                return "redirect:/login";
            }
            // Create pageable with sorting
            Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            // Get user orders
            Page<Order> orderPage = orderService.getOrdersByUser(user, pageable);
            model.addAttribute("user", user);
            model.addAttribute("orders", orderPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", orderPage.getTotalPages());
            model.addAttribute("totalElements", orderPage.getTotalElements());
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("sortDir", sortDir);
            model.addAttribute("hasOrders", !orderPage.getContent().isEmpty());
            log.info("📦 User {} accessed orders page - {} orders found", 
                    user.getEmail(), orderPage.getTotalElements());
            return "user/my-orders";
        } catch (Exception e) {
            log.error("Error loading orders page: {}", e.getMessage(), e);
            return "redirect:/";
        }
    }

    /**
     * Update user profile
     */
    @PostMapping("/my-profile/update")
    public String updateProfile(
            @RequestParam String fullname,
            @RequestParam String phone,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
//            User user = getCurrentUser(authentication);
            User user = sessionUtil.getUserFromSession();
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "User not found");
                return "redirect:/login";
            }
            if (userService.existsByPhone(phone) && !user.getPhone().equals(phone)){
                redirectAttributes.addFlashAttribute("error", "Phone number is already taken. Please try another one.");
                return "redirect:/my-profile";
            }
            // Update user info (only allow certain fields)
            user.setFullname(fullname != null ? fullname.trim() : user.getFullname());
            user.setPhone(phone != null ? phone.trim() : user.getPhone());
            userService.handleSaveUser(user);
            // Update session attributes
            sessionUtil.updateSessionUserInfo(user);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
            log.info("✅ User {} updated profile", user.getEmail());
            return "redirect:/my-profile";

        } catch (Exception e) {
            log.error("Error updating profile: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Failed to update profile: " + e.getMessage());
            return "redirect:/my-profile";
        }
    }

    /**
     * Update user default address
     */
    @PostMapping("/my-profile/address/update")
    public String updateAddress(
            @RequestParam String receiverName,
            @RequestParam String receiverPhone,
            @RequestParam String address,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
//            User user = getCurrentUser(authentication);
            User user = sessionUtil.getUserFromSession();

            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "User not found");
                return "redirect:/login";
            }

            // Update address info with validation
            if (receiverName != null && !receiverName.trim().isEmpty()) {
                user.setReceiverName(receiverName.trim());
            }
            if (receiverPhone != null && !receiverPhone.trim().isEmpty()) {
                user.setReceiverPhone(receiverPhone.trim());
            }
            if (address != null && !address.trim().isEmpty()) {
                user.setAddress(address.trim());
            }

            userService.handleSaveUser(user);

            redirectAttributes.addFlashAttribute("success", "Default address updated successfully!");
            log.info("✅ User {} updated default address - Name: {}, Phone: {}, Address: {}",
                    user.getEmail(), user.getReceiverName(), user.getReceiverPhone(),
                    user.getAddress() != null ? user.getAddress().substring(0, Math.min(50, user.getAddress().length())) + "..." : "null");
            return "redirect:/my-profile";

        } catch (Exception e) {
            log.error("Error updating address: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Failed to update address: " + e.getMessage());
            return "redirect:/my-profile";
        }
    }

    /**
     * Change password with email verification
     */
    @PostMapping("/my-profile/change-password")
    public String changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
//            User user = getCurrentUser(authentication);
            User user = sessionUtil.getUserFromSession();

            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "User not found");
                return "redirect:/login";
            }

            // Only allow for local accounts
            if ("GOOGLE".equals(user.getProvider())) {
                redirectAttributes.addFlashAttribute("error", "Google accounts cannot change password here");
                return "redirect:/my-profile";
            }

            // Validate passwords match
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "New passwords do not match");
                return "redirect:/my-profile";
            }

            // Password change with email verification (placeholder)
//            redirectAttributes.addFlashAttribute("info",
//                "Password change functionality with email verification will be implemented soon. " +
//                "A verification email would be sent to " + user.getEmail());
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                redirectAttributes.addFlashAttribute("error", "Current password is incorrect");
                return "redirect:/my-profile";
            }
            String encodedPassword = passwordEncoder.encode(newPassword);
            user.setPassword(encodedPassword);
            userService.handleSaveUser(user);
            log.info("🔐 User {} requested password change", user.getEmail());
            return "redirect:/my-profile";

        } catch (Exception e) {
            log.error("Error changing password: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Failed to process password change: " + e.getMessage());
            return "redirect:/my-profile";
        }
    }

    /**
     * View order details
     */
    @GetMapping("/my-orders/{orderId}")
    public String viewOrderDetail(
            @PathVariable Long orderId,
            Model model,
            Authentication authentication) {
        try {
//            User user = getCurrentUser(authentication);
            User user = sessionUtil.getUserFromSession();

            if (user == null) {
                return "redirect:/login";
            }

            Order order = orderService.getOrderById(orderId);
            if (order == null || !order.getUser().getId().equals(user.getId())) {
                log.warn("Order {} not found or not owned by user {}", orderId, user.getEmail());
                return "redirect:/my-orders?error=Order not found";
            }

            model.addAttribute("order", order);
            model.addAttribute("user", user);
            
            log.info("📋 User {} viewed order details: {}", user.getEmail(), orderId);
            return "user/order-detail";

        } catch (Exception e) {
            log.error("Error loading order detail: {}", e.getMessage(), e);
            return "redirect:/my-orders?error=Failed to load order";
        }
    }

    /**
     * Cancel order
     */
    @PostMapping("/my-orders/{orderId}/cancel")
    public String cancelOrder(
            @PathVariable Long orderId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
//            User user = getCurrentUser(authentication);
            User user = sessionUtil.getUserFromSession();
            if (user == null) {
                return "redirect:/login";
            }

            Order order = orderService.getOrderById(orderId);
            if (order == null || !order.getUser().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("error", "Order not found");
                return "redirect:/my-orders";
            }

            // Only allow cancellation for pending orders
            if (!"PENDING".equals(order.getStatus())) {
                redirectAttributes.addFlashAttribute("error", "Cannot cancel order in current status");
                return "redirect:/my-orders";
            }

//            orderService.cancelOrder(orderId);
            // use this method to update inventory as well
            orderService.updateOrderStatus(orderId, OrderStatus.CANCELLED);
            redirectAttributes.addFlashAttribute("success", "Order cancelled successfully");
            emailSenderService.notifyOrderCancelled(order);
            
            log.info("❌ User {} cancelled order: {}", user.getEmail(), orderId);
            return "redirect:/my-orders";

        } catch (Exception e) {
            log.error("Error cancelling order: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Failed to cancel order");
            return "redirect:/my-orders";
        }
    }

    /**
     * Get current user from authentication
     */
    private User getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return null;
        }

        try {
            // Handle OAuth2 users
            if (authentication.getPrincipal() instanceof OAuth2User oAuth2User) {
                String email = oAuth2User.getAttribute("email");
                return userService.findByEmail(email);
            }
            
            // Handle regular users
            String username = authentication.getName();
            return userService.findByUsername(username);
            
        } catch (Exception e) {
            log.error("Error getting current user: {}", e.getMessage(), e);
            return null;
        }
    }
}
