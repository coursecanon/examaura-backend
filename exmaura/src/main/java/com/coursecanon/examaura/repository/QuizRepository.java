package com.coursecanon.examaura.repository;

import com.coursecanon.examaura.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, UUID>, org.springframework.data.jpa.repository.JpaSpecificationExecutor<Quiz> {
    //fast lookup using the unique database index on slug
    Optional<Quiz> findBySlug(String slug);

    //fetch published quizzes for standard users
    List<Quiz> findByIsPublishedTrue();

    //Fetch featured quizzes for landing page components
    List<Quiz> findByIsFeaturedTrueAndIsPublishedTrue();

    //Fetch quizzes belonging to a specific category slug

    @Query("SELECT q FROM Quiz q WHERE q.category.slug = :categorySlug AND q.isPublished = true")
    List<Quiz> findByCategorySlug(@Param("categorySlug") String categorySlug);

    // Track historical metrics or list creator-owned content
    List<Quiz> findByCreatorId(UUID creatorId);

    // 🚀 NEW: Fetch top 10 latest quizzes created by specific user
    List<Quiz> findTop10ByCreatorIdOrderByCreatedAtDesc(UUID creatorId);
}
