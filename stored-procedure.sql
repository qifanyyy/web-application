use moviedb;
delimiter //

CREATE PROCEDURE add_movie 
  (arg_title VARCHAR(100),
   arg_year INTEGER,
   arg_director VARCHAR(100),
   arg_banner_url VARCHAR(200),
   arg_trailer_url VARCHAR(200),
   arg_genre_name VARCHAR(32),
   arg_star_first_name VARCHAR(50),
   arg_star_last_name VARCHAR(50),
   arg_star_dob DATE,
   arg_photo_url VARCHAR(200)
   )
BEGIN
	SET @movie_count = (SELECT count(*) FROM movies as m WHERE 
		m.title = arg_title AND 
		m.year = arg_year AND 
		m.director = arg_director AND 
		m.banner_url = arg_banner_url AND 
		m.trailer_url = arg_trailer_url);
	SET @genre_count = (SELECT count(*) FROM genres as g WHERE g.name = arg_genre_name);
	SET @star_count = (SELECT count(*) FROM stars as s WHERE
		s.first_name = arg_star_first_name AND
		s.last_name = arg_star_last_name AND 
		s.dob = arg_star_dob AND 
		s.photo_url = arg_photo_url);
	IF @movie_count = 0 THEN
		INSERT INTO movies (title,year,director,banner_url,trailer_url) VALUES
			(arg_title,arg_year,arg_director,arg_banner_url,arg_trailer_url);
	END IF;
	SET @tmp_movie_id = (SELECT max(id) FROM movies as m WHERE 
		m.title = arg_title AND 
		m.year = arg_year AND 
		m.director = arg_director AND 
		m.banner_url = arg_banner_url AND 
		m.trailer_url = arg_trailer_url); 
	IF @genre_count = 0 THEN
		INSERT INTO genres (name) VALUES (arg_genre_name);
	END IF;
	SET @tmp_genre_id = (SELECT max(id) FROM genres as g WHERE g.name = arg_genre_name);
	IF @star_count = 0 THEN
		INSERT INTO stars (first_name, last_name, dob,photo_url) VALUES 
			(arg_star_first_name, arg_star_last_name, arg_star_dob, arg_photo_url );
	END IF;
	SET @tmp_star_id = (SELECT max(id) FROM stars as s WHERE
		s.first_name = arg_star_first_name AND
		s.last_name = arg_star_last_name AND 
		s.dob = arg_star_dob AND 
		s.photo_url = arg_photo_url);
	IF (SELECT count(*) from genres_in_movies as gim WHERE gim.genre_id = @tmp_genre_id AND gim.movie_id = @tmp_movie_id) = 0 THEN
		INSERT INTO genres_in_movies  (genre_id,movie_id) VALUES (@tmp_genre_id,@tmp_movie_id);
	END IF;
	IF (SELECT count(*) from stars_in_movies as sim WHERE sim.star_id = @tmp_star_id AND sim.movie_id = @tmp_movie_id) = 0 THEN
		INSERT INTO stars_in_movies  (star_id,movie_id) VALUES (@tmp_genre_id,@tmp_movie_id);
	END IF;
	SELECT @movie_count = 0 AS 'Movie Added', @tmp_movie_id AS 'Movie ID',
	        @genre_count = 0 AS 'Genre Added', @tmp_genre_id AS 'Genre ID',
			@star_count  = 0 AS 'Star Added' , @tmp_star_id  AS 'Star ID'; 
END//

delimiter ;