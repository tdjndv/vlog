package com.vlog.vlog.post;

import com.vlog.vlog.post.dto.PostFilter;
import com.vlog.vlog.user.User;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class PostSpecifications {

    private PostSpecifications() {}

    /** Partial, case-insensitive match on the author's username. */
    public static Specification<Post> byUsername(String username) {
        return (root, query, cb) -> {
            Join<Post, User> author = root.join("author");
            return cb.like(cb.lower(author.get("username")),
                    "%" + username.toLowerCase() + "%");
        };
    }

    /** Case-insensitive match against title OR body. */
    public static Specification<Post> byKeyword(String keyword) {
        return (root, query, cb) -> {
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("body")), pattern)
            );
        };
    }

    public static Specification<Post> onDate(LocalDate date) {
        return (root, query, cb) -> cb.equal(root.get("postDate"), date);
    }

    public static Specification<Post> from(LocalDate from) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("postDate"), from);
    }

    public static Specification<Post> to(LocalDate to) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("postDate"), to);
    }

    public static Specification<Post> build(PostFilter filter) {
        List<Specification<Post>> specs = new ArrayList<>();

        if (filter.hasUsername()) {
            specs.add(byUsername(filter.username()));
        }
        if (filter.hasKeyword()) {
            specs.add(byKeyword(filter.keyword()));
        }

        if (filter.hasDate()) {
            specs.add(onDate(filter.date()));
        } else {
            if (filter.hasFrom()) specs.add(from(filter.from()));
            if (filter.hasTo())   specs.add(to(filter.to()));
        }

        return Specification.allOf(specs);
    }
}