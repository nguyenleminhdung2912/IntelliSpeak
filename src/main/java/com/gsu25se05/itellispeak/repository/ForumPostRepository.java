package com.gsu25se05.itellispeak.repository;

import com.gsu25se05.itellispeak.entity.ForumPost;
import com.gsu25se05.itellispeak.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ForumPostRepository extends JpaRepository<ForumPost, Long> {
    List<ForumPost> findByIsDeletedFalseOrderByCreateAtDesc();

    List<ForumPost> findByUserAndIsDeletedFalseOrderByCreateAtDesc(User user);


    @Query("""
    select p
    from ForumPost p
    where (p.isDeleted is null or p.isDeleted = false)
    order by p.repliedCount desc
""")
    List<ForumPost> findTopPostsByReplyCount(Pageable pageable);

    List<ForumPost> findByForumTopicType_IdAndIsDeletedFalseOrderByCreateAtDesc(Long forumTopicTypeId);
}
