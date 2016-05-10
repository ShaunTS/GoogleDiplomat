# --- !Ups
CREATE TABLE IF NOT EXISTS "fake_users" (
    "id" serial PRIMARY KEY,
    "name" text NOT NULL,
    "email" text NOT NULL UNIQUE,
    "created" timestamp(3) with time zone NOT NULL
);

CREATE TABLE IF NOT EXISTS "fake_groups" (
    "id" serial PRIMARY KEY,
    "name" text NOT NULL UNIQUE
);

INSERT INTO "fake_users" ("name", "email", "created") VALUES
('Jawa-01', 'joe01@gmail.jawa', '2016-05-09 16:01:10.642'::timestamp - INTERVAL '1 day'),
('Jawa-02', 'joe02@gmail.jawa', '2016-05-09 16:01:10.642');

INSERT INTO "fake_groups" ("name") VALUES
('General Users'),
('Jawas');

# --- !Downs
DROP TABLE IF EXISTS "fake_groups";
DROP TABLE IF EXISTS "fake_users";