use moviedb;
DELIMITER '//';

CREATE PROCEDURE add_movie(
	arg_title VARCHAR(100),
	arg_year INTEGER,
	arg_director VARCHAR(100),
	arg_genre_name VARCHAR(32),
	arg_star_first_name VARCHAR(50),
	arg_star_last_name VARCHAR(50),
	arg_star_dob DATE)
BEGIN
	SET movie_count = (SELECT count(*) FROM movies AS m WHERE 
		m.title = arg_title AND 
		m.year = arg_year AND 
		m.director = arg_director);
	SET genre_count = (SELECT count(*) FROM genres AS g WHERE g.name = arg_genre_name);
	IF arg_star_dob IS NULL THEN
		SET star_count = (SELECT count(*) FROM stars AS s WHERE
			s.first_name = arg_star_first_name AND
			s.last_name = arg_star_last_name AND 
			s.dob IS NULL);
	ELSE
		SET star_count = (SELECT count(*) FROM stars AS s WHERE
			s.first_name = arg_star_first_name AND
			s.last_name = arg_star_last_name AND 
			s.dob = arg_star_dob);
	END IF;
	IF movie_count = 0 THEN
		INSERT INTO movies (title,year,director) VALUES
			(arg_title,arg_year,arg_director);
	END IF;
	SET tmp_movie_id = (SELECT max(id) FROM movies AS m WHERE 
		m.title = arg_title AND 
		m.year = arg_year AND 
		m.director = arg_director); 
	IF genre_count = 0 THEN
		INSERT INTO genres (name) VALUES (arg_genre_name);
	END IF;
	SET tmp_genre_id = (SELECT max(id) FROM genres AS g WHERE g.name = arg_genre_name);
	IF star_count = 0 THEN
		INSERT INTO stars (first_name, last_name, dob) VALUES 
			(arg_star_first_name, arg_star_last_name, arg_star_dob);
	END IF;
	IF arg_star_dob IS NULL THEN
		SET tmp_star_id = (SELECT max(id) FROM stars AS s WHERE
			s.first_name = arg_star_first_name AND
			s.last_name = arg_star_last_name AND 
			s.dob IS NULL);
	ELSE
		SET tmp_star_id = (SELECT max(id) FROM stars AS s WHERE
			s.first_name = arg_star_first_name AND
			s.last_name = arg_star_last_name AND 
			s.dob = arg_star_dob);
	END IF;
	SET genres_in_movies_count = (SELECT count(*) from genres_in_movies AS gim WHERE gim.genre_id = tmp_genre_id AND gim.movie_id = tmp_movie_id);
	IF genres_in_movies_count = 0 THEN
		INSERT INTO genres_in_movies (genre_id, movie_id) VALUES (tmp_genre_id, tmp_movie_id);
	END IF;
	SET stars_in_movies_count = (SELECT count(*) from stars_in_movies AS sim WHERE sim.star_id = tmp_star_id AND sim.movie_id = tmp_movie_id);
	IF stars_in_movies_count = 0 THEN
		INSERT INTO stars_in_movies (star_id, movie_id) VALUES (tmp_star_id, tmp_movie_id);
	END IF;
END//

DELIMITER ;