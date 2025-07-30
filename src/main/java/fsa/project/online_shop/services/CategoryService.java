package fsa.project.online_shop.services;

import java.util.List;

import fsa.project.online_shop.models.Category;

public interface CategoryService {
    public Category handleSaveCategory(Category category);

    public boolean checkCategoryExists(String name);
    public List<Category> getAllCategories();
    public Category getCategoryById(Long categoryId);
    public void deleteCategoryById(Long categoryId);

    List<Category> searchCategories(String query);
}
