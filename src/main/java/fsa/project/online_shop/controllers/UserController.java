package fsa.project.online_shop.controllers;

import fsa.project.online_shop.models.Category;
import fsa.project.online_shop.models.Product;
import fsa.project.online_shop.repositories.ProductRepository;
import fsa.project.online_shop.services.CategoryService;
import fsa.project.online_shop.services.FileService;
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
    public final FileService fileService;

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

    @GetMapping("admin/products/update/{pid}")
    public String updateProductPage(@PathVariable Long pid, Model model) {
        List<Category> categories = categoryService.getAllCategories();
        Product product = productService.getProductById(pid);
        model.addAttribute("product", product);
        model.addAttribute("categories", categories);
        return "admin/admin-product-update";
    }

    @PostMapping("/admin/products/update/{id}")
    public String handleUpdateProduct(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam Double price,
            @RequestParam Integer quantity,
            @RequestParam String description,
            @RequestParam Long categoryId,
            @RequestParam(required = false) MultipartFile image

    ) {
        Product product = productService.getProductById(id);
        product.setName(name);
        product.setPrice(price);
        product.setQuantity(quantity);
        product.setDescription(description);
        Category category = categoryService.getCategoryById(categoryId);
        product.setCategory(category);

        try {
            if (image != null && !image.isEmpty()) {
                String fileName = fileService.handleUploadImage(image);
                product.setImage(fileName);
            }
            productRepository.save(product);
        } catch (IOException e) {
        }

        return "redirect:/admin/products";
    }

    @GetMapping("/admin/products/add-product")
    public String addProductPage(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("product", new Product());
        return "admin/admin-product-add";
    }

    @PostMapping("/admin/products/add-product")
    public String handleAddProduct(
            @RequestParam String name,
            @RequestParam Double price,
            @RequestParam Integer quantity,
            @RequestParam String description,
            @RequestParam Long categoryId,
            @RequestParam(required = false) MultipartFile image) {
        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        product.setQuantity(quantity);
        product.setDescription(description);
        product.setStatus(true);
        Category category = categoryService.getCategoryById(categoryId);
        product.setCategory(category);

        try {
            String fileName = fileService.handleUploadImage(image);
            product.setImage(fileName);
            productRepository.save(product);
        } catch (IOException e) {
        }

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
