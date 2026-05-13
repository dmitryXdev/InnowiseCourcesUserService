CREATE TABLE cards(
    id bigint primary key,
    user_id bigint not null,
    number varchar(20) not null,
    holder varchar(255) not null,
    expiration_date date not null,
    active boolean not null,
    foreign key (user_id) references users(id)
)