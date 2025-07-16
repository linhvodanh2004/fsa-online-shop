package fsa.project.online_shop.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import fsa.project.online_shop.models.Product;

public interface AdminProductRepository extends JpaRepository<Product, Long> {
    // Additional query methods can be defined here if needed

}
