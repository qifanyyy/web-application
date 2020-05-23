#ifndef FUZZY_SEARCH_MIN_EDIT_DISTANCE_H
#define FUZZY_SEARCH_MIN_EDIT_DISTANCE_H

#include <mysql.h>

int min_edit_distance_init(UDF_INIT* init_id, UDF_ARGS* args, char* message);
long long min_edit_distance(UDF_INIT* init_id, UDF_ARGS* args, char* is_null, char* error);

#ifdef DEBUG
int min_edit_distance_dp(const char *s1, const char *s2, size_t s1_len, size_t s2_len);
#endif

#endif  // FUZZY_SEARCH_MIN_EDIT_DISTANCE_H
