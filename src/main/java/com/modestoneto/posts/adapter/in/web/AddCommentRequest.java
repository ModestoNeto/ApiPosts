package com.modestoneto.posts.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddCommentRequest(@NotNull Long authorId, @NotBlank String text) {
}
