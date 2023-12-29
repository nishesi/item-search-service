drop table if exists item_sku;
drop table if exists remain;
drop table if exists item;
drop table if exists catalogue;
drop table if exists brand;

create table catalogue
(
    catalogue_id bigserial primary key,
    url          varchar not null,
    image        varchar not null,
    parent_id    bigint  not null references catalogue (catalogue_id),
    name         varchar not null
);

create table brand
(
    brand_id bigserial primary key,
    name     varchar
);

create table item
(
    item_id      bigserial primary key,
    catalogue_id bigint references catalogue (catalogue_id),
    brand_id     bigint references brand (brand_id),
    name         varchar,
    description  varchar,
    url     varchar,
    type         varchar,
    image        varchar
);

create table item_sku
(
    item_id bigint  not null references item (item_id) on delete cascade,
    sku     varchar not null unique
);

create table remain
(
    item_id   bigint references item (item_id) on delete cascade,
    region_id bigint not null,
    price     bigint not null
);
insert into brand (brand_id, name)
values
(1001, 'brand'),
(1002, 'brand'),
(1003, 'brand');
insert into catalogue (catalogue_id, url, image, parent_id, name)
values (101, 'catalogue_1', 'image', 101, 'catalogue'),
       (102, 'catalogue_2', 'image', 102, 'catalogue'),
       (103, 'catalogue_3', 'image', 103, 'catalogue'),
       (104, 'catalogue_4', 'image', 104, 'catalogue'),

       (105, 'catalogue_5', 'image', 105, 'cat_name_5'),
       (106, 'catalogue_6', 'image', 106, 'cat_name_6'),

       (110, 'non_uniq_url', 'image', 110, 'cat_name_10'),
       (111, 'non_uniq_url', 'image', 111, 'cat_name_11'),

       (112, 'smartphones', 'image', 113, 'cat_name_12'),
       (113, 'techniques', 'image', 113, 'cat_name_13'),

       (114, 'catalogue_1', 'image', 113, 'cat_name_14');

insert into item (item_id, catalogue_id, brand_id, name, description, url, type, image)
values (1, 101, 1001, 'name', 'desc', 'itemurl', 'type', 'i'),
       (2, 101, 1001, 'name', 'desc', 'itemurl2', 'type', 'i'),
       (3, 102, 1001, 'name', 'desc', 'itemurl', 'type', 'i'),

       (5, 103, 1001, 'name', 'desc', 'itemurl', 'type', 'i'),
       (6, 103, 1001, 'name', 'desc', 'itemurl', 'type', 'i'),

       (7, 103, 1001, 'name', 'desc', 'itemurl', 'type', 'i'),
       (8, 103, 1001, 'name', 'desc', 'itemurl', 'type', 'i'),

       (9, 104, 1002, 'Kit Kat', 'mega tasty', 'kit-kat-1', 'Chocolate Bar', 'kit-kat-image.jpg'),

       (10, 105, 1003, 'name', 'desc', 'itemurl', 'type', 'i'),
       (11, 106, 1003, 'name', 'desc', 'itemurl', 'type', 'i'),

       (15, 110, 1003, 'name', 'desc', 'itemurl', 'type', 'i'),
       (16, 111, 1003, 'name', 'desc', 'itemurl', 'type', 'i'),

       (17, 112, 1003, 'name', 'desc', 'itemurl', 'type', 'i'),

       (18, 114, 1003, 'name', 'desc', 'itemurl', 'type', 'i');

insert into remain (item_id, region_id, price)
values (1, 1, 10),
       (2, 1, 10),
       (3, 1, 10),

       (5, 2, 20),

       (7, 3, 101001001000),
       (7, 4, 40),
       (7, 5, 50),
       (8, 3, 30),
       (8, 4, 40),

       (9, 1, 10),

       (10, 1, 10),
       (11, 1, 10),

       (15, 1, 10),
       (16, 1, 10),

       (17, 1, 10),
       (18, 1, 10);

