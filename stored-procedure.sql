DROP PROCEDURE IF EXISTS add_movie;
DELIMITER $$

CREATE PROCEDURE add_movie (
    IN newTitle VARCHAR(100),
    IN newYear INTEGER,
    IN newDirector VARCHAR(100),
    IN newGenreName VARCHAR(32),
    IN newStarName VARCHAR(100),
    IN newStarBirthYear INTEGER,
    OUT hasDupMovie BOOLEAN,
    OUT hasDupStar BOOLEAN,
    OUT hasDupGenre BOOLEAN,
    OUT outMovieId VARCHAR(10),
    OUT outStarId VARCHAR(10),
    OUT outGenreId INTEGER
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
    SET outMovieId = NULL;
    SET outStarId = NULL;
    SET outGenreId = NULL;

    IF (SELECT COUNT(*) FROM movies WHERE title = newTitle AND year = newYear AND director = newDirector) > 0 THEN
        SET hasDupMovie = TRUE;
        SET outMovieId = (SELECT id FROM movies WHERE title = newTitle AND year = newYear AND director = newDirector LIMIT 1);
        LEAVE this_procedure;
    END IF;

    SET newMovieId = (
        SELECT COALESCE(MAX(CONVERT(SUBSTR(id, 3), UNSIGNED INTEGER)), -1) FROM movies WHERE id LIKE 'pm%'
    ) + 1;

    SET newMovieIdStr = CONCAT('pm', LPAD(newMovieId, 8, '0'));

    INSERT INTO movies VALUES (newMovieIdStr, newTitle, newYear, newDirector);
    INSERT INTO ratings VALUES (newMovieIdStr, 0.0, 0);

    SET outMovieId = newMovieIdStr;

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

    SET outStarId = starIdStr;
    INSERT IGNORE INTO stars_in_movies VALUES (starIdStr, newMovieIdStr);

    IF (SELECT COUNT(*) FROM genres WHERE name = newGenreName) = 0 THEN
        SET newGenreId = (SELECT MAX(id) FROM genres) + 1;
        INSERT INTO genres VALUES (newGenreId, newGenreName);
    ELSE
        SET newGenreId = (SELECT id FROM genres WHERE name = newGenreName);
        SET hasDupGenre = TRUE;
    END IF;

    SET outGenreId = newGenreId;
    INSERT IGNORE INTO genres_in_movies VALUES (newGenreId, newMovieIdStr);
END
$$

DELIMITER ;