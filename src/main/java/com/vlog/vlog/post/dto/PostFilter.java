package com.vlog.vlog.post.dto;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record PostFilter(

        String username,

        String keyword,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate date,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate from,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate to
) {
    public boolean hasUsername() { return username != null && !username.isBlank(); }
    public boolean hasKeyword()  { return keyword != null && !keyword.isBlank(); }
    public boolean hasDate()     { return date != null; }
    public boolean hasFrom()     { return from != null; }
    public boolean hasTo()       { return to != null; }
}