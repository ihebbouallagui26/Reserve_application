package com.rezkna.identity.account;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexInfo;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Testcontainers(disabledWithoutDocker = true)
class PhoneVerificationCodeRepositoryIntegrationTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7");

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private PhoneVerificationCodeRepository repository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    void savesAndFindsByPhone() {
        PhoneVerificationCode code = new PhoneVerificationCode();
        code.setPhone("21612345678");
        code.setCodeHash("hashed");
        code.setExpiresAt(Instant.now().plusSeconds(600));
        code.setAttempts(0);
        code.setCreatedAt(Instant.now());
        repository.save(code);

        Optional<PhoneVerificationCode> found = repository.findByPhone("21612345678");
        assertThat(found).isPresent();
        assertThat(found.get().getCodeHash()).isEqualTo("hashed");
    }

    @Test
    void deleteByPhoneRemovesTheDocument() {
        PhoneVerificationCode code = new PhoneVerificationCode();
        code.setPhone("21687654321");
        code.setCodeHash("hashed");
        code.setExpiresAt(Instant.now().plusSeconds(600));
        repository.save(code);

        repository.deleteByPhone("21687654321");

        assertThat(repository.findByPhone("21687654321")).isEmpty();
    }

    @Test
    void expiresAtHasATtlIndexConfiguredToExpireImmediatelyPastDeadline() {
        PhoneVerificationCode code = new PhoneVerificationCode();
        code.setPhone("21611112222");
        code.setCodeHash("hashed");
        code.setExpiresAt(Instant.now().plusSeconds(600));
        repository.save(code);

        List<IndexInfo> indexes = mongoTemplate.indexOps(PhoneVerificationCode.class).getIndexInfo();
        IndexInfo ttlIndex = indexes.stream()
                .filter(i -> i.getIndexFields().stream().anyMatch(f -> f.getKey().equals("expiresAt")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No index found on 'expiresAt'"));

        assertThat(ttlIndex.getExpireAfter()).isPresent();
        assertThat(ttlIndex.getExpireAfter().get().getSeconds()).isZero();
    }
}
