package fsa.project.online_shop.controllers;

import fsa.project.online_shop.models.Product;
import fsa.project.online_shop.repositories.ProductRepository;
import fsa.project.online_shop.services.ProductService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final ProductService productService;
    private final ProductRepository productRepository;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("LatestProducts", productService.getLatestProducts(9));
        return "user/index";
    }

    @GetMapping("admin/products")
    public String adminProductPage(Model model) {
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);
        return "admin/admin-product-manager";
    }

    @PostMapping("/product/{id}/status")
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
    @GetMapping("admin/update/{id}")
    public String updateProductPage(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);
        model.addAttribute("product", product);
        return "admin/admin-product-form";
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
