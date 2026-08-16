package com.rezkna.identity.account;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface DinerAccountRepository extends MongoRepository<DinerAccount, String> {

    Optional<DinerAccount> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<DinerAccount> findByPhone(String phone);

    Optional<DinerAccount> findByProviderAndProviderId(String provider, String providerId);
}
