package com.modestoneto.posts.application.port.in;

import com.modestoneto.posts.domain.model.Post;

public interface AddCommentUseCase {

    Post addComment(Long postId, Long authorId, String text);
}
