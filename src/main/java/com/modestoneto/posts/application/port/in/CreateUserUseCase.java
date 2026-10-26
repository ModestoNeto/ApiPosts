package com.modestoneto.posts.application.port.in;

import com.modestoneto.posts.domain.model.User;

public interface CreateUserUseCase {

    User create(String name, String email);
}
