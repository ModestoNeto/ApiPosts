package com.modestoneto.posts.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.modestoneto.posts.application.common.PageRequest;
import com.modestoneto.posts.application.common.PageResult;
import com.modestoneto.posts.application.common.SortDirection;
import com.modestoneto.posts.application.port.in.CreateUserUseCase;
import com.modestoneto.posts.application.port.in.DeleteUserUseCase;
import com.modestoneto.posts.application.port.in.FindAuthorRankingUseCase;
import com.modestoneto.posts.application.port.in.FindPostsByAuthorUseCase;
import com.modestoneto.posts.application.port.in.FindUserByIdUseCase;
import com.modestoneto.posts.application.port.in.FindUsersUseCase;
import com.modestoneto.posts.application.port.in.UpdateUserUseCase;
import com.modestoneto.posts.domain.exception.ConflictException;
import com.modestoneto.posts.domain.exception.InvalidRequestException;
import com.modestoneto.posts.domain.exception.ResourceNotFoundException;
import com.modestoneto.posts.domain.model.AuthorRanking;
import com.modestoneto.posts.domain.model.Comment;
import com.modestoneto.posts.domain.model.Post;
import com.modestoneto.posts.domain.model.User;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
class UserControllerTest {

    static final Instant NOW = Instant.parse("2026-02-01T08:00:00Z");
    static final User ANA = new User(1L, "Ana Souza", "ana@exemplo.com", NOW);

    @Autowired
    MockMvc mvc;

    @MockitoBean
    FindUsersUseCase findUsers;
    @MockitoBean
    FindUserByIdUseCase findUserById;
    @MockitoBean
    CreateUserUseCase createUser;
    @MockitoBean
    UpdateUserUseCase updateUser;
    @MockitoBean
    DeleteUserUseCase deleteUser;
    @MockitoBean
    FindPostsByAuthorUseCase findPostsByAuthor;
    @MockitoBean
    FindAuthorRankingUseCase findAuthorRanking;

    @Test
    void listaUsuariosComPaginaPadraoCreatedAtDesc() throws Exception {
        when(findUsers.findAll(any())).thenReturn(new PageResult<>(List.of(ANA), 1, 0, 20));

        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Ana Souza"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));

        var captor = ArgumentCaptor.forClass(PageRequest.class);
        verify(findUsers).findAll(captor.capture());
        assertThat(captor.getValue()).isEqualTo(new PageRequest(0, 20, "createdAt", SortDirection.DESC));
    }

    @Test
    void repassaPageSizeESortDaQuery() throws Exception {
        when(findUsers.findAll(any())).thenReturn(new PageResult<>(List.of(), 0, 1, 2));

        mvc.perform(get("/users?page=1&size=2&sort=name,asc")).andExpect(status().isOk());

        verify(findUsers).findAll(new PageRequest(1, 2, "name", SortDirection.ASC));
    }

