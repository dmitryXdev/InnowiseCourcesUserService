CREATE TABLE payment_cards(
    id bigserial primary key,
    user_id bigint not null,
    number varchar(20) unique not null,
    holder varchar(255) not null,
    expiration_date date not null,
    active boolean not null,
    created_at date not null,
    updated_at date,
    foreign key (user_id) references users(id)
)