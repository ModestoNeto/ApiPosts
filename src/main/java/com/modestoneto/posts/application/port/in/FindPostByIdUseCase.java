package com.modestoneto.posts.application.port.in;

import com.modestoneto.posts.domain.model.Post;

public interface FindPostByIdUseCase {

    Post findById(Long id);
}
