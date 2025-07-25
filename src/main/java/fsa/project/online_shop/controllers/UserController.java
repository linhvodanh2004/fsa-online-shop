package fsa.project.online_shop.controllers;

import fsa.project.online_shop.models.Category;
import fsa.project.online_shop.models.Product;
import fsa.project.online_shop.models.User;
import fsa.project.online_shop.repositories.ProductRepository;
import fsa.project.online_shop.services.CategoryService;
import fsa.project.online_shop.services.FileService;
import fsa.project.online_shop.services.ProductService;
import fsa.project.online_shop.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final ProductService productService;
    private final ProductRepository productRepository;
    public final CategoryService categoryService;
    private final UserService userService;
    private final FileService fileService;

    // GET mapping for user management page
    @GetMapping("/admin/users")
    public String getUserManagementPage(Model model) {
        Page<User> userPage = userService.getAllUsers(Pageable.unpaged());
        List<User> users = userPage.getContent();
        model.addAttribute("users", users);
        return "admin/admin-user-manager"; // returns the HTML template
    }

    // GET mapping for add user page
    @GetMapping("/admin/users/add-user")
    public String getAddUserPage(Model model) {
        model.addAttribute("user", new User());
        return "admin/admin-user-add";
    }

    // POST mapping for updating user status (AJAX)
    @PostMapping("/admin/users/{id}/status")
    @ResponseBody
    public ResponseEntity<?> updateUserStatus(@PathVariable("id") Long id,
                                              @RequestBody Map<String, Boolean> statusRequest) {
        try {
            Boolean newStatus = statusRequest.get("status");
            User user = userService.findById(id);

            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            user.setStatus(newStatus);
            userService.save(user);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // DELETE mapping for deleting user (AJAX)
//    @DeleteMapping("/user/{id}/delete")
//    @ResponseBody
//    public ResponseEntity<?> deleteUser(@PathVariable("id") Long id) {
//        try {
//            User user = userService.findById(id);
//
//            if (user == null) {
//                return ResponseEntity.notFound().build();
//            }
//
//            // Prevent deletion of admin users
//            if (user.getRole() != null && "ADMIN".equals(user.getRole().getName())) {
//                return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                        .body(Map.of("error", "Cannot delete admin users"));
//            }
//
//            userService.deleteById(id);
//            return ResponseEntity.ok().build();
//
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Map.of("error", "Failed to delete user"));
//        }
//    }

    // POST mapping for creating new user
    @PostMapping("/admin/users/add-user")
    public String addUser(@ModelAttribute("user") User user,
                          RedirectAttributes redirectAttributes) {
        try {
            // Set default values
            if (user.getStatus() == null) {
                user.setStatus(true);
            }

            userService.save(user);
            redirectAttributes.addFlashAttribute("success", "User created successfully!");
            return "redirect:/admin/users";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create user: " + e.getMessage());
            return "redirect:/admin/add-user";
        }
    }

    // POST mapping for updating user
//    @PostMapping("/update-user/{id}")
//    public String updateUser(@PathVariable("id") Long id,
//                             @ModelAttribute("user") User user,
//                             RedirectAttributes redirectAttributes) {
//        try {
//            User existingUser = userService.findById(id);
//
//            if (existingUser == null) {
//                redirectAttributes.addFlashAttribute("error", "User not found");
//                return "redirect:/admin/users";
//            }
//
//            // Update user fields
//            existingUser.setUsername(user.getUsername());
//            existingUser.setFullname(user.getFullname());
//            existingUser.setEmail(user.getEmail());
//            existingUser.setPhone(user.getPhone());
//            existingUser.setStatus(user.getStatus());
//            existingUser.setRole(user.getRole());
//
//            // Don't update password if it's empty
//            if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
//                existingUser.setPassword(user.getPassword());
//            }
//
//            userService.save(existingUser);
//            redirectAttributes.addFlashAttribute("success", "User updated successfully!");
//            return "redirect:/admin/users";
//
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("error", "Failed to update user: " + e.getMessage());
//            return "redirect:/admin/update-user/" + id;
//        }
//    }

    // GET mapping with search, filter, and pagination parameters
    @GetMapping("/admin/users/search")
    public String searchUsers(@RequestParam(value = "query", required = false) String query,
                              @RequestParam(value = "role", required = false) String role,
                              @RequestParam(value = "provider", required = false) String provider,
                              @RequestParam(value = "status", required = false) Boolean status,
                              @RequestParam(value = "page", defaultValue = "0") int page,
                              @RequestParam(value = "size", defaultValue = "10") int size,
                              @RequestParam(value = "sort", defaultValue = "id") String sort,
                              @RequestParam(value = "direction", defaultValue = "asc") String direction,
                              Model model) {

        // Create pageable object
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        Page<User> userPage;
        List<User> users;

        // Check if any search/filter parameters are provided
        if (query != null || role != null || provider != null || status != null) {
            // For search with filters, we need to get all matching results first
            // then apply pagination manually (since searchUsers returns List)
            List<User> allFilteredUsers = userService.searchUsers(query, role, provider, status);

            // Manual pagination for filtered results
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), allFilteredUsers.size());
            List<User> pageContent = start >= allFilteredUsers.size() ?
                    Collections.emptyList() : allFilteredUsers.subList(start, end);

            userPage = new PageImpl<>(pageContent, pageable, allFilteredUsers.size());
            users = pageContent;
        } else {
            // No filters - use direct pagination from repository
            userPage = userService.getAllUsers(pageable);
            users = userPage.getContent();
        }

        // Add attributes for the view
        model.addAttribute("users", users);
        model.addAttribute("userPage", userPage);
        model.addAttribute("searchQuery", query);
        model.addAttribute("roleFilter", role);
        model.addAttribute("providerFilter", provider);
        model.addAttribute("statusFilter", status);

        // Pagination attributes
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("totalElements", userPage.getTotalElements());
        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);

        // Helper attributes for pagination controls
        model.addAttribute("hasPrevious", userPage.hasPrevious());
        model.addAttribute("hasNext", userPage.hasNext());
        model.addAttribute("isFirst", userPage.isFirst());
        model.addAttribute("isLast", userPage.isLast());

        return "admin/admin-user-manager";
    }

    @GetMapping("/admin/edit-profile")
    public String editAdminProfile(){
        return "admin/admin-profile";
    }
}