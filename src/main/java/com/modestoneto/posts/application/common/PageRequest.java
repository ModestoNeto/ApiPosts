package com.modestoneto.posts.application.common;

import com.modestoneto.posts.domain.exception.InvalidRequestException;
import java.util.Set;

public record PageRequest(int page, int size, String sortBy, SortDirection direction) {

    public static final int MAX_SIZE = 100;

    public PageRequest {
        if (page < 0) {
            throw new InvalidRequestException("page deve ser maior ou igual a 0");
        }
        if (size < 1 || size > MAX_SIZE) {
            throw new InvalidRequestException("size deve estar entre 1 e " + MAX_SIZE);
        }
    }

    // Lista fixa de campos: o nome vem do cliente e vira ORDER BY, então não pode passar sem checagem.
    public PageRequest requireSortIn(Set<String> allowed) {
        if (!allowed.contains(sortBy)) {
            throw new InvalidRequestException("sort inválido: '" + sortBy + "'. Permitidos: " + String.join(", ", allowed.stream().sorted().toList()));
        }
        return this;
    }
}
