package fsa.project.online_shop.repositories;

import fsa.project.online_shop.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category,Long> {
}
