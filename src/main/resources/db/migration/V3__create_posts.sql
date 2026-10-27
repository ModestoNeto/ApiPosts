CREATE TABLE posts (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    author_id  BIGINT       NOT NULL,
    title      VARCHAR(200) NOT NULL,
    body       TEXT         NOT NULL,
    created_at DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_posts_author FOREIGN KEY (author_id) REFERENCES users (id),
    INDEX idx_posts_author_created (author_id, created_at)
) ENGINE=InnoDB;
