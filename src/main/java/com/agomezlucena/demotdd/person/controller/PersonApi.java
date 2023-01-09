package com.agomezlucena.demotdd.person.controller;

import com.agomezlucena.demotdd.person.model.Person;
import com.agomezlucena.demotdd.person.view.PersonCreationPetition;
import com.agomezlucena.demotdd.person.view.PersonCreationResponse;
import com.agomezlucena.demotdd.person.view.PersonView;
import com.agomezlucena.demotdd.person.view.UpdatePersonNameView;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequestMapping("/api/v1/persons")
public interface PersonApi {
    @GetMapping
    ResponseEntity<List<PersonView>> getAll();
    @GetMapping("/{personId}")
    ResponseEntity<Person> findById(@PathVariable UUID personId);
    @PostMapping
    ResponseEntity<PersonCreationResponse> create(@RequestBody @Validated PersonCreationPetition petition);
    @DeleteMapping("/{personId}")
    ResponseEntity<Void> delete(@PathVariable UUID personId);
    @PatchMapping("/{personId}/name")
    ResponseEntity<PersonView> updateName(@PathVariable UUID personId, @RequestBody UpdatePersonNameView nameView);
}
