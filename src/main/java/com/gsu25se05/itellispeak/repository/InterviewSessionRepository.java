package com.gsu25se05.itellispeak.repository;

import com.gsu25se05.itellispeak.entity.Company;
import com.gsu25se05.itellispeak.entity.InterviewSession;
import com.gsu25se05.itellispeak.entity.Question;
import com.gsu25se05.itellispeak.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewSessionRepository extends JpaRepository<InterviewSession, Long>, JpaSpecificationExecutor<InterviewSession> {
    List<InterviewSession> findByTopic_TopicIdAndIsDeletedFalse(Long topicId);
    List<InterviewSession> findAllBySourceNotOrSourceIsNullAndIsDeletedFalse(String source);

    List<InterviewSession> findByIsDeletedFalseAndCreatedBy(User createdBy);

    @EntityGraph(attributePaths = {"topic", "tags", "questions"})
    @Query("""
        SELECT i
        FROM InterviewSession i
        WHERE (i.source IS NULL OR i.source <> :excluded)
        ORDER BY i.createAt DESC
    """)
    List<InterviewSession> findAllVisibleFetchAll(@Param("excluded") String excluded);


    @EntityGraph(attributePaths = {"topic", "tags", "questions"})
    @Query("""
              select i
              from InterviewSession i
              where i.interviewSessionId = :id
                and i.isDeleted = false
                and (i.source is null or i.source <> :excluded)
            """)
    Optional<InterviewSession> findVisibleById(@Param("id") Long id, @Param("excluded") String excluded);

    @Query("""
           select distinct s
           from InterviewSession s
           join s.questions q
           where s.company = :company
             and s.isDeleted = false
             and q in :questions
           """)
    List<InterviewSession> findAllByCompanyAndQuestionsIn(
            @Param("company") Company company,
            @Param("questions") Collection<Question> questions
    );

    List<InterviewSession> findByCompanyIsNotNullAndIsDeletedFalse();

    List<InterviewSession> findByCompanyIsNullAndSourceNotOrderByCreateAtDesc(String excludedSource);

    List<InterviewSession> findByCompanyIsNullAndIsDeletedFalseAndSourceNotOrderByCreateAtDesc(String excludedSource);

    List<InterviewSession> findAllByQuestions(Question question);
}
