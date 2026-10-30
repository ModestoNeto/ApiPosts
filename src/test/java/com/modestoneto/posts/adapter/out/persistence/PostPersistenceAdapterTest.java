package com.modestoneto.posts.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.modestoneto.posts.MySqlContainerTest;
import com.modestoneto.posts.application.common.PageRequest;
import com.modestoneto.posts.application.common.PageResult;
import com.modestoneto.posts.application.common.SortDirection;
import com.modestoneto.posts.domain.model.Post;
import com.modestoneto.posts.domain.model.User;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

// Usa o seed das migrações: posts 1 e 2 da Ana, 3 e 4 do Bruno, 5 e 6 da Carla, 2 comentários por post.
@DataJpaTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({PostPersistenceAdapter.class, PostMapper.class, UserMapper.class})
class PostPersistenceAdapterTest extends MySqlContainerTest {

    static final Instant NOW = Instant.parse("2026-06-01T10:00:00Z");
    static final PageRequest RECENTES = new PageRequest(0, 20, "createdAt", SortDirection.DESC);

    @Autowired
    PostPersistenceAdapter adapter;
    @Autowired
    SpringDataPostRepository repository;
    @Autowired
    PostMapper mapper;
    @Autowired
    EntityManager em;

    Statistics statistics;

    @BeforeEach
    void zeraEstatisticas() {
        statistics = em.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
        statistics.clear();
    }

    // Limpa o contexto de persistência para a consulta ir ao banco e as queries serem contadas.
    private void limparContexto() {
        em.flush();
        em.clear();
        statistics.clear();
    }

    @Test
    void buscaPostPorIdComAutorEComentariosEmOrdem() {
        Post post = adapter.findById(1L).orElseThrow();

        assertThat(post.title()).isEqualTo("Primeiros passos com Spring Boot");
        assertThat(post.author().name()).isEqualTo("Ana Souza");
        assertThat(post.comments()).extracting(c -> c.text())
                .containsExactly("Ótimo começo!", "Também uso o Docker para o banco.");
        assertThat(post.comments().get(0).author().name()).isEqualTo("Bruno Lima");
    }

    @Test
    void postInexistenteDevolveVazio() {
        assertThat(adapter.findById(999L)).isEmpty();
    }

    @Test
    void salvaPostNovoComAutorPorReferencia() {
        User ana = adapter.findById(1L).orElseThrow().author();

        Post saved = adapter.save(new Post(null, ana, "Novo", "Corpo", NOW, List.of()));

        assertThat(saved.id()).isNotNull();
        assertThat(adapter.findById(saved.id()).orElseThrow().author().id()).isEqualTo(1L);
    }

    @Test
    void salvarPostExistenteComComentarioNovoInsereSoOComentarioEMantemOsAntigos() {
        Post post = adapter.findById(1L).orElseThrow();
        User bruno = post.comments().get(0).author();

        Post saved = adapter.save(post.addComment(bruno, "Mais um", NOW));
        limparContexto();

        Post reloaded = adapter.findById(1L).orElseThrow();
        assertThat(saved.comments()).hasSize(3);
        assertThat(reloaded.comments()).extracting(c -> c.text())
                .containsExactly("Ótimo começo!", "Também uso o Docker para o banco.", "Mais um");
        assertThat(reloaded.comments()).allSatisfy(c -> assertThat(c.id()).isNotNull());
    }

    @Test
    void paginaPostsDoAutorOrdenadosPorTitulo() {
        PageResult<Post> first = adapter.findByAuthorId(3L, new PageRequest(0, 1, "title", SortDirection.ASC));
        PageResult<Post> second = adapter.findByAuthorId(3L, new PageRequest(1, 1, "title", SortDirection.ASC));

        assertThat(first.totalElements()).isEqualTo(2);
        assertThat(first.content()).extracting(Post::title).containsExactly("JPQL ou SQL nativo?");
        assertThat(second.content()).extracting(Post::title).containsExactly("Paginação sem dor");
        assertThat(first.content().get(0).comments()).hasSize(2);
    }

