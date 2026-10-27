package com.modestoneto.posts.domain.model;

import java.time.Instant;

public record Comment(Long id, User author, String text, Instant createdAt) {
}
