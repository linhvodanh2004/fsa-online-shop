package fsa.project.online_shop.services;

import fsa.project.online_shop.models.Category;

import java.util.List;

public interface CategoryService {
    public List<Category> findAllCategories();

    public Category handleSaveCategory(Category category);

    public boolean checkCategoryExists(String name);
}
