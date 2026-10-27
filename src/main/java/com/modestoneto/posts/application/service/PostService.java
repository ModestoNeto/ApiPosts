package com.modestoneto.posts.application.service;

import com.modestoneto.posts.application.port.in.CreatePostUseCase;
import com.modestoneto.posts.application.port.in.FindPostByIdUseCase;
import com.modestoneto.posts.application.port.out.PostRepositoryPort;
import com.modestoneto.posts.application.port.out.UserRepositoryPort;
import com.modestoneto.posts.domain.exception.ResourceNotFoundException;
import com.modestoneto.posts.domain.model.Post;
import com.modestoneto.posts.domain.model.User;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PostService implements CreatePostUseCase, FindPostByIdUseCase {

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
}
