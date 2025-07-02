package com.gsu25se05.itellispeak.service;

import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.dto.reply.CreateReplyPostRequestDTO;
import com.gsu25se05.itellispeak.dto.reply.CreateReplyPostResponseDTO;
import com.gsu25se05.itellispeak.dto.reply.UpdateReplyPostRequestDTO;
import com.gsu25se05.itellispeak.dto.reply.UpdateReplyPostResponseDTO;
import com.gsu25se05.itellispeak.entity.ForumPost;
import com.gsu25se05.itellispeak.entity.ForumPostReply;
import com.gsu25se05.itellispeak.entity.User;
import com.gsu25se05.itellispeak.exception.auth.NotFoundException;
import com.gsu25se05.itellispeak.exception.service.CreateServiceException;
import com.gsu25se05.itellispeak.repository.ForumPostReplyRepository;
import com.gsu25se05.itellispeak.repository.ForumPostRepository;
import com.gsu25se05.itellispeak.utils.AccountUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ForumPostReplyService {
    @Autowired
    ForumPostReplyRepository forumPostReplyRepository;

    @Autowired
    AccountUtils accountUtils;

    @Autowired
    ForumPostRepository forumPostRepository;

    public List<ForumPostReply> getAllReplies() {
        return forumPostReplyRepository.findByIsDeletedFalse();
    }

    public ForumPostReply getReplyById(Long id) {
        return forumPostReplyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Reply not found with id: " + id));
    }

    public Response<CreateReplyPostResponseDTO> createReply(@Valid CreateReplyPostRequestDTO replyRequestDTO) {
        User account = accountUtils.getCurrentAccount();
        if (account == null) return new Response<>(401, "Please login first", null);

        ForumPostReply forumPostReply = new ForumPostReply();
        forumPostReply.setTitle(replyRequestDTO.getTitle());
        forumPostReply.setContent(replyRequestDTO.getContent());
        forumPostReply.setUser(account);
        ForumPost post = forumPostRepository.findById(replyRequestDTO.getPostId())
                .orElseThrow(() -> new NotFoundException("Post not found"));
        forumPostReply.setForumPost(post);
        forumPostReply.setCreateAt(LocalDateTime.now());
        forumPostReply.setIsDeleted(false);

        try {
            forumPostReplyRepository.save(forumPostReply);
        } catch (Exception e) {
            throw new CreateServiceException("There was something wrong when creating the forum reply post, please try again...");
        }

        CreateReplyPostResponseDTO replyResponseDTO = new CreateReplyPostResponseDTO(
                forumPostReply.getId(),
                forumPostReply.getForumPost(),
                forumPostReply.getContent(),
                forumPostReply.getTitle(),
                forumPostReply.getCreateAt()
        );

        return new Response<>(200, "Reply created successfully!", replyResponseDTO);
    }

    public Response<UpdateReplyPostResponseDTO> updateReply (Long id, @Valid UpdateReplyPostRequestDTO replyRequestDTO ) {
        User account = accountUtils.getCurrentAccount();
        if (account == null) return new Response<>(401, "Please login first", null);

        ForumPostReply reply = forumPostReplyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Reply not found with id: " + id));

        if (!reply.getUser().equals(account)) {
            return new Response<>(403, "You are not authorized to update this reply", null);
        }

        if (replyRequestDTO.getTitle() != null && !replyRequestDTO.getTitle().trim().isEmpty()) {
            reply.setTitle(replyRequestDTO.getTitle());
        }

        if (replyRequestDTO.getContent() != null && !replyRequestDTO.getContent().trim().isEmpty()) {
            reply.setContent(replyRequestDTO.getContent());
        }

        reply.setUpdateAt(LocalDateTime.now());

        try {
            forumPostReplyRepository.save(reply);
        } catch (Exception e) {
            throw new CreateServiceException("There was something wrong when updating the forum reply post, please try again...");
        }

        UpdateReplyPostResponseDTO replyResponseDTO = new UpdateReplyPostResponseDTO(
                reply.getId(),
                reply.getForumPost(),
                reply.getContent(),
                reply.getTitle(),
                reply.getUpdateAt()
        );

        return new Response<>(200, "Reply updated successfully!", replyResponseDTO);
    }



    public Response<String> deleteReply(Long id) {
        User account = accountUtils.getCurrentAccount();
        if (account == null) return new Response<>(401, "Please login first", null);
        ForumPostReply reply = getReplyById(id);
        if (reply == null) return new Response<>(404, "Forum post reply not found", null);
        if (reply.getIsDeleted()) return new Response<>(400, "Forum post reply is already deleted", null);
        reply.setIsDeleted(true);
        reply.setUpdateAt(LocalDateTime.now());
        forumPostReplyRepository.save(reply);
        return new Response<>(200, "Forum post reply deleted successfully!", "reply with ID " + id + " was soft deleted.");
    }


}
