package com.vlog.vlog.post;

import com.vlog.vlog.common.NotFoundException;
import com.vlog.vlog.post.dto.CreatePostRequest;
import com.vlog.vlog.post.dto.PostFilter;
import com.vlog.vlog.post.dto.PostResponse;
import com.vlog.vlog.user.User;
import com.vlog.vlog.user.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository repository;
    private final UserService userService;

    public PostService(PostRepository repository, UserService userService) {
        this.repository = repository;
        this.userService = userService;
    }

    public Page<PostResponse> search(PostFilter filter, Pageable pageable) {
        Specification<Post> spec = PostSpecifications.build(filter);
        return repository.findAll(spec, pageable).map(PostResponse::from);
    }

    public PostResponse findById(Long id) {
        return repository.findById(id)
                .map(PostResponse::from)
                .orElseThrow(() -> new NotFoundException("Post not found: " + id));
    }

    @Transactional
    public PostResponse create(CreatePostRequest request, String authorUsername) {
        User author = userService.getEntityByUsername(authorUsername);

        Post post = new Post(
                request.title(),
                request.body(),
                author,
                request.postDate()
        );
        return PostResponse.from(repository.save(post));
    }

    @Transactional
    public PostResponse update(Long id, CreatePostRequest request, String username) {
        Post post = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Post not found: " + id));

        requireOwnerOrManager(post, username);

        post.update(request.title(), request.body(), request.postDate());
        return PostResponse.from(post);
    }

    @Transactional
    public void delete(Long id, String username) {
        Post post = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Post not found: " + id));

        requireOwnerOrManager(post, username);
        repository.delete(post);
    }

    public long countByAuthor(String username) {
        return repository.countByAuthorUsername(username);
    }

    /** Authors may modify their own vlogs; managers may modify any. */
    private void requireOwnerOrManager(Post post, String username) {
        if (post.getAuthor().getUsername().equals(username)) {
            return;
        }

        User caller = userService.getEntityByUsername(username);
        if (!caller.isManager()) {
            throw new AccessDeniedException("You can only modify your own vlogs");
        }
    }
}