    @Test
    void parametrosDePaginacaoInvalidosRetornam400SemChamarOCasoDeUso() throws Exception {
        mvc.perform(get("/users?sort=name,up")).andExpect(status().isBadRequest());
        mvc.perform(get("/users?size=101")).andExpect(status().isBadRequest());
        mvc.perform(get("/users?page=-1")).andExpect(status().isBadRequest());
        mvc.perform(get("/users?page=abc")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Valor inválido para o parâmetro 'page'"));

        verifyNoInteractions(findUsers);
    }

    @Test
    void campoDeOrdenacaoNaoPermitidoRetorna400() throws Exception {
        when(findUsers.findAll(any())).thenThrow(new InvalidRequestException("sort inválido: 'email'"));

        mvc.perform(get("/users?sort=email"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("sort inválido: 'email'"))
                .andExpect(jsonPath("$.path").value("/users"));
    }

    @Test
    void buscaUsuarioPorId() throws Exception {
        when(findUserById.findById(1L)).thenReturn(ANA);

        mvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ana@exemplo.com"));
    }

    @Test
    void usuarioInexistenteRetorna404NoFormatoPadrao() throws Exception {
        when(findUserById.findById(9L)).thenThrow(new ResourceNotFoundException("Usuário não encontrado: 9"));

        mvc.perform(get("/users/9"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Usuário não encontrado: 9"))
                .andExpect(jsonPath("$.path").value("/users/9"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void idNaoNumericoRetorna400() throws Exception {
        mvc.perform(get("/users/abc")).andExpect(status().isBadRequest());
    }

    @Test
    void criaUsuarioRetorna201ComLocation() throws Exception {
        when(createUser.create("Ana Souza", "ana@exemplo.com")).thenReturn(ANA);

        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ana Souza\",\"email\":\"ana@exemplo.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/users/1"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void criaUsuarioInvalidoRetorna400ComOsCamposComErro() throws Exception {
        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\" \",\"email\":\"nao-e-email\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("email")))
                .andExpect(jsonPath("$.message").value(containsString("name")));

        verifyNoInteractions(createUser);
    }

    @Test
    void corpoMalformadoRetorna400() throws Exception {
        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content("{nao é json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Corpo da requisição ausente ou malformado"));
    }

    @Test
    void emailRepetidoRetorna409() throws Exception {
        when(createUser.create(any(), any())).thenThrow(new ConflictException("E-mail já cadastrado: ana@exemplo.com"));

        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ana\",\"email\":\"ana@exemplo.com\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("E-mail já cadastrado: ana@exemplo.com"));
    }

    @Test
    void atualizaUsuario() throws Exception {
        when(updateUser.update(eq(1L), any(), any())).thenReturn(ANA);

        mvc.perform(put("/users/1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ana Souza\",\"email\":\"ana@exemplo.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ana Souza"));
    }

    @Test
    void atualizaUsuarioInexistenteRetorna404() throws Exception {
        when(updateUser.update(eq(9L), any(), any())).thenThrow(new ResourceNotFoundException("Usuário não encontrado: 9"));

        mvc.perform(put("/users/9").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ana\",\"email\":\"ana@exemplo.com\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void removeUsuarioRetorna204() throws Exception {
        mvc.perform(delete("/users/1")).andExpect(status().isNoContent());

        verify(deleteUser).delete(1L);
    }

    @Test
    void removerUsuarioComPostsRetorna409() throws Exception {
        doThrow(new ConflictException("Usuário possui posts e não pode ser removido: 1")).when(deleteUser).delete(1L);

        mvc.perform(delete("/users/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Usuário possui posts e não pode ser removido: 1"));
    }

    @Test
    void listaPostsDoUsuarioComComentarios() throws Exception {
        var comment = new Comment(10L, new User(2L, "Bruno", "b@exemplo.com", NOW), "Ótimo!", NOW);
        var post = new Post(5L, ANA, "Título", "Corpo", NOW, List.of(comment));
        when(findPostsByAuthor.findByAuthor(eq(1L), any())).thenReturn(new PageResult<>(List.of(post), 1, 0, 20));

        mvc.perform(get("/users/1/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Título"))
                .andExpect(jsonPath("$.content[0].author.name").value("Ana Souza"))
                .andExpect(jsonPath("$.content[0].comments[0].text").value("Ótimo!"))
                .andExpect(jsonPath("$.content[0].comments[0].author.name").value("Bruno"));

        verify(findPostsByAuthor).findByAuthor(1L, new PageRequest(0, 20, "createdAt", SortDirection.DESC));
    }

    @Test
    void postsDeUsuarioInexistenteRetorna404() throws Exception {
        when(findPostsByAuthor.findByAuthor(eq(9L), any())).thenThrow(new ResourceNotFoundException("Usuário não encontrado: 9"));

        mvc.perform(get("/users/9/posts")).andExpect(status().isNotFound());
    }

    @Test
    void rankingUsaLimitePadraoDe10() throws Exception {
        when(findAuthorRanking.ranking(10)).thenReturn(List.of(new AuthorRanking(1L, "Ana", 3), new AuthorRanking(2L, "Zeca", 0)));

        mvc.perform(get("/users/ranking"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(1))
                .andExpect(jsonPath("$[0].postCount").value(3))
                .andExpect(jsonPath("$[1].postCount").value(0));
    }

    @Test
    void rankingComLimiteInvalidoRetorna400() throws Exception {
        when(findAuthorRanking.ranking(0)).thenThrow(new InvalidRequestException("limit deve estar entre 1 e 100"));

        mvc.perform(get("/users/ranking?limit=0")).andExpect(status().isBadRequest());
        mvc.perform(get("/users/ranking?limit=abc")).andExpect(status().isBadRequest());
    }
}
