package com.modestoneto.posts.application.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.modestoneto.posts.domain.exception.InvalidRequestException;
import java.util.Set;
import org.junit.jupiter.api.Test;

class PageRequestTest {

    @Test
    void paginaNegativaEhInvalida() {
        assertThatThrownBy(() -> new PageRequest(-1, 10, "name", SortDirection.ASC))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void tamanhoForaDoLimiteEhInvalido() {
        assertThatThrownBy(() -> new PageRequest(0, 0, "name", SortDirection.ASC))
                .isInstanceOf(InvalidRequestException.class);
        assertThatThrownBy(() -> new PageRequest(0, 101, "name", SortDirection.ASC))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void campoDeOrdenacaoForaDaListaEhInvalido() {
        var request = new PageRequest(0, 10, "senha", SortDirection.ASC);

        assertThatThrownBy(() -> request.requireSortIn(Set.of("name")))
                .isInstanceOf(InvalidRequestException.class);
        assertThat(new PageRequest(0, 10, "name", SortDirection.ASC).requireSortIn(Set.of("name")).sortBy())
                .isEqualTo("name");
    }
}
