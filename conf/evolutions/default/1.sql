# --- !Ups

CREATE TABLE IF NOT EXISTS "api_keys" (
    "id" integer PRIMARY KEY,
    "name" text NOT NULL UNIQUE,
    "alias" text NOT NULL UNIQUE,
    "scope" text NULL,
    "key" char(128) NULL
);

CREATE TABLE IF NOT EXISTS "place_type_use_types" (
    "id" integer PRIMARY KEY,
    "name" text NOT NULL UNIQUE
);

INSERT INTO "place_type_use_types" ("id", "name") VALUES
(1, 'Input - Place Search'),
(2, 'Output only - Place Search'),
(3, 'Input - Place Autocomplete')
;

CREATE TABLE IF NOT EXISTS "place_types" (
    "id" integer PRIMARY KEY,
    "name" text NOT NULL,
    "use_type_id" integer NOT NULL REFERENCES "place_type_use_types" (id) ON DELETE CASCADE,
    "deprecate" date NULL
);

INSERT INTO "place_types"("id", "name", "use_type_id", "deprecate") VALUES
(1, 'accounting', 1, NULL),
(2, 'airport', 1, NULL),
(3, 'amusement_park', 1, NULL),
(4, 'aquarium', 1, NULL),
(5, 'art_gallery', 1, NULL),
(6, 'atm', 1, NULL),
(7, 'bakery', 1, NULL),
(8, 'bank', 1, NULL),
(9, 'bar', 1, NULL),
(10, 'beauty_salon', 1, NULL),
(11, 'bicycle_store', 1, NULL),
(12, 'book_store', 1, NULL),
(13, 'bowling_alley', 1, NULL),
(14, 'bus_station', 1, NULL),
(15, 'cafe', 1, NULL),
(16, 'campground', 1, NULL),
(17, 'car_dealer', 1, NULL),
(18, 'car_rental', 1, NULL),
(19, 'car_repair', 1, NULL),
(20, 'car_wash', 1, NULL),
(21, 'casino', 1, NULL),
(22, 'cemetery', 1, NULL),
(23, 'church', 1, NULL),
(24, 'city_hall', 1, NULL),
(25, 'clothing_store', 1, NULL),
(26, 'convenience_store', 1, NULL),
(27, 'courthouse', 1, NULL),
(28, 'dentist', 1, NULL),
(29, 'department_store', 1, NULL),
(30, 'doctor', 1, NULL),
(31, 'electrician', 1, NULL),
(32, 'electronics_store', 1, NULL),
(33, 'embassy', 1, NULL),
(34, 'fire_station', 1, NULL),
(35, 'florist', 1, NULL),
(36, 'funeral_home', 1, NULL),
(37, 'furniture_store', 1, NULL),
(38, 'gas_station', 1, NULL),
(39, 'grocery_or_supermarket', 1, NULL),
(40, 'gym', 1, NULL),
(41, 'hair_care', 1, NULL),
(42, 'hardware_store', 1, NULL),
(43, 'hindu_temple', 1, NULL),
(44, 'home_goods_store', 1, NULL),
(45, 'hospital', 1, NULL),
(46, 'insurance_agency', 1, NULL),
(47, 'jewelry_store', 1, NULL),
(48, 'laundry', 1, NULL),
(49, 'lawyer', 1, NULL),
(50, 'library', 1, NULL),
(51, 'liquor_store', 1, NULL),
(52, 'local_government_office', 1, NULL),
(53, 'locksmith', 1, NULL),
(54, 'lodging', 1, NULL),
(55, 'meal_delivery', 1, NULL),
(56, 'meal_takeaway', 1, NULL),
(57, 'mosque', 1, NULL),
(58, 'movie_rental', 1, NULL),
(59, 'movie_theater', 1, NULL),
(60, 'moving_company', 1, NULL),
(61, 'museum', 1, NULL),
(62, 'night_club', 1, NULL),
(63, 'painter', 1, NULL),
(64, 'park', 1, NULL),
(65, 'parking', 1, NULL),
(66, 'pet_store', 1, NULL),
(67, 'pharmacy', 1, NULL),
(68, 'physiotherapist', 1, NULL),
(69, 'plumber', 1, NULL),
(70, 'police', 1, NULL),
(71, 'post_office', 1, NULL),
(72, 'real_estate_agency', 1, NULL),
(73, 'restaurant', 1, NULL),
(74, 'roofing_contractor', 1, NULL),
(75, 'rv_park', 1, NULL),
(76, 'school', 1, NULL),
(77, 'shoe_store', 1, NULL),
(78, 'shopping_mall', 1, NULL),
(79, 'spa', 1, NULL),
(80, 'stadium', 1, NULL),
(81, 'storage', 1, NULL),
(82, 'store', 1, NULL),
(83, 'subway_station', 1, NULL),
(84, 'synagogue', 1, NULL),
(85, 'taxi_stand', 1, NULL),
(86, 'train_station', 1, NULL),
(87, 'transit_station', 1, NULL),
(88, 'travel_agency', 1, NULL),
(89, 'university', 1, NULL),
(90, 'veterinary_care', 1, NULL),
(91, 'zoo', 1, NULL),

