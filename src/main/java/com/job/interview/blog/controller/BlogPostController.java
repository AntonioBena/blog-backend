package com.job.interview.blog.controller;

import com.job.interview.blog.model.BlogCategory;
import com.job.interview.blog.model.dto.CommentDto;
import com.job.interview.blog.service.impl.BlogPostService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

@Log4j2
@Tag(name = "Blog post")
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "blog")
@SecurityRequirement(name = "bearerAuth")
public class BlogPostController {

    private final BlogPostService blogPostService;

    @PostMapping
    public ResponseEntity<?> uploadAndPublishBlogPost(@RequestPart("blogPost") String blogPostJson,
                                                      @RequestPart("htmlContent") MultipartFile htmlFile) throws IOException {
        blogPostService.saveAndPublish(blogPostJson, htmlFile);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllBlogPosts(
            @RequestParam(name = "category") BlogCategory category,
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size){
        return ResponseEntity.ok(blogPostService.getAllDisplayablePosts(page, size, category));
    }

    @GetMapping("/all/author")
    public ResponseEntity<?> getAllBlogPosts(
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size){
        return ResponseEntity.ok(blogPostService.getAllDisplayablePosts(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBlogPost(@PathVariable Long id) {
        var blogPost = blogPostService.getBlogPostById(id);
        return ResponseEntity.ok(blogPost);
    }

    @GetMapping(value = "/{id}/getHtmlContent", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<?> downloadFile(@PathVariable(value = "id") Long id){
        Resource file = blogPostService.download(id);
        if(file == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(file);
        }

    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteBlogPostById(@PathVariable(value = "id") Long id) {
        blogPostService.deleteBlogPost(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/like")
    public ResponseEntity<?> likeUnlikeBlogPost(@RequestParam(value = "id") Long id) {
        var likeCount = blogPostService.likeUnlikeBlogPost(id);
        return ResponseEntity.ok(likeCount);
    }

    @PostMapping("/comment")
    public ResponseEntity<?> commentBlogPost(@RequestParam(value = "id") Long id, @RequestBody CommentDto comment) {
        return ResponseEntity.ok(blogPostService.commentBlogPost(comment, id));
    }
}