package fsa.project.online_shop.controllers;

import fsa.project.online_shop.models.Category;
import fsa.project.online_shop.services.CategoryService;
import fsa.project.online_shop.services.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;
    private final FileService fileService;

    @GetMapping("/admin/category-manager")
    public String getCategoryManagementPage(Model model) {
        List<Category> categories = categoryService.findAllCategories();
        model.addAttribute("categories", categories);
        return "admin/category-manager";
    }
    @GetMapping("/admin/add-category")
    public String getAddCategoryPage(){
        return "admin/add-category";
    }

    @PostMapping("/admin/add-category")
    public String handleAddCategory(
            @RequestParam String name,
            @ModelAttribute("image") MultipartFile image
    ){
        if(categoryService.checkCategoryExists(name)){
            return "redirect:/admin/add-category?error=category-exists";
        }
        try {
            String fileName = fileService.handleUploadImage(image);
            Category category = Category.builder()
                    .name(name)
                    .image(fileName)
                    .build();
            categoryService.handleSaveCategory(category);
            return "redirect:/admin/add-category?success=add-category-successfully";
        } catch (IOException e) {
            return "redirect:/admin/add-category?error=failed-to-upload-category-image";
        }
    }
}
