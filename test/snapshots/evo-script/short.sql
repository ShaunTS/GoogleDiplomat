# --- !Ups

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

INSERT INTO "temp"(id, name) VALUES (1, "aaa");

# --- !Downs
DROP TABLE IF EXISTS "test_nearby_search_requests";
DROP TABLE IF EXISTS "test_place_types";
DROP TABLE IF EXISTS "test_place_type_use_types";
DROP TABLE IF EXISTS "test_api_keys";