package fsa.project.online_shop.services.implement;

import fsa.project.online_shop.models.Product;
import fsa.project.online_shop.repositories.OrderItemRepository;
import fsa.project.online_shop.repositories.ProductRepository;
import fsa.project.online_shop.services.ProductService;
import fsa.project.online_shop.utils.SlugUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;

    @Override
    public List<Product> getLatestProducts(int limit) {
        return productRepository.findLatestActiveProducts(PageRequest.of(0, limit));
    }

    @Override
    public List<Product> getFeaturedProducts(int limit) {
        return productRepository.getFeaturedActiveProductsSimple(PageRequest.of(0, limit));
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
                PageRequest.of(0, 4));
    }

    @Override
    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryIdAndStatusTrueOrderByIdDesc(categoryId);
    }

    @Override
    @Transactional
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

    @Override
    public List<Product> getTopSoldProducts(int limit) {
        return productRepository.findTopOrderBySold(PageRequest.of(0, limit));
    }

    @Override
    public int getTotalSold() {
//        Integer total = productRepository.sumAllSold();
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        Integer total = orderItemRepository.sumQuantityFromValidOrders(sevenDaysAgo);
        return total != null ? total : 0;
    }

    @Override
    public List<Product> searchProducts(String query) {
        List<Product> products = productRepository.searchProducts(query);
        // Filter only active products
        return products.stream()
                .filter(product -> Boolean.TRUE.equals(product.getStatus()))
                .collect(Collectors.toList());
    }
}
