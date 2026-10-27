package com.modestoneto.posts.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreatePostRequest(
        @NotNull Long authorId,
        @NotBlank @Size(max = 200) String title,
        @NotBlank String body) {
}
