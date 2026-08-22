alter table account
add column updated_at timestamp with time zone default now() not null;