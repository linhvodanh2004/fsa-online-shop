package fsa.project.online_shop.repositories;

import fsa.project.online_shop.models.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategoryIdAndIdNotAndQuantityGreaterThanAndStatusIsTrueOrderByIdDesc(
            Long categoryId,
            Long productId,
            Integer quantity,
            Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.status = true AND p.quantity > 0 ORDER BY p.id DESC")
    List<Product> findLatestActiveProducts(Pageable pageable);

    @Query("""
            SELECT DISTINCT p FROM Product p
            WHERE p.status = true
            AND p.quantity > 0
            ORDER BY (
                (SELECT COUNT(oi) FROM OrderItem oi WHERE oi.product = p) +
                (SELECT COUNT(ci) FROM CartItem ci WHERE ci.product = p)
            ) DESC, p.id ASC
            """)
    List<Product> getFeaturedActiveProductsSimple(Pageable pageable);
    // AND (
    // EXISTS (SELECT 1 FROM OrderItem oi WHERE oi.product = p)
    // OR EXISTS (SELECT 1 FROM CartItem ci WHERE ci.product = p)
    // )

    @Query("SELECT p FROM Product p WHERE p.status = true AND p.quantity > 0 ORDER BY p.price DESC")
    List<Product> findFeaturedProductsByHighestPrice(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.status = true ORDER BY p.id DESC")
    List<Product> findByCategoryIdAndStatusTrueOrderByIdDesc(@Param("categoryId") Long categoryId);

    Product findBySlug(String slug);

    @Query("SELECT p FROM Product p ORDER BY p.sold DESC")
    List<Product> findTopOrderBySold(Pageable pageable);

    @Query("SELECT SUM(p.sold) FROM Product p")
    Integer sumAllSold();
}