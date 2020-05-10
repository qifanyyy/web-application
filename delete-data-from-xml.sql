USE moviedb;
DELETE FROM stars_in_movies WHERE starId LIKE 'xs_%' OR movieId LIKE 'xm_%';
DELETE FROM genres_in_movies WHERE movieId LIKE 'xm_%';
DELETE FROM stars WHERE id LIKE 'xs_%';
DELETE FROM movies WHERE id LIKE 'xm_%';
DELETE FROM genres WHERE name NOT IN (
    'Action', 'Adult', 'Adventure', 'Animation', 'Biography', 'Comedy',
    'Crime', 'Documentary', 'Drama', 'Family', 'Fantasy', 'History', 'Horror',
    'Music', 'Musical', 'Mystery', 'Reality-TV', 'Romance', 'Sci-Fi', 'Sport',
    'Thriller', 'War', 'Western'
)
