USE moviedb;

INSERT INTO ratings (movieId, rating, numVotes)
SELECT id, 0.0, 0 FROM movies LEFT OUTER JOIN ratings r on movies.id = r.movieId WHERE movieId IS NULL;
