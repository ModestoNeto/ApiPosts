package com.modestoneto.posts.adapter.out.persistence;

import com.modestoneto.posts.application.common.PageRequest;
import com.modestoneto.posts.application.common.PageResult;
import com.modestoneto.posts.application.common.SortDirection;
import java.util.function.Function;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

final class PageConversions {

    private PageConversions() {
    }

    // O id entra como desempate: sem ele, linhas com o mesmo valor podem repetir ou sumir entre páginas.
    static Pageable toPageable(PageRequest request) {
        Sort.Direction direction = request.direction() == SortDirection.ASC ? Sort.Direction.ASC : Sort.Direction.DESC;
        return org.springframework.data.domain.PageRequest.of(request.page(), request.size(),
                Sort.by(direction, request.sortBy(), "id"));
    }

    static <E, D> PageResult<D> toResult(Page<E> page, Function<E, D> mapper) {
        return new PageResult<>(page.getContent().stream().map(mapper).toList(),
                page.getTotalElements(), page.getNumber(), page.getSize());
    }
}
