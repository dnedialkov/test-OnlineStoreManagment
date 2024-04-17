create database storedb;
use storedb;


create table user(
user_id int primary key auto_increment,
username varchar(60),
passwordH varchar(100),
role enum("admin","user"),
UNIQUE(username)
);

create table products(
product_id int primary key auto_increment,
name varchar(100),
price decimal(10,2),
quantity int,
minimalPrice decimal(10,2)
-- ,activePromotion decimal(10,2) null
);

create table purchases(
purchase_id int primary key auto_increment,
product_id int,
quantity int,
purchase_price double,
user_id int,
purchaseDate date,
constraint foreign key (product_id) references products(product_id),
constraint foreign key (user_id) references user(user_id)
);

create table salesCampain(
campain_id int primary key auto_increment,
campainStart date,
campainEnd date,
isActive int
);

create table sales(
-- sale_id int primary key,
campain_id int,
product_id int,
discount int,
new_price double,
primary key(campain_id,product_id),
constraint foreign key(campain_id) references salesCampain(campain_id),
constraint foreign key(product_id) references products(product_id)
);

create table adminLog(
admin_id int,
user_id int,
action varchar(20),
date date,
constraint foreign key(admin_id) references user(user_id),
constraint foreign key(user_id) references user(user_id)
);


insert into user
values
(1,"admin","8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918","admin"),
(2,"user","04f8996da763b7a969b1028ee3007569eaf3a635486ddab211d512c85b9df8fb","user"),
(3,"ivan","ivan","admin");

select username,passwordH,role from user where username="ivan" and passwordH="ivan";

INSERT INTO products (product_id, name, price, quantity, minimalPrice)
VALUES
(1, 'Laptop', 1200.00, 10, 1000.00),
(2, 'Smartphone', 800.00, 20, 700.00),
(3, 'Headphones', 100.00, 50, 80.00),
(4, 'Tablet', 500.00, 15, 400.00);

INSERT INTO purchases (purchase_id, product_id, user_id, quantity, purchaseDate)
VALUES
(1, 1, 2, 1, '2024-04-01'),
(2, 2, 2, 2, '2024-04-02'),
(3, 3, 3, 3, '2024-04-03'),
(4, 1, 3, 2, '2024-04-04'),
(5, 4, 2, 1, '2024-04-05');

INSERT INTO products (product_id, name, price, quantity, minimalPrice)
VALUES
(5, 'Laptop', 1200.00, 10, 1000.00);

INSERT INTO salesCampain (campainStart, campainEnd, isActive) VALUES ('2024-04-16', '2024-04-30', 1);

INSERT INTO sales (campain_id, product_id, discount, new_price) VALUES (1, 1, 10, (SELECT price FROM products WHERE product_id = 1) * 0.9);