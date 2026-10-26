package com.modestoneto.posts.adapter.in.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateUserRequest(@NotBlank String name, @NotBlank @Email String email) {
}
