package com.modestoneto.posts.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.modestoneto.posts.application.common.PageRequest;
import com.modestoneto.posts.application.common.PageResult;
import com.modestoneto.posts.application.common.SortDirection;
import com.modestoneto.posts.application.port.out.PostRepositoryPort;
import com.modestoneto.posts.application.port.out.UserRepositoryPort;
import com.modestoneto.posts.domain.exception.InvalidRequestException;
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

    @Test
    void addCommentSalvaPostComComentarioNovo() {
        User author = new User(2L, "Bruno", "bruno@exemplo.com", Instant.parse("2026-01-10T09:00:00Z"));
        Post post = new Post(1L, author, "t", "b", Instant.now(), java.util.List.of());
        when(posts.findById(1L)).thenReturn(Optional.of(post));
        when(users.findById(2L)).thenReturn(Optional.of(author));
        when(posts.save(any(Post.class))).thenAnswer(i -> i.getArgument(0));

        Post saved = service.addComment(1L, 2L, " oi ");

        assertThat(saved.comments()).hasSize(1);
        assertThat(saved.comments().get(0).text()).isEqualTo("oi");
        assertThat(saved.comments().get(0).author()).isEqualTo(author);
    }

    @Test
    void addCommentComPostInexistenteLancaNotFoundENaoSalva() {
        when(posts.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addComment(999L, 1L, "x"))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(posts, never()).save(any());
    }

    @Test
    void addCommentComAutorInexistenteLancaNotFoundENaoSalva() {
        User author = new User(1L, "Ana", "ana@exemplo.com", Instant.now());
        when(posts.findById(1L)).thenReturn(Optional.of(new Post(1L, author, "t", "b", Instant.now(), java.util.List.of())));
        when(users.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addComment(1L, 999L, "x"))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(posts, never()).save(any());
    }

    @Test
    void findByAuthorRepassaOPedidoParaAPorta() {
        User author = new User(1L, "Ana", "ana@exemplo.com", Instant.now());
        var request = new PageRequest(0, 10, "createdAt", SortDirection.DESC);
        var page = new PageResult<Post>(java.util.List.of(), 0, 0, 10);
        when(users.findById(1L)).thenReturn(Optional.of(author));
        when(posts.findByAuthorId(1L, request)).thenReturn(page);

        assertThat(service.findByAuthor(1L, request)).isSameAs(page);
    }

    @Test
    void findByAuthorComUsuarioInexistenteLancaNotFoundENaoConsultaPosts() {
        when(users.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByAuthor(999L, new PageRequest(0, 10, "createdAt", SortDirection.DESC)))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(posts, never()).findByAuthorId(any(), any());
    }

    @Test
    void findByAuthorComCampoNaoPermitidoLancaInvalidRequest() {
        assertThatThrownBy(() -> service.findByAuthor(1L, new PageRequest(0, 10, "body", SortDirection.ASC)))
                .isInstanceOf(InvalidRequestException.class);
        verify(posts, never()).findByAuthorId(any(), any());
    }
}
