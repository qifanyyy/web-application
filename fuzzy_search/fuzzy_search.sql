USE moviedb;
CREATE FUNCTION min_edit_distance RETURNS INTEGER SONAME 'fuzzy_search.so';
