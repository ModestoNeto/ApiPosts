package com.modestoneto.posts.adapter.out.persistence;

import com.modestoneto.posts.application.common.PageRequest;
import com.modestoneto.posts.application.common.PageResult;
import com.modestoneto.posts.application.port.out.PostRepositoryPort;
import com.modestoneto.posts.domain.model.Post;
import java.time.Instant;
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
        // referências sem consulta: o serviço já confirmou que os autores existem
        return mapper.toDomain(posts.save(mapper.toEntity(post, users::getReferenceById)));
    }

    @Override
    public Optional<Post> findById(Long id) {
        return posts.findById(id).map(mapper::toDomain);
    }

    @Override
    public PageResult<Post> findByAuthorId(Long authorId, PageRequest request) {
        return PageConversions.toResult(posts.findByAuthorId(authorId, PageConversions.toPageable(request)), mapper::toDomain);
    }

    @Override
    public PageResult<Post> searchByTitle(String title, PageRequest request) {
        return PageConversions.toResult(
                posts.findByTitleContainingIgnoreCase(title, PageConversions.toPageable(request)), mapper::toDomain);
    }

    @Override
    public PageResult<Post> search(String text, Instant from, Instant toExclusive, PageRequest request) {
        // escapa \, % e _ para o texto do usuário valer literalmente no LIKE
        String pattern = text == null ? null : "%" + text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
        return PageConversions.toResult(
                posts.search(pattern, from, toExclusive, PageConversions.toPageable(request)), mapper::toDomain);
    }
}
