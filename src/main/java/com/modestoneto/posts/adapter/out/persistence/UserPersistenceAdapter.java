package com.modestoneto.posts.adapter.out.persistence;

import com.modestoneto.posts.application.port.out.UserRepositoryPort;
import com.modestoneto.posts.domain.exception.ConflictException;
import com.modestoneto.posts.domain.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
class UserPersistenceAdapter implements UserRepositoryPort {

    private final SpringDataUserRepository repository;
    private final UserMapper mapper;

    UserPersistenceAdapter(SpringDataUserRepository repository, UserMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public List<User> findAll() {
        return repository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<User> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public User save(User user) {
        try {
            // saveAndFlush: a violação do índice único só estoura no flush
            return mapper.toDomain(repository.saveAndFlush(mapper.toEntity(user)));
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("E-mail já cadastrado: " + user.email());
        }
    }
}
