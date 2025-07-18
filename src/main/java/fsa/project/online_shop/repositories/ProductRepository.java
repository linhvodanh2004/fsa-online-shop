package fsa.project.online_shop.repositories;

import fsa.project.online_shop.models.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("SELECT p FROM Product p WHERE p.status = true AND p.quantity > 0 ORDER BY p.id DESC")
    List<Product> findLatestActiveProducts(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.status = true AND p.quantity > 0 ORDER BY p.price DESC")
    List<Product> findFeaturedProductsByHighestPrice(Pageable pageable);
}
