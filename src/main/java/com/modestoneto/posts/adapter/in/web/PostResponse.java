package com.modestoneto.posts.adapter.in.web;

import com.modestoneto.posts.domain.model.Post;
import java.time.Instant;

public record PostResponse(Long id, AuthorSummary author, String title, String body, Instant createdAt) {

    public record AuthorSummary(Long id, String name) {
    }

    static PostResponse from(Post post) {
        return new PostResponse(post.id(), new AuthorSummary(post.author().id(), post.author().name()),
                post.title(), post.body(), post.createdAt());
    }
}
