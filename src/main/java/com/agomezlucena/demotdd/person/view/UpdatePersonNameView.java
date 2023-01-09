package com.agomezlucena.demotdd.person.view;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UpdatePersonNameView {
    @JsonProperty
    private String name;
}
