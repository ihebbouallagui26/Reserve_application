package com.rezkna.identity.platform;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PlatformUserRepository extends MongoRepository<PlatformUser, String> {

    Optional<PlatformUser> findByEmail(String email);
}
