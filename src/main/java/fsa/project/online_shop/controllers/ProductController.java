package fsa.project.online_shop.controllers;

import fsa.project.online_shop.models.Category;
import fsa.project.online_shop.models.Product;
import fsa.project.online_shop.services.*;
import fsa.project.online_shop.dtos.SearchResults;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final FileService fileService;
    private final CategoryService categoryService;
    private final CartItemService cartItemService;
    private final EmailSenderService emailSenderService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("latestProducts", productService.getLatestProducts(9));
        model.addAttribute("featuredProducts", productService.getFeaturedProducts(9));
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("pageType", "home");
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
        List<Product> products = productService.getActiveProducts();

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
        model.addAttribute("pageType", "shop");

        return "user/shop";
    }

    @GetMapping("/shop-single/{id}")
    public String showProductDetail(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);
        if (product == null) {
            return "redirect:/shop";
        }

        // Generate slug if not exists
        if (product.getSlug() == null || product.getSlug().isEmpty()) {
            String slug = productService.generateUniqueSlug(product.getName(), product.getId());
            product.setSlug(slug);
            productService.saveProduct(product);
            // Redirect to SEO-friendly URL after generating slug
            return "redirect:/product/" + product.getSlug();
        }

        // If slug exists, redirect to SEO-friendly URL
        return "redirect:/product/" + product.getSlug();
    }

    @GetMapping("/product/{slug}")
    public String showProductDetailBySlug(@PathVariable String slug, Model model) {
        try {
            Product product = productService.getProductBySlug(slug);
            if (product == null || product.getStatus() == null || !product.getStatus()) {
                return "redirect:/shop?error=product-not-found";
            }

            model.addAttribute("product", product);

            // Safe handling of related products
            List<Product> relatedProducts = null;
            if (product.getCategory() != null && product.getCategory().getId() != null) {
                relatedProducts = productService.getRelatedProducts(
                        product.getCategory().getId(),
                        product.getId()
                );
            }

            if (relatedProducts == null) {
                relatedProducts = new java.util.ArrayList<>();
            }

            model.addAttribute("relatedProducts", relatedProducts);

            return "user/shop-single";

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/shop?error=server-error";
        }
    }

    @GetMapping("/contact")
    public String showContactPage(Model model) {
        model.addAttribute("pageType", "contact");
        return "user/contact";
    }

    @PostMapping("/contact")
    public String handleContactForm(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String subject,
            @RequestParam String message,
            Model model
    ){
        try {
            emailSenderService.sendContactEmail(name, email, subject, message);
        } catch (MessagingException e) {
        }
        finally {
            return "redirect:/contact";
        }
    }

    @GetMapping("/about")
    public String showAboutPage(Model model) {
        model.addAttribute("pageType", "about");
        return "user/about";
    }

    @GetMapping("/privacy")
    public String showPrivacyPage(Model model) {
        model.addAttribute("pageType", "privacy");
        return "user/privacy";
    }

    @GetMapping("/search")
    public ResponseEntity<SearchResults> search(@RequestParam(name = "q", required = false) String q) {
        if (q == null || q.trim().isEmpty()) {
            return ResponseEntity.ok(new SearchResults(new ArrayList<>(), new ArrayList<>()));
        }

        List<Category> categories = categoryService.searchCategories(q.trim());
        List<Product> products = productService.searchProducts(q.trim());

        SearchResults results = new SearchResults(categories, products);
        return ResponseEntity.ok(results);
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

    @PostMapping("/admin/products/{id}/status")
    @ResponseBody
    public ResponseEntity<?> updateProductStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {
        Boolean status = (Boolean) payload.get("status");
        Product product = productService.getProductById(id);
        product.setStatus(status);
        if (!status) {
            cartItemService.deleteCartItemByProductId(id);
        }
        productService.saveProduct(product);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/admin/products")
    public String adminProductPage(Model model, org.springframework.security.web.csrf.CsrfToken csrfToken) {
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);
        model.addAttribute("_csrf", csrfToken);
        return "admin/admin-product-manager";
    }

    @GetMapping("/admin/products/update/{pid}")
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
            String fileName = fileService.handleUploadImage(image);
            if (fileName == null || fileName.isEmpty()) {
                fileName = product.getImage();
            }
            product.setImage(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }


        productService.saveProduct(product);
        return "redirect:/admin/products";
    }

    @GetMapping("/admin/products/add")
    public String addProductPage(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("product", new Product());
        return "admin/admin-product-add";
    }

    @PostMapping("/admin/products/add")
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
        product.setSold(0);

        Category category = categoryService.getCategoryById(categoryId);
        product.setCategory(category);

        try {
            String fileName = null;
            if (image != null && !image.isEmpty()) {
                fileName = fileService.handleUploadImage(image);
            }
            if (fileName == null || fileName.isEmpty()) {
                fileName = "/upload/default-product-image.png";
            }
            product.setImage(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Save first to get ID, then generate slug
        Product savedProduct = productService.saveProduct(product);
        String slug = productService.generateUniqueSlug(name, savedProduct.getId());
        savedProduct.setSlug(slug);
        productService.saveProduct(savedProduct);
        return "redirect:/admin/products";
    }

    @PostMapping("/admin/products/generate-slugs")
    public String generateSlugsForExistingProducts() {
        List<Product> products = productService.getAllProducts();
        for (Product product : products) {
            if (product.getSlug() == null || product.getSlug().isEmpty()) {
                String slug = productService.generateUniqueSlug(product.getName(), product.getId());
                product.setSlug(slug);
                productService.saveProduct(product);
            }
        }
        return "redirect:/admin/products?success=slugs-generated";
    }
}
