package com.modestoneto.posts.application.port.out;

import com.modestoneto.posts.domain.model.User;
import java.util.List;
import java.util.Optional;

public interface UserRepositoryPort {

    List<User> findAll();

    Optional<User> findById(Long id);
}
