package com.modestoneto.posts.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts")
class PostJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id")
    private UserJpaEntity author;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt, id")
    private List<CommentJpaEntity> comments = new ArrayList<>();

    protected PostJpaEntity() {
    }

    PostJpaEntity(Long id, UserJpaEntity author, String title, String body, Instant createdAt) {
        this.id = id;
        this.author = author;
        this.title = title;
        this.body = body;
        this.createdAt = createdAt;
    }

    Long getId() { return id; }
    UserJpaEntity getAuthor() { return author; }
    String getTitle() { return title; }
    String getBody() { return body; }
    Instant getCreatedAt() { return createdAt; }
    List<CommentJpaEntity> getComments() { return comments; }
}
