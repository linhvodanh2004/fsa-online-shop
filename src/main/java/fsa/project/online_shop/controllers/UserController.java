package fsa.project.online_shop.controllers;

import fsa.project.online_shop.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserController {
    @Autowired
    private ProductService productService;


    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("LatestProducts", productService.getLatestProducts(9));
        return "user/index";
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
