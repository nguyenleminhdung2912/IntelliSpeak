package com.gsu25se05.itellispeak.service;

import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.dto.category.CategoryRequest;
import com.gsu25se05.itellispeak.dto.category.CategoryResponse;
import com.gsu25se05.itellispeak.dto.category.UpdateCategoryRequest;
import com.gsu25se05.itellispeak.dto.category.UpdateCategoryResponse;
import com.gsu25se05.itellispeak.entity.ForumCategory;
import com.gsu25se05.itellispeak.entity.User;
import com.gsu25se05.itellispeak.exception.auth.NotFoundException;
import com.gsu25se05.itellispeak.exception.service.CreateServiceException;
import com.gsu25se05.itellispeak.repository.ForumCategoryRepository;
import com.gsu25se05.itellispeak.utils.AccountUtils;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ForumCategoryService {

    private final ForumCategoryRepository forumCategoryRepository;
    private final AccountUtils accountUtils;

    public ForumCategoryService(ForumCategoryRepository forumCategoryRepository, AccountUtils accountUtils) {
        this.forumCategoryRepository = forumCategoryRepository;
        this.accountUtils = accountUtils;
    }

    public List<ForumCategory> getAllCategories() {
        return forumCategoryRepository.findByIsDeletedFalse();
    }

    public ForumCategory getCategoryById(Long id) {
        return forumCategoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + id));
    }

    public Response<CategoryResponse> createCategory(@Valid CategoryRequest categoryRequest) {
        User account = accountUtils.getCurrentAccount();
        if (account == null) return new Response<>(401, "Please login first", null);

        ForumCategory forumCategory = new ForumCategory();
        forumCategory.setTitle(categoryRequest.getTitle());
        forumCategory.setCreateAt(LocalDateTime.now());
        forumCategory.setDeleted(false);

        try {
            forumCategoryRepository.save(forumCategory);
        } catch (Exception e) {
            throw new CreateServiceException("There was something wrong when creating the category, please try again...");
        }

        CategoryResponse categoryResponse = new CategoryResponse(
                forumCategory.getId(),
                forumCategory.getTitle(),
                forumCategory.getCreateAt()
        );

        return new Response<>(200, "Category created successfully!", categoryResponse);
    }

    public Response<UpdateCategoryResponse> updateCategory(Long id, UpdateCategoryRequest updated) {

        User account = accountUtils.getCurrentAccount();
        if (account == null) return new Response<>(401, "Please login first", null);

        ForumCategory category = forumCategoryRepository.findById(id).orElse(null);
        if (category == null) return new Response<>(404, "Category not found", null);

        if (updated.getTitle() != null) category.setTitle(updated.getTitle());
        category.setUpdateAt(LocalDateTime.now());
        try {
            forumCategoryRepository.save(category);
        } catch (Exception e) {
            throw new CreateServiceException("There was something wrong when updating the category, please try again...");
        }

        UpdateCategoryResponse data = new UpdateCategoryResponse(
                category.getId(),
                category.getTitle(),
                category.getUpdateAt()
        );
        return new Response<>(200, "Category updated successfully!", data);
    }

    public Response<String> deleteCategory(Long id) {
        User account = accountUtils.getCurrentAccount();
        if (account == null) return new Response<>(401, "Please login first", null);
        ForumCategory category = getCategoryById(id);
        if (category == null) return new Response<>(404, "Forum category type not found", null);
        if (category.isDeleted()) return new Response<>(400, "Forum category is already deleted", null);
        category.setDeleted(true);
        category.setUpdateAt(LocalDateTime.now());
        forumCategoryRepository.save(category);
        return new Response<>(200, "Forum category deleted successfully!", "category with ID " + id + " was soft deleted.");
    }
}
