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
alter table remain add constraint uniq_item_reg unique (item_id, region_id);