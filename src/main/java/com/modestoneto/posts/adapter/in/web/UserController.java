package com.modestoneto.posts.adapter.in.web;

import com.modestoneto.posts.application.port.in.CreateUserUseCase;
import com.modestoneto.posts.application.port.in.DeleteUserUseCase;
import com.modestoneto.posts.application.port.in.FindAuthorRankingUseCase;
import com.modestoneto.posts.application.port.in.FindPostsByAuthorUseCase;
import com.modestoneto.posts.application.port.in.FindUserByIdUseCase;
import com.modestoneto.posts.application.port.in.FindUsersUseCase;
import com.modestoneto.posts.application.port.in.UpdateUserUseCase;
import com.modestoneto.posts.domain.model.User;
import com.modestoneto.posts.adapter.in.web.exception.StandardError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/users")
@Tag(name = "Usuários")
@ApiResponse(responseCode = "400", description = "Requisição inválida",
        content = @Content(schema = @Schema(implementation = StandardError.class)))
class UserController {

    private final FindUsersUseCase findUsers;
    private final FindUserByIdUseCase findUserById;
    private final CreateUserUseCase createUser;
    private final UpdateUserUseCase updateUser;
    private final DeleteUserUseCase deleteUser;
    private final FindPostsByAuthorUseCase findPostsByAuthor;
    private final FindAuthorRankingUseCase findAuthorRanking;

    UserController(FindUsersUseCase findUsers, FindUserByIdUseCase findUserById,
                   CreateUserUseCase createUser, UpdateUserUseCase updateUser,
                   DeleteUserUseCase deleteUser, FindPostsByAuthorUseCase findPostsByAuthor,
                   FindAuthorRankingUseCase findAuthorRanking) {
        this.findUsers = findUsers;
        this.findUserById = findUserById;
        this.createUser = createUser;
        this.updateUser = updateUser;
        this.deleteUser = deleteUser;
        this.findPostsByAuthor = findPostsByAuthor;
        this.findAuthorRanking = findAuthorRanking;
    }

    @GetMapping
    @Operation(summary = "Lista usuários paginados")
    PageResponse<UserResponse> findAll(@Parameter(description = "Página, a partir de 0") @RequestParam(defaultValue = "0") int page,
                                       @Parameter(description = "Itens por página, de 1 a 100") @RequestParam(defaultValue = "20") int size,
                                       @Parameter(description = "campo,asc|desc. Campos: name, createdAt") @RequestParam(defaultValue = "createdAt,desc") String sort) {
        return PageResponse.from(findUsers.findAll(PageParams.of(page, size, sort)), UserResponse::from);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um usuário pelo id")
    @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
            content = @Content(schema = @Schema(implementation = StandardError.class)))
    UserResponse findById(@PathVariable Long id) {
        return UserResponse.from(findUserById.findById(id));
    }

    @PostMapping
    @Operation(summary = "Cria um usuário")
    @ApiResponse(responseCode = "409", description = "E-mail já cadastrado",
            content = @Content(schema = @Schema(implementation = StandardError.class)))
    ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        User user = createUser.create(request.name(), request.email());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(user.id()).toUri();
        return ResponseEntity.created(location).body(UserResponse.from(user));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza nome e e-mail de um usuário")
    @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
            content = @Content(schema = @Schema(implementation = StandardError.class)))
    @ApiResponse(responseCode = "409", description = "E-mail já cadastrado",
            content = @Content(schema = @Schema(implementation = StandardError.class)))
    UserResponse update(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        return UserResponse.from(updateUser.update(id, request.name(), request.email()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um usuário sem posts nem comentários")
    @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
            content = @Content(schema = @Schema(implementation = StandardError.class)))
    @ApiResponse(responseCode = "409", description = "Usuário com posts ou comentários",
            content = @Content(schema = @Schema(implementation = StandardError.class)))
    ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteUser.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/posts")
    @Operation(summary = "Lista os posts de um usuário, com comentários")
    @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
            content = @Content(schema = @Schema(implementation = StandardError.class)))
    PageResponse<PostResponse> findPosts(@PathVariable Long id,
                                         @Parameter(description = "Página, a partir de 0") @RequestParam(defaultValue = "0") int page,
                                         @Parameter(description = "Itens por página, de 1 a 100") @RequestParam(defaultValue = "20") int size,
                                         @Parameter(description = "campo,asc|desc. Campos: title, createdAt") @RequestParam(defaultValue = "createdAt,desc") String sort) {
        return PageResponse.from(findPostsByAuthor.findByAuthor(id, PageParams.of(page, size, sort)), PostResponse::from);
    }

    @GetMapping("/ranking")
    @Operation(summary = "Ranking de autores por número de posts (inclui quem tem 0)")
    List<AuthorRankingResponse> ranking(@Parameter(description = "Quantidade de autores, de 1 a 100") @RequestParam(defaultValue = "10") int limit) {
        return findAuthorRanking.ranking(limit).stream().map(AuthorRankingResponse::from).toList();
    }
}