(92, 'administrative_area_level_1', 2, NULL),
(93, 'administrative_area_level_2', 2, NULL),
(94, 'administrative_area_level_3', 2, NULL),
(95, 'administrative_area_level_4', 2, NULL),
(96, 'administrative_area_level_5', 2, NULL),
(97, 'colloquial_area', 2, NULL),
(98, 'country', 2, NULL),
(99, 'establishment', 2, NULL),
(100, 'finance', 2, NULL),
(101, 'floor', 2, NULL),
(102, 'food', 2, NULL),
(103, 'general_contractor', 2, NULL),
(104, 'geocode', 2, NULL),
(105, 'health', 2, NULL),
(106, 'intersection', 2, NULL),
(107, 'locality', 2, NULL),
(108, 'natural_feature', 2, NULL),
(109, 'neighborhood', 2, NULL),
(110, 'place_of_worship', 2, NULL),
(111, 'political', 2, NULL),
(112, 'point_of_interest', 2, NULL),
(113, 'post_box', 2, NULL),
(114, 'postal_code', 2, NULL),
(115, 'postal_code_prefix', 2, NULL),
(116, 'postal_code_suffix', 2, NULL),
(117, 'postal_town', 2, NULL),
(118, 'premise', 2, NULL),
(119, 'room', 2, NULL),
(120, 'route', 2, NULL),
(121, 'street_address', 2, NULL),
(122, 'street_number', 2, NULL),
(123, 'sublocality', 2, NULL),
(124, 'sublocality_level_4', 2, NULL),
(125, 'sublocality_level_5', 2, NULL),
(126, 'sublocality_level_3', 2, NULL),
(127, 'sublocality_level_2', 2, NULL),
(128, 'sublocality_level_1', 2, NULL),
(129, 'subpremise', 2, NULL),

(130, 'geocode', 3, NULL),
(131, 'address', 3, NULL),
(132, 'establishment', 3, NULL),
(133, 'regions', 3, NULL),
(134, 'cities', 3, NULL),

(135, 'establishment', 1, '2017-2-16'),
(136, 'finance', 1, '2017-2-16'),
(137, 'food', 1, '2017-2-16'),
(138, 'general_contractor', 1, '2017-2-16'),
(139, 'health', 1, '2017-2-16'),
(140, 'place_of_worship', 1, '2017-2-16')
;

CREATE TABLE IF NOT EXISTS "place_type_notes" (
    "id" serial PRIMARY KEY,
    "note" text
);

CREATE TABLE IF NOT EXISTS "countries" (
    "id" integer PRIMARY KEY,
    "name" text NOT NULL UNIQUE,
    "code" character(3) NOT NULL DEFAULT ''
);

INSERT INTO "countries" ("id", "name", "code") VALUES
(1, 'United States', 'US')
;

CREATE TABLE IF NOT EXISTS "states_provinces" (
    "id" integer PRIMARY KEY,
    "name" text NOT NULL,
    "abbr" character(3) NOT NULL DEFAULT '',
    "country_id" integer NOT NULL REFERENCES "countries" (id) ON DELETE CASCADE
);

INSERT INTO "states_provinces" ("id", "name", "abbr", "country_id") VALUES
(1, 'Alabama', 'AL', 1),
(2, 'Alaska', 'AK', 1),
(3, 'Arizona', 'AZ', 1),
(4, 'Arkansas', 'AR', 1),
(5, 'California', 'CA', 1),
(6, 'Colorado', 'CO', 1),
(7, 'Connecticut', 'CT', 1),
(8, 'Delaware', 'DE', 1),
(9, 'District Of Columbia', 'DC', 1),
(10, 'Florida', 'FL', 1),
(11, 'Georgia', 'GA', 1),
(12, 'Hawaii', 'HI', 1),
(13, 'Idaho', 'ID', 1),
(14, 'Illinois', 'IL', 1),
(15, 'Indiana', 'IN', 1),
(16, 'Iowa', 'IA', 1),
(17, 'Kansas', 'KS', 1),
(18, 'Kentucky', 'KY', 1),
(19, 'Louisiana', 'LA', 1),
(20, 'Maine', 'ME', 1),
(21, 'Maryland', 'MD', 1),
(22, 'Massachusetts', 'MA', 1),
(23, 'Michigan', 'MI', 1),
(24, 'Minnesota', 'MN', 1),
(25, 'Mississippi', 'MS', 1),
(26, 'Missouri', 'MO', 1),
(27, 'Montana', 'MT', 1),
(28, 'Nebraska', 'NE', 1),
(29, 'Nevada', 'NV', 1),
(30, 'New Hampshire', 'NH', 1),
(31, 'New Jersey', 'NJ', 1),
(32, 'New Mexico', 'NM', 1),
(33, 'New York', 'NY', 1),
(34, 'North Carolina', 'NC', 1),
(35, 'North Dakota', 'ND', 1),
(36, 'Ohio', 'OH', 1),
(37, 'Oklahoma', 'OK', 1),
(38, 'Oregon', 'OR', 1),
(39, 'Pennsylvania', 'PA', 1),
(40, 'Rhode Island', 'RI', 1),
(41, 'South Carolina', 'SC', 1),
(42, 'South Dakota', 'SD', 1),
(43, 'Tennessee', 'TN', 1),
(44, 'Texas', 'TX', 1),
(45, 'Utah', 'UT', 1),
(46, 'Vermont', 'VT', 1),
(47, 'Virginia', 'VA', 1),
(48, 'Washington', 'WA', 1),
(49, 'West Virginia', 'WV', 1),
(50, 'Wisconsin', 'WI', 1),
(51, 'Wyoming', 'WY', 1)
;

CREATE TABLE IF NOT EXISTS "nearby_search_requests" (
    "id" serial PRIMARY KEY,
    "lat" double precision NOT NULL,
    "lng" double precision NOT NULL,
    "radius" integer NOT NULL,
    "params" json NULL DEFAULT NULL
);

# --- !Downs
DROP TABLE IF EXISTS "nearby_search_requests";
DROP TABLE IF EXISTS "states_provinces";
DROP TABLE IF EXISTS "countries";
DROP TABLE IF EXISTS "place_type_notes";
DROP TABLE IF EXISTS "place_types";
DROP TABLE IF EXISTS "place_type_use_types";
DROP TABLE IF EXISTS "api_keys";