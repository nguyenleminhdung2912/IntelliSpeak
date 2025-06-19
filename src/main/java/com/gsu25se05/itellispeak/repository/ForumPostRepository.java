package com.gsu25se05.itellispeak.repository;

import com.gsu25se05.itellispeak.entity.ForumPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ForumPostRepository extends JpaRepository<ForumPost, Long> {
    List<ForumPost> findByIsDeletedFalse();
}
