package com.gsu25se05.itellispeak.service;


import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.dto.forum.CreateRequestForumPostDTO;
import com.gsu25se05.itellispeak.dto.forum.CreateResponseForumDTO;
import com.gsu25se05.itellispeak.entity.*;
import com.gsu25se05.itellispeak.exception.ErrorCode;
import com.gsu25se05.itellispeak.exception.auth.AuthAppException;
import com.gsu25se05.itellispeak.exception.auth.NotFoundException;
import com.gsu25se05.itellispeak.repository.*;
import com.gsu25se05.itellispeak.utils.AccountUtils;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ForumPostService {
    private final AccountUtils accountUtils;
    private final UserRepository userRepository;
    private final ForumPostRepository forumPostRepository;
    private final ForumPostPictureRepository forumPostPictureRepository;
    private final ForumCategoryRepository forumCategoryRepository;
    private final ForumTopicTypeRepository forumTopicTypeRepository;

    public ForumPostService(AccountUtils accountUtils, UserRepository userRepository, ForumPostRepository forumPostRepository, ForumPostPictureRepository forumPostPictureRepository, ForumCategoryRepository forumCategoryRepository, ForumTopicTypeRepository forumTopicTypeRepository) {
        this.accountUtils = accountUtils;
        this.userRepository = userRepository;
        this.forumPostRepository = forumPostRepository;
        this.forumPostPictureRepository = forumPostPictureRepository;
        this.forumCategoryRepository = forumCategoryRepository;
        this.forumTopicTypeRepository = forumTopicTypeRepository;
    }

    public List<ForumPost> getAllPosts() {
        return forumPostRepository.findByIsDeletedFalse();
    }

    public ForumPost getPostById(Long id) {
        return forumPostRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Forum Post not found with id: " + id));
    }

    public Response<CreateResponseForumDTO> createForumPost(@Valid CreateRequestForumPostDTO dto) {
        User user = accountUtils.getCurrentAccount();
        if (user == null) return new Response<>(401, "Please login first", null);
        ForumPost post = new ForumPost();
        post.setUser(user);
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setCreateAt(LocalDateTime.now());
        post.setIsDeleted(false);

        ForumCategory category = forumCategoryRepository.findById(dto.getForumCategoryId())
                .orElseThrow(() -> new NotFoundException("Category not found"));
        post.setForumCategory(category);

        ForumTopicType topicType = forumTopicTypeRepository.findById(dto.getForumTopicTypeId())
                .orElseThrow(() -> new NotFoundException("Topic type not found"));
        post.setForumTopicType(topicType);

        List<ForumPostPicture> pictures = dto.getImages().stream().map(url -> {
            ForumPostPicture pic = new ForumPostPicture();
            pic.setForumPost(post);
            pic.setUrl(url);
            pic.setCreateAt(LocalDateTime.now());
            pic.setDeleted(false);
            return pic;
        }).collect(Collectors.toList());

        post.setPictures(pictures);

        forumPostRepository.save(post);

        // response
        CreateResponseForumDTO responseDTO = new CreateResponseForumDTO(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                dto.getImages().isEmpty() ? null : dto.getImages().get(0),
                post.getForumTopicType(),
                post.getForumCategory(),
                post.getCreateAt()
        );

        return new Response<>(201, "Forum Post created successfully!", responseDTO);
    }


    public Response<String> deletePost(Long id) {
        User user = accountUtils.getCurrentAccount();
        if (user == null) return new Response<>(401, "Please login first", null);
        ForumPost forumPost = getPostById(id);
        if (forumPost == null) return new Response<>(404, "Forum post not found", null);
        if (forumPost.getIsDeleted()) return new Response<>(400, "Forum post is already deleted", null);
        forumPost.setIsDeleted(true);
        forumPost.setUpdateAt(LocalDateTime.now());
        forumPostRepository.save(forumPost);
        return new Response<>(200, "Forum post deleted successfully!", "post with ID " + id + " was soft deleted.");
    }


}
