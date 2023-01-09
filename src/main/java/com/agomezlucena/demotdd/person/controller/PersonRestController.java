package com.agomezlucena.demotdd.person.controller;

import com.agomezlucena.demotdd.person.model.Person;
import com.agomezlucena.demotdd.person.model.PersonRepository;
import com.agomezlucena.demotdd.person.model.PhoneLineRepository;
import com.agomezlucena.demotdd.person.view.PersonCreationPetition;
import com.agomezlucena.demotdd.person.view.PersonCreationResponse;
import com.agomezlucena.demotdd.person.view.PersonView;
import com.agomezlucena.demotdd.person.view.UpdatePersonNameView;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PersonRestController implements PersonApi {
    private final PersonRepository personRepository;
    private final PhoneLineRepository phoneLineRepository;

    @Override
    public ResponseEntity<List<PersonView>> getAll() {
        return ResponseEntity.ok(
                personRepository.findAll().stream().map(it -> {
                    val view = new PersonView();
                    view.setId(it.getId());
                    view.setName(it.getName());
                    view.setLastName(it.getLastName());
                    return view;
                }).collect(Collectors.toList())
        );
    }

    @Override
    public ResponseEntity<Person> findById(UUID personId) {
        return personRepository.findById(personId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<PersonCreationResponse> create(PersonCreationPetition petition) {
        val generatedPerson = new Person(UUID.randomUUID(), petition.getName(), petition.getLastName(), null);
        val response = new PersonCreationResponse();
        personRepository.save(generatedPerson);

        response.setId(generatedPerson.getId());
        response.setName(generatedPerson.getName());
        response.setLastName(generatedPerson.getLastName());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    @Transactional
    public ResponseEntity<Void> delete(UUID personId) {
        personRepository.findById(personId)
                .ifPresent(it -> {
                    phoneLineRepository.deleteByPerson(it);
                    personRepository.delete(it);
                });
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<PersonView> updateName(UUID personId, UpdatePersonNameView nameView) {
        return personRepository.findById(personId)
                .map(it -> {
                    it.setName(nameView.getName());
                    return personRepository.save(it);
                })
                .map(it -> {
                    val personView = new PersonView();
                    personView.setId(it.getId());
                    personView.setName(it.getName());
                    personView.setLastName(it.getLastName());
                    return personView;
                })
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
