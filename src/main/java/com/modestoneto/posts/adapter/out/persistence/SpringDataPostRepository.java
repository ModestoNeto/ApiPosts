package com.modestoneto.posts.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataPostRepository extends JpaRepository<PostJpaEntity, Long> {
}
