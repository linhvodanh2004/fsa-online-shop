package fsa.project.online_shop.services.implement;

import fsa.project.online_shop.repositories.CartItemRepository;
import fsa.project.online_shop.repositories.ProductRepository;
import fsa.project.online_shop.services.CartItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CartItemServiceImpl implements CartItemService {
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;

    @Override
    @Transactional
    public void deleteCartItemByProductId(Long productId) {
        if (productRepository.existsById(productId)
                && cartItemRepository.existsByProduct_Id(productId)) {
            cartItemRepository.deleteByProductId(productId);
        }
    }
}
