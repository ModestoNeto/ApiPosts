package com.modestoneto.posts.adapter.in.web;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.modestoneto.posts.application.common.PageRequest;
import com.modestoneto.posts.application.common.PageResult;
import com.modestoneto.posts.application.common.SortDirection;
import com.modestoneto.posts.application.port.in.AddCommentUseCase;
import com.modestoneto.posts.application.port.in.CreatePostUseCase;
import com.modestoneto.posts.application.port.in.FindPostByIdUseCase;
import com.modestoneto.posts.application.port.in.SearchPostsByTitleUseCase;
import com.modestoneto.posts.application.port.in.SearchPostsUseCase;
import com.modestoneto.posts.domain.exception.InvalidRequestException;
import com.modestoneto.posts.domain.exception.ResourceNotFoundException;
import com.modestoneto.posts.domain.model.Comment;
import com.modestoneto.posts.domain.model.Post;
import com.modestoneto.posts.domain.model.User;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PostController.class)
class PostControllerTest {

    static final Instant NOW = Instant.parse("2026-02-01T08:00:00Z");
    static final User ANA = new User(1L, "Ana Souza", "ana@exemplo.com", NOW);
    static final User BRUNO = new User(2L, "Bruno Lima", "bruno@exemplo.com", NOW);
    static final Post POST = new Post(5L, ANA, "Título", "Corpo", NOW,
            List.of(new Comment(10L, BRUNO, "Ótimo!", NOW)));

    @Autowired
    MockMvc mvc;

    @MockitoBean
    CreatePostUseCase createPost;
    @MockitoBean
    FindPostByIdUseCase findPostById;
    @MockitoBean
    AddCommentUseCase addComment;
    @MockitoBean
    SearchPostsByTitleUseCase searchByTitle;
    @MockitoBean
    SearchPostsUseCase searchPosts;

