package fsa.project.online_shop.repositories;

import fsa.project.online_shop.models.Cart;
import fsa.project.online_shop.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    @Modifying
    @Query(value = """
                UPDATE carts c
                SET c.sum = (
                    SELECT COALESCE(SUM(ci.price), 0)
                    FROM cart_items ci
                    WHERE ci.cart_id = :cartId
                )
                WHERE c.id = :cartId
            """, nativeQuery = true)
    void updateCartSumById(@Param("cartId") Long cartId);

    Cart findByUser(User user);
}
