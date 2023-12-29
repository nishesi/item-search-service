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
    url          varchar,
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

insert into catalogue (catalogue_id, url, image, parent_id, name)
values
    (101, 'cat_url_1', 'image', 101, 'catalogue_1'),
    (102, 'cat_url_2', 'image', 102, 'catalogue_2');

insert into brand (brand_id, name) values (1001, 'brand_1');

insert into item (item_id, catalogue_id, brand_id, name, description, url, type, image)
values (1, 101, 1001, 'default name 1', 'def desc 1', 'item_url', 'type', 'i');