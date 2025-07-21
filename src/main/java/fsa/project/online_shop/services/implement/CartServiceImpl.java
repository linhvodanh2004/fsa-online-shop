package fsa.project.online_shop.services.implement;

import fsa.project.online_shop.models.Cart;
import fsa.project.online_shop.repositories.CartRepository;
import fsa.project.online_shop.services.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    @Override
    public Integer getCartItemCount(Long cartId) {
        Optional<Cart> cart = cartRepository.findById(cartId);
        return cart.map(value -> value.getCartItems().size()).orElse(0);
    }
}
