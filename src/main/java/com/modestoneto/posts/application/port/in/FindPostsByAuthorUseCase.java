package com.modestoneto.posts.application.port.in;

import com.modestoneto.posts.application.common.PageRequest;
import com.modestoneto.posts.application.common.PageResult;
import com.modestoneto.posts.domain.model.Post;

public interface FindPostsByAuthorUseCase {

    PageResult<Post> findByAuthor(Long authorId, PageRequest request);
}
