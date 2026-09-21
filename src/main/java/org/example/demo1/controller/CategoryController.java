package org.example.demo1.controller;

import org.example.demo1.pojo.Result;
import org.example.demo1.service.CategoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/api/categories")
    public Result listEnabled() {
        return Result.success(categoryService.listEnabled());
    }
}
