package com.modestoneto.posts.adapter.out.persistence;

import com.modestoneto.posts.application.port.out.PostRepositoryPort;
import com.modestoneto.posts.domain.model.Post;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
class PostPersistenceAdapter implements PostRepositoryPort {

    private final SpringDataPostRepository posts;
    private final SpringDataUserRepository users;
    private final PostMapper mapper;

    PostPersistenceAdapter(SpringDataPostRepository posts, SpringDataUserRepository users, PostMapper mapper) {
        this.posts = posts;
        this.users = users;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public Post save(Post post) {
        // referência sem consulta: o serviço já confirmou que o autor existe
        UserJpaEntity author = users.getReferenceById(post.author().id());
        return mapper.toDomain(posts.save(mapper.toEntity(post, author)));
    }

    @Override
    public Optional<Post> findById(Long id) {
        return posts.findById(id).map(mapper::toDomain);
    }
}
