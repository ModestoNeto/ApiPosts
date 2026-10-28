package com.modestoneto.posts.adapter.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataPostRepository extends JpaRepository<PostJpaEntity, Long> {

    // Hibernate 7.4: o LIMIT vai para uma subconsulta na raiz (posts) e a coleção entra por LEFT JOIN, sem paginar em memória.
    @EntityGraph(attributePaths = {"author", "comments", "comments.author"})
    Page<PostJpaEntity> findByAuthorId(Long authorId, Pageable pageable);

    // o Spring Data escapa % e _ no Containing, então o texto do usuário não vira curinga
    @EntityGraph(attributePaths = {"author", "comments", "comments.author"})
    Page<PostJpaEntity> findByTitleContainingIgnoreCase(String title, Pageable pageable);
}
