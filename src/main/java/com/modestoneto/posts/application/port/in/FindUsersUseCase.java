package com.modestoneto.posts.application.port.in;

import com.modestoneto.posts.domain.model.User;
import java.util.List;

public interface FindUsersUseCase {

    List<User> findAll();
}
