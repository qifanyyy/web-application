CREATE DATABASE moviedb;
USE moviedb;

CREATE TABLE movies(
    id VARCHAR(10) PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    year INTEGER NOT NULL,
    director VARCHAR(100) NOT NULL
);

CREATE INDEX movies_index ON movies(title, year, director);

CREATE TABLE stars(
    id VARCHAR(10) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    birthYear INTEGER
);

CREATE INDEX stars_index ON stars(name, birthYear);

CREATE TABLE stars_in_movies(
    starId VARCHAR(10),
    movieId VARCHAR(10),
    PRIMARY KEY (starId, movieId),
    FOREIGN KEY (starId) REFERENCES stars(id) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (movieId) REFERENCES movies(id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE genres(
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(32) NOT NULL UNIQUE
);

CREATE TABLE genres_in_movies(
    genreId INTEGER,
    movieId VARCHAR(10),
    PRIMARY KEY (genreId, movieId),
    FOREIGN KEY (genreId) REFERENCES genres(id) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (movieId) REFERENCES movies(id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE creditcards(
    id VARCHAR(20) PRIMARY KEY,
    firstName VARCHAR(50) NOT NULL,
    lastName VARCHAR(50) NOT NULL,
    expiration DATE
);

CREATE TABLE customers(
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    firstName VARCHAR(50) NOT NULL,
    lastName VARCHAR(50) NOT NULL,
    ccId VARCHAR(20) NOT NULL,
    address VARCHAR(200) NOT NULL,
    email VARCHAR(50) NOT NULL,
    password VARCHAR(20) NOT NULL,
    FOREIGN KEY (ccId) REFERENCES creditcards(id) ON UPDATE CASCADE ON DELETE CASCADE,
    INDEX (id, email)
);

CREATE TABLE sales(
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    customerId INTEGER NOT NULL,
    movieId VARCHAR(10) NOT NULL,
    saleDate DATE NOT NULL,
    FOREIGN KEY (customerId) REFERENCES customers(id) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (movieId) REFERENCES movies(id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE ratings(
    movieId VARCHAR(10) PRIMARY KEY,
    rating FLOAT NOT NULL,
    numVotes INTEGER NOT NULL,
    FOREIGN KEY (movieId) REFERENCES movies(id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE employees(
	email VARCHAR(50) NOT NULL PRIMARY KEY,
	password VARCHAR(20) NOT NULL ,
	fullName VARCHAR(100)
);
