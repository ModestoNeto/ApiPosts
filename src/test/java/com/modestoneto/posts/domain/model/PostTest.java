package com.modestoneto.posts.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class PostTest {

    private final Instant now = Instant.parse("2026-02-01T08:00:00Z");
    private final User ana = new User(1L, "Ana", "ana@exemplo.com", now);
    private final User bruno = new User(2L, "Bruno", "bruno@exemplo.com", now);

    @Test
    void addCommentDevolveNovoPostSemAlterarOOriginal() {
        Post post = new Post(1L, ana, "Título", "Corpo", now, List.of());

        Post comentado = post.addComment(bruno, "  Ótimo!  ", now);

        assertThat(post.comments()).isEmpty();
        assertThat(comentado.comments()).hasSize(1);
        assertThat(comentado.comments().get(0).text()).isEqualTo("Ótimo!");
        assertThat(comentado.comments().get(0).author()).isEqualTo(bruno);
        assertThat(comentado.id()).isEqualTo(post.id());
    }

    @Test
    void addCommentMantemOsComentariosExistentesEmOrdem() {
        Comment primeiro = new Comment(10L, bruno, "Primeiro", now);
        Post post = new Post(1L, ana, "Título", "Corpo", now, List.of(primeiro));

        Post comentado = post.addComment(ana, "Segundo", now);

        assertThat(comentado.comments()).extracting(Comment::text).containsExactly("Primeiro", "Segundo");
    }
}
