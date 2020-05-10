USE moviedb;

CREATE TABLE IF NOT EXISTS employees
(
	email VARCHAR(50) NOT NULL PRIMARY KEY,
	password VARCHAR(20) NOT NULL ,
	fullname VARCHAR(100)
);


# DROP INDEX id ON genres;
# DROP INDEX id ON movies;
# DROP INDEX id ON stars;

ALTER TABLE genres ADD UNIQUE (name);
CREATE INDEX movies_index ON movies(title, year, director);
CREATE INDEX stars_index ON stars(name, birthyear)
