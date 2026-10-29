package com.modestoneto.posts.application.service;

import com.modestoneto.posts.application.common.PageRequest;
import com.modestoneto.posts.application.common.PageResult;
import com.modestoneto.posts.application.port.in.AddCommentUseCase;
import com.modestoneto.posts.application.port.in.CreatePostUseCase;
import com.modestoneto.posts.application.port.in.FindPostByIdUseCase;
import com.modestoneto.posts.application.port.in.FindPostsByAuthorUseCase;
import com.modestoneto.posts.application.port.in.SearchPostsByTitleUseCase;
import com.modestoneto.posts.application.port.in.SearchPostsUseCase;
import com.modestoneto.posts.application.port.out.PostRepositoryPort;
import com.modestoneto.posts.application.port.out.UserRepositoryPort;
import com.modestoneto.posts.domain.exception.InvalidRequestException;
import com.modestoneto.posts.domain.exception.ResourceNotFoundException;
import com.modestoneto.posts.domain.model.Post;
import com.modestoneto.posts.domain.model.User;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PostService implements CreatePostUseCase, FindPostByIdUseCase, AddCommentUseCase,
        FindPostsByAuthorUseCase, SearchPostsByTitleUseCase, SearchPostsUseCase {

    static final Set<String> SORT_FIELDS = Set.of("title", "createdAt");

    private final PostRepositoryPort posts;
    private final UserRepositoryPort users;

    public PostService(PostRepositoryPort posts, UserRepositoryPort users) {
        this.posts = posts;
        this.users = users;
    }

    @Override
    @Transactional
    public Post create(Long authorId, String title, String body) {
        User author = users.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("Autor não encontrado: " + authorId));
        return posts.save(new Post(null, author, title.trim(), body.trim(), Instant.now(), List.of()));
    }

    @Override
    public Post findById(Long id) {
        return posts.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post não encontrado: " + id));
    }

    @Override
    @Transactional
    public Post addComment(Long postId, Long authorId, String text) {
        Post post = findById(postId);
        User author = users.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("Autor não encontrado: " + authorId));
        return posts.save(post.addComment(author, text, Instant.now()));
    }

    @Override
    public PageResult<Post> findByAuthor(Long authorId, PageRequest request) {
        request.requireSortIn(SORT_FIELDS);
        users.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + authorId));
        return posts.findByAuthorId(authorId, request);
    }

    @Override
    public PageResult<Post> searchByTitle(String title, PageRequest request) {
        request.requireSortIn(SORT_FIELDS);
        if (title.isBlank()) {
            throw new InvalidRequestException("title não pode estar em branco");
        }
        return posts.searchByTitle(title.trim(), request);
    }

    @Override
    public PageResult<Post> search(String text, LocalDate minDate, LocalDate maxDate, PageRequest request) {
        request.requireSortIn(SORT_FIELDS);
        if (minDate != null && maxDate != null && minDate.isAfter(maxDate)) {
            throw new InvalidRequestException("minDate não pode ser posterior a maxDate");
        }
        String filter = text == null || text.isBlank() ? null : text.trim();
        Instant from = minDate == null ? null : minDate.atStartOfDay(ZoneOffset.UTC).toInstant();
        // limite superior exclusivo no início do dia seguinte: inclui o dia de maxDate inteiro
        Instant toExclusive = maxDate == null ? null : maxDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        return posts.search(filter, from, toExclusive, request);
    }
}
