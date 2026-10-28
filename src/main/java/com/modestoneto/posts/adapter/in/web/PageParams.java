package com.modestoneto.posts.adapter.in.web;

import com.modestoneto.posts.application.common.PageRequest;
import com.modestoneto.posts.application.common.SortDirection;
import com.modestoneto.posts.domain.exception.InvalidRequestException;

final class PageParams {

    private PageParams() {
    }

    // sort no formato "campo,asc|desc" (a direção é opcional e vale asc)
    static PageRequest of(int page, int size, String sort) {
        String[] parts = sort.split(",", 2);
        SortDirection direction = parts.length == 1 ? SortDirection.ASC : switch (parts[1].trim().toLowerCase()) {
            case "asc" -> SortDirection.ASC;
            case "desc" -> SortDirection.DESC;
            default -> throw new InvalidRequestException("Direção de ordenação inválida: '" + parts[1] + "'. Use asc ou desc");
        };
        return new PageRequest(page, size, parts[0].trim(), direction);
    }
}
