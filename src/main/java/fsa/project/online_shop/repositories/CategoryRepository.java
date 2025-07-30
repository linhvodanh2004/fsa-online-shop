package fsa.project.online_shop.repositories;

import fsa.project.online_shop.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>{
    boolean existsCategoryByNameIgnoreCase(String name);

    List<Category> findByNameContainingIgnoreCase(String name);
}
