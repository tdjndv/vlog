package com.vlog.vlog.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreatePostRequest(

        @NotBlank(message = "Title is required")
        @Size(max = 200, message = "Title must be 200 characters or fewer")
        String title,

        @NotBlank(message = "Body is required")
        @Size(max = 60000, message = "Body is too long")
        String body,

        @NotNull(message = "Post date is required")
        @PastOrPresent(message = "Post date cannot be in the future")
        LocalDate postDate
) {}