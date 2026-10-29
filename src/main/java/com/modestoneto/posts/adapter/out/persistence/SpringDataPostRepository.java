package com.modestoneto.posts.adapter.out.persistence;

import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataPostRepository extends JpaRepository<PostJpaEntity, Long> {

    // Hibernate 7.4: o LIMIT vai para uma subconsulta na raiz (posts) e a coleção entra por LEFT JOIN, sem paginar em memória.
    @EntityGraph(attributePaths = {"author", "comments", "comments.author"})
    Page<PostJpaEntity> findByAuthorId(Long authorId, Pageable pageable);

    // o Spring Data escapa % e _ no Containing, então o texto do usuário não vira curinga
    @EntityGraph(attributePaths = {"author", "comments", "comments.author"})
    Page<PostJpaEntity> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    // EXISTS nos comentários (e não JOIN): um JOIN repetiria o post que tem vários comentários batendo e quebraria o count.
    @EntityGraph(attributePaths = {"author", "comments", "comments.author"})
    @Query("""
            select p from PostJpaEntity p
            where (:pattern is null
                   or lower(p.title) like lower(:pattern) escape '\\'
                   or lower(p.body) like lower(:pattern) escape '\\'
                   or exists (select 1 from CommentJpaEntity c
                              where c.post = p and lower(c.text) like lower(:pattern) escape '\\'))
              and (:from is null or p.createdAt >= :from)
              and (:to is null or p.createdAt < :to)
            """)
    Page<PostJpaEntity> search(@Param("pattern") String pattern, @Param("from") Instant from,
                               @Param("to") Instant to, Pageable pageable);
}
