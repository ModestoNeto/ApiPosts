package com.modestoneto.posts.adapter.in.web;

import com.modestoneto.posts.application.port.in.FindUserByIdUseCase;
import com.modestoneto.posts.application.port.in.FindUsersUseCase;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
class UserController {

    private final FindUsersUseCase findUsers;
    private final FindUserByIdUseCase findUserById;

    UserController(FindUsersUseCase findUsers, FindUserByIdUseCase findUserById) {
        this.findUsers = findUsers;
        this.findUserById = findUserById;
    }

    @GetMapping
    List<UserResponse> findAll() {
        return findUsers.findAll().stream().map(UserResponse::from).toList();
    }

    @GetMapping("/{id}")
    UserResponse findById(@PathVariable Long id) {
        return UserResponse.from(findUserById.findById(id));
    }
}
