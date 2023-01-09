package com.agomezlucena.demotdd.person.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class PhoneLineId implements Serializable {
    @Column(name = "person_id")
    private UUID personId;
    @Column(name = "phone_line_id")
    private UUID phoneLineId;
}
