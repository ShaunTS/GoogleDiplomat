
# --- !Ups
INSERT INTO "nearby_search_requests" ("id", "lat", "lng", "radius", "params") VALUES
(1, 41.4804518, -73.2200572, 1000, '{"name": "Southbury Library"}');

# ---!Downs
DELETE FROM "nearby_search_requests";
ALTER SEQUENCE "nearby_search_requests_id_seq" RESTART;
