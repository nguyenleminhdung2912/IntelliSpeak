package com.gsu25se05.itellispeak.repository;

import com.gsu25se05.itellispeak.entity.ForumPost;
import com.gsu25se05.itellispeak.entity.SavedPost;
import com.gsu25se05.itellispeak.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedPostRepository extends JpaRepository<SavedPost, Long> {
    // Kiểm tra xem bài viết đã được người dùng lưu chưa
    Optional<SavedPost> findByUserAndForumPost(User user, ForumPost forumPost);

    // Lấy toàn bộ bài viết đã lưu của người dùng
    List<SavedPost> findByUser(User user);

    List<SavedPost> findByUserAndIsDeletedFalseOrderBySavedAtDesc(User user);

    // SavedPostRepository.java
    @Query("select sp.forumPost.id from SavedPost sp where sp.user = :user and sp.isDeleted = false")
    List<Long> findActiveSavedPostIdsByUser(@Param("user") User user);



    //  Xóa bài viết đã lưu
    void deleteByUserAndForumPost(User user, ForumPost forumPost);
}
