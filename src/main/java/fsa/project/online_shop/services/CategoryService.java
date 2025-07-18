package fsa.project.online_shop.services;

import fsa.project.online_shop.models.Category;

import java.util.List;

public interface CategoryService {
    List<Category> getAllCategories();

    Category getCategoryById(Long categoryId);
}
