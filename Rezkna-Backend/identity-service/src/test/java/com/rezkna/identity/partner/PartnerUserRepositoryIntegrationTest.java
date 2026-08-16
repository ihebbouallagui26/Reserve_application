package com.rezkna.identity.partner;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataMongoTest
@Testcontainers(disabledWithoutDocker = true)
class PartnerUserRepositoryIntegrationTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7");

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private PartnerUserRepository repository;

    @Test
    void enforcesUniqueEmailPerProperty() {
        PartnerUser first = new PartnerUser();
        first.setEmail("owner@example.com");
        first.setPropertyId("prop-a");
        first.setRole("OWNER");
        repository.save(first);

        PartnerUser duplicate = new PartnerUser();
        duplicate.setEmail("owner@example.com");
        duplicate.setPropertyId("prop-a");
        duplicate.setRole("OWNER");

        assertThatThrownBy(() -> repository.save(duplicate)).isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void allowsTheSameEmailAcrossDifferentProperties() {
        PartnerUser propertyA = new PartnerUser();
        propertyA.setEmail("multi@example.com");
        propertyA.setPropertyId("prop-a");
        propertyA.setRole("OWNER");
        repository.save(propertyA);

        PartnerUser propertyB = new PartnerUser();
        propertyB.setEmail("multi@example.com");
        propertyB.setPropertyId("prop-b");
        propertyB.setRole("HOST");

        PartnerUser saved = repository.save(propertyB);
        assertThat(saved.getId()).isNotNull();

        List<PartnerUser> byEmail = repository.findByEmail("multi@example.com");
        assertThat(byEmail).hasSize(2);
    }

    @Test
    void findsStaffByProperty() {
        PartnerUser owner = new PartnerUser();
        owner.setEmail("owner2@example.com");
        owner.setPropertyId("prop-c");
        owner.setRole("OWNER");
        repository.save(owner);

        PartnerUser host = new PartnerUser();
        host.setEmail("host2@example.com");
        host.setPropertyId("prop-c");
        host.setRole("HOST");
        repository.save(host);

        assertThat(repository.findByPropertyId("prop-c")).hasSize(2);
    }
}
