package com.agomezlucena.demotdd.person.view;

import lombok.Data;

import java.util.UUID;

@Data
public class PersonCreationResponse {
    private UUID id;
    private String name;
    private String lastName;
}
