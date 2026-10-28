package com.modestoneto.posts.adapter.out.persistence;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataPostRepository extends JpaRepository<PostJpaEntity, Long> {

    // Um JOIN só em vez de N+1. Sem paginação: com coleção no fetch, o Hibernate paginaria em memória (Dia 6).
    @Override
    @EntityGraph(attributePaths = {"author", "comments", "comments.author"})
    List<PostJpaEntity> findAll();

    Page<PostJpaEntity> findByAuthorId(Long authorId, Pageable pageable);
}
