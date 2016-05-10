# --- !Ups

CREATE TABLE IF NOT EXISTS "fake_users_groups" (
    "user_id" integer NOT NULL REFERENCES "fake_users" ("id") ON DELETE CASCADE,
    "group_id" integer NOT NULL REFERENCES "fake_groups" ("id") ON DELETE CASCADE,
    UNIQUE("user_id", "group_id")
);

INSERT INTO "fake_users_groups" ("user_id", "group_id") VALUES
(1, 1),
(1, 2),
(2, 2);


# --- !Downs
DROP TABLE IF EXISTS "fake_users_groups";