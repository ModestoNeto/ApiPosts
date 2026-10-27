package com.modestoneto.posts.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "comments")
class CommentJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id")
    private PostJpaEntity post;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id")
    private UserJpaEntity author;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected CommentJpaEntity() {
    }

    CommentJpaEntity(Long id, PostJpaEntity post, UserJpaEntity author, String text, Instant createdAt) {
        this.id = id;
        this.post = post;
        this.author = author;
        this.text = text;
        this.createdAt = createdAt;
    }

    Long getId() { return id; }
    UserJpaEntity getAuthor() { return author; }
    String getText() { return text; }
    Instant getCreatedAt() { return createdAt; }
}
