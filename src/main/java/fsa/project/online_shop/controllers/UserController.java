package fsa.project.online_shop.controllers;

import fsa.project.online_shop.models.Category;
import fsa.project.online_shop.models.Product;
import fsa.project.online_shop.repositories.ProductRepository;
import fsa.project.online_shop.services.CategoryService;
import fsa.project.online_shop.services.ProductService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final ProductService productService;
    private final ProductRepository productRepository;
    public final CategoryService categoryService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("latestProducts", productService.getLatestProducts(9));
        model.addAttribute("featuredProducts", productService.getLatestProducts(3));
        model.addAttribute("categories", categoryService.getAllCategories());
        return "user/index";
    }

    @PostMapping("/admin/product/{id}/status")
    @ResponseBody
    public ResponseEntity<?> updateProductStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {
        Boolean status = (Boolean) payload.get("status");
        Product product = productService.getProductById(id);
        product.setStatus(status);
        productRepository.save(product);
        return ResponseEntity.ok().build();
    }

    @GetMapping("admin/products")
    public String adminProductPage(Model model, org.springframework.security.web.csrf.CsrfToken csrfToken) {
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);
        model.addAttribute("_csrf", csrfToken);
        return "admin/admin-product-manager";
    }

    @GetMapping("admin/update/{pid}")
    public String updateProductPage(@PathVariable Long pid, Model model) {
        List<Category> categories = categoryService.getAllCategories();
        Product product = productService.getProductById(pid);
        model.addAttribute("product", product);
        model.addAttribute("categories", categories);
        return "admin/admin-product-update";
    }

    @PostMapping("/admin/update/{id}")
    public String handleUpdateProduct(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam Double price,
            @RequestParam Integer quantity,
            @RequestParam Integer sold,
            @RequestParam String description,
            @RequestParam(required = false) Boolean status,
            @RequestParam Long categoryId,
            @RequestParam(required = false) MultipartFile image

    ) {
        Product product = productService.getProductById(id);
        product.setName(name);
        product.setPrice(price);
        product.setQuantity(quantity);
        product.setSold(sold);
        product.setDescription(description);
        product.setStatus(status != null ? status : false);

        Category category = categoryService.getCategoryById(categoryId);
        product.setCategory(category);

        Path uploadPath = Paths.get(System.getProperty("user.dir"), "src", "main", "resources", "static", "productImg");

        try {
            // Create uploads folder if it doesn't exist
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            // Get safe file name and save path
            Path filePath = uploadPath.resolve(Paths.get(image.getOriginalFilename()).getFileName());

            // Save the file to the path
            Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {
            e.getMessage();
        }
        if (image != null && !image.isEmpty()) {
            product.setImage(image.getOriginalFilename());
        }
        productRepository.save(product);
        return "redirect:/admin/products";
    }

    @GetMapping("/admin/add-product")
    public String addProductPage(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("product", new Product());
        return "admin/admin-product-add";
    }

    @PostMapping("/admin/add-product")
    public String handleAddProduct(
            @RequestParam String name,
            @RequestParam Double price,
            @RequestParam Integer quantity,
            @RequestParam Integer sold,
            @RequestParam String description,
            @RequestParam(required = false) Boolean status,
            @RequestParam Long categoryId,
            @RequestParam(required = false) MultipartFile image) {
        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        product.setQuantity(quantity);
        product.setSold(sold);
        product.setDescription(description);
        product.setStatus(status != null ? status : false);

        Category category = categoryService.getCategoryById(categoryId);
        product.setCategory(category);

        if (image != null && !image.isEmpty()) {
            Path uploadPath = Paths.get(System.getProperty("user.dir"), "src", "main", "resources", "static", "productImg");
            try {
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                Path filePath = uploadPath.resolve(Paths.get(image.getOriginalFilename()).getFileName());
                Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                product.setImage(image.getOriginalFilename());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        productRepository.save(product);
        return "redirect:/admin/products";
    }

    @GetMapping("/login-ok")
    public String loginOk() {
        return "redirect:/?success=login-ok";
    }

    @GetMapping("/login-failed")
    public String loginFailed() {
        return "redirect:/?error=login-failed";
    }
}
