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

insert into brand (brand_id, name) values
                                       (1001, 'brand_1'),
                                       (1002, 'Xiaomi'),
                                       (1011, 'Xiaomi'),
                                       (1012, 'Samsung'),
                                       (1013, 'Apple'),
                                       (1014, 'Honor'),
                                       (1020, 'Toyota'),
                                       (1021, 'Chevrolet'),
                                       (1030, 'brand');

insert into catalogue (catalogue_id, url, image, parent_id, name)
values (101, 'catalogue_1', 'image', 101, 'catalogue_1'),
       (102, 'catalogue_1', 'image', 101, 'catalogue_2'),
       (111, 'catalogue_1', 'image', 101, 'China'),
       (112, 'catalogue_1', 'image', 101, 'Corey'),
       (113, 'catalogue_1', 'image', 101, 'USA'),

       (120, 'catalogue_20', 'image', 120, 'Автомобили'),
       (121, 'catalogue_20', 'image', 120, 'Электромобили'),

       (131, 'catalogue_20', 'image', 131, 'Футболки'),
       (132, 'catalogue_20', 'image', 132, 'Шорты');

insert into item (item_id, catalogue_id, brand_id, name, description, url, type, image)
values (1, 101, 1001, 'name_1', 'description_1', '/item_url1', 'type_1', 'some_image'),
       (2, 101, 1001, '200002', 'description_1', '/item_url2', 'type_1', 'some_image'),
       (3, 101, 1001, 'Something 200003', 'description_1', '/item_url2', '300001', 'some_image'),
       (4, 102, 1001, '200003 Another', 'description_1', '/item_url2', '300001', 'some_image'),
       (5, 102, 1001, 'Something 200003 Another', 'description_1', '/item_url2', '300001', 'some_image'),
       (6, 102, 1001, 'Laptop', 'description_1', '/item_url2', '300001', 'some_image'),
       (7, 102, 1001, 'Gaming Laptop', 'description_1', '/item_url2', '300001', 'some_image'),
       (8, 102, 1002, 'name_2', 'description_1', '/item_url2', 'type_2', 'some_image'),
       (9, 111, 1011, 'redmi note 8 pro', 'description_1', '/item_url2', 'smartphone', 'some_image'),
       (10, 112, 1012, 'galaxy 10', 'description_1', '/item_url2', 'smartphone', 'some_image'),
       (11, 111, 1011, 'mi 14', 'desc_word', '/item_url2', 'vacuum cleaner', 'some_image'),
       (12, 113, 1013, 'Iphone 13', 'description_1', '/item_url2', 'smartphone', 'some_image'),

       (13, 113, 1013, 'Iphone 14', 'description_1', '/item_url2', 'smartphone', 'some_image'),
       (15, 113, 1013, 'Mac Air', 'description_1', '/item_url2', 'notebook', 'some_image'),
       (16, 112, 1014, 'MagicBook', 'description_1', '/item_url2', 'notebook', 'some_image'),
       (14, 101, 1013, 'something 13 Air', 'description_1', '/item_url2', 'type_1', 'some_image'),
       (17, 113, 1014, 'MagicBook', 'description_1', '/item_url2', 'notebook', 'some_image'),
-- id used in another test        (18, 113, 1014, 'MagicBook', 'description_1', '/item_url2', 'notebook'),

       (20, 120, 1020, 'Rav 4 2018 новый', 'new urban comfortable large', '/item_url18', 'Кроссовер', 'some_new_image'),
       (24, 120, 1020, 'Rav 4 2018 БУ', 'new urban comfortable small', '/item_url18', 'Кроссовер', 'some_new_image'),
       (29, 120, 1020, 'Rav 10 2018 БУ', 'standard desc', '/item_url18', 'Хэтчбек', 'some_new_image'),
       (21, 120, 1021, 'Cruze 2019 новый', 'standard desc', '/item_url18', 'Кроссовер', 'some_new_image'),
       (22, 120, 1021, 'Cruze 2019 БУ', 'standard desc', '/item_url18', 'Кроссовер', 'some_new_image'),
       (23, 120, 1021, 'Cruze 2019 Кроссовер', 'standard desc', '/item_url18', 'автомобиль', 'some_new_image'),
       (30, 121, 1021, 'Cruze 2019 новый', 'standard desc', '/item_url18', 'Кроссовер', 'some_new_image'),
       (25, 121, 1021, 'Cruze 2020 БУ', 'standard desc', '/item_url18', 'Кроссовер', 'some_new_image'),
       (26, 121, 1020, 'Rav 4 2120', 'standard desc', '/item_url18', 'Кроссовер', 'some_new_image'),
       (27, 120, 1021, 'Aveo 2010 БУ', 'standard desc', '/item_url18', 'Хэтчбек', 'some_new_image'),
       (28, 120, 1020, 'Corolla 2013 БУ', 'standard desc', '/item_url18', 'Хэтчбек', 'some_new_image'),

       (31, 131, 1030, 'Trasher', 'description_1', '/item_url_1', 'Оверсайз', 'some_image'),
       (32, 131, 1030, 'Черная с черепом', 'description_1', '/item_url_1', 'Оверсайз', 'some_image'),
       (33, 131, 1030, 'Trasher', 'description_1', '/item_url_1', 'Обычный',  'some_image'),
       (34, 131, 1030, 'Белая стандартная', 'description_1', '/item_url_1', 'Обычный',  'some_image'),
       (35, 132, 1030, 'Trasher', 'description_1', '/item_url_1', 'Обычный',  'some_image'),
       (36, 132, 1030, 'Черная с черепом', 'description_1', '/item_url_1', 'Обычный',  'some_image'),
       (37, 132, 1030, 'Trasher', 'description_1', '/item_url_1', 'Оверсайз', 'some_image'),
       (38, 132, 1030, 'Белая стандартная', 'description_1', '/item_url_1', 'Оверсайз', 'some_image');

insert into item_sku (item_id, sku)
values (1, '100001');
-- used in another test        (18, '100002')

insert into remain (item_id, region_id, price)
values (1, 1, 1),
       (2, 1, 1),
       (3, 1, 1),
       (4, 1, 1),
       (5, 1, 1),
       (6, 1, 1),
       (7, 1, 1),
       (8, 1, 1),
       (9, 1, 1),
       (10, 1, 1),
       (11, 1, 1),
       (12, 1, 1),
       (13, 1, 1),
       (14, 1, 1),
       (15, 1, 1),
       (16, 1, 1),
       (17, 1, 1);