package fsa.project.online_shop.services.implement;

import fsa.project.online_shop.models.Product;
import fsa.project.online_shop.repositories.ProductRepository;
import fsa.project.online_shop.services.ProductService;
import fsa.project.online_shop.utils.SlugUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public List<Product> getLatestProducts(int limit) {
        return productRepository.findLatestActiveProducts(PageRequest.of(0, limit));
    }

    @Override
    public List<Product> getFeaturedProducts(int limit) {
        return productRepository.findFeaturedProductsByHighestPrice(PageRequest.of(0, limit));
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    @Override
    public List<Product> getRelatedProducts(Long categoryId, Long productId) {
        return productRepository.findByCategoryIdAndIdNotAndQuantityGreaterThanAndStatusIsTrueOrderByIdDesc(
                categoryId,
                productId,
                0,
                PageRequest.of(0, 4)
        );
    }

    @Override
    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryIdAndStatusTrueOrderByIdDesc(categoryId);
    }

    @Override
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    public Product getProductBySlug(String slug) {
        return productRepository.findBySlug(slug);
    }

    @Override
    public String generateUniqueSlug(String productName, Long productId) {
        String baseSlug = SlugUtils.generateSlug(productName);

        // Check if base slug already exists (excluding current product)
        Product existingProduct = productRepository.findBySlug(baseSlug);

        if (existingProduct == null || existingProduct.getId().equals(productId)) {
            // Base slug is available, use it
            return baseSlug;
        } else {
            // Base slug exists, append ID for uniqueness
            return baseSlug + "-" + productId;
        }
    }
}
