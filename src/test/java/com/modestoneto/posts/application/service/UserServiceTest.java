package com.modestoneto.posts.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.modestoneto.posts.application.common.PageRequest;
import com.modestoneto.posts.application.common.PageResult;
import com.modestoneto.posts.application.common.SortDirection;
import com.modestoneto.posts.application.port.out.UserRepositoryPort;
import com.modestoneto.posts.domain.exception.InvalidRequestException;
import com.modestoneto.posts.domain.exception.ResourceNotFoundException;
import com.modestoneto.posts.domain.model.AuthorRanking;
import com.modestoneto.posts.domain.model.User;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepositoryPort users;

    @InjectMocks
    UserService service;

    @Test
    void createNormalizaDadosEDefineCreatedAt() {
        when(users.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User created = service.create("  Diego Alves ", "DIEGO@Exemplo.COM");

        assertThat(created.name()).isEqualTo("Diego Alves");
        assertThat(created.email()).isEqualTo("diego@exemplo.com");
        assertThat(created.createdAt()).isNotNull();
    }

    @Test
    void findByIdInexistenteLancaNotFound() {
        when(users.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updatePreservaCreatedAt() {
        Instant createdAt = Instant.parse("2026-01-10T09:00:00Z");
        when(users.findById(1L)).thenReturn(Optional.of(new User(1L, "Ana", "ana@exemplo.com", createdAt)));
        when(users.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User updated = service.update(1L, " Ana Maria ", "ANA@exemplo.com");

        assertThat(updated.id()).isEqualTo(1L);
        assertThat(updated.name()).isEqualTo("Ana Maria");
        assertThat(updated.createdAt()).isEqualTo(createdAt);
    }

    @Test
    void deleteInexistenteLancaNotFoundENaoRemove() {
        when(users.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(users, never()).deleteById(any());
    }

    @Test
    void findAllComCampoPermitidoRepassaOPedidoParaAPorta() {
        var request = new PageRequest(0, 10, "name", SortDirection.ASC);
        var page = new PageResult<User>(List.of(), 0, 0, 10);
        when(users.findAll(request)).thenReturn(page);

        assertThat(service.findAll(request)).isSameAs(page);
    }

    @Test
    void findAllComCampoNaoPermitidoLancaInvalidRequestENaoConsultaOBanco() {
        var request = new PageRequest(0, 10, "email", SortDirection.ASC);

        assertThatThrownBy(() -> service.findAll(request)).isInstanceOf(InvalidRequestException.class);
        verify(users, never()).findAll(any());
    }

    @Test
    void rankingRepassaOLimiteParaAPorta() {
        var ranking = List.of(new AuthorRanking(1L, "Ana", 2));
        when(users.ranking(5)).thenReturn(ranking);

        assertThat(service.ranking(5)).isEqualTo(ranking);
    }

    @Test
    void rankingComLimiteForaDoIntervaloLancaInvalidRequestENaoConsultaOBanco() {
        assertThatThrownBy(() -> service.ranking(0)).isInstanceOf(InvalidRequestException.class);
        assertThatThrownBy(() -> service.ranking(101)).isInstanceOf(InvalidRequestException.class);
        verify(users, never()).ranking(anyInt());
    }
}
