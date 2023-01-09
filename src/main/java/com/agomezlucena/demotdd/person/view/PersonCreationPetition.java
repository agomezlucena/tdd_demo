package com.agomezlucena.demotdd.person.view;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
public class PersonCreationPetition {
    @NotNull
    @JsonProperty
    private String name;
    @NotNull
    @JsonProperty
    private String lastName;
}
