package fsa.project.online_shop.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import fsa.project.online_shop.models.Product;
import fsa.project.online_shop.services.implement.AdminServiceImpl;


@Controller
public class AdminController {
    @Autowired
    AdminServiceImpl adminServiceImpl;

    @GetMapping("/product_manager")
    public String adminProductPage(Model model) {
        List<Product> products = adminServiceImpl.getAllProducts();
        model.addAttribute("products", products);
        return "admin/admin-product-manager";
    }
    

}
