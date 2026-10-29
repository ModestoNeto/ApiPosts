package com.modestoneto.posts.adapter.out.persistence;

// Projeção do Spring Data: não sai do adaptador, é convertida para AuthorRanking.
interface AuthorRankingProjection {

    Long getUserId();

    String getName();

    Long getPostCount();
}
