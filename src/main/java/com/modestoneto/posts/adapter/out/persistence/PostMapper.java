package com.modestoneto.posts.adapter.out.persistence;

import com.modestoneto.posts.domain.model.Post;
import org.springframework.stereotype.Component;

@Component
class PostMapper {

    private final UserMapper userMapper;

    PostMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    // Lê o autor LAZY: só pode ser chamado dentro da transação do adaptador.
    Post toDomain(PostJpaEntity entity) {
        return new Post(entity.getId(), userMapper.toDomain(entity.getAuthor()),
                entity.getTitle(), entity.getBody(), entity.getCreatedAt());
    }

    PostJpaEntity toEntity(Post post, UserJpaEntity author) {
        return new PostJpaEntity(post.id(), author, post.title(), post.body(), post.createdAt());
    }
}
