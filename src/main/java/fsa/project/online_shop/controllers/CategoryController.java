package fsa.project.online_shop.controllers;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import fsa.project.online_shop.models.Category;
import fsa.project.online_shop.services.CategoryService;
import fsa.project.online_shop.services.FileService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;
    private final FileService fileService;

    @GetMapping("/admin/category-manager")
    public String getCategoryManagementPage(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        return "admin/category-manager";
    }

    @PostMapping("/admin/add-category")
    public String handleAddCategory(
            @RequestParam String name,
            @RequestParam("image") MultipartFile image
    ){
        if(categoryService.checkCategoryExists(name)){
            return "redirect:/admin/category-manager?error=category-exists";
        }
        try {
            String fileName = fileService.handleUploadImage(image);
            Category category = Category.builder()
                    .name(name)
                    .image(fileName)
                    .build();
            categoryService.handleSaveCategory(category);
            return "redirect:/admin/category-manager?success=add-category-successfully";
        } catch (IOException e) {
            return "redirect:/admin/category-manager?error=failed-to-upload-category-image";
        }
    }

    @PostMapping("/admin/edit-category")
    public String handleEditCategory(
            @RequestParam Long id,
            @RequestParam String name,
            @RequestParam("image") MultipartFile image
    ){
        Category category = categoryService.getCategoryById(id);
        if (categoryService.checkCategoryExists(name) && !category.getName().equals(name)) {
            return "redirect:/admin/category-manager?error=category-exists";
        }
        try {
            if (!image.isEmpty()) {
                // Delete old image if exists
                if (category.getImage() != null && !category.getImage().isEmpty()) {
                    fileService.handleDeleteImage(category.getImage());
                }
                // Upload new image
                String fileName = fileService.handleUploadImage(image);
                category.setImage(fileName);
            }
            category.setName(name);
            categoryService.handleSaveCategory(category);
            return "redirect:/admin/category-manager?success=edit-category-successfully";
        } catch (IOException e) {
            return "redirect:/admin/category-manager?error=failed-to-upload-category-image";
        }
    }
    @GetMapping("/admin/delete-category/{id}")
    public String handleDeleteCategory(@PathVariable("id") Long id){
        Category category = categoryService.getCategoryById(id);
        if(category == null){
            return "redirect:/admin/category-manager?error=category-not-found";
        }
        if(!category.getProducts().isEmpty()){
            return "redirect:/admin/category-manager?error=category-has-products";
        }
        try {
            String image = category.getImage();
            if(image != null && !image.isEmpty()){
                fileService.handleDeleteImage(image);
            }
            categoryService.deleteCategoryById(id);
            return "redirect:/admin/category-manager?success=delete-category-successfully";
        } catch (IOException e) {
            return "redirect:/admin/category-manager?error=failed-to-delete-category-image";
        }
    }
}
