# --- !Ups

CREATE TABLE IF NOT EXISTS "fake_users"(
    "id" serial PRIMARY KEY,
    "name" varchar NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS "users_notes"(
    "user_id" integer NOT NULL REFERENCES "fake_users" ("id") ON DELETE RESTRICT,
    "note" text NOT NULL
);

CREATE TABLE IF NOT EXISTS "fake_groups"(
    "id" serial PRIMARY KEY,
    "name" varchar NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS "fake_users_groups"(
    "user_id" integer NOT NULL REFERENCES "fake_users" ("id") ON DELETE CASCADE,
    "group_id" integer NOT NULL REFERENCES "fake_groups" ("id") ON DELETE CASCADE,
    UNIQUE("user_id", "group_id")
);

# --- !Downs
DROP TABLE IF EXISTS "fake_users_groups";
DROP TABLE IF EXISTS "fake_groups";
DROP TABLE IF EXISTS "user_address";
DROP TABLE IF EXISTS "fake_users";