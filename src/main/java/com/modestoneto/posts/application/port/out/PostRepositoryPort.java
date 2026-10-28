package com.modestoneto.posts.application.port.out;

import com.modestoneto.posts.application.common.PageRequest;
import com.modestoneto.posts.application.common.PageResult;
import com.modestoneto.posts.domain.model.Post;
import java.time.Instant;
import java.util.Optional;

public interface PostRepositoryPort {

    Post save(Post post);

    Optional<Post> findById(Long id);

    PageResult<Post> findByAuthorId(Long authorId, PageRequest request);

    PageResult<Post> searchByTitle(String title, PageRequest request);

    PageResult<Post> search(String text, Instant from, Instant toExclusive, PageRequest request);
}
