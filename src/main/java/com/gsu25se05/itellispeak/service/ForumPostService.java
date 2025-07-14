package com.gsu25se05.itellispeak.service;


import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.dto.forum.*;
import com.gsu25se05.itellispeak.entity.*;
import com.gsu25se05.itellispeak.exception.auth.NotFoundException;
import com.gsu25se05.itellispeak.repository.*;
import com.gsu25se05.itellispeak.utils.AccountUtils;
import jakarta.transaction.Transactional;
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
    private final SavedPostRepository savedPostRepository;

    public ForumPostService(AccountUtils accountUtils, UserRepository userRepository, ForumPostRepository forumPostRepository, ForumPostPictureRepository forumPostPictureRepository, ForumCategoryRepository forumCategoryRepository, ForumTopicTypeRepository forumTopicTypeRepository, SavedPostRepository savedPostRepository) {
        this.accountUtils = accountUtils;
        this.userRepository = userRepository;
        this.forumPostRepository = forumPostRepository;
        this.forumPostPictureRepository = forumPostPictureRepository;
        this.forumCategoryRepository = forumCategoryRepository;
        this.forumTopicTypeRepository = forumTopicTypeRepository;
        this.savedPostRepository = savedPostRepository;
    }

    public List<ForumPost> getAllPosts() {
        List<ForumPost> posts = forumPostRepository.findByIsDeletedFalse();
        for (ForumPost post : posts) {
            List<ForumPostPicture> activePictures = post.getPictures().stream()
                    .filter(p -> !Boolean.TRUE.equals(p.isDeleted()))
                    .collect(Collectors.toList());
            post.setPictures(activePictures);
        }
        return posts;
    }

    public ForumPost getPostById(Long id) {
        ForumPost post = forumPostRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Forum Post not found with id: " + id));

        List<ForumPostPicture> activePictures = post.getPictures().stream()
                .filter(p -> !Boolean.TRUE.equals(p.isDeleted()))
                .collect(Collectors.toList());
        post.setPictures(activePictures);

        return post;
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

//        ForumCategory category = forumCategoryRepository.findById(dto.getForumCategoryId())
//                .orElseThrow(() -> new NotFoundException("Category not found"));
//        post.setForumCategory(category);

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

        List<String> imageUrls = post.getPictures().stream()
                .filter(p -> !Boolean.TRUE.equals(p.isDeleted()))
                .map(ForumPostPicture::getUrl)
                .collect(Collectors.toList());

        // response
        CreateResponseForumDTO responseDTO = new CreateResponseForumDTO(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                imageUrls,
                post.getForumTopicType(),
//                post.getForumCategory(),
                post.getCreateAt()
        );

        return new Response<>(201, "Forum Post created successfully!", responseDTO);
    }


    public Response<UpdateResponsePostDTO> updateForumPost(Long postId, @Valid UpdateRequestPostDTO dto) {
        User user = accountUtils.getCurrentAccount();
        if (user == null) return new Response<>(401, "Please login first", null);

        ForumPost post = forumPostRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Forum post not found"));

        // Only the post owner can edit
        if (!post.getUser().getUserId().equals(user.getUserId())) {
            return new Response<>(403, "You are not the owner of this post", null);
        }


        if (dto.getTitle() != null) {
            post.setTitle(dto.getTitle());
        }

        if (dto.getContent() != null) {
            post.setContent(dto.getContent());
        }
        post.setUpdateAt(LocalDateTime.now());


//        if (dto.getForumCategoryId() != null) {
//            ForumCategory category = forumCategoryRepository.findById(dto.getForumCategoryId())
//                    .orElseThrow(() -> new NotFoundException("Category not found"));
//            post.setForumCategory(category);
//        }

        if (dto.getForumTopicTypeId() != null) {
            ForumTopicType topicType = forumTopicTypeRepository.findById(dto.getForumTopicTypeId())
                    .orElseThrow(() -> new NotFoundException("Topic type not found"));
            post.setForumTopicType(topicType);
        }

        List<ForumPostPicture> currentPictures = post.getPictures();


        for (UpdateImageDTO imageDTO : dto.getImages()) {
            if (imageDTO.getId() != null) {
                ForumPostPicture pic = currentPictures.stream()
                        .filter(p -> p.getId().equals(imageDTO.getId()))
                        .findFirst()
                        .orElseThrow(() -> new NotFoundException("Image not found with id: " + imageDTO.getId()));
                pic.setUrl(imageDTO.getUrl());
            } else {
                ForumPostPicture newPic = new ForumPostPicture();
                newPic.setForumPost(post);
                newPic.setUrl(imageDTO.getUrl());
                newPic.setCreateAt(LocalDateTime.now());
                newPic.setDeleted(false);
                currentPictures.add(newPic);
            }
        }

            post.setPictures(currentPictures);

            forumPostRepository.save(post);

        List<String> imageUrls = post.getPictures().stream()
                .filter(p -> !Boolean.TRUE.equals(p.isDeleted()))
                .map(ForumPostPicture::getUrl)
                .collect(Collectors.toList());

            UpdateResponsePostDTO responseDTO = new UpdateResponsePostDTO(
                    post.getId(),
                    post.getTitle(),
                    post.getContent(),
                    imageUrls,
                    post.getForumTopicType(),
//                    post.getForumCategory(),
                    post.getUpdateAt()
            );

            return new Response<>(200, "Forum post updated successfully!", responseDTO);
        }

    @Transactional
    public Response<String> deleteImageFromPost(Long postId, Long imageId) {
        User user = accountUtils.getCurrentAccount();
        if (user == null) return new Response<>(401, "Please login first", null);

        ForumPost post = forumPostRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));

        if (!post.getUser().getUserId().equals(user.getUserId())) {
            return new Response<>(403, "You are not the owner of this post", null);
        }

        ForumPostPicture image = forumPostPictureRepository.findById(imageId)
                .orElseThrow(() -> new NotFoundException("Image not found"));

        if (!image.getForumPost().getId().equals(postId)) {
            return new Response<>(400, "Image does not belong to the specified post", null);
        }

        image.setDeleted(true);
        image.setUpdateAt(LocalDateTime.now());
        forumPostPictureRepository.save(image);

        return new Response<>(200, "Image deleted from post successfully", null);
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



    public Response<String> savePost(Long postId) {
        User user = accountUtils.getCurrentAccount();
        if (user == null) return new Response<>(401, "Please login first", null);

        ForumPost post = forumPostRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));

        if (savedPostRepository.findByUserAndForumPost(user, post).isPresent()) {
            return new Response<>(400, "Post already saved", null);
        }

        SavedPost savedPost = SavedPost.builder()
                .user(user)
                .forumPost(post)
                .savedAt(LocalDateTime.now())
                .build();

        savedPostRepository.save(savedPost);
        return new Response<>(200, "Post saved successfully", null);
    }

    public Response<List<ForumPost>> getSavedPosts() {
        User user = accountUtils.getCurrentAccount();
        if (user == null) return new Response<>(401, "Please login first", null);

        List<SavedPost> savedPosts = savedPostRepository.findByUser(user).stream()
                .filter(savedPost -> !savedPost.isDeleted())
                .collect(Collectors.toList());

        List<ForumPost> posts = savedPosts.stream()
                .map(SavedPost::getForumPost)
                .filter(post -> !Boolean.TRUE.equals(post.getIsDeleted()))
                .collect(Collectors.toList());

        return new Response<>(200, "Retrieved saved posts", posts);
    }

    public Response<String> unSavePost(Long postId) {
        User user = accountUtils.getCurrentAccount();
        if (user == null) return new Response<>(401, "Please login first", null);

        ForumPost post = forumPostRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));

        SavedPost savedPost = savedPostRepository.findByUserAndForumPost(user, post)
                .orElseThrow(() -> new NotFoundException("Saved post not found"));

        savedPost.setDeleted(true);
        savedPost.setSavedAt(LocalDateTime.now());
        savedPostRepository.save(savedPost);

        return new Response<>(200, "Post unsaved successfully", null);
    }



    public Response<List<ForumPost>> getTopPostsByReplies(int limit) {
        List<ForumPost> posts = forumPostRepository.findTopPostsByReplyCount(limit);

        // Lọc ảnh không bị xóa
        for (ForumPost post : posts) {
            List<ForumPostPicture> activePictures = post.getPictures().stream()
                    .filter(p -> !Boolean.TRUE.equals(p.isDeleted()))
                    .collect(Collectors.toList());
            post.setPictures(activePictures);
        }

        return new Response<>(200, "Top posts by reply count", posts);
    }
}
