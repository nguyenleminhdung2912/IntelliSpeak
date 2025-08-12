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
                .orElseThrow(() -> new NotFoundException("Không tìm thấy phản hồi với ID: " + id));
    }

    public Response<CreateReplyPostResponseDTO> createReply(@Valid CreateReplyPostRequestDTO replyRequestDTO) {
        User account = accountUtils.getCurrentAccount();
        if (account == null) return new Response<>(401, "Vui lòng đăng nhập để tiếp tục", null);


        ForumPostReply forumPostReply = new ForumPostReply();
        forumPostReply.setTitle(replyRequestDTO.getTitle());
        forumPostReply.setContent(replyRequestDTO.getContent());
        forumPostReply.setUser(account);
        ForumPost post = forumPostRepository.findById(replyRequestDTO.getPostId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy bài viết"));
        forumPostReply.setForumPost(post);
        forumPostReply.setCreateAt(LocalDateTime.now());
        forumPostReply.setIsDeleted(false);

        try {
            forumPostReplyRepository.save(forumPostReply);
        } catch (Exception e) {
            throw new CreateServiceException("Đã xảy ra lỗi khi tạo phản hồi bài viết, vui lòng thử lại...");
        }

        CreateReplyPostResponseDTO replyResponseDTO = new CreateReplyPostResponseDTO(
                forumPostReply.getId(),
                forumPostReply.getForumPost(),
                forumPostReply.getContent(),
                forumPostReply.getTitle(),
                forumPostReply.getCreateAt()
        );

        return new Response<>(200, "Tạo phản hồi thành công!", replyResponseDTO);
    }

    public Response<UpdateReplyPostResponseDTO> updateReply (Long id, @Valid UpdateReplyPostRequestDTO replyRequestDTO ) {
        User account = accountUtils.getCurrentAccount();
        if (account == null) return new Response<>(401, "Vui lòng đăng nhập để tiếp tục", null);

        ForumPostReply reply = forumPostReplyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy phản hồi với ID: " + id));


        if (!reply.getUser().equals(account)) {
            return new Response<>(403, "Bạn không có quyền cập nhật phản hồi này", null);
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
            throw new CreateServiceException("Đã xảy ra lỗi khi cập nhật bài phản hồi, vui lòng thử lại...");
        }

        UpdateReplyPostResponseDTO replyResponseDTO = new UpdateReplyPostResponseDTO(
                reply.getId(),
                reply.getForumPost(),
                reply.getContent(),
                reply.getTitle(),
                reply.getUpdateAt()
        );

        return new Response<>(200, "Cập nhật phản hồi thành công!", replyResponseDTO);

    }



    public Response<String> deleteReply(Long id) {
        User account = accountUtils.getCurrentAccount();
        if (account == null) return new Response<>(401, "Vui lòng đăng nhập để tiếp tục", null);

        ForumPostReply reply = getReplyById(id);
        if (reply == null) return new Response<>(404, "Không tìm thấy phản hồi bài viết", null);

        if (reply.getIsDeleted()) return new Response<>(400, "Phản hồi bài viết đã bị xóa trước đó", null);

        reply.setIsDeleted(true);
        reply.setUpdateAt(LocalDateTime.now());
        forumPostReplyRepository.save(reply);

        return new Response<>(200, "Xóa phản hồi bài viết thành công", "Phản hồi có ID " + id + " đã được xóa mềm.");
    }



}
