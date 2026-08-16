package com.rezkna.identity.account;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PhoneVerificationCodeRepository extends MongoRepository<PhoneVerificationCode, String> {

    Optional<PhoneVerificationCode> findByPhone(String phone);

    void deleteByPhone(String phone);
}
