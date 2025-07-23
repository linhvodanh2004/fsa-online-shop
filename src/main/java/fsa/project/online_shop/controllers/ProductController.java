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
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

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
    public String showShopPage(
            @RequestParam(value = "category", required = false) Long categoryId,
            @RequestParam(value = "minPrice", required = false) Double minPrice,
            @RequestParam(value = "maxPrice", required = false) Double maxPrice,
            @RequestParam(value = "inStock", required = false) Boolean inStock,
            @RequestParam(value = "sort", required = false, defaultValue = "featured") String sort,
            @RequestParam(value = "search", required = false) String search,
            Model model) {

        // Get all products first
        List<Product> products = productService.getAllProducts();

        // Apply filters
        if (categoryId != null) {
            products = products.stream()
                    .filter(p -> p.getCategory().getId().equals(categoryId))
                    .collect(Collectors.toList());
        }

        if (minPrice != null) {
            products = products.stream()
                    .filter(p -> p.getPrice() >= minPrice)
                    .collect(Collectors.toList());
        }

        if (maxPrice != null) {
            products = products.stream()
                    .filter(p -> p.getPrice() <= maxPrice)
                    .collect(Collectors.toList());
        }

        if (inStock != null && inStock) {
            products = products.stream()
                    .filter(p -> p.getQuantity() > 0)
                    .collect(Collectors.toList());
        }

        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.toLowerCase().trim();
            products = products.stream()
                    .filter(p -> p.getName().toLowerCase().contains(searchLower) ||
                               p.getDescription().toLowerCase().contains(searchLower) ||
                               p.getCategory().getName().toLowerCase().contains(searchLower))
                    .collect(Collectors.toList());
        }

        // Apply sorting
        switch (sort) {
            case "price-low":
                products = products.stream()
                        .sorted((a, b) -> Double.compare(a.getPrice(), b.getPrice()))
                        .collect(Collectors.toList());
                break;
            case "price-high":
                products = products.stream()
                        .sorted((a, b) -> Double.compare(b.getPrice(), a.getPrice()))
                        .collect(Collectors.toList());
                break;
            case "name-az":
                products = products.stream()
                        .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                        .collect(Collectors.toList());
                break;
            case "name-za":
                products = products.stream()
                        .sorted((a, b) -> b.getName().compareToIgnoreCase(a.getName()))
                        .collect(Collectors.toList());
                break;
            case "best-selling":
                products = products.stream()
                        .sorted((a, b) -> Integer.compare(
                                b.getSold() != null ? b.getSold() : 0,
                                a.getSold() != null ? a.getSold() : 0))
                        .collect(Collectors.toList());
                break;
            case "newest":
                products = products.stream()
                        .sorted((a, b) -> Long.compare(b.getId(), a.getId()))
                        .collect(Collectors.toList());
                break;
            case "featured":
            default:
                // Keep original order or sort by featured logic
                break;
        }

        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.getAllCategories());

        // Add filter parameters back to model for maintaining state
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("inStock", inStock);
        model.addAttribute("currentSort", sort);
        model.addAttribute("searchQuery", search);

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

    // Add missing endpoints for better navigation
    @GetMapping("/shop-category/{id}")
    public String showShopByCategory(@PathVariable Long id, Model model) {
        List<Product> products = productService.getProductsByCategory(id);
        model.addAttribute("products", products);

        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);

        Category selectedCategory = categoryService.getCategoryById(id);
        model.addAttribute("selectedCategory", selectedCategory);

        return "user/shop";
    }
}
