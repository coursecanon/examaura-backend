package com.coursecanon.examaura.repository;

import com.coursecanon.examaura.entity.QuestionAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuestionAnswerRepository extends JpaRepository<QuestionAnswer, UUID> {
    // Useful to prevent a user from submitting an answer to the same question twice in one attempt
    Optional<QuestionAnswer> findByAttemptIdAndQuestionId(UUID attemptId, UUID questionId);
}