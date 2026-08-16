package com.rezkna.identity.account;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexInfo;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Real MongoDB, via Testcontainers, for integration tests only - the application
 * itself always connects to Atlas at runtime (see spring.data.mongodb.uri in
 * application.yml). This container is ephemeral and torn down after the test class.
 */
@DataMongoTest
@Testcontainers(disabledWithoutDocker = true)
class DinerAccountRepositoryIntegrationTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7");

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private DinerAccountRepository repository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    void enforcesUniqueEmail() {
        DinerAccount first = new DinerAccount();
        first.setEmail("diner@example.com");
        first.setProvider("local");
        repository.save(first);

        DinerAccount duplicate = new DinerAccount();
        duplicate.setEmail("diner@example.com");
        duplicate.setProvider("local");

        assertThatThrownBy(() -> repository.save(duplicate)).isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void allowsMultipleAccountsWithoutAnEmailBecauseTheIndexIsSparse() {
        DinerAccount phoneOnlyA = new DinerAccount();
        phoneOnlyA.setPhone("21611111111");
        phoneOnlyA.setPhoneTail("11111111");
        phoneOnlyA.setProvider("phone");
        repository.save(phoneOnlyA);

        DinerAccount phoneOnlyB = new DinerAccount();
        phoneOnlyB.setPhone("21622222222");
        phoneOnlyB.setPhoneTail("22222222");
        phoneOnlyB.setProvider("phone");

        // Must not throw DuplicateKeyException even though both have a null email.
        DinerAccount saved = repository.save(phoneOnlyB);
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void findsAccountByExactPhone() {
        DinerAccount account = new DinerAccount();
        account.setPhone("21699999999");
        account.setPhoneTail("99999999");
        account.setProvider("phone");
        repository.save(account);

        assertThat(repository.findByPhone("21699999999")).isPresent();
        assertThat(repository.findByPhone("21600000000")).isEmpty();
    }

    @Test
    void findsAccountByProviderAndProviderId() {
        DinerAccount account = new DinerAccount();
        account.setProvider("google");
        account.setProviderId("google-sub-123");
        account.setEmail("googleuser@example.com");
        repository.save(account);

        assertThat(repository.findByProviderAndProviderId("google", "google-sub-123")).isPresent();
        assertThat(repository.findByProviderAndProviderId("facebook", "google-sub-123")).isEmpty();
    }

    @Test
    void emailIndexIsUniqueAndSparse() {
        // Force index creation by saving at least one document first.
        DinerAccount account = new DinerAccount();
        account.setEmail("index-check@example.com");
        repository.save(account);

        List<IndexInfo> indexes = mongoTemplate.indexOps(DinerAccount.class).getIndexInfo();
        IndexInfo emailIndex = indexes.stream()
                .filter(i -> i.getIndexFields().stream().anyMatch(f -> f.getKey().equals("email")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No index found on 'email'"));

        assertThat(emailIndex.isUnique()).isTrue();
        assertThat(emailIndex.isSparse()).isTrue();
    }

    @Test
    void phoneTailIndexExists() {
        DinerAccount account = new DinerAccount();
        account.setPhone("21688888888");
        account.setPhoneTail("88888888");
        repository.save(account);

        List<IndexInfo> indexes = mongoTemplate.indexOps(DinerAccount.class).getIndexInfo();
        boolean hasPhoneTailIndex = indexes.stream()
                .anyMatch(i -> i.getIndexFields().stream().anyMatch(f -> f.getKey().equals("phoneTail")));

        assertThat(hasPhoneTailIndex).isTrue();
    }
}
