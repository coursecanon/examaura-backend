package com.coursecanon.examaura.repository;

import com.coursecanon.examaura.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, UUID> {
    List<QuizAttempt> findAllByUserIdOrderByStartedAtDesc(UUID userId);
    List<QuizAttempt> findAllByQuizId(UUID quizId);
}