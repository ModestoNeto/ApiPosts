package com.modestoneto.posts.application.port.in;

import com.modestoneto.posts.application.common.PageRequest;
import com.modestoneto.posts.application.common.PageResult;
import com.modestoneto.posts.domain.model.Post;

public interface SearchPostsByTitleUseCase {

    PageResult<Post> searchByTitle(String title, PageRequest request);
}
