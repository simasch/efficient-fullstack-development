-- Demo users, projects and tasks for development (spring-boot:test-run) and tests.
-- Lives on the test classpath only; production starts with an empty database.
-- Numbered V999 so it always runs last and never collides with future main migrations.

-- The password of each user is the username, hashed with bcrypt.
insert into app_user (username, full_name, email, password_hash)
values ('admin', 'Alex Keller', 'admin@example.com', '$2y$10$mM.uSdxefiaQts3hjYogo.a/245wdTgHOBe16kkG/lKPvFSd8wDK.'),
       ('alice', 'Alice Meyer', 'alice@example.com', '$2y$10$Fu2v/oduwVEs0V39GMUhm.iSNw9K69tVQTUh72qeJQ492DxelIXE2'),
       ('bob', 'Bob Fischer', 'bob@example.com', '$2y$10$r/Dosu.KjmxKipo7yi2Nz.gmB4SEY/5.zh8/HlbEr2Ayiq0jR5GdS');

insert into user_role (user_id, role)
values ((select id from app_user where username = 'admin'), 'ADMIN'),
       ((select id from app_user where username = 'admin'), 'USER'),
       ((select id from app_user where username = 'alice'), 'USER'),
       ((select id from app_user where username = 'bob'), 'USER');

insert into project (name, description, owner_id)
values ('Website Relaunch', 'Replace the old CMS with the new corporate website',
        (select id from app_user where username = 'admin')),
       ('Mobile App', 'Native mobile client for the customer portal',
        (select id from app_user where username = 'alice'));

insert into task (project_id, assignee_id, title, description, status, priority, due_date, estimate_hours)
values ((select id from project where name = 'Website Relaunch'),
        (select id from app_user where username = 'alice'),
        'Define information architecture', 'Sitemap and navigation concept', 'DONE', 'HIGH',
        current_date - 20, 16),
       ((select id from project where name = 'Website Relaunch'),
        (select id from app_user where username = 'alice'),
        'Design landing page', 'Hero, teaser sections, footer', 'IN_PROGRESS', 'HIGH',
        current_date + 3, 24),
       ((select id from project where name = 'Website Relaunch'),
        (select id from app_user where username = 'bob'),
        'Set up CMS', 'Install and configure the content management system', 'IN_PROGRESS', 'MEDIUM',
        current_date + 7, 8),
       ((select id from project where name = 'Website Relaunch'),
        (select id from app_user where username = 'bob'),
        'Migrate news articles', 'Import the last two years of news', 'OPEN', 'LOW',
        current_date + 21, 12),
       ((select id from project where name = 'Website Relaunch'),
        null,
        'Write imprint and privacy policy', 'Legal needs to review the drafts', 'BLOCKED', 'MEDIUM',
        current_date - 2, 4),
       ((select id from project where name = 'Website Relaunch'),
        (select id from app_user where username = 'admin'),
        'Configure web analytics', 'Cookie-less analytics, respect do-not-track', 'OPEN', 'LOW',
        current_date + 30, 4),
       ((select id from project where name = 'Website Relaunch'),
        (select id from app_user where username = 'alice'),
        'Accessibility audit', 'WCAG 2.1 AA check of all page templates', 'OPEN', 'HIGH',
        current_date + 14, 16),
       ((select id from project where name = 'Mobile App'),
        (select id from app_user where username = 'alice'),
        'Evaluate push notification service', 'Compare vendors, pricing and GDPR compliance', 'DONE', 'MEDIUM',
        current_date - 10, 8),
       ((select id from project where name = 'Mobile App'),
        (select id from app_user where username = 'bob'),
        'Implement login screen', 'Username/password plus biometric unlock', 'IN_PROGRESS', 'HIGH',
        current_date + 2, 16),
       ((select id from project where name = 'Mobile App'),
        (select id from app_user where username = 'bob'),
        'Offline mode', 'Cache the last synced state and queue changes', 'OPEN', 'HIGH',
        current_date + 28, 40),
       ((select id from project where name = 'Mobile App'),
        (select id from app_user where username = 'alice'),
        'App store screenshots', 'Marketing needs final designs first', 'BLOCKED', 'LOW',
        current_date + 10, 4),
       ((select id from project where name = 'Mobile App'),
        null,
        'Crash reporting', 'Integrate a crash reporting SDK', 'OPEN', 'MEDIUM',
        current_date + 12, 8),
       ((select id from project where name = 'Mobile App'),
        (select id from app_user where username = 'admin'),
        'Performance test on old devices', 'Test on the five most used devices older than 4 years', 'OPEN', 'MEDIUM',
        current_date - 1, 8),
       ((select id from project where name = 'Mobile App'),
        (select id from app_user where username = 'bob'),
        'Set up CI pipeline', 'Build, test and sign on every merge', 'DONE', 'HIGH',
        current_date - 15, 8),
       ((select id from project where name = 'Mobile App'),
        (select id from app_user where username = 'alice'),
        'Release candidate 1', 'Feature freeze and regression test', 'OPEN', 'HIGH',
        current_date + 45, 24);

-- Two subtasks to demonstrate the task hierarchy (task.parent_id)
insert into task (project_id, parent_id, assignee_id, title, status, priority, due_date, estimate_hours)
values ((select id from project where name = 'Website Relaunch'),
        (select id from task where title = 'Design landing page'),
        (select id from app_user where username = 'alice'),
        'Design mobile variant of landing page', 'OPEN', 'MEDIUM', current_date + 3, 8),
       ((select id from project where name = 'Website Relaunch'),
        (select id from task where title = 'Design landing page'),
        (select id from app_user where username = 'bob'),
        'Provide landing page assets', 'OPEN', 'LOW', current_date + 1, 2);
