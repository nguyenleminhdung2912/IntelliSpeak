package com.gsu25se05.itellispeak.repository;

import com.gsu25se05.itellispeak.entity.Difficulty;
import com.gsu25se05.itellispeak.entity.Question;
import com.gsu25se05.itellispeak.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByTags_TitleContainingIgnoreCase(String tagTitle);

    @Query("SELECT DISTINCT q FROM Question q JOIN q.tags t " +
            "WHERE (:tagIds IS NULL OR t.tagId IN :tagIds) " +
            "AND q.difficulty = :difficulty AND q.is_deleted = false")
    List<Question> findByTagsAndDifficultyAndIsDeletedFalse(
            @Param("tagIds") Set<Long> tagIds,
            @Param("difficulty") Difficulty difficulty
    );

    List<Question> findByCreatedBy(User createdBy);
}