    @Test
    void criaPostRetorna201ComLocationEAutorResumido() throws Exception {
        when(createPost.create(1L, "Título", "Corpo")).thenReturn(POST);

        mvc.perform(post("/posts").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"authorId\":1,\"title\":\"Título\",\"body\":\"Corpo\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/posts/5"))
                .andExpect(jsonPath("$.author.id").value(1))
                .andExpect(jsonPath("$.author.name").value("Ana Souza"))
                .andExpect(jsonPath("$.author.email").doesNotExist());
    }

    @Test
    void criaPostInvalidoRetorna400ComOsCamposComErro() throws Exception {
        mvc.perform(post("/posts").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\",\"body\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("authorId")))
                .andExpect(jsonPath("$.message").value(containsString("title")))
                .andExpect(jsonPath("$.message").value(containsString("body")));

        verifyNoInteractions(createPost);
    }

    @Test
    void tituloComMaisDe200CaracteresRetorna400() throws Exception {
        String longo = "a".repeat(201);

        mvc.perform(post("/posts").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"authorId\":1,\"title\":\"" + longo + "\",\"body\":\"Corpo\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("title")));
    }

    @Test
    void criaPostComAutorInexistenteRetorna404() throws Exception {
        when(createPost.create(any(), any(), any())).thenThrow(new ResourceNotFoundException("Autor não encontrado: 9"));

        mvc.perform(post("/posts").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"authorId\":9,\"title\":\"T\",\"body\":\"C\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Autor não encontrado: 9"));
    }

    @Test
    void buscaPostPorIdComComentarios() throws Exception {
        when(findPostById.findById(5L)).thenReturn(POST);

        mvc.perform(get("/posts/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Título"))
                .andExpect(jsonPath("$.comments.length()").value(1))
                .andExpect(jsonPath("$.comments[0].author.name").value("Bruno Lima"));
    }

    @Test
    void postInexistenteRetorna404() throws Exception {
        when(findPostById.findById(9L)).thenThrow(new ResourceNotFoundException("Post não encontrado: 9"));

        mvc.perform(get("/posts/9"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.path").value("/posts/9"));
    }

    @Test
    void adicionaComentarioRetorna201ComOPostInteiro() throws Exception {
        when(addComment.addComment(5L, 2L, "Ótimo!")).thenReturn(POST);

        mvc.perform(post("/posts/5/comments").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"authorId\":2,\"text\":\"Ótimo!\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.comments[0].text").value("Ótimo!"));
    }

    @Test
    void comentarioEmBrancoRetorna400() throws Exception {
        mvc.perform(post("/posts/5/comments").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"authorId\":2,\"text\":\"  \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("text")));

        verifyNoInteractions(addComment);
    }

    @Test
    void comentarioEmPostOuAutorInexistenteRetorna404() throws Exception {
        when(addComment.addComment(any(), any(), any())).thenThrow(new ResourceNotFoundException("Post não encontrado: 999"));

        mvc.perform(post("/posts/999/comments").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"authorId\":2,\"text\":\"x\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void buscaPorTituloRepassaTextoEPaginaPadrao() throws Exception {
        when(searchByTitle.searchByTitle(any(), any())).thenReturn(new PageResult<>(List.of(POST), 1, 0, 20));

        mvc.perform(get("/posts/search/title?title=spring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Título"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(searchByTitle).searchByTitle("spring", new PageRequest(0, 20, "createdAt", SortDirection.DESC));
    }

    @Test
    void buscaPorTituloSemOParametroRetorna400() throws Exception {
        mvc.perform(get("/posts/search/title"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Parâmetro obrigatório ausente: 'title'"));

        verifyNoInteractions(searchByTitle);
    }

    @Test
    void buscaPorTituloEmBrancoRetorna400() throws Exception {
        when(searchByTitle.searchByTitle(any(), any())).thenThrow(new InvalidRequestException("title não pode estar em branco"));

        mvc.perform(get("/posts/search/title?title=%20")).andExpect(status().isBadRequest());
    }

    @Test
    void buscaCompletaConverteAsDatasIso() throws Exception {
        when(searchPosts.search(any(), any(), any(), any())).thenReturn(new PageResult<>(List.of(POST), 1, 0, 20));

        mvc.perform(get("/posts/search?text=docker&minDate=2026-02-01&maxDate=2026-02-28"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(5));

        verify(searchPosts).search("docker", LocalDate.parse("2026-02-01"), LocalDate.parse("2026-02-28"),
                new PageRequest(0, 20, "createdAt", SortDirection.DESC));
    }

    @Test
    void buscaCompletaSemFiltrosPassaNulos() throws Exception {
        when(searchPosts.search(any(), any(), any(), any())).thenReturn(new PageResult<>(List.of(), 0, 0, 20));

        mvc.perform(get("/posts/search")).andExpect(status().isOk());

        verify(searchPosts).search(null, null, null, new PageRequest(0, 20, "createdAt", SortDirection.DESC));
    }

    @Test
    void buscaCompletaComDataInvalidaRetorna400() throws Exception {
        mvc.perform(get("/posts/search?minDate=01/02/2026")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Valor inválido para o parâmetro 'minDate'"));
        mvc.perform(get("/posts/search?maxDate=2026-02-30")).andExpect(status().isBadRequest());

        verifyNoInteractions(searchPosts);
    }

    @Test
    void buscaCompletaComMinDateDepoisDeMaxDateRetorna400() throws Exception {
        when(searchPosts.search(any(), any(), any(), any()))
                .thenThrow(new InvalidRequestException("minDate não pode ser posterior a maxDate"));

        mvc.perform(get("/posts/search?minDate=2026-03-01&maxDate=2026-02-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("minDate não pode ser posterior a maxDate"));
    }

    @Test
    void rotaLiteralSearchNaoCaiEmPostsPorId() throws Exception {
        when(searchPosts.search(any(), any(), any(), any())).thenReturn(new PageResult<>(List.of(), 0, 0, 20));

        mvc.perform(get("/posts/search")).andExpect(status().isOk());

        verifyNoInteractions(findPostById);
    }
}
