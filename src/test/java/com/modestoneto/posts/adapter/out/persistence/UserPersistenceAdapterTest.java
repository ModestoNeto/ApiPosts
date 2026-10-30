package com.modestoneto.posts.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.modestoneto.posts.MySqlContainerTest;
import com.modestoneto.posts.application.common.PageRequest;
import com.modestoneto.posts.application.common.PageResult;
import com.modestoneto.posts.application.common.SortDirection;
import com.modestoneto.posts.domain.exception.ConflictException;
import com.modestoneto.posts.domain.model.AuthorRanking;
import com.modestoneto.posts.domain.model.User;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

// Usa o seed das migrações: 3 usuários (Ana, Bruno e Carla), 2 posts cada.
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({UserPersistenceAdapter.class, UserMapper.class})
class UserPersistenceAdapterTest extends MySqlContainerTest {

    static final Instant NOW = Instant.parse("2026-06-01T10:00:00Z");

    @Autowired
    UserPersistenceAdapter adapter;

    @Test
    void salvaUsuarioNovoEGeraId() {
        User saved = adapter.save(new User(null, "Dora", "dora@exemplo.com", NOW));

        assertThat(saved.id()).isNotNull();
        assertThat(adapter.findById(saved.id())).contains(saved);
    }

    @Test
    void emailRepetidoViolaOIndiceUnicoELancaConflict() {
        assertThatThrownBy(() -> adapter.save(new User(null, "Outra Ana", "ana@exemplo.com", NOW)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("ana@exemplo.com");
    }

    @Test
    void atualizarMantendoOProprioEmailNaoEhConflito() {
        User ana = adapter.findById(1L).orElseThrow();

        User updated = adapter.save(new User(ana.id(), "Ana Maria", ana.email(), ana.createdAt()));

        assertThat(updated.name()).isEqualTo("Ana Maria");
    }

    @Test
    void removerUsuarioComPostsViolaAChaveEstrangeiraELancaConflict() {
        assertThatThrownBy(() -> adapter.deleteById(1L)).isInstanceOf(ConflictException.class);
    }

    @Test
    void removeUsuarioSemPosts() {
        User dora = adapter.save(new User(null, "Dora", "dora@exemplo.com", NOW));

        adapter.deleteById(dora.id());

        assertThat(adapter.findById(dora.id())).isEmpty();
    }

    @Test
    void paginaUsuariosOrdenadosPorNome() {
        PageResult<User> first = adapter.findAll(new PageRequest(0, 2, "name", SortDirection.ASC));
        PageResult<User> second = adapter.findAll(new PageRequest(1, 2, "name", SortDirection.ASC));

        assertThat(first.totalElements()).isEqualTo(3);
        assertThat(first.content()).extracting(User::name).containsExactly("Ana Souza", "Bruno Lima");
        assertThat(second.content()).extracting(User::name).containsExactly("Carla Mendes");
    }

    @Test
    void ordenacaoDescendente() {
        PageResult<User> page = adapter.findAll(new PageRequest(0, 1, "name", SortDirection.DESC));

        assertThat(page.content()).extracting(User::name).containsExactly("Carla Mendes");
    }

    @Test
    void usuariosComOMesmoCreatedAtNaoRepetemNemSomemEntrePaginas() {
        for (int i = 0; i < 5; i++) {
            adapter.save(new User(null, "Empate " + i, "empate" + i + "@exemplo.com", NOW));
        }

        List<Long> ids = new ArrayList<>();
        for (int page = 0; page < 4; page++) {
            adapter.findAll(new PageRequest(page, 2, "createdAt", SortDirection.DESC))
                    .content().forEach(u -> ids.add(u.id()));
        }

        assertThat(ids).hasSize(8).doesNotHaveDuplicates();
    }

    @Test
    void rankingContaPostsPorAutorComDesempatePorNome() {
        List<AuthorRanking> ranking = adapter.ranking(10);

        assertThat(ranking).extracting(AuthorRanking::name)
                .containsExactly("Ana Souza", "Bruno Lima", "Carla Mendes");
        assertThat(ranking).extracting(AuthorRanking::postCount).containsExactly(2L, 2L, 2L);
    }

    @Test
    void rankingIncluiAutorSemPostsNoFimELimitaOResultado() {
        User dora = adapter.save(new User(null, "Dora", "dora@exemplo.com", NOW));

        List<AuthorRanking> ranking = adapter.ranking(10);

        assertThat(ranking).hasSize(4);
        assertThat(ranking.get(3)).isEqualTo(new AuthorRanking(dora.id(), "Dora", 0));
        assertThat(adapter.ranking(2)).hasSize(2);
    }
}
