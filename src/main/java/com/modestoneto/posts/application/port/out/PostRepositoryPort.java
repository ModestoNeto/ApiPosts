package com.modestoneto.posts.application.port.out;

import com.modestoneto.posts.domain.model.Post;
import java.util.Optional;

public interface PostRepositoryPort {

    Post save(Post post);

    Optional<Post> findById(Long id);
}
