package com.modestoneto.posts.adapter.in.web;

import com.modestoneto.posts.application.port.in.CreateUserUseCase;
import com.modestoneto.posts.application.port.in.FindUserByIdUseCase;
import com.modestoneto.posts.application.port.in.FindUsersUseCase;
import com.modestoneto.posts.domain.model.User;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

    UserController(FindUsersUseCase findUsers, FindUserByIdUseCase findUserById,
                   CreateUserUseCase createUser) {
        this.findUsers = findUsers;
        this.findUserById = findUserById;
        this.createUser = createUser;
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
}
