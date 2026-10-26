package com.modestoneto.posts.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.modestoneto.posts.application.port.out.PostRepositoryPort;
import com.modestoneto.posts.application.port.out.UserRepositoryPort;
import com.modestoneto.posts.domain.exception.ResourceNotFoundException;
import com.modestoneto.posts.domain.model.Post;
import com.modestoneto.posts.domain.model.User;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    PostRepositoryPort posts;

    @Mock
    UserRepositoryPort users;

    @InjectMocks
    PostService service;

    @Test
    void createComAutorExistenteSalvaPostNormalizado() {
        User author = new User(1L, "Ana", "ana@exemplo.com", Instant.parse("2026-01-10T09:00:00Z"));
        when(users.findById(1L)).thenReturn(Optional.of(author));
        when(posts.save(any(Post.class))).thenAnswer(i -> i.getArgument(0));

        Post created = service.create(1L, "  Meu título ", " Corpo do post ");

        assertThat(created.author()).isEqualTo(author);
        assertThat(created.title()).isEqualTo("Meu título");
        assertThat(created.body()).isEqualTo("Corpo do post");
        assertThat(created.createdAt()).isNotNull();
    }

    @Test
    void createComAutorInexistenteLancaNotFoundENaoSalva() {
        when(users.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(999L, "t", "b"))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(posts, never()).save(any());
    }

    @Test
    void findByIdInexistenteLancaNotFound() {
        when(posts.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
