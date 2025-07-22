package fsa.project.online_shop.services.implement;

import fsa.project.online_shop.models.Product;
import fsa.project.online_shop.repositories.ProductRepository;
import fsa.project.online_shop.services.ProductService;
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
}
