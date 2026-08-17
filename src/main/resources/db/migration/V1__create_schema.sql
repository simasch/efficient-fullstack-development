create table app_user
(
    id        bigint generated always as identity primary key,
    username  varchar(50)  not null unique,
    full_name varchar(200) not null,
    email     varchar(255) not null,
    active    boolean      not null default true
);

create table project
(
    id          bigint generated always as identity primary key,
    name        varchar(200)             not null,
    description text,
    owner_id    bigint                   not null references app_user (id),
    created_at  timestamp with time zone not null default now()
);

create table task
(
    id             bigint generated always as identity primary key,
    project_id     bigint                   not null references project (id),
    parent_id      bigint references task (id),
    assignee_id    bigint references app_user (id),
    title          varchar(200)             not null,
    description    text,
    status         varchar(20)              not null,
    priority       varchar(20)              not null,
    due_date       date,
    estimate_hours integer,
    created_at     timestamp with time zone not null default now(),
    version        integer                  not null default 0
);

create index idx_task_project on task (project_id);
create index idx_task_assignee on task (assignee_id);
create index idx_task_due_date on task (due_date, id);
