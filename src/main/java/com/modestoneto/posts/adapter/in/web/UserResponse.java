package com.modestoneto.posts.adapter.in.web;

import com.modestoneto.posts.domain.model.User;
import java.time.Instant;

public record UserResponse(Long id, String name, String email, Instant createdAt) {

    static UserResponse from(User user) {
        return new UserResponse(user.id(), user.name(), user.email(), user.createdAt());
    }
}
