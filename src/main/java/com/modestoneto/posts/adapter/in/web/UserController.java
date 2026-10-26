package com.modestoneto.posts.adapter.in.web;

import com.modestoneto.posts.application.port.in.CreateUserUseCase;
import com.modestoneto.posts.application.port.in.DeleteUserUseCase;
import com.modestoneto.posts.application.port.in.FindUserByIdUseCase;
import com.modestoneto.posts.application.port.in.FindUsersUseCase;
import com.modestoneto.posts.application.port.in.UpdateUserUseCase;
import com.modestoneto.posts.domain.model.User;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/users")
class UserController {

    private final FindUsersUseCase findUsers;
    private final FindUserByIdUseCase findUserById;
    private final CreateUserUseCase createUser;
    private final UpdateUserUseCase updateUser;
    private final DeleteUserUseCase deleteUser;

    UserController(FindUsersUseCase findUsers, FindUserByIdUseCase findUserById,
                   CreateUserUseCase createUser, UpdateUserUseCase updateUser,
                   DeleteUserUseCase deleteUser) {
        this.findUsers = findUsers;
        this.findUserById = findUserById;
        this.createUser = createUser;
        this.updateUser = updateUser;
        this.deleteUser = deleteUser;
    }

    @GetMapping
    List<UserResponse> findAll() {
        return findUsers.findAll().stream().map(UserResponse::from).toList();
    }

    @GetMapping("/{id}")
    UserResponse findById(@PathVariable Long id) {
        return UserResponse.from(findUserById.findById(id));
    }

    @PostMapping
    ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        User user = createUser.create(request.name(), request.email());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(user.id()).toUri();
        return ResponseEntity.created(location).body(UserResponse.from(user));
    }

    @PutMapping("/{id}")
    UserResponse update(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        return UserResponse.from(updateUser.update(id, request.name(), request.email()));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteUser.delete(id);
        return ResponseEntity.noContent().build();
    }
}
