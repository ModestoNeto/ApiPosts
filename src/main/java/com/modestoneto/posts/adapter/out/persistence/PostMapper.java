package com.modestoneto.posts.adapter.out.persistence;

import com.modestoneto.posts.domain.model.Comment;
import com.modestoneto.posts.domain.model.Post;
import java.util.function.Function;
import org.springframework.stereotype.Component;

@Component
class PostMapper {

    private final UserMapper userMapper;

    PostMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    // Lê autor e comentários LAZY: só pode ser chamado dentro da transação do adaptador.
    Post toDomain(PostJpaEntity entity) {
        var comments = entity.getComments().stream()
                .map(c -> new Comment(c.getId(), userMapper.toDomain(c.getAuthor()), c.getText(), c.getCreatedAt()))
                .toList();
        return new Post(entity.getId(), userMapper.toDomain(entity.getAuthor()),
                entity.getTitle(), entity.getBody(), entity.getCreatedAt(), comments);
    }

    // authorRef devolve uma referência (sem SELECT) para o id do autor.
    PostJpaEntity toEntity(Post post, Function<Long, UserJpaEntity> authorRef) {
        var entity = new PostJpaEntity(post.id(), authorRef.apply(post.author().id()),
                post.title(), post.body(), post.createdAt());
        post.comments().forEach(c -> entity.getComments().add(
                new CommentJpaEntity(c.id(), entity, authorRef.apply(c.author().id()), c.text(), c.createdAt())));
        return entity;
    }
}
