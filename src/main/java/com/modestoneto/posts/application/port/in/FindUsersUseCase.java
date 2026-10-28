package com.modestoneto.posts.application.port.in;

import com.modestoneto.posts.domain.model.User;
import com.modestoneto.posts.application.common.PageRequest;
import com.modestoneto.posts.application.common.PageResult;

public interface FindUsersUseCase {

    PageResult<User> findAll(PageRequest request);
}
