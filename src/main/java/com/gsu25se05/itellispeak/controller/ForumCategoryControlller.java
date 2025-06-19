package com.gsu25se05.itellispeak.controller;

import com.gsu25se05.itellispeak.entity.ForumCategory;
import com.gsu25se05.itellispeak.service.ForumCategoryService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
@CrossOrigin("**")
@SecurityRequirement(name = "api")
public class ForumCategoryControlller {

    @Autowired
    private ForumCategoryService forumCategoryService;

    @GetMapping
    public List<ForumCategory> getAllCategories() {
        return forumCategoryService.getAllCategories();
    }

    @GetMapping("/{id}")
    public ForumCategory getCategoryById(@PathVariable Long id) {
        return forumCategoryService.getCategoryById(id);
    }

    @PostMapping
    public ForumCategory createCategory(@RequestBody ForumCategory category) {
        return forumCategoryService.createCategory(category);
    }

    @PutMapping("/{id}")
    public ForumCategory updateCategory(@PathVariable Long id, @RequestBody ForumCategory category) {
        return forumCategoryService.updateCategory(id, category);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        forumCategoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}

