package com.job.interview.blog.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
        var savedHtml = saveToDisc(htmlFile, savedBlogPost.getId());

        savedBlogPost.setHtmlContentPath(savedHtml);
        log.info("Created Blog Post: {}", blogPost);
        blogPostRepository.save(blogPost);
    }

    @Transactional
    private void updateBlogPost(BlogPost foundBlogPost, BlogPost request, MultipartFile htmlFile) {
        userDetailsService.verifyUserPermission(
                request.getPostOwner(), "You can not edit someone's else blog post!");

        var savedHtml = saveToDisc(htmlFile, request.getId());

        foundBlogPost.setHtmlContentPath(savedHtml);
        foundBlogPost.setCategory(request.getCategory());
        foundBlogPost.setShortContent(request.getShortContent());
        foundBlogPost.setShortContentImageUrl(request.getShortContentImageUrl());
        foundBlogPost.setTitle(request.getTitle());

        log.info("Updated Blog Post: {}", request);
        blogPostRepository.save(foundBlogPost);
    }

    public PageResponse<?> getAllDisplayablePosts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        var authenticatedUser = userDetailsService.getAuthenticatedUserEntity();
        Page<BlogPost> blogPosts = blogPostRepository.findAllByAuthor(authenticatedUser.getId(), pageable);
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
        var sortedComments = sortByCreatedAt(blogPost.getComments());
        blogPost.setComments(sortedComments);
        return mapper.map(blogPost, BlogPostDto.class);
    }

    public Resource download(Long id) {
        var blogPost = findPostByIdOrThrow(id);

        if (blogPost.getHtmlContentPath().isEmpty()) {
            throw new RuntimeException("Content path of blog post is not found");
        }
        return downloadFile(blogPost.getHtmlContentPath());
    }

    public void deleteBlogPost(Long id) {
        var authUser = userDetailsService.getAuthenticatedUserEntity();
        var foundBlogPost = findPostByIdOrThrow(id);

        userDetailsService.verifyUserPermission(
                foundBlogPost.getPostOwner(), "You can not delete someone's else blog post!");

        blogPostRepository.delete(foundBlogPost);
    }

    public long likeUnlikeBlogPost(Long id) {
        var authUser = userDetailsService.getAuthenticatedUserEntity();
        var blogPost = blogPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog Post not found"));

        Set<String> likedBy = blogPost.getLikedBy();
        String userFullName = authUser.fullName();

        if (likedBy.contains(userFullName)) {
            likedBy.remove(userFullName);
        } else {
            likedBy.add(userFullName);
        }

        long likesCount = likedBy.size();
        blogPost.setLikeCount(likesCount);

        blogPostRepository.save(blogPost);
        return likesCount;
    }

    public Collection<CommentDto> commentBlogPost(CommentDto commentDto, Long id) {
        var foundPost = findPostByIdOrThrow(id);
        var foundComments = foundPost.getComments();

        var comment = mapper.map(commentDto, BlogPostComment.class);

        var authUser = userDetailsService.getAuthenticatedUserEntity();
        comment.setUser(authUser);

        comment.setBlogPost(foundPost);
        foundComments.add(comment);
        commentsRepository.save(comment);

        foundPost.setComments(foundComments);
        foundPost.setCommentCount(foundPost.getCommentCount() + 1);
        var saved = blogPostRepository.save(foundPost);

        var mappedComments = mapComments(saved.getComments());

        return sortByCreatedAt(mappedComments);
    }

    private BlogPost findPostByIdOrThrow(Long id){
        return blogPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog Post not found"));
    }
}