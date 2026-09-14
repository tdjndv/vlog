package com.vlog.vlog.post;

import com.vlog.vlog.post.dto.CreatePostRequest;
import com.vlog.vlog.post.dto.PostFilter;
import com.vlog.vlog.post.dto.PostResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService service;

    public PostController(PostService service) {
        this.service = service;
    }

    @GetMapping
    public Page<PostResponse> search(
            @ModelAttribute PostFilter filter,
            @PageableDefault(size = 20, sort = "postDate", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return service.search(filter, pageable);
    }

    @GetMapping("/{id}")
    public PostResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostResponse create(@Valid @RequestBody CreatePostRequest request,
                               Authentication authentication) {
        return service.create(request, authentication.getName());
    }

    @PutMapping("/{id}")
    public PostResponse update(@PathVariable Long id,
                               @Valid @RequestBody CreatePostRequest request,
                               Authentication authentication) {
        return service.update(id, request, authentication.getName());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication authentication) {
        service.delete(id, authentication.getName());
    }

    @GetMapping("/count")
    public long countByAuthor(@RequestParam String username) {
        return service.countByAuthor(username);
    }
}