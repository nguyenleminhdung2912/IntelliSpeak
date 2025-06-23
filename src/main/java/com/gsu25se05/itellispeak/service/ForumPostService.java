package com.gsu25se05.itellispeak.service;


import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.dto.forum.CreateRequestForumPostDTO;
import com.gsu25se05.itellispeak.dto.forum.CreateResponseForumDTO;
import com.gsu25se05.itellispeak.entity.ForumPost;
import com.gsu25se05.itellispeak.entity.ForumPostPicture;
import com.gsu25se05.itellispeak.entity.User;
import com.gsu25se05.itellispeak.exception.ErrorCode;
import com.gsu25se05.itellispeak.exception.auth.AuthAppException;
import com.gsu25se05.itellispeak.exception.service.CreateServiceException;
import com.gsu25se05.itellispeak.repository.ForumPostPictureRepository;
import com.gsu25se05.itellispeak.repository.ForumPostRepository;
import com.gsu25se05.itellispeak.repository.UserRepository;
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

    public ForumPostService(AccountUtils accountUtils, UserRepository userRepository, ForumPostRepository forumPostRepository, ForumPostPictureRepository forumPostPictureRepository) {
        this.accountUtils = accountUtils;
        this.userRepository = userRepository;
        this.forumPostRepository = forumPostRepository;
        this.forumPostPictureRepository = forumPostPictureRepository;
    }

    public List<ForumPost> getAllPosts() {
        return forumPostRepository.findByIsDeletedFalse();
    }

    public Response<CreateResponseForumDTO> createForumPost (@Valid CreateRequestForumPostDTO createRequestForumPostDTO){

        User user = checkAccount();

        ForumPost forumPost = new ForumPost();
        forumPost.setUser(user);
        forumPost.setTitle(createRequestForumPostDTO.getTitle());
        forumPost.setContent(createRequestForumPostDTO.getContent());
        forumPost.setCreateAt(LocalDateTime.now());
        forumPost.setIsDeleted(false);
        forumPost.setForumCategory(createRequestForumPostDTO.getForumCategory());
        forumPost.setForumTopicType(createRequestForumPostDTO.getForumTopicType());
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
