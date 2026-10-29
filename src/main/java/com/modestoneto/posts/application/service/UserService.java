package com.modestoneto.posts.application.service;

import com.modestoneto.posts.application.common.PageRequest;
import com.modestoneto.posts.application.common.PageResult;
import com.modestoneto.posts.application.port.in.CreateUserUseCase;
import com.modestoneto.posts.application.port.in.DeleteUserUseCase;
import com.modestoneto.posts.application.port.in.FindAuthorRankingUseCase;
import com.modestoneto.posts.application.port.in.FindUserByIdUseCase;
import com.modestoneto.posts.application.port.in.FindUsersUseCase;
import com.modestoneto.posts.application.port.in.UpdateUserUseCase;
import com.modestoneto.posts.application.port.out.UserRepositoryPort;
import com.modestoneto.posts.domain.exception.InvalidRequestException;
import com.modestoneto.posts.domain.exception.ResourceNotFoundException;
import com.modestoneto.posts.domain.model.AuthorRanking;
import com.modestoneto.posts.domain.model.User;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService implements FindUsersUseCase, FindUserByIdUseCase, CreateUserUseCase,
        UpdateUserUseCase, DeleteUserUseCase, FindAuthorRankingUseCase {

    static final Set<String> SORT_FIELDS = Set.of("name", "createdAt");
    static final int MAX_RANKING = 100;

    private final UserRepositoryPort users;

    public UserService(UserRepositoryPort users) {
        this.users = users;
    }

    @Override
    public PageResult<User> findAll(PageRequest request) {
        return users.findAll(request.requireSortIn(SORT_FIELDS));
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

    @Override
    @Transactional
    public User update(Long id, String name, String email) {
        User current = findById(id);
        return users.save(new User(id, name.trim(), email.trim().toLowerCase(), current.createdAt()));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        findById(id);
        users.deleteById(id);
    }

    @Override
    public List<AuthorRanking> ranking(int limit) {
        if (limit < 1 || limit > MAX_RANKING) {
            throw new InvalidRequestException("limit deve estar entre 1 e " + MAX_RANKING);
        }
        return users.ranking(limit);
    }
}
