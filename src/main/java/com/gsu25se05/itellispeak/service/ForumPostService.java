package com.gsu25se05.itellispeak.service;

import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.dto.forum.*;
import com.gsu25se05.itellispeak.dto.savedPost.ToggleSaveDTO;
import com.gsu25se05.itellispeak.entity.*;
import com.gsu25se05.itellispeak.exception.auth.NotFoundException;
import com.gsu25se05.itellispeak.repository.*;
import com.gsu25se05.itellispeak.utils.AccountUtils;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ForumPostService {
    private final AccountUtils accountUtils;
    private final UserRepository userRepository;
    private final ForumPostRepository forumPostRepository;
    private final ForumPostPictureRepository forumPostPictureRepository;
    private final ForumTopicTypeRepository forumTopicTypeRepository;
    private final SavedPostRepository savedPostRepository;
    private final ForumPostReplyRepository forumPostReplyRepository;

    public ForumPostService(AccountUtils accountUtils, UserRepository userRepository, ForumPostRepository forumPostRepository,
                            ForumPostPictureRepository forumPostPictureRepository,
                            ForumTopicTypeRepository forumTopicTypeRepository, SavedPostRepository savedPostRepository,
                            ForumPostReplyRepository forumPostReplyRepository) {
        this.accountUtils = accountUtils;
        this.userRepository = userRepository;
        this.forumPostRepository = forumPostRepository;
        this.forumPostPictureRepository = forumPostPictureRepository;
        this.forumTopicTypeRepository = forumTopicTypeRepository;
        this.savedPostRepository = savedPostRepository;
        this.forumPostReplyRepository = forumPostReplyRepository;
    }

    public Response<List<CreateResponseForumDTO>> getAllPosts() {
        User current = accountUtils.getCurrentAccount();

        List<ForumPost> posts = forumPostRepository.findByIsDeletedFalseOrderByCreateAtDesc();

        final java.util.Set<Long> savedIds = (current == null)
                ? java.util.Collections.emptySet()
                : new java.util.HashSet<>(savedPostRepository.findActiveSavedPostIdsByUser(current));

        List<CreateResponseForumDTO> data = posts.stream().map(post -> {
            // Ảnh active
            List<String> images = (post.getPictures() == null) ? List.of() :
                    post.getPictures().stream()
                            .filter(p -> !Boolean.TRUE.equals(p.isDeleted()))
                            .map(ForumPostPicture::getUrl)
                            .toList();

            // Tên tác giả
            String authorEmail = (post.getUser() != null) ? post.getUser().getEmail() : null;
            String authorUsername = (authorEmail != null && authorEmail.contains("@"))
                    ? authorEmail.substring(0, authorEmail.indexOf('@'))
                    : "unknown";

            boolean isSaved = savedIds.contains(post.getId());

            int readTime = estimateReadTime(post.getContent());

            return new CreateResponseForumDTO(
                    post.getId(),
                    post.getTitle(),
                    post.getContent(),
                    images,
                    authorUsername,
                    post.getForumTopicType(),
                    isSaved,
                    post.getCreateAt(),
                    post.getLikeCount(),
                    readTime
            );
        }).toList();

        return new Response<>(200, "Success", data);
    }


    public Response<CreateResponseForumDTO> getPostById(Long postId) {
        User current = accountUtils.getCurrentAccount();
        if (current == null) return new Response<>(401, "Please log in first", null);

        ForumPost post = forumPostRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found with ID: " + postId));

        // Ảnh active
        List<String> activeImages = (post.getPictures() == null) ? List.of() :
                post.getPictures().stream()
                        .filter(p -> !Boolean.TRUE.equals(p.isDeleted()))
                        .map(ForumPostPicture::getUrl)
                        .toList();

        String authorEmail = (post.getUser() != null) ? post.getUser().getEmail() : null;
        String authorUsername = (authorEmail != null && authorEmail.contains("@"))
                ? authorEmail.split("@")[0] : "unknown";

        boolean isSaved = savedPostRepository.findByUserAndForumPost(current, post)
                .filter(saved -> !Boolean.TRUE.equals(saved.isDeleted()))
                .isPresent();

        int readTime = estimateReadTime(post.getContent());

        CreateResponseForumDTO dto = new CreateResponseForumDTO(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                activeImages,
                authorUsername,
                post.getForumTopicType(),
                isSaved,
                post.getCreateAt(),
                post.getLikeCount(),
                readTime
        );

        return new Response<>(200, "Get post detail successfully", dto);
    }


    public Response<List<CreateResponseForumDTO>> getPostsByTopic(Long topicId) {

        User current = accountUtils.getCurrentAccount();
        if (current == null) return new Response<>(401, "Please log in first", null);

        ForumTopicType topic = forumTopicTypeRepository.findById(topicId)
                .orElseThrow(() -> new NotFoundException("Topic not found"));

        List<ForumPost> posts = forumPostRepository
                .findByForumTopicType_IdAndIsDeletedFalseOrderByCreateAtDesc(topicId);

        List<CreateResponseForumDTO> data = posts.stream().map(post -> {
            String email = post.getUser() != null ? post.getUser().getEmail() : null;
            String authorUsername = (email != null && email.contains("@")) ? email.split("@")[0] : "unknown";

            // lọc ảnh active
            List<String> activeImages = post.getPictures() == null ? Collections.emptyList() :
                    post.getPictures().stream()
                            .filter(p -> !Boolean.TRUE.equals(p.isDeleted()))
                            .map(ForumPostPicture::getUrl)
                            .collect(Collectors.toList());

            boolean isSaved = savedPostRepository.findByUserAndForumPost(current, post)
                    .filter(saved -> !Boolean.TRUE.equals(saved.isDeleted()))
                    .isPresent();

            int readTime = estimateReadTime(post.getContent());

            return new CreateResponseForumDTO(
                    post.getId(),
                    post.getTitle(),
                    post.getContent(),
                    activeImages,
                    authorUsername,
                    post.getForumTopicType(),
                    isSaved,
                    post.getCreateAt(),
                    post.getLikeCount(),
                    readTime
            );
        }).collect(Collectors.toList());

        return new Response<>(200, "Get posts by topic successfully", data);
    }

    public Response<List<CreateResponseForumDTO>> getMyPosts() {
        User user = accountUtils.getCurrentAccount();
        if (user == null)
            return new Response<>(401, "Please log in first", null);

        String email = user.getEmail();
        String username = email != null && email.contains("@") ? email.split("@")[0] : "unknown";

        List<ForumPost> myPosts = forumPostRepository.findByUserAndIsDeletedFalseOrderByCreateAtDesc(user);




        List<CreateResponseForumDTO> responseList = myPosts.stream()
                .map(post -> {
                    int readTime = estimateReadTime(post.getContent());
                    List<String> activeImages = post.getPictures().stream()
                            .filter(pic -> !Boolean.TRUE.equals(pic.isDeleted()))
                            .map(ForumPostPicture::getUrl)
                            .collect(Collectors.toList());

                    boolean isSaved = savedPostRepository.findByUserAndForumPost(user, post)
                            .filter(saved -> !Boolean.TRUE.equals(saved.isDeleted()))
                            .isPresent();

                    return new CreateResponseForumDTO(
                            post.getId(),
                            post.getTitle(),
                            post.getContent(),
                            activeImages,
                            username,
                            post.getForumTopicType(),
                            isSaved,
                            post.getCreateAt(),
                            post.getLikeCount(),
                            readTime
                    );
                })
                .collect(Collectors.toList());

        return new Response<>(200, "Successfully retrieved your posts", responseList);
    }


    public Response<CreateResponseForumDTO> createForumPost(@Valid CreateRequestForumPostDTO dto) {
        User user = accountUtils.getCurrentAccount();
        if (user == null) return new Response<>(401, "Please log in to continue", null);

        if (dto.getForumTopicTypeId() == null) {
            return new Response<>(400, "You have not selected a topic for the post", null);
        }

        ForumPost post = new ForumPost();
        post.setUser(user);
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setLikeCount(0);
        post.setCreateAt(LocalDateTime.now());
        post.setIsDeleted(false);

        ForumTopicType topicType = forumTopicTypeRepository.findById(dto.getForumTopicTypeId())
                .orElseThrow(() -> new NotFoundException("Topic not found"));
        post.setForumTopicType(topicType);

        List<String> imageUrls = dto.getImages() != null ? dto.getImages() : Collections.emptyList();
        List<ForumPostPicture> pictures = imageUrls.stream().map(url -> {
            ForumPostPicture pic = new ForumPostPicture();
            pic.setForumPost(post);
            pic.setUrl(url);
            pic.setCreateAt(LocalDateTime.now());
            pic.setDeleted(false);
            return pic;
        }).collect(Collectors.toList());

        post.setPictures(pictures);

        forumPostRepository.save(post);

        List<String> responseImageUrls = post.getPictures().stream()
                .filter(p -> !Boolean.TRUE.equals(p.isDeleted()))
                .map(ForumPostPicture::getUrl)
                .collect(Collectors.toList());

        String email = post.getUser().getEmail();
        String username = email != null && email.contains("@") ? email.split("@")[0] : "unknown";

        int readTime = estimateReadTime(post.getContent());

        boolean isSaved = savedPostRepository.findByUserAndForumPost(user, post)
                .filter(saved -> !Boolean.TRUE.equals(saved.isDeleted()))
                .isPresent();

        CreateResponseForumDTO responseDTO = new CreateResponseForumDTO(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                responseImageUrls,
                username,
                post.getForumTopicType(),
                isSaved,
                post.getCreateAt(),
                post.getLikeCount(),
                readTime
        );

        return new Response<>(201, "Post created successfully!", responseDTO);
    }

    private int estimateReadTime(String content) {
        if (content == null || content.trim().isEmpty()) return 1;
        int wordCount = content.trim().split("\\s+").length;
        return Math.max(1, wordCount / 200);
    }

    public Response<UpdateResponsePostDTO> updateForumPost(Long postId, @Valid UpdateRequestPostDTO dto) {
        User user = accountUtils.getCurrentAccount();
        if (user == null) return new Response<>(401, "Please log in to continue", null);

        ForumPost post = forumPostRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));

        if (!post.getUser().getUserId().equals(user.getUserId())) {
            return new Response<>(403, "You do not have permission to edit this post", null);
        }

        if (dto.getTitle() != null) post.setTitle(dto.getTitle());
        if (dto.getContent() != null) post.setContent(dto.getContent());
        post.setUpdateAt(LocalDateTime.now());

        if (dto.getForumTopicTypeId() != null) {
            ForumTopicType topicType = forumTopicTypeRepository.findById(dto.getForumTopicTypeId())
                    .orElseThrow(() -> new NotFoundException("Topic not found"));
            post.setForumTopicType(topicType);
        }

        List<ForumPostPicture> currentPictures = post.getPictures();
        for (UpdateImageDTO imageDTO : dto.getImages()) {
            if (imageDTO.getId() != null) {
                ForumPostPicture pic = currentPictures.stream()
                        .filter(p -> p.getId().equals(imageDTO.getId()))
                        .findFirst()
                        .orElseThrow(() -> new NotFoundException("Image not found with ID: " + imageDTO.getId()));
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
                post.getId(), post.getTitle(), post.getContent(), imageUrls,
                post.getForumTopicType(), post.getUpdateAt()
        );

        return new Response<>(200, "Post updated successfully!", responseDTO);
    }

    @Transactional
    public Response<String> deleteImageFromPost(Long postId, Long imageId) {
        User user = accountUtils.getCurrentAccount();
        if (user == null) return new Response<>(401, "Please log in first", null);

        ForumPost post = forumPostRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));

        if (!post.getUser().getUserId().equals(user.getUserId())) {
            return new Response<>(403, "You do not have permission to delete images from this post", null);
        }

        ForumPostPicture image = forumPostPictureRepository.findById(imageId)
                .orElseThrow(() -> new NotFoundException("Image not found"));

        if (!image.getForumPost().getId().equals(postId)) {
            return new Response<>(400, "The image does not belong to the specified post", null);
        }

        image.setDeleted(true);
        image.setUpdateAt(LocalDateTime.now());
        forumPostPictureRepository.save(image);

        return new Response<>(200, "Image removed from post successfully", null);
    }

    public Response<String> deletePost(Long id) {
        User user = accountUtils.getCurrentAccount();
        if (user == null) return new Response<>(401, "Please log in first", null);

        ForumPost forumPost = forumPostRepository.findById(id).orElseThrow(null);
        if (forumPost.getIsDeleted()) return new Response<>(400, "The post has already been deleted", null);

        forumPost.setIsDeleted(true);
        forumPost.setUpdateAt(LocalDateTime.now());
        forumPostRepository.save(forumPost);

        return new Response<>(200, "Post deleted successfully!", "The post with ID " + id + " has been soft deleted.");

    }

    @Transactional
    public Response<ToggleSaveDTO> savePost(Long postId) {
        User user = accountUtils.getCurrentAccount();
        if (user == null) return new Response<>(401, "Please log in first", null);

        ForumPost post = forumPostRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));

        SavedPost saved = savedPostRepository.findByUserAndForumPost(user, post).orElse(null);

        if (saved == null) {
            SavedPost created = SavedPost.builder()
                    .user(user)
                    .forumPost(post)
                    .savedAt(LocalDateTime.now())
                    .isDeleted(false)
                    .build();
            savedPostRepository.save(created);
            return new Response<>(200, "Post saved successfully",
                    new ToggleSaveDTO(postId, true));
        }

        if (Boolean.TRUE.equals(saved.isDeleted())) {
            saved.setDeleted(false);
            saved.setSavedAt(LocalDateTime.now());
            savedPostRepository.save(saved);
            return new Response<>(200, "Post saved successfully",
                    new ToggleSaveDTO(postId, true));
        }

        saved.setDeleted(true);
        savedPostRepository.save(saved);

        return new Response<>(200, "Post unsaved successfully",
                new ToggleSaveDTO(postId, false));
    }


    public Response<List<CreateResponseForumDTO>> getSavedPosts() {
        User current = accountUtils.getCurrentAccount();
        if (current == null) return new Response<>(401, "Please log in first", null);

        List<SavedPost> savedPosts = savedPostRepository
                .findByUserAndIsDeletedFalseOrderBySavedAtDesc(current);

        List<CreateResponseForumDTO> data = savedPosts.stream()
                .map(SavedPost::getForumPost)
                .filter(p -> !Boolean.TRUE.equals(p.getIsDeleted()))
                .map(post -> {
                    int readTime = estimateReadTime(post.getContent());

                    String authorEmail = post.getUser() != null ? post.getUser().getEmail() : null;
                    String authorUsername = (authorEmail != null && authorEmail.contains("@"))
                            ? authorEmail.split("@")[0] : "unknown";

                    // chỉ lấy ảnh active
                    List<String> images = (post.getPictures() == null) ? List.of() :
                            post.getPictures().stream()
                                    .filter(pic -> !Boolean.TRUE.equals(pic.isDeleted()))
                                    .map(ForumPostPicture::getUrl)
                                    .toList();

                    CreateResponseForumDTO dto = new CreateResponseForumDTO();
                    dto.setPostId(post.getId());
                    dto.setTitle(post.getTitle());
                    dto.setContent(post.getContent());
                    dto.setImage(images);
                    dto.setUserName(authorUsername);
                    dto.setForumTopicType(post.getForumTopicType());
                    dto.setIsSaved(true);
                    dto.setCreateAt(post.getCreateAt());
                    dto.setReactionCount(post.getLikeCount());
                    dto.setReadTimeEstimate(readTime);
                    return dto;
                })
                .toList();

        return new Response<>(200, "Retrieved saved posts successfully", data);
    }


    @Transactional
    public Response<ToggleSaveDTO> unSavePost(Long postId) {
        User user = accountUtils.getCurrentAccount();
        if (user == null) return new Response<>(401, "Please log in first", null);

        ForumPost post = forumPostRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));

        SavedPost saved = savedPostRepository.findByUserAndForumPost(user, post)
                .orElseThrow(() -> new NotFoundException("Saved post not found"));

        // Nếu đã hủy trước đó thì idempotent (không lỗi, cứ trả về isSaved=false)
        if (Boolean.TRUE.equals(saved.isDeleted())) {
            return new Response<>(200, "Post already unsaved",
                    new ToggleSaveDTO(postId, false));
        }

        saved.setDeleted(true);
        savedPostRepository.save(saved);

        return new Response<>(200, "Unsave post successfully",
                new ToggleSaveDTO(postId, false));
    }


    public List<ForumPostReply> getRepliesByPostId(Long postId) {
        ForumPost post = forumPostRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));
        return forumPostReplyRepository.findByForumPostAndIsDeletedFalse(post);
    }

    public List<ForumPostReplyWithUserDTO> getRepliesWithUserByPostId(Long postId) {
        ForumPost post = forumPostRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));
        List<ForumPostReply> replies = forumPostReplyRepository.findByForumPostAndIsDeletedFalse(post);
        return replies.stream().map(reply -> {
            User user = reply.getUser();
            ForumPostReplyWithUserDTO.UserInfo userInfo = new ForumPostReplyWithUserDTO.UserInfo(
                user.getFirstName(),
                user.getLastName(),
                user.getAvatar()
            );
            return new ForumPostReplyWithUserDTO(
                reply.getId(),
                reply.getTitle(),
                reply.getContent(),
                reply.getStatus(),
                reply.getCreateAt(),
                reply.getUpdateAt(),
                reply.getIsDeleted(),
                userInfo
            );
        }).collect(Collectors.toList());
    }
}
