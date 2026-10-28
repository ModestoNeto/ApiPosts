package com.modestoneto.posts.adapter.in.web;

import com.modestoneto.posts.application.common.PageResult;
import java.util.List;
import java.util.function.Function;

public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    static <D, T> PageResponse<T> from(PageResult<D> result, Function<D, T> mapper) {
        var mapped = result.map(mapper);
        int totalPages = (int) Math.ceil((double) result.totalElements() / result.size());
        return new PageResponse<>(mapped.content(), result.page(), result.size(), result.totalElements(), totalPages);
    }
}
