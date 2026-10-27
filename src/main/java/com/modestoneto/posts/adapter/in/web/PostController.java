package com.modestoneto.posts.adapter.in.web;

import com.modestoneto.posts.application.port.in.CreatePostUseCase;
import com.modestoneto.posts.application.port.in.FindPostByIdUseCase;
import com.modestoneto.posts.domain.model.Post;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/posts")
class PostController {

    private final CreatePostUseCase createPost;
    private final FindPostByIdUseCase findPostById;

    PostController(CreatePostUseCase createPost, FindPostByIdUseCase findPostById) {
        this.createPost = createPost;
        this.findPostById = findPostById;
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
}
