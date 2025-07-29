package fsa.project.online_shop.repositories;

import fsa.project.online_shop.dtos.MonthlyRevenueDto;
import fsa.project.online_shop.models.Order;
import fsa.project.online_shop.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Find orders by user with pagination
     */
    Page<Order> findByUser(User user, Pageable pageable);

    /**
     * Find orders by user ordered by creation time desc
     */
    List<Order> findByUserOrderByCreationTimeDesc(User user);

    /**
     * Find orders by status
     */
    List<Order> findByStatus(String status);

    /**
     * Find orders by user and status
     */
    List<Order> findByUserAndStatus(User user, String status);

    /**
     * Count orders by user
     */
    long countByUser(User user);

    /**
     * Get recent orders by user with limit
     */
    @Query("SELECT o FROM Order o WHERE o.user = :user ORDER BY o.creationTime DESC")
    List<Order> findRecentOrdersByUser(@Param("user") User user, Pageable pageable);

    /**
     * Get total order value by user
     */
    @Query("SELECT COALESCE(SUM(o.sum), 0.0) FROM Order o WHERE o.user = :user")
    Double getTotalOrderValueByUser(@Param("user") User user);

    /**
     * Find orders by user and status with pagination
     */
    Page<Order> findByUserAndStatus(User user, String status, Pageable pageable);

    /**
     * Find all orders ordered by creation time desc
     */
    Page<Order> findAllByOrderByCreationTimeDesc(Pageable pageable);

    Page<Order> findByStatusOrderByPaymentStatusAscCreationTimeDesc(String orderStatus, Pageable pageable);

    /**
     * Count orders by status
     */
    long countByStatus(String status);

    /**
     * Find orders created today
     */
    @Query("SELECT o FROM Order o WHERE DATE(o.creationTime) = CURRENT_DATE")
    List<Order> findOrdersCreatedToday();

    /**
     * Find orders in date range
     */
    @Query("SELECT o FROM Order o WHERE o.creationTime BETWEEN :startDate AND :endDate")
    List<Order> findOrdersInDateRange(@Param("startDate") java.time.LocalDateTime startDate,
                                      @Param("endDate") java.time.LocalDateTime endDate);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Order o SET o.status = 'IN TRANSIT', o.transitTime = :now WHERE o.status = 'PENDING'")
    int updatePendingOrdersToInTransit(LocalDateTime now);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Order o SET o.status = 'DELIVERED', o.deliveryTime = :now, o.paymentDate = :now, o.paymentStatus = true WHERE o.status = 'IN TRANSIT'")
    int updateInTransitOrdersToDelivered(LocalDateTime now);

    @Query("SELECT o FROM Order o JOIN FETCH o.orderItems WHERE o.status = :status")
    List<Order> findByStatusWithItems(@Param("status") String status);

    @Query("SELECT SUM(o.sum) FROM Order o")
    Double sumAllEarnings();

    @Query("""
                SELECT SUM(o.sum) 
                FROM Order o 
                WHERE o.status <> 'CANCELLED' 
                  AND o.creationTime >= :fromDate
            """)
    Double sumAllEarningsWithin7Days(@Param("fromDate") LocalDateTime fromDate);

    @Query("""
            SELECT COUNT(o) 
            FROM Order o 
            WHERE o.status <> 'CANCELLED' 
            AND o.creationTime >= :fromDate
            """)
    Integer countOrdersWithing7Days(@Param("fromDate") LocalDateTime fromDate);

    @Query("""
                SELECT MONTH(o.creationTime), SUM(o.sum)
                FROM Order o
                WHERE o.status != 'CANCELLED'
                  AND o.creationTime >= :startDate
                GROUP BY MONTH(o.creationTime)
                ORDER BY MONTH(o.creationTime)
            """)
    List<Object[]> getMonthlyRevenueRaw(@Param("startDate") LocalDateTime startDate);


}
