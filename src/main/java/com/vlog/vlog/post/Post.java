package com.vlog.vlog.post;

import com.vlog.vlog.user.User;
import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "posts", indexes = {
        @Index(name = "idx_post_author", columnList = "author_id"),
        @Index(name = "idx_post_date", columnList = "post_date")
})
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(name = "post_date", nullable = false)
    private LocalDate postDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt;

    protected Post() {}

    public Post(String title, String body, User author, LocalDate postDate) {
        this.title = title;
        this.body = body;
        this.author = author;
        this.postDate = postDate;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getBody() { return body; }
    public User getAuthor() { return author; }
    public LocalDate getPostDate() { return postDate; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void update(String title, String body, LocalDate postDate) {
        this.title = title;
        this.body = body;
        this.postDate = postDate;
        this.updatedAt = Instant.now();
    }
}