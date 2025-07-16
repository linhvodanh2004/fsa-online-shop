package fsa.project.online_shop.services;

import fsa.project.online_shop.models.Product;

import java.util.List;

public interface ProductService {
    List<Product> getLatestProducts(int limit);
}
