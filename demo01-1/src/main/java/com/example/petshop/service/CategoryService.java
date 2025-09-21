package com.example.petshop.service;

import com.example.petshop.entity.Category;
import com.example.petshop.entity.User;
import com.example.petshop.exception.ResourceNotFoundException;
import com.example.petshop.repository.CategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final AdminLogService adminLogService;

    public CategoryService(CategoryRepository categoryRepository, AdminLogService adminLogService) {
        this.categoryRepository = categoryRepository;
        this.adminLogService = adminLogService;
    }

    public Page<Category> findAll(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }

    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    @Transactional
    public Category create(String name, String description, User admin) {
        if (categoryRepository.existsByName(name)) {
            throw new RuntimeException("Category with name '" + name + "' already exists");
        }

        Category category = new Category();
        category.setName(name);
        category.setDescription(description);

        Category saved = categoryRepository.save(category);
        adminLogService.log(admin, AdminLogService.Action.ADD, "Category", saved.getId(), 
                "Created category: " + saved.getName());
        
        return saved;
    }

    @Transactional
    public Category update(Long id, String name, String description, User admin) {
        Category category = findById(id);
        String oldName = category.getName();
        
        if (!category.getName().equals(name) && categoryRepository.existsByName(name)) {
            throw new RuntimeException("Category with name '" + name + "' already exists");
        }

        category.setName(name);
        category.setDescription(description);

        Category updated = categoryRepository.save(category);
        adminLogService.log(admin, AdminLogService.Action.UPDATE, "Category", id, 
                "Updated category from '" + oldName + "' to '" + updated.getName() + "'");
        
        return updated;
    }

    @Transactional
    public void delete(Long id, User admin) {
        Category category = findById(id);
        categoryRepository.delete(category);
        adminLogService.log(admin, AdminLogService.Action.DELETE, "Category", id, 
                "Deleted category: " + category.getName());
    }
}
