package com.job.interview.blog.utils;

import com.job.interview.blog.model.BlogPostComment;
import com.job.interview.blog.model.dto.CommentDto;
import org.modelmapper.ModelMapper;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class MapUtils {
    private static final ModelMapper mapper = new ModelMapper();

    public static Collection<CommentDto> mapComments(Collection<BlogPostComment> comments) {
        Set<CommentDto> commentSet = new HashSet<>();

        comments.forEach(c -> {
            var cm = mapper.map(c, CommentDto.class);
            commentSet.add(cm);
        });
        return commentSet;
    }
}