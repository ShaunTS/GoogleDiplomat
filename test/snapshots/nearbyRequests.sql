# --- !Ups
INSERT INTO "nearby_search_requests" ("id", "lat", "lng", "radius", "params") VALUES
(1, 41.4804518, -73.2200572, 750, toJSON('{"name": "Southbury Library", "opennow": true, "types" : ["pharmacy","home_goods_store","electronics_store","store","point_of_interest","establishment"]}')),
(2, 40.5, -72.3, 1000, NULL),
(3, 42.5, -72.3, 1005, toJSON('{}'));

# --- !Downs
DELETE FROM "nearby_search_requests";
ALTER SEQUENCE "nearby_search_requests_id_seq" RESTART;