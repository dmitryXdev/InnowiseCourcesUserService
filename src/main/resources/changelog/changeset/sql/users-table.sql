CREATE TABLE users(
    id bigint primary key ,
    name varchar(255) not null ,
    surname varchar(255) not null ,
    birth_date date,
    email varchar(255) not null,
    active boolean not null
)