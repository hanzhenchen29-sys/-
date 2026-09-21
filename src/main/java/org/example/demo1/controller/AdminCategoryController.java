package org.example.demo1.controller;

import org.example.demo1.pojo.Result;
import org.example.demo1.service.CategoryService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class AdminCategoryController {
    private final CategoryService categoryService;

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/api/admin/categories")
    public Result page(@RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "10") int size,
                       @RequestParam(required = false) String name,
                       @RequestParam(required = false) Integer status) {
        return Result.success(categoryService.adminPage(page, size, name, status));
    }

    @PostMapping("/api/admin/categories")
    public Result create(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        Integer sortNo = body.get("sortNo") == null ? null : ((Number) body.get("sortNo")).intValue();
        Long id = categoryService.create(name, sortNo);
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        return Result.success(data);
    }

    @PutMapping("/api/admin/categories/{id}")
    public Result update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        Integer status = body.get("status") == null ? null : ((Number) body.get("status")).intValue();
        Integer sortNo = body.get("sortNo") == null ? null : ((Number) body.get("sortNo")).intValue();
        categoryService.update(id, name, status, sortNo);
        return Result.success();
    }

    @DeleteMapping("/api/admin/categories/{id}")
    public Result delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.success();
    }
}
