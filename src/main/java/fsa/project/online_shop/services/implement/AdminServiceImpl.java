package fsa.project.online_shop.services.implement;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fsa.project.online_shop.models.Product;
import fsa.project.online_shop.repositories.AdminProductRepository;
import fsa.project.online_shop.services.AdminService;
@Service
public class AdminServiceImpl implements AdminService{

    @Autowired
    private AdminProductRepository adminProductRepository;
    @Override
    public List<Product> getAllProducts() {
        return adminProductRepository.findAll();
    }

}
