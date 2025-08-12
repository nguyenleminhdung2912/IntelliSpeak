//package com.gsu25se05.itellispeak.controller;
//
//import com.gsu25se05.itellispeak.dto.Response;
//import com.gsu25se05.itellispeak.dto.category.CategoryRequest;
//import com.gsu25se05.itellispeak.dto.category.CategoryResponse;
//import com.gsu25se05.itellispeak.dto.category.UpdateCategoryRequest;
//import com.gsu25se05.itellispeak.dto.category.UpdateCategoryResponse;
//import com.gsu25se05.itellispeak.entity.ForumCategory;
//import com.gsu25se05.itellispeak.service.ForumCategoryService;
//import io.swagger.v3.oas.annotations.security.SecurityRequirement;
//import jakarta.validation.Valid;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/category")
//@CrossOrigin("**")
//@SecurityRequirement(name = "api")
//public class ForumCategoryControlller {
//
//    @Autowired
//    private ForumCategoryService forumCategoryService;
//
//    @GetMapping
//    public List<ForumCategory> getAllCategories() {
//        return forumCategoryService.getAllCategories();
//    }
//
//    @GetMapping("/{id}")
//    public ForumCategory getCategoryById(@PathVariable Long id) {
//        return forumCategoryService.getCategoryById(id);
//    }
//
//    @PostMapping
//    public Response<CategoryResponse> createCategory( @Valid @RequestBody CategoryRequest category) {
//        return forumCategoryService.createCategory(category);
//    }
//
//    @PutMapping("/{id}")
//    public Response<UpdateCategoryResponse> updateCategory(@PathVariable Long id, @RequestBody UpdateCategoryRequest category) {
//        return forumCategoryService.updateCategory(id, category);
//    }
//
//    @DeleteMapping("/{id}")
//    public Response<String> deleteCategory(@PathVariable Long id) {
//        return forumCategoryService.deleteCategory(id);
//    }
//}
//
