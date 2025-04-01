package com.job.interview.blog.model.dto;

import com.job.interview.blog.model.BlogCategory;
import lombok.*;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;

@Builder
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class BlogPostDto {
    private Long id;
    private String title;
    private String shortContent;
    private String shortContentImageUrl;
    private BlogCategory category;
    private String htmlContentPath;
    private LocalDate publishedAt;
    private long likeCount;
    private long commentCount;
    private long viewCount;
    private Collection<CommentDto> comments;
    private Set<String> likedBy;
    private UserDto postOwner;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BlogPostDto that = (BlogPostDto) o;
        return likeCount == that.likeCount && commentCount == that.commentCount && viewCount == that.viewCount && Objects.equals(id, that.id) && Objects.equals(title, that.title) && Objects.equals(shortContent, that.shortContent) && Objects.equals(shortContentImageUrl, that.shortContentImageUrl) && category == that.category && Objects.equals(htmlContentPath, that.htmlContentPath) && Objects.equals(publishedAt, that.publishedAt) && Objects.equals(comments, that.comments) && Objects.equals(likedBy, that.likedBy) && Objects.equals(postOwner, that.postOwner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, shortContent, shortContentImageUrl, category, htmlContentPath, publishedAt, likeCount, commentCount, viewCount, comments, likedBy, postOwner);
    }

    @Override
    public String toString() {
        return "BlogPostDto{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", shortContent='" + shortContent + '\'' +
                ", shortContentImageUrl='" + shortContentImageUrl + '\'' +
                ", category=" + category +
                ", htmlContentPath='" + htmlContentPath + '\'' +
                ", publishedAt=" + publishedAt +
                ", likeCount=" + likeCount +
                ", commentCount=" + commentCount +
                ", viewCount=" + viewCount +
                ", comments=" + comments +
                ", likedBy=" + likedBy +
                ", postOwner=" + postOwner +
                '}';
    }
}