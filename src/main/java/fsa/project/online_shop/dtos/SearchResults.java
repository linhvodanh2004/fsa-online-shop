package fsa.project.online_shop.dtos;

import fsa.project.online_shop.models.Category;
import fsa.project.online_shop.models.Product;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchResults {
    private List<Category> categories;
    private List<Product> products;
}