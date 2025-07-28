package fsa.project.online_shop.repositories;

import fsa.project.online_shop.models.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    @Query("""
    SELECT SUM(oi.quantity)
    FROM OrderItem oi
    JOIN oi.order o
    WHERE o.status <> 'CANCELLED'
      AND o.creationTime >= :fromDate
""")
    Integer sumQuantityFromValidOrders(@Param("fromDate") LocalDateTime fromDate);

}
