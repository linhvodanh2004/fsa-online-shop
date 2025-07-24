package fsa.project.online_shop.services;

import fsa.project.online_shop.models.Product;

import java.util.List;

public interface ProductService {
    List<Product> getLatestProducts(int limit);

    List<Product> getFeaturedProducts(int limit);

    List<Product> getAllProducts();

    Product getProductById(Long id);

    List<Product> getRelatedProducts(Long categoryId, Long productId);

    List<Product> getProductsByCategory(Long categoryId);

    Product saveProduct(Product product);

}
