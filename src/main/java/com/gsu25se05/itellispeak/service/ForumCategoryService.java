package com.gsu25se05.itellispeak.service;

import com.gsu25se05.itellispeak.entity.ForumCategory;
import com.gsu25se05.itellispeak.exception.auth.NotFoundException;
import com.gsu25se05.itellispeak.repository.ForumCategoryRepository;
import com.gsu25se05.itellispeak.repository.ForumPostPictureRepository;
import com.gsu25se05.itellispeak.repository.ForumPostRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ForumCategoryService {

    private final ForumCategoryRepository forumCategoryRepository;

    public ForumCategoryService(ForumCategoryRepository forumCategoryRepository) {
        this.forumCategoryRepository = forumCategoryRepository;
    }

    public List<ForumCategory> getAllCategories() {
        return forumCategoryRepository.findByIsDeletedFalse();
    }

    public ForumCategory getCategoryById(Long id) {
        return forumCategoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + id));
    }

    public ForumCategory createCategory(ForumCategory category) {
        category.setCreateAt(LocalDateTime.now());
        category.setDeleted(false);
        return forumCategoryRepository.save(category);
    }

    public ForumCategory updateCategory(Long id, ForumCategory updated) {
        ForumCategory category = getCategoryById(id);
        category.setTitle(updated.getTitle());
        category.setUpdateAt(LocalDateTime.now());
        return forumCategoryRepository.save(category);
    }

    public void deleteCategory(Long id) {
        ForumCategory category = getCategoryById(id);
        category.setDeleted(true);
        category.setUpdateAt(LocalDateTime.now());
        forumCategoryRepository.save(category);
    }

}
