package com.job.interview.blog.repository;

import com.job.interview.blog.model.BlogCategory;
import com.job.interview.blog.model.BlogPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {

    @Query("SELECT b FROM BlogPost b WHERE (:id IS NOT NULL AND b.id = :id)")
    Optional<BlogPost> findByIdOrReturnNull(@Param("id") Long id);
    @Query("SELECT b FROM BlogPost b WHERE (b.category = :category)")
    Page<BlogPost> findAllByCategory(@Param("category") BlogCategory category, Pageable pageable);

    @Query("SELECT b FROM BlogPost b WHERE b.postOwner.id = :userId")
    Page<BlogPost> findAllByAuthor(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COUNT(b) FROM BlogPost b WHERE YEAR(b.publishedAt) = :year AND b.postOwner.id = :userId")
    long countPostsByYear(
            @Param("year") int year,
            @Param("userId") Long userId
    );

    @Query("SELECT MONTH(b.publishedAt) AS month, COUNT(b) AS postCount " +
            "FROM BlogPost b WHERE YEAR(b.publishedAt) = :year AND b.postOwner.id = :userId " +
            "GROUP BY MONTH(b.publishedAt) ORDER BY MONTH(b.publishedAt)")
    List<Object[]> countPostsPerMonth(@Param("year") int year, @Param("userId") Long userId);
}