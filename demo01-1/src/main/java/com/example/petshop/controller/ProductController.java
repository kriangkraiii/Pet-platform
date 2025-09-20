package com.example.petshop.controller;

import com.example.petshop.dto.ProductDto;
import com.example.petshop.entity.User;
import com.example.petshop.service.CategoryService;
import com.example.petshop.service.ProductService;
import com.example.petshop.service.UserService;

import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProductController {
    private final ProductService productService;
    private final CategoryService categoryService;
    private final UserService userService;

    public ProductController(ProductService productService, CategoryService categoryService, UserService userService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.userService = userService;
    }

    @GetMapping("/products")
    public String listProducts(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "12") int size,
                             Model model) {
        Pageable pageable = PageRequest.of(page, size);
        model.addAttribute("products", productService.findAll(pageable));
        return "products";
    }

    @GetMapping("/products/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.findById(id));
        return "product-detail";
    }

    @GetMapping("/categories/{id}/products")
    public String productsByCategory(@PathVariable Long id,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "12") int size,
                                   Model model) {
        Pageable pageable = PageRequest.of(page, size);
        model.addAttribute("category", categoryService.findById(id));
        model.addAttribute("products", productService.findByCategory(id, pageable));
        return "products";
    }

    @GetMapping("/admin/products")
    public String adminListProducts(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size,
                                  Model model) {
        Pageable pageable = PageRequest.of(page, size);
        model.addAttribute("products", productService.findAll(pageable));
        return "admin/products";
    }

    @GetMapping("/admin/products/add")
    public String addProductForm(Model model) {
        model.addAttribute("productDto", new ProductDto());
        model.addAttribute("categories", categoryService.findAll(Pageable.unpaged()).getContent());
        return "admin/product-form";
    }

    @PostMapping("/admin/products")
    public String addProduct(@Valid @ModelAttribute ProductDto productDto,
                           BindingResult result, Model model,
                           Authentication authentication,
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll(Pageable.unpaged()).getContent());
            return "admin/product-form";
        }

        try {
            User admin = userService.findByUsername(authentication.getName());
            productService.create(productDto, admin);
            redirectAttributes.addFlashAttribute("successMessage", "Product created successfully!");
            return "redirect:/admin/products";
        } catch (Exception e) {
            model.addAttribute("categories", categoryService.findAll(Pageable.unpaged()).getContent());
            model.addAttribute("errorMessage", "Error creating product: " + e.getMessage());
            return "admin/product-form";
        }
    }

    @GetMapping("/admin/products/{id}/edit")
    public String editProductForm(@PathVariable Long id, Model model) {
        var product = productService.findById(id);
        ProductDto productDto = new ProductDto();
        productDto.setName(product.getName());
        productDto.setDescription(product.getDescription());
        productDto.setPrice(product.getPrice());
        productDto.setStock(product.getStock());
        productDto.setCategoryId(product.getCategory().getId());

        model.addAttribute("productDto", productDto);
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.findAll(Pageable.unpaged()).getContent());
        return "admin/product-form";
    }

    @PostMapping("/admin/products/{id}")
    public String updateProduct(@PathVariable Long id,
                              @Valid @ModelAttribute ProductDto productDto,
                              BindingResult result, Model model,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("product", productService.findById(id));
            model.addAttribute("categories", categoryService.findAll(Pageable.unpaged()).getContent());
            return "admin/product-form";
        }

        try {
            User admin = userService.findByUsername(authentication.getName());
            productService.update(id, productDto, admin);
            redirectAttributes.addFlashAttribute("successMessage", "Product updated successfully!");
            return "redirect:/admin/products";
        } catch (Exception e) {
            model.addAttribute("product", productService.findById(id));
            model.addAttribute("categories", categoryService.findAll(Pageable.unpaged()).getContent());
            model.addAttribute("errorMessage", "Error updating product: " + e.getMessage());
            return "admin/product-form";
        }
    }

    @PostMapping("/admin/products/{id}/delete")
    public String deleteProduct(@PathVariable Long id,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        try {
            User admin = userService.findByUsername(authentication.getName());
            productService.delete(id, admin);
            redirectAttributes.addFlashAttribute("successMessage", "Product deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting product: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }
}
