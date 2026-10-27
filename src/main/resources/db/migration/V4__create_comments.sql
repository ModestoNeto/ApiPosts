CREATE TABLE comments (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    post_id    BIGINT      NOT NULL,
    author_id  BIGINT      NOT NULL,
    text       TEXT        NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_comments_post   FOREIGN KEY (post_id)   REFERENCES posts (id),
    CONSTRAINT fk_comments_author FOREIGN KEY (author_id) REFERENCES users (id),
    INDEX idx_comments_post (post_id)
) ENGINE=InnoDB;
