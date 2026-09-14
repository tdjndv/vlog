package com.vlog.vlog.post.dto;

import com.vlog.vlog.post.Post;
import com.vlog.vlog.user.Role;

import java.time.Instant;
import java.time.LocalDate;

public record PostResponse(
        Long id,
        String title,
        String body,
        LocalDate postDate,
        String authorUsername,
        Role authorRole,
        Instant createdAt,
        Instant updatedAt
) {
    public static PostResponse from(Post post) {
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getBody(),
                post.getPostDate(),
                post.getAuthor().getUsername(),
                post.getAuthor().getRole(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}