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
import java.util.Collections;
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
    private final ForumPostReplyRepository forumPostReplyRepository;

    public ForumPostService(AccountUtils accountUtils, UserRepository userRepository, ForumPostRepository forumPostRepository,
                            ForumPostPictureRepository forumPostPictureRepository, ForumCategoryRepository forumCategoryRepository,
                            ForumTopicTypeRepository forumTopicTypeRepository, SavedPostRepository savedPostRepository,
                            ForumPostReplyRepository forumPostReplyRepository) {
        this.accountUtils = accountUtils;
        this.userRepository = userRepository;
        this.forumPostRepository = forumPostRepository;
        this.forumPostPictureRepository = forumPostPictureRepository;
        this.forumCategoryRepository = forumCategoryRepository;
        this.forumTopicTypeRepository = forumTopicTypeRepository;
        this.savedPostRepository = savedPostRepository;
        this.forumPostReplyRepository = forumPostReplyRepository;
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
                .orElseThrow(() -> new NotFoundException("Không tìm thấy bài viết với ID: " + id));

        List<ForumPostPicture> activePictures = post.getPictures().stream()
                .filter(p -> !Boolean.TRUE.equals(p.isDeleted()))
                .collect(Collectors.toList());
        post.setPictures(activePictures);

        return post;
    }

    public Response<List<CreateResponseForumDTO>> getMyPosts() {
        User user = accountUtils.getCurrentAccount();
        if (user == null)
            return new Response<>(401, "Vui lòng đăng nhập trước", null);

        String email = user.getEmail();
        String username = email != null && email.contains("@") ? email.split("@")[0] : "unknown";

        List<ForumPost> myPosts = forumPostRepository.findByUserAndIsDeletedFalse(user);

        List<CreateResponseForumDTO> responseList = myPosts.stream()
                .map(post -> {
                    int readTime = estimateReadTime(post.getContent());
                    List<String> activeImages = post.getPictures().stream()
                            .filter(pic -> !Boolean.TRUE.equals(pic.isDeleted()))
                            .map(ForumPostPicture::getUrl)
                            .collect(Collectors.toList());

                    return new CreateResponseForumDTO(
                            post.getId(),
                            post.getTitle(),
                            post.getContent(),
                            activeImages,
                            username,
                            post.getForumTopicType(),
                            post.getCreateAt(),
                            post.getLikeCount(),
                            readTime
                    );
                })
                .collect(Collectors.toList());

        return new Response<>(200, "Lấy danh sách bài viết của bạn thành công", responseList);
    }


    public Response<CreateResponseForumDTO> createForumPost(@Valid CreateRequestForumPostDTO dto) {
        User user = accountUtils.getCurrentAccount();
        if (user == null) return new Response<>(401, "Vui lòng đăng nhập để tiếp tục", null);

        if (dto.getForumTopicTypeId() == null) {
            return new Response<>(400, "Bạn chưa chọn chủ đề bài viết", null);
        }

        ForumPost post = new ForumPost();
        post.setUser(user);
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setLikeCount(0);
        post.setCreateAt(LocalDateTime.now());
        post.setIsDeleted(false);

        ForumTopicType topicType = forumTopicTypeRepository.findById(dto.getForumTopicTypeId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy chủ đề"));
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

        CreateResponseForumDTO responseDTO = new CreateResponseForumDTO(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                responseImageUrls,
                username,
                post.getForumTopicType(),
                post.getCreateAt(),
                post.getLikeCount(),
                readTime
        );

        return new Response<>(201, "Tạo bài viết thành công!", responseDTO);
    }

    private int estimateReadTime(String content) {
        if (content == null || content.trim().isEmpty()) return 1;
        int wordCount = content.trim().split("\\s+").length;
        return Math.max(1, wordCount / 200);
    }

    public Response<UpdateResponsePostDTO> updateForumPost(Long postId, @Valid UpdateRequestPostDTO dto) {
        User user = accountUtils.getCurrentAccount();
        if (user == null) return new Response<>(401, "Vui lòng đăng nhập để tiếp tục", null);

        ForumPost post = forumPostRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy bài viết"));

        if (!post.getUser().getUserId().equals(user.getUserId())) {
            return new Response<>(403, "Bạn không có quyền chỉnh sửa bài viết này", null);
        }

        if (dto.getTitle() != null) post.setTitle(dto.getTitle());
        if (dto.getContent() != null) post.setContent(dto.getContent());
        post.setUpdateAt(LocalDateTime.now());

        if (dto.getForumTopicTypeId() != null) {
            ForumTopicType topicType = forumTopicTypeRepository.findById(dto.getForumTopicTypeId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy chủ đề"));
            post.setForumTopicType(topicType);
        }

        List<ForumPostPicture> currentPictures = post.getPictures();
        for (UpdateImageDTO imageDTO : dto.getImages()) {
            if (imageDTO.getId() != null) {
                ForumPostPicture pic = currentPictures.stream()
                        .filter(p -> p.getId().equals(imageDTO.getId()))
                        .findFirst()
                        .orElseThrow(() -> new NotFoundException("Không tìm thấy hình ảnh với ID: " + imageDTO.getId()));
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

        return new Response<>(200, "Cập nhật bài viết thành công!", responseDTO);
    }

    @Transactional
    public Response<String> deleteImageFromPost(Long postId, Long imageId) {
        User user = accountUtils.getCurrentAccount();
        if (user == null) return new Response<>(401, "Vui lòng đăng nhập trước", null);

        ForumPost post = forumPostRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy bài viết"));

        if (!post.getUser().getUserId().equals(user.getUserId())) {
            return new Response<>(403, "Bạn không có quyền xóa hình ảnh khỏi bài viết này", null);
        }

        ForumPostPicture image = forumPostPictureRepository.findById(imageId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy hình ảnh"));

        if (!image.getForumPost().getId().equals(postId)) {
            return new Response<>(400, "Hình ảnh không thuộc bài viết đã chỉ định", null);
        }

        image.setDeleted(true);
        image.setUpdateAt(LocalDateTime.now());
        forumPostPictureRepository.save(image);

        return new Response<>(200, "Xóa hình ảnh khỏi bài viết thành công", null);
    }

    public Response<String> deletePost(Long id) {
        User user = accountUtils.getCurrentAccount();
        if (user == null) return new Response<>(401, "Vui lòng đăng nhập trước", null);

        ForumPost forumPost = getPostById(id);
        if (forumPost.getIsDeleted()) return new Response<>(400, "Bài viết đã bị xóa trước đó", null);

        forumPost.setIsDeleted(true);
        forumPost.setUpdateAt(LocalDateTime.now());
        forumPostRepository.save(forumPost);

        return new Response<>(200, "Xóa bài viết thành công!", "Đã xóa mềm bài viết với ID " + id);
    }

    public Response<String> savePost(Long postId) {
        User user = accountUtils.getCurrentAccount();
        if (user == null) return new Response<>(401, "Vui lòng đăng nhập trước", null);

        ForumPost post = forumPostRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy bài viết"));

        if (savedPostRepository.findByUserAndForumPost(user, post).isPresent()) {
            return new Response<>(400, "Bài viết đã được lưu trước đó", null);
        }

        SavedPost savedPost = SavedPost.builder()
                .user(user)
                .forumPost(post)
                .savedAt(LocalDateTime.now())
                .build();

        savedPostRepository.save(savedPost);
        return new Response<>(200, "Lưu bài viết thành công", null);
    }

    public Response<List<CreateResponseForumDTO>> getSavedPosts() {
        User user = accountUtils.getCurrentAccount();
        if (user == null)
            return new Response<>(401, "Vui lòng đăng nhập trước", null);

        String email = user.getEmail();
        String username = email != null && email.contains("@") ? email.split("@")[0] : "unknown";

        List<SavedPost> savedPosts = savedPostRepository.findByUser(user).stream()
                .filter(savedPost -> !savedPost.isDeleted())
                .collect(Collectors.toList());

        List<CreateResponseForumDTO> responseList = savedPosts.stream()
                .map(SavedPost::getForumPost)
                .filter(post -> !Boolean.TRUE.equals(post.getIsDeleted()))
                .map(post -> {
                    int readTime = estimateReadTime(post.getContent());
                    CreateResponseForumDTO dto = new CreateResponseForumDTO();
                    dto.setPostId(post.getId());
                    dto.setTitle(post.getTitle());
                    dto.setContent(post.getContent());
                    dto.setImage(post.getPictures().stream().map(p -> p.getUrl()).toList());
                    dto.setUserName(username);
                    dto.setForumTopicType(post.getForumTopicType());
                    dto.setCreateAt(post.getCreateAt());
                    dto.setReactionCount(post.getLikeCount());
                    dto.setReadTimeEstimate(readTime);
                    return dto;
                })
                .collect(Collectors.toList());

        return new Response<>(200, "Lấy danh sách bài viết đã lưu thành công", responseList);
    }

    public Response<String> unSavePost(Long postId) {
        User user = accountUtils.getCurrentAccount();
        if (user == null) return new Response<>(401, "Vui lòng đăng nhập trước", null);

        ForumPost post = forumPostRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy bài viết"));

        SavedPost savedPost = savedPostRepository.findByUserAndForumPost(user, post)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy bài viết đã lưu"));

        savedPost.setDeleted(true);
        savedPost.setSavedAt(LocalDateTime.now());
        savedPostRepository.save(savedPost);

        return new Response<>(200, "Bỏ lưu bài viết thành công", null);
    }

    public Response<List<ForumPost>> getTopPostsByReplies(int limit) {
        List<ForumPost> posts = forumPostRepository.findTopPostsByReplyCount(limit);

        for (ForumPost post : posts) {
            List<ForumPostPicture> activePictures = post.getPictures().stream()
                    .filter(p -> !Boolean.TRUE.equals(p.isDeleted()))
                    .collect(Collectors.toList());
            post.setPictures(activePictures);
        }

        return new Response<>(200, "Lấy danh sách bài viết nổi bật thành công", posts);
    }

    public List<ForumPostReply> getRepliesByPostId(Long postId) {
        ForumPost post = forumPostRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found"));
        return forumPostReplyRepository.findByForumPostAndIsDeletedFalse(post);
    }
}
