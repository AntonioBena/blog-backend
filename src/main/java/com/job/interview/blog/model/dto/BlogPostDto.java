package com.job.interview.blog.model.dto;

import com.job.interview.blog.model.BlogCategory;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;

@Builder
@Getter
@Setter
@EqualsAndHashCode
@ToString //TODO remove this
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
    private Set<CommentDto> comments;
    private Set<String> likedBy;
    private UserDto postOwner;
}