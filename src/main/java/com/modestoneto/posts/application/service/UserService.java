package com.modestoneto.posts.application.service;

import com.modestoneto.posts.application.port.in.CreateUserUseCase;
import com.modestoneto.posts.application.port.in.FindUserByIdUseCase;
import com.modestoneto.posts.application.port.in.FindUsersUseCase;
import com.modestoneto.posts.application.port.out.UserRepositoryPort;
import com.modestoneto.posts.domain.exception.ResourceNotFoundException;
import com.modestoneto.posts.domain.model.User;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService implements FindUsersUseCase, FindUserByIdUseCase, CreateUserUseCase {

    private final UserRepositoryPort users;

    public UserService(UserRepositoryPort users) {
        this.users = users;
    }

    @Override
    public List<User> findAll() {
        return users.findAll();
    }

    @Override
    public User findById(Long id) {
        return users.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));
    }

    @Override
    @Transactional
    public User create(String name, String email) {
        return users.save(new User(null, name.trim(), email.trim().toLowerCase(), Instant.now()));
    }
}
