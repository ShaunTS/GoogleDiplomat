
# --- !Ups
INSERT INTO "nearby_search_requests" ("id", "lat", "lng", "radius", "params") VALUES
(1, 41.4804518, -73.2200572, 750, toJSON('{"name": "Southbury Library", "opennow": true, "types" : ["pharmacy","home_goods_store","electronics_store","store","point_of_interest","establishment"]}'));

# --- !Downs
DELETE FROM "nearby_search_requests";
ALTER SEQUENCE "nearby_search_requests_id_seq" RESTART;