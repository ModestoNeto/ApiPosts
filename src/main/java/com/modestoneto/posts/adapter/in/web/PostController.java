package com.modestoneto.posts.adapter.in.web;

import com.modestoneto.posts.application.port.in.AddCommentUseCase;
import com.modestoneto.posts.application.port.in.CreatePostUseCase;
import com.modestoneto.posts.application.port.in.FindPostByIdUseCase;
import com.modestoneto.posts.application.port.in.SearchPostsByTitleUseCase;
import com.modestoneto.posts.application.port.in.SearchPostsUseCase;
import com.modestoneto.posts.domain.model.Post;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/posts")
class PostController {

    private final CreatePostUseCase createPost;
    private final FindPostByIdUseCase findPostById;
    private final AddCommentUseCase addComment;
    private final SearchPostsByTitleUseCase searchPostsByTitle;
    private final SearchPostsUseCase searchPosts;

    PostController(CreatePostUseCase createPost, FindPostByIdUseCase findPostById,
                   AddCommentUseCase addComment, SearchPostsByTitleUseCase searchPostsByTitle,
                   SearchPostsUseCase searchPosts) {
        this.createPost = createPost;
        this.findPostById = findPostById;
        this.addComment = addComment;
        this.searchPostsByTitle = searchPostsByTitle;
        this.searchPosts = searchPosts;
    }

    @PostMapping
    ResponseEntity<PostResponse> create(@Valid @RequestBody CreatePostRequest request) {
        Post post = createPost.create(request.authorId(), request.title(), request.body());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(post.id()).toUri();
        return ResponseEntity.created(location).body(PostResponse.from(post));
    }

    @GetMapping("/{id}")
    PostResponse findById(@PathVariable Long id) {
        return PostResponse.from(findPostById.findById(id));
    }

    @PostMapping("/{id}/comments")
    ResponseEntity<PostResponse> addComment(@PathVariable Long id, @Valid @RequestBody AddCommentRequest request) {
        Post post = addComment.addComment(id, request.authorId(), request.text());
        return ResponseEntity.status(HttpStatus.CREATED).body(PostResponse.from(post));
    }

    @GetMapping("/search/title")
    PageResponse<PostResponse> searchByTitle(@RequestParam String title,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "20") int size,
                                             @RequestParam(defaultValue = "createdAt,desc") String sort) {
        return PageResponse.from(searchPostsByTitle.searchByTitle(title, PageParams.of(page, size, sort)), PostResponse::from);
    }

    @GetMapping("/search")
    PageResponse<PostResponse> search(@RequestParam(required = false) String text,
                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate minDate,
                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate maxDate,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "20") int size,
                                      @RequestParam(defaultValue = "createdAt,desc") String sort) {
        return PageResponse.from(searchPosts.search(text, minDate, maxDate, PageParams.of(page, size, sort)), PostResponse::from);
    }
}
