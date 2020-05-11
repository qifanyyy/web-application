DROP PROCEDURE IF EXISTS add_movie2;
DELIMITER $$

CREATE PROCEDURE add_movie2 (
    IN newTitle VARCHAR(100),
    IN newYear INTEGER,
    IN newDirector VARCHAR(100),
    IN newGenreName VARCHAR(32),
    IN newStarName VARCHAR(100),
    IN newStarBirthYear INTEGER,
    OUT hasDupMovie BOOLEAN,
    OUT hasDupStar BOOLEAN,
    OUT hasDupGenre BOOLEAN
)

this_procedure: BEGIN
    DECLARE newMovieId INTEGER;
    DECLARE newMovieIdStr VARCHAR(10);
    DECLARE starIdStr VARCHAR(10);
    DECLARE newStarId INTEGER;
    DECLARE newStarIdStr VARCHAR(10);
    DECLARE newGenreId INTEGER;

    SET hasDupMovie = FALSE;
    SET hasDupStar = FALSE;
    SET hasDupGenre = FALSE;

    IF (SELECT COUNT(*) FROM movies WHERE title = newTitle AND year = newYear AND director = newDirector) > 0 THEN
        SET hasDupMovie = TRUE;
        LEAVE this_procedure;
    END IF;

    SET newMovieId = (
        SELECT COALESCE(MAX(CONVERT(SUBSTR(id, 3), UNSIGNED INTEGER)), -1) FROM movies WHERE id LIKE 'pm%'
    ) + 1;

    SET newMovieIdStr = CONCAT('pm', LPAD(newMovieId, 8, '0'));

    INSERT INTO movies VALUES (newMovieIdStr, newTitle, newYear, newDirector);

    IF (SELECT COUNT(*) FROM stars WHERE name = newStarName AND (ISNULL(newStarBirthYear) OR birthYear = newStarBirthYear)) = 0 THEN
        SET newStarId = (
            SELECT COALESCE(MAX(CONVERT(SUBSTR(id, 3), UNSIGNED INTEGER)), -1) FROM stars WHERE id LIKE 'ps%'
        ) + 1;
        SET newStarIdStr = CONCAT('ps', LPAD(newStarId, 8, '0'));
        INSERT INTO stars VALUES (newStarIdStr, newStarName, newStarBirthYear);
        SET starIdStr = newStarIdStr;
    ELSE
        SET starIdStr = (SELECT id FROM stars WHERE name = newStarName AND (ISNULL(newStarBirthYear) OR birthYear = newStarBirthYear) LIMIT 1);
        SET hasDupStar = TRUE;
    END IF;

    INSERT IGNORE INTO stars_in_movies VALUES (starIdStr, newMovieIdStr);

    IF (SELECT COUNT(*) FROM genres WHERE name = newGenreName) = 0 THEN
        SET newGenreId = (SELECT MAX(id) FROM genres) + 1;
        INSERT INTO genres VALUES (newGenreId, newGenreName);
    ELSE
        SET newGenreId = (SELECT id FROM genres WHERE name = newGenreName);
        SET hasDupGenre = TRUE;
    END IF;

    INSERT IGNORE INTO genres_in_movies VALUES (newGenreId, newMovieIdStr);
END
$$

DELIMITER ;