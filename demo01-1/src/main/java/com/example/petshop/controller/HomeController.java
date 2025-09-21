package com.example.petshop.controller;

import com.example.petshop.service.CategoryService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {
    private final CategoryService categoryService;

    public HomeController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/")
    public String home(@RequestParam(defaultValue = "0") int page,
                      @RequestParam(defaultValue = "20") int size,
                      Model model) {
        Pageable pageable = PageRequest.of(page, size);
        model.addAttribute("categories", categoryService.findAll(pageable));
        return "index";
    }
}
