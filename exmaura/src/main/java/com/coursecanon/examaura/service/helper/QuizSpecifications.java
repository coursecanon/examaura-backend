package com.coursecanon.examaura.service.helper;

import com.coursecanon.examaura.entity.Quiz;
import com.coursecanon.examaura.entity.enums.QuizDifficulty;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

//To make the multi paramater filter(category,, difficulty, search) clean and performant, create a helper class using JPA specifications;
//Note:  to use this specification helper, make sure existing QuizRepository extends JpaSpecificationExecutor<Quiz>
// like this public interface QuizRepository extends JpaRepository<Quiz, UUID>, org.springframework.data.jpa.repository.JpaSpecificationExecutor<Quiz>

public class QuizSpecifications {
    public static Specification<Quiz> filterQuizzes(UUID categoryId, String difficulty, String search){
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates=new ArrayList<>();

            //Always enforce showing only published quizzes to standard users
            if (categoryId != null){
                predicates.add(criteriaBuilder.equal(root.get("category").get("id"), categoryId));
            }

            if (difficulty != null && !difficulty.isBlank()){
                try {
                    QuizDifficulty diffEnum=QuizDifficulty.valueOf(difficulty.toUpperCase());
                    predicates.add(criteriaBuilder.equal(root.get("difficulty"), diffEnum));
                } catch (IllegalArgumentException e){
                    // Ignore or log invalid difficulty filter gracefully
                }
            }

            if (search !=null && !search.isBlank()){
                String searchPattern= "%" + search.toLowerCase() + "%";
                Predicate titleMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), searchPattern);
                Predicate descMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), searchPattern);
                predicates.add(criteriaBuilder.or(titleMatch, descMatch));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
