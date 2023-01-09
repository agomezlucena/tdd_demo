package com.agomezlucena.demotdd.person.controller;

import com.agomezlucena.demotdd.person.model.Person;
import com.agomezlucena.demotdd.person.model.PersonRepository;
import com.agomezlucena.demotdd.person.model.PhoneLine;
import com.agomezlucena.demotdd.person.model.PhoneLineRepository;
import com.agomezlucena.demotdd.person.view.PersonCreationPetition;
import com.agomezlucena.demotdd.person.view.PersonCreationResponse;
import com.agomezlucena.demotdd.person.view.PersonView;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
public class PersonRestApiTest {
    private MockMvc mockMvc;

    @Autowired
    private PersonRepository repository;
    @Autowired
    private PhoneLineRepository phoneLineRepository;
    @Autowired
    private ObjectMapper mapper;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext, RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(documentationConfiguration(restDocumentation))
                .build();
    }

    @BeforeEach
    void initPersons() {
        Stream.<Person>builder()
                .add(new Person(UUID.fromString("e9b7127e-8ce8-11ed-a1eb-0242ac120002"), "alex", "gomez lucena", Lists.newArrayList()))
                .add(new Person(UUID.fromString("f0c26a8c-8ce8-11ed-a1eb-0242ac120002"), "ana", "lucena pavon", Lists.newArrayList()))
                .build()
                .map(repository::save)
                .forEach(it -> {
                    var givenPhoneLine = new PhoneLine(UUID.fromString("1ca39482-8ce9-11ed-a1eb-0242ac120002"), "34", "662432215", it);
                    givenPhoneLine = phoneLineRepository.save(givenPhoneLine);
                    it.getPhoneLines().add(givenPhoneLine);
                    repository.save(it);
                });
    }

    @Test
    void check_if_return_the_expected_list_of_people() throws Exception {
        val responseAsString = mockMvc.perform(
                        get("/api/v1/persons")
                                .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andDo(
                        document(
                                "get all persons",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint())
                        )
                ).andReturn().getResponse().getContentAsString();
        List<PersonView> response = mapper.readerForListOf(PersonView.class).readValue(responseAsString);
        assertThat(response).isNotEmpty();
        assertThat(response).hasSizeGreaterThanOrEqualTo(2);
        assertNotNull(response.get(0).getId());
        assertNotNull(response.get(0).getName());
        assertNotNull(response.get(0).getLastName());
    }

    @Test
    void check_if_return_the_expected_person_for_a_given_id() throws Exception {
        val result = mockMvc.perform(
                        get("/api/v1/persons/{personId}", "e9b7127e-8ce8-11ed-a1eb-0242ac120002")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andDo(
                        document(
                                "find person by id",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint())
                        )
                )
                .andReturn()
                .getResponse()
                .getContentAsString();
        val resultAsJsonNode = mapper.readTree(result);
        val phoneLines = resultAsJsonNode
                .findPath("phoneLines");

        assertEquals("alex", resultAsJsonNode.findPath("name").asText());
        assertEquals("gomez lucena", resultAsJsonNode.findPath("lastName").asText());
        assertTrue(phoneLines.isArray());
        assertFalse(phoneLines.isEmpty());
    }

    @Test
    void check_if_you_dont_find_any_person_return_a_404_code() throws Exception {
        mockMvc.perform(
                        get("/api/v1/persons/{personId}", "e9b7127e-8ce8-11ed-a1eb-0242ac120003")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andDo(
                        document(
                                "find non existing person by id",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint())
                        )
                );
    }

    @Test
    void check_if_generate_the_expected_person_for_a_given_value() throws Exception {
        val givenName = "alejandro";
        val givenLastName = "gomez lucena";
        val givenPersonCreationView = new PersonCreationPetition();
        givenPersonCreationView.setName(givenName);
        givenPersonCreationView.setLastName(givenLastName);
        val creationViewAsJson = mapper.writeValueAsString(givenPersonCreationView);
        val response = mockMvc.perform(
                        post("/api/v1/persons")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(creationViewAsJson)
                ).andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andDo(
                        document(
                                "create person ok",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint())
                        )
                )
                .andReturn()
                .getResponse()
                .getContentAsString();

        val obtainedPersonCreationResponse = mapper.readValue(response, PersonCreationResponse.class);
        assertNotNull(obtainedPersonCreationResponse.getId());
        assertEquals(givenName, obtainedPersonCreationResponse.getName());
        assertEquals(givenLastName, obtainedPersonCreationResponse.getLastName());
    }

    @Test
    void check_if_you_try_to_create_a_person_without_name_should_not_allow_to_create_it() throws Exception {
        val givenLastName = "gomez lucena";
        val givenPersonCreationView = new PersonCreationPetition();
        givenPersonCreationView.setLastName(givenLastName);
        val creationViewAsJson = mapper.writeValueAsString(givenPersonCreationView);
        mockMvc.perform(
                        post("/api/v1/persons")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(creationViewAsJson)
                ).andExpect(status().isBadRequest())
                .andDo(
                        document(
                                "create person ko without name",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint())
                        )
                );
    }

    @Test
    void check_if_you_try_to_create_a_person_without_lastname_should_not_allow_to_create_it() throws Exception {
        val givenName = "alejandro";
        val givenPersonCreationView = new PersonCreationPetition();
        givenPersonCreationView.setName(givenName);
        val creationViewAsJson = mapper.writeValueAsString(givenPersonCreationView);
        mockMvc.perform(
                        post("/api/v1/persons")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(creationViewAsJson)
                ).andExpect(status().isBadRequest())
                .andDo(
                        document(
                                "create person ko without lastname",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint())
                        )
                );
    }

    @Test
    void check_if_you_can_delete_an_existing_person() throws Exception {
        assertTrue(repository.existsById(UUID.fromString("f0c26a8c-8ce8-11ed-a1eb-0242ac120002")));
        mockMvc.perform(
                        delete("/api/v1/persons/{personId}", "f0c26a8c-8ce8-11ed-a1eb-0242ac120002")
                                .accept(MediaType.APPLICATION_JSON)
                ).andExpect(status().isNoContent())
                .andDo(
                        document(
                                "delete person",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint())
                        )
                );
        assertFalse(repository.findById(UUID.fromString("f0c26a8c-8ce8-11ed-a1eb-0242ac120002")).isPresent());
    }

    @Test
    void check_if_you_can_update_the_name_of_a_existing_person() throws Exception {
        assertTrue(repository.existsById(UUID.fromString("e9b7127e-8ce8-11ed-a1eb-0242ac120002")));
        val givenJsonUpdateString = """
                {
                    "name":"alejandro"
                }
                """;

        val responseAsJsonString = mockMvc.perform(
                        patch("/api/v1/persons/{personId}/name",
                                "e9b7127e-8ce8-11ed-a1eb-0242ac120002")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(givenJsonUpdateString)
                ).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andDo(document(
                        "update person name ok",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ))
                .andReturn().getResponse().getContentAsString();

        val responseAsObject = mapper.readValue(responseAsJsonString, PersonView.class);

        assertEquals(UUID.fromString("e9b7127e-8ce8-11ed-a1eb-0242ac120002"), responseAsObject.getId());
        assertEquals("alejandro", responseAsObject.getName());
        assertEquals("gomez lucena", responseAsObject.getLastName());
    }

    @Test
    void check_if_you_try_to_update_the_name_of_a_non_existing_person_return_a_not_found_status_without_body() throws Exception {
        val givenPetition = """
                {
                    "name":"Fernando"
                }
                """;
        mockMvc.perform(
                        patch(
                                "/api/v1/persons/{personId}/name",
                                "f0c26a8c-8ce8-11ed-a1eb-0242ac120013"
                        ).accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(givenPetition)
                ).andExpect(status().isNotFound())
                .andDo(
                        document(
                                "update person name ko",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint())
                        )
                );
    }

    @AfterEach
    void tearDown() {
        repository.deleteAll();
        phoneLineRepository.deleteAll();
    }
}
