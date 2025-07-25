package fsa.project.online_shop.controllers;

import fsa.project.online_shop.models.User;
import fsa.project.online_shop.models.constant.UserRole;
import fsa.project.online_shop.services.EmailSenderService;
import fsa.project.online_shop.services.RoleService;
import fsa.project.online_shop.services.UserService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class AuthController implements ErrorController {

    private final UserService userService;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final EmailSenderService emailSenderService;


    @GetMapping("/register")
    public String registerPage() {
        return "user/register";
    }

    @PostMapping("/register")
    public String handleRegister(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String fullname,
            @RequestParam String email,
            @RequestParam(required = false) String phone
    ) {
        User user = userService.findByUsername(username);
        if (user != null) {
            return "redirect:/register?error=username-exists";
        }
        if (userService.existsByEmail(email)) {
            return "redirect:/register?error=email-exists";
        }
        user = User.builder()
                .username(username)
                .password(password)
                .fullname(fullname)
                .email(email)
                .phone(phone)
                .status(true)
                .role(roleService.getRoleByName(UserRole.USER))
                .build();
        userService.save(user);
        return "redirect:/register?success=register-successfully";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "user/login";
    }

    @GetMapping("/forgot-password")
    public String getForgotPasswordPage() {
        return "user/forgot-password";
    }

    // This method generates 6-characters verification code
    private String generateRandomCode() {
        return String.valueOf((int) (Math.random() * 900000) + 100000); // 6-digit code
    }

    @PostMapping("/forgot-password")
    public String handleForgotPassword(
            @RequestParam String email,
            @RequestParam String username,
            Model model
    ){
        User user = userService.findByUsername(username);
        if (user == null || user.getEmail() == null || !user.getEmail().equals(email)) {
            return "redirect:/forgot-password?error=username-or-email-not-matched";
        }
        String verificationCode = generateRandomCode();
        String hashedVerificationCode = DigestUtils.sha256Hex(verificationCode);
        model.addAttribute("hashedVerificationCode", hashedVerificationCode);
        model.addAttribute("email", email);
        try {
            emailSenderService.sendVerificationCode(email, username, verificationCode);
            return "user/verification";
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/reset-password")
    public String handleResetPassword(
            @RequestParam String email,
            @RequestParam String password,
            Model model
    ){
        User user = userService.findByEmail(email);
        user.setPassword(password);
        userService.save(user);
        return "redirect:/login?success=reset-successfully";
    }

    @PostMapping("/resend-code")
    public String resendCode(
            @RequestParam String email,
            Model model
    ){
        String verificationCode = generateRandomCode();
        String hashedVerificationCode = DigestUtils.sha256Hex(verificationCode);
        model.addAttribute("hashedVerificationCode", hashedVerificationCode);
        model.addAttribute("email", email);
        try {
            emailSenderService.sendVerificationCode(email, userService.findByEmail(email).getUsername()
                    , verificationCode);
            return "user/verification";
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/admin/dashboard")
    public String getAdminDashboardPage(
    ){
        return "admin/dashboard";
    }
}
