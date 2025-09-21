package com.example.petshop.service;

import com.example.petshop.dto.ProductDto;
import com.example.petshop.entity.Category;
import com.example.petshop.entity.Product;
import com.example.petshop.entity.User;
import com.example.petshop.exception.ResourceNotFoundException;
import com.example.petshop.repository.ProductRepository;
import com.example.petshop.util.FileStorageUtil;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final AdminLogService adminLogService;
    private final FileStorageUtil fileStorageUtil;

    public ProductService(ProductRepository productRepository, CategoryService categoryService,
                         AdminLogService adminLogService, FileStorageUtil fileStorageUtil) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
        this.adminLogService = adminLogService;
        this.fileStorageUtil = fileStorageUtil;
    }

    public Page<Product> findAll(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    public Page<Product> findByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable);
    }

    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    @Transactional
    public Product create(ProductDto productDto, User admin) {
        Category category = categoryService.findById(productDto.getCategoryId());
        
        Product product = new Product();
        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        product.setStock(productDto.getStock());
        product.setCategory(category);

        // Handle image upload
        if (productDto.getImage() != null && !productDto.getImage().isEmpty()) {
            String imagePath = fileStorageUtil.storeFile(productDto.getImage(), "products");
            product.setImagePath(imagePath);
        }

        Product saved = productRepository.save(product);
        adminLogService.log(admin, AdminLogService.Action.ADD, "Product", saved.getId(), 
                "Created product: " + saved.getName());
        
        return saved;
    }

    @Transactional
    public Product update(Long id, ProductDto productDto, User admin) {
        Product product = findById(id);
        Category category = categoryService.findById(productDto.getCategoryId());
        
        String oldName = product.getName();
        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        product.setStock(productDto.getStock());
        product.setCategory(category);

        // Handle image upload
        if (productDto.getImage() != null && !productDto.getImage().isEmpty()) {
            String imagePath = fileStorageUtil.storeFile(productDto.getImage(), "products");
            product.setImagePath(imagePath);
        }

        Product updated = productRepository.save(product);
        adminLogService.log(admin, AdminLogService.Action.UPDATE, "Product", id, 
                "Updated product from '" + oldName + "' to '" + updated.getName() + "'");
        
        return updated;
    }

    @Transactional
    public void delete(Long id, User admin) {
        Product product = findById(id);
        productRepository.delete(product);
        adminLogService.log(admin, AdminLogService.Action.DELETE, "Product", id, 
                "Deleted product: " + product.getName());
    }
}
