package com.rezkna.identity.platform;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataMongoTest
@Testcontainers(disabledWithoutDocker = true)
class PlatformUserRepositoryIntegrationTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7");

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private PlatformUserRepository repository;

    @Test
    void enforcesUniqueEmail() {
        PlatformUser first = new PlatformUser();
        first.setEmail("admin@rezkna.com");
        first.setRole("PLATFORM_OWNER");
        repository.save(first);

        PlatformUser duplicate = new PlatformUser();
        duplicate.setEmail("admin@rezkna.com");
        duplicate.setRole("PLATFORM_OWNER");

        assertThatThrownBy(() -> repository.save(duplicate)).isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void findsByEmail() {
        PlatformUser user = new PlatformUser();
        user.setEmail("owner@rezkna.com");
        user.setRole("PLATFORM_OWNER");
        repository.save(user);

        Optional<PlatformUser> found = repository.findByEmail("owner@rezkna.com");

        assertThat(found).isPresent();
        assertThat(found.get().getRole()).isEqualTo("PLATFORM_OWNER");
    }
}
