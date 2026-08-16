package com.rezkna.identity.partner;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface PartnerUserRepository extends MongoRepository<PartnerUser, String> {

    List<PartnerUser> findByEmail(String email);

    Optional<PartnerUser> findByEmailAndPropertyId(String email, String propertyId);

    List<PartnerUser> findByPropertyId(String propertyId);

    boolean existsByEmailAndPropertyId(String email, String propertyId);
}
