package fsa.project.online_shop.controllers;

import fsa.project.online_shop.models.Category;
import fsa.project.online_shop.models.Product;
import fsa.project.online_shop.services.CategoryService;
import fsa.project.online_shop.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    private final CategoryService categoryService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("latestProducts", productService.getLatestProducts(9));
        model.addAttribute("featuredProducts", productService.getLatestProducts(3));
        model.addAttribute("categories", categoryService.getAllCategories());
        return "user/index";
    }

    @GetMapping("/shop")
    public String showShopPage(Model model) {
        // Get all products
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);

        // Get all categories for filtering
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);

        return "user/shop";
    }

    @GetMapping("/shop-single/{id}")
    public String showProductDetail(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);
        if (product == null) {
            return "redirect:/shop";
        }

        model.addAttribute("product", product);
        List<Product> relatedProducts = productService.getRelatedProducts(
                product.getCategory().getId(),
                product.getId()
        );
        model.addAttribute("relatedProducts", relatedProducts);

        return "user/shop-single";
    }

    @GetMapping("/contact")
    public String showContactPage() {
        return "user/contact";
    }

    @GetMapping("/about")
    public String showAboutPage() {
        return "user/about";
    }
}
