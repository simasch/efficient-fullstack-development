alter table app_user
    add column password_hash varchar(100) not null default '';

create table user_role
(
    user_id bigint      not null references app_user (id) on delete cascade,
    role    varchar(20) not null,

    primary key (user_id, role)
);
