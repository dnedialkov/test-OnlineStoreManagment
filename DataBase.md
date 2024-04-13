create database storedb;
use storedb;


create table user(
user_id int primary key,
username varchar(60),
passwordH varchar(100),
role enum("admin","user"),
UNIQUE(username)
);

create table products(
product_id int primary key,
name varchar(100),
price decimal(10,2),
quantity int,
minimalPrice decimal(10,2)
-- ,activePromotion decimal(10,2) null
);

create table purchases(
purchase_id int primary key,
product_id int,
user_id int,
quantity int,
purchaseDate date,
constraint foreign key (product_id) references products(product_id),
constraint foreign key (user_id) references user(user_id)
);

create table salesCampain(
campain_id int primary key,
campainStart date,
campainEnd date);

create table sales(
-- sale_id int primary key,
campain_id int,
product_id int,
discount int,
primary key(campain_id,product_id),
constraint foreign key(campain_id) references salesCampain(campain_id),
constraint foreign key(product_id) references products(product_id)
);

insert into user
values
(1,"admin","admin","admin"),
(2,"user","user","user"),
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

SELECT SUM(p.price * pur.quantity) AS total_sales FROM purchases pur JOIN products p ON pur.product_id = p.product_id WHERE pur.purchaseDate >= '2024-04-01' AND pur.purchaseDate < '2024-05-01';

INSERT INTO products (product_id, name, price, quantity, minimalPrice)
VALUES
(5, 'Laptop', 1200.00, 10, 1000.00);

update users
set username='as', passwordH='123', role='admin'
where id=1;

select products.id,products.quantity from products where quantity<10;
