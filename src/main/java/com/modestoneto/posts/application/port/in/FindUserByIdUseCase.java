package com.modestoneto.posts.application.port.in;

import com.modestoneto.posts.domain.model.User;

public interface FindUserByIdUseCase {

    User findById(Long id);
}
