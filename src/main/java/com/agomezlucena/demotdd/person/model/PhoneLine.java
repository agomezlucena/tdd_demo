package com.agomezlucena.demotdd.person.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;

import java.util.Objects;
import java.util.UUID;


@Data
@Entity
@Table(name = "phone_lines")
@NoArgsConstructor
public class PhoneLine {
    @EmbeddedId
    private PhoneLineId id;
    private String internationalPrefix;
    private String phoneNumber;
    @MapsId("personId")
    @ManyToOne
    @JoinColumn(name="person_id")
    @ToString.Exclude
    @JsonIgnore
    private Person person;
    public PhoneLine(UUID phoneLineId, String internationalPrefix, String phoneNumber, Person person) {
        this.id = new PhoneLineId(phoneLineId, person.getId());
        this.internationalPrefix = internationalPrefix;
        this.phoneNumber = phoneNumber;
        this.person = person;
    }


    @Override
    @Generated
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        PhoneLine phoneLine = (PhoneLine) o;
        return id != null && Objects.equals(id, phoneLine.id);
    }

    @Override
    @Generated
    public int hashCode() {
        return getClass().hashCode();
    }
}
