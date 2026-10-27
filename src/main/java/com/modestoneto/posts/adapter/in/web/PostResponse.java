package com.modestoneto.posts.adapter.in.web;

import com.modestoneto.posts.domain.model.Post;
import java.time.Instant;
import java.util.List;

public record PostResponse(Long id, AuthorSummary author, String title, String body, Instant createdAt,
                           List<CommentResponse> comments) {

    public record AuthorSummary(Long id, String name) {
    }

    public record CommentResponse(Long id, AuthorSummary author, String text, Instant createdAt) {
    }

    static PostResponse from(Post post) {
        var comments = post.comments().stream()
                .map(c -> new CommentResponse(c.id(), new AuthorSummary(c.author().id(), c.author().name()),
                        c.text(), c.createdAt()))
                .toList();
        return new PostResponse(post.id(), new AuthorSummary(post.author().id(), post.author().name()),
                post.title(), post.body(), post.createdAt(), comments);
    }
}
