package com.modestoneto.posts.adapter.out.persistence;

import com.modestoneto.posts.domain.model.User;
import org.springframework.stereotype.Component;

@Component
class UserMapper {

    User toDomain(UserJpaEntity entity) {
        return new User(entity.getId(), entity.getName(), entity.getEmail(), entity.getCreatedAt());
    }

    UserJpaEntity toEntity(User user) {
        return new UserJpaEntity(user.id(), user.name(), user.email(), user.createdAt());
    }
}
