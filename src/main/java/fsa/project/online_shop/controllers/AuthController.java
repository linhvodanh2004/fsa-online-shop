package fsa.project.online_shop.controllers;

import fsa.project.online_shop.models.User;
import fsa.project.online_shop.models.constant.UserRole;
import fsa.project.online_shop.services.EmailSenderService;
import fsa.project.online_shop.services.RoleService;
import fsa.project.online_shop.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final EmailSenderService emailSenderService;

    @GetMapping("/notfound")
    public String notFoundError() {
        return "error/pages-404";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "error/pages-403";
    }

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
        User user = userService.getUserByUsername(username);
        if (user != null) {
            return "redirect:/register?error=username-exists";
        }
        if (userService.checkEmailExists(email)) {
            return "redirect:/register?error=email-exists";
        }
        // We dont save password directly into database, instead we use PasswordEncoder in Security
        // to hash into another string, then we save to database.
        // The special thing is every time we encode, the encoded string is difference
        // ex 123456 -> abcscascas/ascascascaca/ascaccxvbcxvx
        String encodedPassword = passwordEncoder.encode(password);
        user = User.builder()
                .username(username)
                .password(encodedPassword)
                .fullname(fullname)
                .email(email)
                .phone(phone)
                .status(true)
                .role(roleService.getRoleByName(UserRole.USER))
                .build();
        userService.handleSaveUser(user);
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
    ) {
        User user = userService.getUserByUsername(username);
        if (user == null || user.getEmail() == null || !user.getEmail().equals(email)) {
            return "redirect:/forgot-password?error=username-or-email-not-matched";
        }
        String verificationCode = generateRandomCode();
        String hashedVerificationCode = passwordEncoder.encode(verificationCode);
        model.addAttribute("hashedVerificationCode", hashedVerificationCode);
        model.addAttribute("email", email);
        return "user/verification";
    }
}
