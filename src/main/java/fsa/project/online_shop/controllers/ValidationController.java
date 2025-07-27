package fsa.project.online_shop.controllers;

import fsa.project.online_shop.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@RestController
@RequestMapping("/api/validation")
@RequiredArgsConstructor
public class ValidationController {

    private final UserService userService;

    @GetMapping("/check-username")
    public ResponseEntity<Map<String, Object>> checkUsername(@RequestParam String username) {
        return performCheck(
                () -> userService.existsByUsername(username),
                "Username has existed",
                "Username is available",
                "Error checking username");
    }

    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmail(@RequestParam String email) {
        return performCheck(
                () -> userService.existsByEmail(email),
                "Email has existed",
                "Email is available",
                "Error checking email");
    }

    @GetMapping("/check-phone")
    public ResponseEntity<Map<String, Object>> checkPhone(@RequestParam String phone) {
        return performCheck(
                () -> userService.existsByPhone(phone.trim()),
                "Phone number has existed",
                "Phone number is available",
                "Error checking phone");
    }

    private ResponseEntity<Map<String, Object>> performCheck(
            Supplier<Boolean> existenceCheck, String existsMessage, String availableMessage, String errorMessage) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean exists = existenceCheck.get();
            response.put("exists", exists);
            response.put("message", exists ? existsMessage : availableMessage);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("exists", false);
            response.put("message", errorMessage);
            return ResponseEntity.ok(response);
        }
    }
}