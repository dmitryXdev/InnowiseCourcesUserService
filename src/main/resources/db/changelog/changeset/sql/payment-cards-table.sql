CREATE TABLE payment_cards(
    id bigserial primary key,
    user_id bigint not null,
    number varchar(20) unique not null,
    holder varchar(255) not null,
    expiration_date timestamp not null,
    active boolean not null,
    created_at timestamp not null,
    updated_at timestamp,
    foreign key (user_id) references users(id)
)