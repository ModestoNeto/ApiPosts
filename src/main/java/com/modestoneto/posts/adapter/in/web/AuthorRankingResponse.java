package com.modestoneto.posts.adapter.in.web;

import com.modestoneto.posts.domain.model.AuthorRanking;

public record AuthorRankingResponse(Long userId, String name, long postCount) {

    static AuthorRankingResponse from(AuthorRanking ranking) {
        return new AuthorRankingResponse(ranking.userId(), ranking.name(), ranking.postCount());
    }
}
