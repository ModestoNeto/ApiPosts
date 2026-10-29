package com.modestoneto.posts.adapter.out.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, Long> {

    // LEFT JOIN: autor sem posts aparece com 0. Todo campo não agregado entra no GROUP BY (ONLY_FULL_GROUP_BY do MySQL).
    @Query(nativeQuery = true, value = """
            select u.id as userId, u.name as name, count(p.id) as postCount
            from users u left join posts p on p.author_id = u.id
            group by u.id, u.name
            order by postCount desc, u.name, u.id
            limit :limit
            """)
    List<AuthorRankingProjection> ranking(@Param("limit") int limit);
}
