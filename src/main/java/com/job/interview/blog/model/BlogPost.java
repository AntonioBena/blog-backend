package com.job.interview.blog.model;

import com.job.interview.blog.model.dto.BlogPostStatus;
import com.job.interview.blog.model.user.UserEntity;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "blog_post")
public class BlogPost implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "blog_post_id", updatable = false)
    private Long id;
    private String title;
    @NotEmpty(message = "Short Content is mandatory")
    @NotBlank(message = "Short Content is mandatory")
    @Column(name = "description", columnDefinition = "text")
    private String shortContent;
    private String shortContentImageUrl;
    private LocalDate publishedAt;
    @Enumerated(EnumType.STRING)
    private BlogCategory category;

    @Enumerated(EnumType.STRING)
    private BlogPostStatus blogPostStatus;

    @Column(name = "html_content_path")
    private String htmlContentPath;

    private long likeCount;
    private long commentCount;
    private long viewCount;

    @OneToMany(mappedBy = "blogPost", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Collection<BlogPostComment> comments;
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> likedBy;

    @Valid
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity postOwner;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BlogPost blogPost = (BlogPost) o;
        return likeCount == blogPost.likeCount && commentCount == blogPost.commentCount && viewCount == blogPost.viewCount && Objects.equals(id, blogPost.id) && Objects.equals(title, blogPost.title) && Objects.equals(shortContent, blogPost.shortContent) && Objects.equals(shortContentImageUrl, blogPost.shortContentImageUrl) && Objects.equals(publishedAt, blogPost.publishedAt) && category == blogPost.category && blogPostStatus == blogPost.blogPostStatus && Objects.equals(htmlContentPath, blogPost.htmlContentPath) && Objects.equals(comments, blogPost.comments) && Objects.equals(likedBy, blogPost.likedBy) && Objects.equals(postOwner, blogPost.postOwner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, shortContent, shortContentImageUrl, publishedAt, category, blogPostStatus, htmlContentPath, likeCount, commentCount, viewCount, comments, likedBy, postOwner);
    }

    @Override
    public String toString() {
        return "BlogPost{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", shortContent='" + shortContent + '\'' +
                ", shortContentImageUrl='" + shortContentImageUrl + '\'' +
                ", publishedAt=" + publishedAt +
                ", category=" + category +
                ", blogPostStatus=" + blogPostStatus +
                ", htmlContentPath='" + htmlContentPath + '\'' +
                ", likeCount=" + likeCount +
                ", commentCount=" + commentCount +
                ", viewCount=" + viewCount +
                ", comments=" + comments +
                ", likedBy=" + likedBy +
                ", postOwner=" + postOwner +
                '}';
    }
}