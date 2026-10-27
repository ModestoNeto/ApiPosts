package com.modestoneto.posts.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public record Post(Long id, User author, String title, String body, Instant createdAt, List<Comment> comments) {

    public Post {
        comments = List.copyOf(comments);
    }

    public Post addComment(User commentAuthor, String text, Instant now) {
        var all = new ArrayList<>(comments);
        all.add(new Comment(null, commentAuthor, text.trim(), now));
        return new Post(id, author, title, body, createdAt, all);
    }
}
