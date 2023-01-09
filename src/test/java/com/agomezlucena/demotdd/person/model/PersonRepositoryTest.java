package com.agomezlucena.demotdd.person.model;

import lombok.val;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class PersonRepositoryTest {
    @Autowired
    private PersonRepository repository;
    @Autowired
    private DataSource dataSource;

    @BeforeEach
    public void setUp() {
        truncateTables();
        try (
                val connection = dataSource.getConnection();
                val personInsertionStatement = connection.prepareStatement("insert into persons(person_id,name,last_name) values(?,?,?)");
                val phoneInsertionStatement = connection.prepareStatement("insert into phone_lines(person_id,phone_line_id,international_prefix,phone_number) values (?,?,?,?)");
        ) {
            connection.setAutoCommit(false);
            Lists.list(
                            new Person(UUID.fromString("e9b7127e-8ce8-11ed-a1eb-0242ac120002"), "alex", "gomez lucena", Lists.newArrayList()),
                            new Person(UUID.fromString("f0c26a8c-8ce8-11ed-a1eb-0242ac120002"), "ana", "lucena pavón", Lists.newArrayList())
                    )
                    .forEach(it -> {
                        try {
                            if(it.getName().equals("alex")) it.setPhoneLines(Lists.list(new PhoneLine(UUID.fromString("1ca39482-8ce9-11ed-a1eb-0242ac120002"), "34", "662432215",it)));
                            personInsertionStatement.setObject(1, it.getId());
                            personInsertionStatement.setString(2, it.getName());
                            personInsertionStatement.setString(3, it.getLastName());
                            personInsertionStatement.addBatch();
                            if (!it.getPhoneLines().isEmpty()) {
                                it.getPhoneLines()
                                        .forEach(pl -> {
                                            try {
                                                phoneInsertionStatement.setObject(1, it.getId());
                                                phoneInsertionStatement.setObject(2, pl.getId().getPhoneLineId());
                                                phoneInsertionStatement.setString(3, pl.getInternationalPrefix());
                                                phoneInsertionStatement.setString(4, pl.getPhoneNumber());
                                                phoneInsertionStatement.addBatch();
                                            } catch (SQLException e) {
                                                throw new RuntimeException(e);
                                            }
                                        });
                            }
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });
            personInsertionStatement.executeBatch();
            connection.commit();
            phoneInsertionStatement.executeBatch();
            connection.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void truncateTables() {
        try (
                val connection = dataSource.getConnection();
                val truncateStatement = connection.prepareStatement("truncate table phone_lines, persons");
        ) {
            truncateStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void check_if_not_exists_a_given_person_return_a_empty_optional() {
        Optional<Person> obtainedValue = repository.findById(UUID.randomUUID());
        assertTrue(obtainedValue.isEmpty());
    }

    @Test
    void check_if_exist_a_given_person_return_the_expected_value() {
        val givenId = UUID.fromString("e9b7127e-8ce8-11ed-a1eb-0242ac120002");
        val expectedPerson = new Person(givenId, "alex", "gomez lucena", null);
        expectedPerson.setPhoneLines(Lists.newArrayList(new PhoneLine( UUID.fromString("e9b7127e-8ce8-11ed-a1eb-0242ac120002"), "34", "662432215",expectedPerson)));
        Optional<Person> obtainedValue = repository.findById(givenId);
        assertTrue(obtainedValue.isPresent());
        assertEquals(expectedPerson, obtainedValue.get());
        assertNotNull(obtainedValue.get().getPhoneLines());
        assertEquals(expectedPerson.getPhoneLines(),obtainedValue.get().getPhoneLines());
    }

    @AfterEach
    void tearDown() {
        truncateTables();
    }
}
