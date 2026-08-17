-- Demo users. The password of each user is the username, hashed with bcrypt.
insert into app_user (username, full_name, email, password_hash)
values ('admin', 'Alex Keller', 'admin@example.com', '$2y$10$mM.uSdxefiaQts3hjYogo.a/245wdTgHOBe16kkG/lKPvFSd8wDK.'),
       ('alice', 'Alice Meyer', 'alice@example.com', '$2y$10$Fu2v/oduwVEs0V39GMUhm.iSNw9K69tVQTUh72qeJQ492DxelIXE2'),
       ('bob', 'Bob Fischer', 'bob@example.com', '$2y$10$r/Dosu.KjmxKipo7yi2Nz.gmB4SEY/5.zh8/HlbEr2Ayiq0jR5GdS');

insert into user_role (user_id, role)
values ((select id from app_user where username = 'admin'), 'ADMIN'),
       ((select id from app_user where username = 'admin'), 'USER'),
       ((select id from app_user where username = 'alice'), 'USER'),
       ((select id from app_user where username = 'bob'), 'USER');
