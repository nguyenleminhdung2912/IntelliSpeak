package com.gsu25se05.itellispeak.repository;

import com.gsu25se05.itellispeak.entity.ForumPostReply;
import com.gsu25se05.itellispeak.entity.ForumTopicType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ForumPostReplyRepository extends JpaRepository<ForumPostReply, Long> {
    List<ForumPostReply> findByIsDeletedFalse();
}
