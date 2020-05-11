DROP PROCEDURE IF EXISTS add_movie;

DELIMITER $$
CREATE PROCEDURE add_movie (
    IN new_movie_title VARCHAR(100),
    IN new_movie_year INTEGER,
    IN new_movie_director VARCHAR(100),
    IN new_genre_name VARCHAR(32),
    IN new_star_name VARCHAR(100),
    IN new_star_dob DATE
)
BEGIN
    DECLARE movie_count INTEGER DEFAULT 0;
    DECLARE genre_count INTEGER DEFAULT 0;
    DECLARE star_count INTEGER DEFAULT 0;
    DECLARE ref_movie_id INTEGER DEFAULT 0;
    DECLARE ref_genre_id INTEGER DEFAULT 0;
    DECLARE ref_star_id INTEGER DEFAULT 0;
    DECLARE genres_in_movies_count INTEGER DEFAULT 0;
    DECLARE stars_in_movies_count INTEGER DEFAULT 0;

    START TRANSACTION;
        SET movie_count = (
                    SELECT count(*) FROM movies AS m WHERE
                        m.title = new_movie_title AND
                        m.year = new_movie_year AND
                        m.director = new_movie_director);

        SET genre_count = (
            SELECT count(*) FROM genres AS g WHERE
            g.name = new_genre_name);

        IF new_star_dob IS NULL THEN
            SET star_count = (
                SELECT count(*) FROM stars AS s WHERE
                s.name = new_star_name AND
                s.birthYear IS NULL);
        ELSE
            SET star_count = (
                SELECT count(*) FROM stars AS s WHERE
                s.name = new_star_name AND
                s.birthYear = new_star_dob);
        END IF;

        IF movie_count = 0 THEN
            INSERT INTO movies (title, year, director)
                VALUES (new_movie_title, new_movie_year, new_movie_director);
        END IF;

        IF genre_count = 0 THEN
            INSERT INTO genres (name)
                VALUES (new_genre_name);
        END IF;

        IF star_count = 0 THEN
            INSERT INTO stars (name, birthYear)
                VALUES (new_star_name, new_star_dob);
        END IF;


        SET ref_movie_id = (
            SELECT max(id) FROM movies AS m WHERE
                m.title = new_movie_title AND
                m.year = new_movie_year AND
                m.director = new_movie_director);


        SET ref_genre_id = (SELECT max(id) FROM genres  WHERE name = new_genre_name);


        IF new_star_dob IS NULL IS NULL THEN
            SET ref_star_id = (
                SELECT max(id) FROM stars AS s WHERE
                    s.name = new_star_name AND
                    s.birthYear IS NULL);

        ELSE
            SET ref_star_id = (
                SELECT max(id) FROM stars AS s WHERE
                    s.name = new_star_name AND
                    s.birthYear = new_star_dob);
        END IF;

        SET genres_in_movies_count = (
            SELECT count(*) FROM genres_in_movies AS gmc WHERE
                gmc.genre_id = ref_genre_id AND
                gmc.movie_id = ref_movie_id);

        IF genres_in_movies_count = 0 THEN
            INSERT INTO genres_in_movies (genre_id, movie_id) VALUES (ref_genre_id, ref_movie_id);
        END IF;

        SET stars_in_movies_count = (
            SELECT count(*) FROM stars_in_movies AS smc WHERE
                smc.star_id = ref_star_id AND
                smc.movie_id = ref_movie_id);

        IF stars_in_movies_count = 0 THEN
            INSERT INTO stars_in_movies (star_id, movie_id) VALUES (ref_star_id, ref_movie_id);
        END IF;

    COMMIT;
END
$$

DELIMITER ;