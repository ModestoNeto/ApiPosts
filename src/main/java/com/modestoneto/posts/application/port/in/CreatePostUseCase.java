package com.modestoneto.posts.application.port.in;

import com.modestoneto.posts.domain.model.Post;

public interface CreatePostUseCase {

    Post create(Long authorId, String title, String body);
}
