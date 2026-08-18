-- The task list filters titles and descriptions with a case-insensitive infix search
-- (lower(title) like '%text%'). A B-tree index cannot serve a leading wildcard, so the
-- filter degrades into a sequential scan as the table grows. Trigram indexes can.
-- Chapter 8 measures both plans.
create extension if not exists pg_trgm;

create index idx_task_title_trgm on task using gin (lower(title) gin_trgm_ops);
create index idx_task_description_trgm on task using gin (lower(description) gin_trgm_ops);