    @Test
    void autorSemPostsDevolvePaginaVazia() {
        assertThat(adapter.findByAuthorId(999L, RECENTES).content()).isEmpty();
    }

    @Test
    void buscaPorTituloIgnoraMaiusculasEAcentos() {
        assertThat(adapter.searchByTitle("SPRING", RECENTES).content()).extracting(Post::title)
                .containsExactly("Primeiros passos com Spring Boot");
        assertThat(adapter.searchByTitle("paginacao", RECENTES).content()).extracting(Post::title)
                .containsExactly("Paginação sem dor");
    }

    @Test
    void buscaPorTituloTrataCuringasComoTextoLiteral() {
        assertThat(adapter.searchByTitle("%", RECENTES).totalElements()).isZero();
        assertThat(adapter.searchByTitle("_", RECENTES).totalElements()).isZero();
    }

    @Test
    void buscaCompletaAchaTextoSoNoCorpoOuSoNoComentario() {
        assertThat(adapter.search("Initializr", null, null, RECENTES).content()).extracting(Post::id).containsExactly(1L);
        assertThat(adapter.search("Concordo", null, null, RECENTES).content()).extracting(Post::id).containsExactly(5L);
    }

    @Test
    void buscaCompletaComVariosComentariosCasandoNaoDuplicaPostNemQuebraOTotal() {
        // "o" casa nos dois comentários de cada post: com JOIN haveria 12 linhas
        PageResult<Post> result = adapter.search("o", null, null, new PageRequest(0, 100, "createdAt", SortDirection.DESC));

        assertThat(result.totalElements()).isEqualTo(6);
        assertThat(result.content()).extracting(Post::id).doesNotHaveDuplicates().hasSize(6);
    }

    @Test
    void buscaCompletaSemFiltrosComIntervaloDeDatasInclusivoNoInicio() {
        Instant from = Instant.parse("2026-02-03T00:00:00Z");
        Instant to = Instant.parse("2026-02-08T00:00:00Z");

        PageResult<Post> result = adapter.search(null, from, to, new PageRequest(0, 20, "createdAt", SortDirection.ASC));

        // posts de 02-03 (id 2), 02-05 (id 3) e 02-07 (id 4)
        assertThat(result.content()).extracting(Post::id).containsExactly(2L, 3L, 4L);
    }

    @Test
    void buscaCompletaCombinaTextoEDatas() {
        PageResult<Post> result = adapter.search("docker", null, Instant.parse("2026-02-02T00:00:00Z"), RECENTES);

        assertThat(result.content()).extracting(Post::id).containsExactly(1L);
        assertThat(adapter.search("docker", Instant.parse("2026-02-02T00:00:00Z"), null, RECENTES).content()).isEmpty();
    }

    @Test
    void contadorDeConsultasDetectaN1QuandoNaoHaEntityGraph() {
        // controle: sem fetch, 1 (posts) + 6 (comentários) + 3 (autores) = 10
        repository.findAll().forEach(mapper::toDomain);

        assertThat(statistics.getPrepareStatementCount()).isGreaterThan(5);
    }

    @Test
    void postsDoAutorSaemEmUmaUnicaConsultaSemN1() {
        limparContexto();

        PageResult<Post> page = adapter.findByAuthorId(1L, new PageRequest(0, 20, "createdAt", SortDirection.DESC));

        assertThat(page.content()).hasSize(2);
        assertThat(page.content()).allSatisfy(p -> assertThat(p.comments()).hasSize(2));
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
    }

    @Test
    void buscaCompletaCarregaAutorEComentariosSemN1() {
        limparContexto();

        PageResult<Post> page = adapter.search(null, null, null, new PageRequest(0, 20, "createdAt", SortDirection.DESC));

        assertThat(page.content()).hasSize(6);
        assertThat(page.content()).allSatisfy(p -> assertThat(p.comments()).hasSize(2));
        // 1 consulta dos dados e 1 count: não cresce com o número de posts
        assertThat(statistics.getPrepareStatementCount()).isLessThanOrEqualTo(2);
    }
}
