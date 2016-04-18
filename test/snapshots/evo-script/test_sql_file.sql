# --- !Ups

CREATE TABLE IF NOT EXISTS "test_api_keys" (
    "id" integer PRIMARY KEY,
    "name" text NOT NULL UNIQUE,
    "alias" text NOT NULL UNIQUE,
    "scope" text NULL,
    "key" char(128) NULL
);

CREATE TABLE IF NOT EXISTS "test_place_type_use_types" (
    "id" integer PRIMARY KEY,
    "name" text NOT NULL UNIQUE
);

INSERT INTO "test_place_type_use_types" ("id", "name") VALUES
(1, 'Input - Place Search'),
(2, 'Output only - Place Search'),
(3, 'Input - Place Autocomplete')
;

CREATE TABLE IF NOT EXISTS "test_place_types" (
    "id" integer PRIMARY KEY,
    "name" text NOT NULL,
    "use_type_id" integer NOT NULL REFERENCES "test_place_type_use_types" (id) ON DELETE CASCADE,
    "deprecate" date NULL
);

INSERT INTO "test_place_types"("id", "name", "use_type_id", "deprecate") VALUES
(1, 'accounting', 1, NULL),
(2, 'airport', 1, NULL),
(3, 'amusement_park', 1, NULL),
(4, 'aquarium', 1, NULL),
(5, 'art_gallery', 1, NULL),
(6, 'atm', 1, NULL),

(7, 'establishment', 1, '2017-2-16'),
(8, 'finance', 1, '2017-2-16'),
(9, 'food', 1, '2017-2-16'),
(10, 'general_contractor', 1, '2017-2-16'),
(11, 'health', 1, '2017-2-16'),
(12, 'place_of_worship', 1, '2017-2-16')
;

CREATE TABLE IF NOT EXISTS "test_nearby_search_requests" (
    "id" serial PRIMARY KEY,
    "lat" double precision NOT NULL,
    "lng" double precision NOT NULL,
    "radius" integer NOT NULL,
    "params" json NULL DEFAULT NULL
)
;

INSERT INTO "test_nearby_search_requests" ("id", "lat", "lng", "radius", "params") VALUES
(1, 41.4804518, -73.2200572, 1000, '{"name": "Southbury Library"}');

# --- !Downs
DROP TABLE IF EXISTS "nearby_search_requests";
DROP TABLE IF EXISTS "test_place_types";
DROP TABLE IF EXISTS "test_place_type_use_types";
DROP TABLE IF EXISTS "test_api_keys";