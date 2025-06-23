package com.gsu25se05.itellispeak.repository;

import com.gsu25se05.itellispeak.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByTags_TitleContainingIgnoreCase(String tagTitle);
}
