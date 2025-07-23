package fsa.project.online_shop.repositories;

import fsa.project.online_shop.models.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    boolean existsByCartIdAndProductId(Long cartId, Long productId);

    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);
}
