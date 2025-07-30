package fsa.project.online_shop.services.implement;

import fsa.project.online_shop.models.Category;
import fsa.project.online_shop.repositories.CategoryRepository;
import fsa.project.online_shop.services.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Category handleSaveCategory(Category category) {
        return categoryRepository.save(category);
    }

    @Override
    public boolean checkCategoryExists(String name) {
        return categoryRepository.existsCategoryByNameIgnoreCase(name.trim());
    }
    
    @Override
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id).orElse(null);
    }

    @Override
    public void deleteCategoryById(Long categoryId) {
        categoryRepository.deleteById(categoryId);
    }

    @Override
    public List<Category> searchCategories(String query) {
        return categoryRepository.findByNameContainingIgnoreCase(query);
    }
}
