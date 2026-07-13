CREATE TABLE users
(
    id         bigserial primary key,
    name       varchar(255)        not null,
    surname    varchar(255)        not null,
    birth_date date,
    email      varchar(255) unique not null,
    active     boolean             not null,
    created_at timestamp           not null,
    updated_at timestamp
)