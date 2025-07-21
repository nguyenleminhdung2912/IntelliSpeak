package com.gsu25se05.itellispeak.repository;

import com.gsu25se05.itellispeak.entity.Difficulty;
import com.gsu25se05.itellispeak.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByTags_TitleContainingIgnoreCase(String tagTitle);

    @Query("SELECT q FROM Question q JOIN q.interviewSessions s " +
            "WHERE s.interviewSessionId = :sessionId " +
            "AND q.difficulty = :difficulty " +
            "AND (:tagIds IS NULL OR EXISTS (SELECT t FROM q.tags t WHERE t.tagId IN :tagIds))")
    List<Question> findBySessionAndDifficultyAndTags(
            @Param("sessionId") Long sessionId,
            @Param("difficulty") Difficulty difficulty,
            @Param("tagIds") Set<Long> tagIds
    );
}
