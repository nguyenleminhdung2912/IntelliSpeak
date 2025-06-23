package com.gsu25se05.itellispeak.service;


import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.dto.forum.CreateRequestForumPostDTO;
import com.gsu25se05.itellispeak.dto.forum.CreateResponseForumDTO;
import com.gsu25se05.itellispeak.entity.*;
import com.gsu25se05.itellispeak.exception.ErrorCode;
import com.gsu25se05.itellispeak.exception.auth.AuthAppException;
import com.gsu25se05.itellispeak.exception.auth.NotFoundException;
import com.gsu25se05.itellispeak.exception.service.CreateServiceException;
import com.gsu25se05.itellispeak.repository.*;
import com.gsu25se05.itellispeak.utils.AccountUtils;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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

    public Response<CreateResponseForumDTO> createForumPost (@Valid CreateRequestForumPostDTO createRequestForumPostDTO){

        User user = checkAccount();

        ForumPost forumPost = new ForumPost();
        forumPost.setUser(user);
        forumPost.setTitle(createRequestForumPostDTO.getTitle());
        forumPost.setContent(createRequestForumPostDTO.getContent());
        forumPost.setCreateAt(LocalDateTime.now());
        forumPost.setIsDeleted(false);

        ForumCategory forumCategory = forumCategoryRepository.findById(createRequestForumPostDTO.getForumCategoryId())
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + createRequestForumPostDTO.getForumCategoryId()));

        forumPost.setForumCategory(forumCategory);

        ForumTopicType forumTopicType = forumTopicTypeRepository.findById(createRequestForumPostDTO.getForumTopicTypeId())
                .orElseThrow(() -> new NotFoundException("Forum topic type not found with id: " + createRequestForumPostDTO.getForumTopicTypeId()));

        forumPost.setForumTopicType(forumTopicType);

        ForumPostPicture forumPostPicture = new ForumPostPicture();
        forumPostPicture.setForumPost(forumPost);
        forumPostPicture.setUrl(createRequestForumPostDTO.getImage());
        forumPostPicture.setDeleted(false);
        forumPostPicture.setCreateAt(LocalDateTime.now());


        try {
            forumPostRepository.save(forumPost);
            forumPostPictureRepository.save(forumPostPicture);

        } catch (Exception e) {
            throw new CreateServiceException("There was something wrong when creating the blog, please try again...");
        }

        CreateResponseForumDTO createForumRespnseDTO = new CreateResponseForumDTO(forumPost.getId(), forumPost.getTitle(), forumPost.getContent(), forumPostPicture.getUrl(), forumPost.getForumTopicType(), forumPost.getForumCategory(), forumPost.getCreateAt());

        return new Response<>(201, "Forum Post created successfully!", createForumRespnseDTO);

    }


    private User checkAccount() {
        // Get the current account
        User account = accountUtils.getCurrentAccount();
        if (account == null) {
            throw new AuthAppException(ErrorCode.NOT_LOGIN);
        }

        account = userRepository.findByEmail(account.getEmail()).orElse(null);
        if (account == null) {
            throw new AuthAppException(ErrorCode.ACCOUNT_NOT_FOUND);
        }
        return account;
    }

}
