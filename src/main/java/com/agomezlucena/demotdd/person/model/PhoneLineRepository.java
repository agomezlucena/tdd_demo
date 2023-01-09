package com.agomezlucena.demotdd.person.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PhoneLineRepository extends JpaRepository<PhoneLine, PhoneLineId> {
    void deleteByPerson(Person person);

    Optional<PhoneLine> findFirstByPersonAndInternationalPrefixAndPhoneNumber(Person person, String internationalPrefix, String phoneNumber);
}
