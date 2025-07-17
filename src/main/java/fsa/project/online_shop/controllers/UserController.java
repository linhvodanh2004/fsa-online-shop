package fsa.project.online_shop.controllers;

import fsa.project.online_shop.models.Product;
import fsa.project.online_shop.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final ProductService productService;


    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("LatestProducts", productService.getLatestProducts(9));
        return "user/index";
    }
    @GetMapping("/product_manager")
    public String adminProductPage(Model model) {
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);
        return "admin/admin-product-manager";
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
