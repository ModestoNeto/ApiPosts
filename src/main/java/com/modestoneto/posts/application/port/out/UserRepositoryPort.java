package com.modestoneto.posts.application.port.out;

import com.modestoneto.posts.domain.model.AuthorRanking;
import com.modestoneto.posts.domain.model.User;
import com.modestoneto.posts.application.common.PageRequest;
import com.modestoneto.posts.application.common.PageResult;
import java.util.List;
import java.util.Optional;

public interface UserRepositoryPort {

    PageResult<User> findAll(PageRequest request);

    Optional<User> findById(Long id);

    User save(User user);

    void deleteById(Long id);

    List<AuthorRanking> ranking(int limit);
}
