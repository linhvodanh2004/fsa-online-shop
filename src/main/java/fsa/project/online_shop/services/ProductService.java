package fsa.project.online_shop.services;

import fsa.project.online_shop.models.Product;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

public interface ProductService {
    List<Product> getLatestProducts(int limit);

    List<Product> getFeaturedProducts(int limit);

    List<Product> getAllProducts();

    List<Product> getActiveProducts();

    Product getProductById(Long id);

    List<Product> getRelatedProducts(Long categoryId, Long productId);

    List<Product> getProductsByCategory(Long categoryId);

    Product saveProduct(Product product);

    Product getProductBySlug(String slug);

    String generateUniqueSlug(String productName, Long productId);

    List<Product> getTopSoldProducts(int limit);

    int getTotalSold();

    List<Product> searchProducts(String query);
}
