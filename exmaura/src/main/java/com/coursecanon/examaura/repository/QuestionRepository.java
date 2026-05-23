package com.coursecanon.examaura.repository;

import com.coursecanon.examaura.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface QuestionRepository extends JpaRepository<Question, UUID> {
    Page<Question> findByQuizId(UUID quizId, Pageable pageable);

}
