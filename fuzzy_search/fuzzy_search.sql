DROP FUNCTION IF EXISTS min_edit_distance;
CREATE FUNCTION min_edit_distance RETURNS INTEGER SONAME 'lib_fuzzy_search.so';
