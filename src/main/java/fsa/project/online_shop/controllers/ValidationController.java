package fsa.project.online_shop.controllers;

import fsa.project.online_shop.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/validation")
@RequiredArgsConstructor
public class ValidationController {

    private final UserService userService;

    @GetMapping("/check-username")
    public ResponseEntity<Map<String, Object>> checkUsername(@RequestParam String username) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean exists = userService.existsByUsername(username);
            response.put("exists", exists);
            response.put("message", exists ? "Username has existed" : "Username is available");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("exists", false);
            response.put("message", "Error checking username");
            return ResponseEntity.ok(response);
        }
    }

    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmail(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean exists = userService.existsByEmail(email);
            response.put("exists", exists);
            response.put("message", exists ? "Email has existed" : "Email is available");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("exists", false);
            response.put("message", "Error checking email");
            return ResponseEntity.ok(response);
        }
    }

    @GetMapping("/check-phone")
    public ResponseEntity<Map<String, Object>> checkPhone(@RequestParam String phone) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Only check if phone is provided and not empty
            if (phone == null || phone.trim().isEmpty()) {
                response.put("exists", false);
                response.put("message", "Phone is optional");
                return ResponseEntity.ok(response);
            }
            
            boolean exists = userService.existsByPhone(phone.trim());
            response.put("exists", exists);
            response.put("message", exists ? "Phone number has existed" : "Phone number is available");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("exists", false);
            response.put("message", "Error checking phone");
            return ResponseEntity.ok(response);
        }
    }
}
