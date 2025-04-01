package com.job.interview.blog.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.job.interview.blog.exception.BlogPostNotFountException;
import com.job.interview.blog.exception.ResourceNotFoundException;
import com.job.interview.blog.model.BlogCategory;
import com.job.interview.blog.model.BlogPost;
import com.job.interview.blog.model.BlogPostComment;
import com.job.interview.blog.model.dto.BlogPostDto;
import com.job.interview.blog.model.dto.CommentDto;
import com.job.interview.blog.model.dto.response.PageResponse;
import com.job.interview.blog.model.user.UserEntity;
import com.job.interview.blog.repository.BlogPostRepository;
import com.job.interview.blog.repository.CommentsRepository;
import com.job.interview.blog.service.impl.auth.UserDetailsServiceImpl;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.*;

import static com.job.interview.blog.utils.MapUtils.mapComments;
import static com.job.interview.blog.utils.SortUtil.sortByCreatedAt;

@Log4j2
@Service
@RequiredArgsConstructor
public class BlogPostService extends FileProcessor {
    private final BlogPostRepository blogPostRepository;
    private final CommentsRepository commentsRepository;
    private final ModelMapper mapper;
    private final UserDetailsServiceImpl userDetailsService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void saveAndPublish(String blogPostJson, MultipartFile htmlFile) throws JsonProcessingException {
        var authUser = userDetailsService.getAuthenticatedUserEntity();
        validateAndSanitize(htmlFile);
        var blogPostDto = objectMapper.readValue(blogPostJson, BlogPostDto.class);
        var blogPostEntityRequest = mapper.map(blogPostDto, BlogPost.class);

        blogPostRepository.findByIdOrReturnNull(blogPostDto.getId())
                .ifPresentOrElse(
                        existingPost -> updateBlogPost(existingPost, blogPostEntityRequest, htmlFile),
                        () -> createBlogPost(blogPostEntityRequest, htmlFile, authUser)
                );
    }

    @Transactional
    private void createBlogPost(BlogPost blogPost, MultipartFile htmlFile, UserEntity user) {
        blogPost.setPostOwner(user);
        blogPost.setPublishedAt(LocalDate.now());
        var savedBlogPost = blogPostRepository.save(blogPost);

        savedBlogPost.setHtmlContentPath(saveToDisc(htmlFile, savedBlogPost.getId()));
        log.info("Created Blog Post: {}", blogPost);
        blogPostRepository.save(blogPost);
    }

    @Transactional
    private void updateBlogPost(BlogPost foundBlogPost, BlogPost request, MultipartFile htmlFile) {
        verifyUserPermission(
                request.getPostOwner(), "You can not edit someone's else blog post!");

        updateBlogPostDetails(foundBlogPost, request, saveToDisc(htmlFile, request.getId()));
        log.info("Updated Blog Post: {}", request);
        blogPostRepository.save(foundBlogPost);
    }

    private void updateBlogPostDetails(BlogPost foundBlogPost, BlogPost request, String savedHtml) {
        foundBlogPost.setHtmlContentPath(savedHtml);
        foundBlogPost.setCategory(request.getCategory());
        foundBlogPost.setShortContent(request.getShortContent());
        foundBlogPost.setShortContentImageUrl(request.getShortContentImageUrl());
        foundBlogPost.setTitle(request.getTitle());
    }

    public PageResponse<?> getAllDisplayablePosts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        Page<BlogPost> blogPosts = blogPostRepository.findAllByAuthor(getAuthenticatedUser().getId(), pageable);
        log.info("Get all posts by user: {}", blogPosts);
        return mapPostsToPageResponse(blogPosts);
    }

    public PageResponse<?> getAllDisplayablePosts(int page, int size, BlogCategory category) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        Page<BlogPost> blogPosts = blogPostRepository.findAllByCategory(category, pageable);
        log.info("Get all posts by param: {}", blogPosts);
        return mapPostsToPageResponse(blogPosts);
    }

    private PageResponse<BlogPostDto> mapPostsToPageResponse(Page<BlogPost> posts) {
        List<BlogPostDto> blogPostDtos = posts
                .stream()
                .map(p -> mapper.map(p, BlogPostDto.class))
                .toList();
        log.info("Blog posts number of total elements: {}, blog posts total elements - long {}",
                posts.getNumberOfElements(), posts.getTotalElements());
        return new PageResponse<>(
                blogPostDtos,
                posts.getNumber(),
                posts.getSize(),
                posts.getTotalElements(),
                posts.getTotalPages(),
                posts.isLast(),
                posts.isFirst()
        );
    }

    public BlogPostDto getBlogPostById(Long id) {
        var blogPost = findPostByIdOrThrow(id);
        blogPost.setComments(sortByCreatedAt(blogPost.getComments()));
        return mapper.map(blogPost, BlogPostDto.class);
    }

    public Resource downloadPostHtml(Long id) {
        var blogPost = findPostByIdOrThrow(id);

        if (blogPost.getHtmlContentPath().isEmpty()) {
            throw new ResourceNotFoundException("Content path of blog post is not found");
        }
        return downloadFile(blogPost.getHtmlContentPath());
    }

    public void deleteBlogPost(Long id) {
        var foundBlogPost = findPostByIdOrThrow(id);
        verifyUserPermission(
                foundBlogPost.getPostOwner(), "You can not delete someone's else blog post!");
        blogPostRepository.delete(foundBlogPost);
    }

    public long toggleLikePost(Long id) {
        var blogPost = findPostByIdOrThrow(id);

        Set<String> likedBy = blogPost.getLikedBy();
        var userFullName = getAuthenticatedUser().fullName();

        if (!likedBy.add(userFullName)) {
            likedBy.remove(userFullName);
        }

        blogPost.setLikeCount(likedBy.size());
        blogPostRepository.save(blogPost);
        log.info("{} Liked post with id {}", userFullName, blogPost.getId());
        return likedBy.size();
    }

    public Collection<CommentDto> commentBlogPost(CommentDto commentDto, Long id) {
        var foundPost = findPostByIdOrThrow(id);
        var comment = mapper.map(commentDto, BlogPostComment.class);

        comment.setUser(getAuthenticatedUser());
        comment.setBlogPost(foundPost);
        foundPost.getComments().add(comment);

        commentsRepository.save(comment);
        foundPost.setCommentCount(foundPost.getCommentCount() + 1);
        var saved = blogPostRepository.save(foundPost);
        log.info("New comment created for blog post with id {}", foundPost.getId());
        return sortByCreatedAt(mapComments(saved.getComments()));
    }

    private BlogPost findPostByIdOrThrow(Long id) {
        return blogPostRepository.findById(id)
                .orElseThrow(() -> new BlogPostNotFountException("Blog Post not found"));
    }

    private UserEntity getAuthenticatedUser() {
        return userDetailsService.getAuthenticatedUserEntity();
    }

    private void verifyUserPermission(UserEntity user, String message) {
        userDetailsService.verifyUserPermission(user, message);
    }
}