DROP PROCEDURE IF EXISTS add_movie;

DELIMITER $$
CREATE PROCEDURE add_movie (
    IN new_title VARCHAR(100),
    IN new_year INTEGER,
    IN new_director VARCHAR(100),
    IN new_genre VARCHAR(32),
    IN new_star VARCHAR(100),
    IN new_dob DATE)
BEGIN
    DECLARE movies_count INTEGER;
    DECLARE genres_count INTEGER;
    DECLARE stars_count INTEGER;
    DECLARE movie_id INTEGER;
    DECLARE genre_id INTEGER;
    DECLARE star_id INTEGER;
    DECLARE genres_in_movies_count INTEGER;
    DECLARE stars_in_movies_count INTEGER;

    SET movies_count = (
        SELECT count(*)
        FROM movies
        WHERE title = new_title
        AND year = new_year
        AND director = new_director);

    SET genres_count = (
        SELECT count(*)
        FROM genres
        WHERE name = new_genre);

    IF new_dob IS NULL THEN
        SET stars_count = (
            SELECT count(*)
            FROM stars
            WHERE name = new_star
            AND birthYear IS NULL);

    ELSE
        SET stars_count = (
            SELECT count(*)
            FROM stars
            WHERE name = new_star
            AND birthYear = new_dob);
    END IF;

    IF movies_count = 0 THEN
        INSERT INTO movies (title, year, director)
            VALUES (new_title, new_year, new_director);
    END IF;

    IF genres_count = 0 THEN
        INSERT INTO genres (name)
            VALUES (new_genre);
    END IF;

    IF stars_count = 0 THEN
        INSERT INTO stars (name, birthYear)
            VALUES (new_star, new_dob);
    END IF;

    SET movie_id = (
        SELECT max(id)
        FROM movies
        WHERE title = new_title
        AND year = new_year
        AND director = new_director);

    SET genre_id = (
        SELECT max(id)
        FROM genres
        WHERE name = new_genre);


    IF new_dob IS NULL IS NULL THEN
        SET star_id = (
            SELECT max(id)
            FROM stars
            WHERE name = new_star
            AND birthYear IS NULL);

    ELSE
        SET star_id = (
            SELECT max(id)
            FROM stars
            WHERE name = new_star
            AND birthYear = new_dob);
    END IF;

    SET genres_in_movies_count = (
        SELECT count(*)
        FROM genres_in_movies
        WHERE genreId = genre_id
        AND movieId = movie_id);

    IF genres_in_movies_count = 0 THEN
        INSERT INTO genres_in_movies (genreId, movieId) VALUES (genre_id, movie_id);
    END IF;

    SET stars_in_movies_count = (
        SELECT count(*)
        FROM stars_in_movies
        WHERE starId = star_id
        AND movieId = movie_id);

    IF stars_in_movies_count = 0 THEN
        INSERT INTO stars_in_movies (starId, movieId) VALUES (star_id, movie_id);
    END IF;

END
$$

DELIMITER ;