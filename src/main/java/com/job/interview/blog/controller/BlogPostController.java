package com.job.interview.blog.controller;

import com.job.interview.blog.model.BlogCategory;
import com.job.interview.blog.model.dto.BlogPostDto;
import com.job.interview.blog.model.dto.CommentDto;
import com.job.interview.blog.model.dto.response.PageResponse;
import com.job.interview.blog.service.impl.BlogPostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Collection;
import java.util.Optional;

@Log4j2
@Tag(name = "Blog post")
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "blog")
@SecurityRequirement(name = "bearerAuth")
public class BlogPostController {

    private final BlogPostService blogPostService;

    @PreAuthorize("hasRole('WRITER')")
    @Operation(
            description = "Endpoint for creating and publishing post requests",
            summary = "Creates and publish blog post"
    )
    @PostMapping
    public ResponseEntity<HttpStatus> uploadAndPublishBlogPost(@RequestPart("blogPost") String blogPostJson,
                                                      @RequestPart("htmlContent") MultipartFile htmlFile) throws IOException {
        blogPostService.saveAndPublish(blogPostJson, htmlFile);
        return ResponseEntity.accepted().build();
    }

    @Operation(
            description = "Endpoint for getting all blog posts for authenticated user",
            summary = "Gets all blog posts, optionally filtered by category or author"
    )
    @PreAuthorize("hasAnyRole('READER','WRITER')")
    @GetMapping("/all")
    public ResponseEntity<PageResponse<BlogPostDto>> getAllBlogPostsByFilter(
            @RequestParam(name = "category", required = false) BlogCategory category,
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size) {
        return ResponseEntity
                .ok(blogPostService.getAllDisplayablePosts(page, size, Optional.ofNullable(category)));
    }

    @Operation(
            description = "Endpoint for getting blog post by id",
            summary = "Gets blog post by id"
    )
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('READER','WRITER')")
    public ResponseEntity<BlogPostDto> getBlogPost(@PathVariable Long id) {
        return ResponseEntity
                .ok(blogPostService.getBlogPostById(id));
    }

    @Operation(
            description = "Endpoint for getting html content for blog post id",
            summary = "Gets html content by blog post id"
    )
    @PreAuthorize("hasAnyRole('READER','WRITER')")
    @GetMapping(value = "/{id}/getHtmlContent", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> downloadFile(@PathVariable(value = "id") Long id){
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(blogPostService.downloadPostHtml(id));
    }

    @Operation(
            description = "Endpoint deleting blog post by id for authenticated user",
            summary = "Deletes blog post by id"
    )
    @PreAuthorize("hasRole('WRITER')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<HttpStatus> deleteBlogPostById(@PathVariable(value = "id") Long id) {
        blogPostService.deleteBlogPost(id);
        return ResponseEntity.ok().build();
    }

    @Operation(
            description = "Endpoint for like post",
            summary = "Likes blog post"
    )
    @PreAuthorize("hasAnyRole('READER','WRITER')")
    @PostMapping("/like")
    public ResponseEntity<Long> likeUnlikeBlogPost(@RequestParam(value = "id") Long id) {
        return ResponseEntity
                .ok(blogPostService.toggleLikePost(id));
    }

    @Operation(
            description = "Endpoint adding comments to blog post",
            summary = "Creates new comment for blog post"
    )
    @PreAuthorize("hasAnyRole('READER','WRITER')")
    @PostMapping("/comment")
    public ResponseEntity<Collection<CommentDto>> commentBlogPost(@RequestParam(value = "id") Long id, @RequestBody CommentDto comment) {
        return ResponseEntity
                .ok(blogPostService.commentBlogPost(comment, id));
    }
}