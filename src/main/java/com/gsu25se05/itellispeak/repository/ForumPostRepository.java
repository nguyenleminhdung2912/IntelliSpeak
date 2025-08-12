package com.gsu25se05.itellispeak.repository;

import com.gsu25se05.itellispeak.entity.ForumPost;
import com.gsu25se05.itellispeak.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ForumPostRepository extends JpaRepository<ForumPost, Long> {
    List<ForumPost> findByIsDeletedFalse();

    List<ForumPost> findByUserAndIsDeletedFalse(User user);


    @Query(value = """
    SELECT fp.* 
    FROM forum_post fp
    LEFT JOIN forum_post_reply fpr ON fp.id = fpr.forum_post_id AND fpr.is_deleted = false
    WHERE fp.is_deleted = false
    GROUP BY fp.id
    ORDER BY COUNT(fpr.id) DESC
    LIMIT :limit
    """, nativeQuery = true)
    List<ForumPost> findTopPostsByReplyCount(@Param("limit") int limit);

}
