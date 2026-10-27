package com.modestoneto.posts.domain.model;

import java.time.Instant;

public record Post(Long id, User author, String title, String body, Instant createdAt) {
}
