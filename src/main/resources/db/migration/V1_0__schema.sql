create table persons
(
    person_id uuid primary key,
    name      varchar not null,
    last_name varchar not null
);

create table phone_lines
(
    person_id            uuid,
    phone_line_id        uuid,
    international_prefix varchar not null,
    phone_number         varchar not null,
    constraint pk_phone_lines primary key (person_id,phone_line_id)
);

alter table phone_lines add constraint fk_to_persons foreign key (person_id) references persons(person_id) on delete cascade;