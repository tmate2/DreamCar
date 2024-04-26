DROP DATABASE dream_car;
CREATE DATABASE dream_car;
USE dream_car;

CREATE TABLE user (
  username CHAR(64),
  password CHAR(64),
  is_admin BOOLEAN,
  name VARCHAR(128),
  is_active BOOLEAN
);

CREATE TABLE fav_car (
  id CHAR(64),
  car_type_id CHAR(64),
  user_id CHAR(64),
  year YEAR,
  color VARCHAR(32),
  fuel VARCHAR(32)
);

CREATE TABLE car_type (
  id CHAR(64),
  car_brand CHAR(64),
  name VARCHAR(64)
);

CREATE TABLE car_brand (
  id CHAR(64),
  name VARCHAR(64)
);

CREATE TABLE car_pic (
  id CHAR(64),
  fav_car_id CHAR(64),
  img VARCHAR(64)
);

CREATE TABLE request (
	request VARCHAR(64),
	username CHAR(64)
);
