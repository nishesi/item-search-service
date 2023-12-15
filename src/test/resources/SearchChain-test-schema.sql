create table item
(
    item_id      bigint,
    catalogue_id bigint,
    brand_id     bigint,
    name         varchar,
    description  varchar,
    itemurl      varchar,
    type         varchar,

    i            varchar,
    brand        varchar,
    catalogue    varchar
);

create table item_sku
(
    item_id bigint,
    sku     varchar
);

insert into item (item_id, catalogue_id, brand_id, name, description, itemurl, type, i, brand, catalogue)
values
    (1, 101, 1001, 'name 1', 'description 1', '/item_url1', 'type 1', 'i', 'brand 1', 'catalogue 1'),
    (2, 101, 1001, '200002', 'description 1', '/item_url2', 'type 1', 'i', 'brand 1', 'catalogue 1'),
    (3, 101, 1001, 'Something 200003', 'description 1', '/item_url2', '300001', 'i', 'brand 1', 'catalogue 1'),
    (4, 102, 1001, '200003 Another', 'description 1', '/item_url2', '300001', 'i', 'brand 1', 'catalogue 2'),
    (5, 102, 1001, 'Something 200003 Another', 'description 1', '/item_url2', '300001', 'i', 'brand 1', 'catalogue 2');


insert into item_sku (item_id, sku)
values
    (1, '100001')