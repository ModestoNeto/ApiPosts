package com.modestoneto.posts.adapter.in.web;

import com.modestoneto.posts.application.port.in.AddCommentUseCase;
import com.modestoneto.posts.application.port.in.CreatePostUseCase;
import com.modestoneto.posts.application.port.in.FindPostByIdUseCase;
import com.modestoneto.posts.application.port.in.SearchPostsByTitleUseCase;
import com.modestoneto.posts.application.port.in.SearchPostsUseCase;
import com.modestoneto.posts.domain.model.Post;
import com.modestoneto.posts.adapter.in.web.exception.StandardError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Posts")
@ApiResponse(responseCode = "400", description = "Requisição inválida",
        content = @Content(schema = @Schema(implementation = StandardError.class)))
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
    @Operation(summary = "Cria um post")
    @ApiResponse(responseCode = "404", description = "Autor não encontrado",
            content = @Content(schema = @Schema(implementation = StandardError.class)))
    ResponseEntity<PostResponse> create(@Valid @RequestBody CreatePostRequest request) {
        Post post = createPost.create(request.authorId(), request.title(), request.body());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(post.id()).toUri();
        return ResponseEntity.created(location).body(PostResponse.from(post));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um post com autor e comentários")
    @ApiResponse(responseCode = "404", description = "Post não encontrado",
            content = @Content(schema = @Schema(implementation = StandardError.class)))
    PostResponse findById(@PathVariable Long id) {
        return PostResponse.from(findPostById.findById(id));
    }

    @PostMapping("/{id}/comments")
    @Operation(summary = "Adiciona um comentário e devolve o post inteiro")
    @ApiResponse(responseCode = "404", description = "Post ou autor não encontrado",
            content = @Content(schema = @Schema(implementation = StandardError.class)))
    ResponseEntity<PostResponse> addComment(@PathVariable Long id, @Valid @RequestBody AddCommentRequest request) {
        Post post = addComment.addComment(id, request.authorId(), request.text());
        return ResponseEntity.status(HttpStatus.CREATED).body(PostResponse.from(post));
    }

    @GetMapping("/search/title")
    @Operation(summary = "Busca posts pelo título (contém, sem diferenciar caixa nem acento)")
    PageResponse<PostResponse> searchByTitle(@Parameter(description = "Texto do título; obrigatório e não pode ser em branco") @RequestParam String title,
                                             @Parameter(description = "Página, a partir de 0") @RequestParam(defaultValue = "0") int page,
                                             @Parameter(description = "Itens por página, de 1 a 100") @RequestParam(defaultValue = "20") int size,
                                             @Parameter(description = "campo,asc|desc. Campos: title, createdAt") @RequestParam(defaultValue = "createdAt,desc") String sort) {
        return PageResponse.from(searchPostsByTitle.searchByTitle(title, PageParams.of(page, size, sort)), PostResponse::from);
    }

    @GetMapping("/search")
    @Operation(summary = "Busca completa: texto no título, corpo ou comentários, com intervalo de datas")
    PageResponse<PostResponse> search(@Parameter(description = "Texto procurado (opcional)") @RequestParam(required = false) String text,
                                      @Parameter(description = "Data inicial AAAA-MM-DD, em UTC, inclusive (opcional)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate minDate,
                                      @Parameter(description = "Data final AAAA-MM-DD, em UTC, inclusive (opcional)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate maxDate,
                                      @Parameter(description = "Página, a partir de 0") @RequestParam(defaultValue = "0") int page,
                                      @Parameter(description = "Itens por página, de 1 a 100") @RequestParam(defaultValue = "20") int size,
                                      @Parameter(description = "campo,asc|desc. Campos: title, createdAt") @RequestParam(defaultValue = "createdAt,desc") String sort) {
        return PageResponse.from(searchPosts.search(text, minDate, maxDate, PageParams.of(page, size, sort)), PostResponse::from);
    }
}
