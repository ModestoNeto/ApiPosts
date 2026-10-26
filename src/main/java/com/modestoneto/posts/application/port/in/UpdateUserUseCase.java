package com.modestoneto.posts.application.port.in;

import com.modestoneto.posts.domain.model.User;

public interface UpdateUserUseCase {

    User update(Long id, String name, String email);
}
