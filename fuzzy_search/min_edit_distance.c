#include "min_edit_distance.h"
#include <ctype.h>
#include <string.h>

int min(int a, int b)
{
    return a < b ? a : b;
}

int min3(int a, int b, int c) {
    return min(min(a, b), c);
}

int min_edit_distance_dp(const char *s1, const char *s2, size_t s1_len, size_t s2_len)
{
    char *s1_lower = strdup(s1), *s2_lower = strdup(s2);
    for (size_t i = 0; i < s1_len; ++i) {
        s1_lower[i] = (char) tolower(s1_lower[i]);
    }
    for (size_t i = 0; i < s2_len; ++i) {
        s2_lower[i] = (char) tolower(s2_lower[i]);
    }

    int dp[s1_len + 1][s2_len + 1];
    memset(dp, 0, sizeof(dp));

    for (size_t i = 0; i < s1_len + 1; ++i) {
        dp[i][0] = i;
    }
    for (size_t j = 0; j < s2_len + 1; ++j) {
        dp[0][j] = j;
    }

    for (size_t i = 1; i < s1_len + 1; ++i) {
        for (size_t j = 1; j < s2_len + 1; ++j) {
            if (s1_lower[i - 1] == s2_lower[j - 1])
                dp[i][j] = dp[i - 1][j - 1];
            else
                dp[i][j] = 1 + min3(dp[i - 1][j], dp[i][j - 1], dp[i - 1][j - 1]);
        }
    }

    return dp[s1_len][s2_len];
}

int min_edit_distance_init(UDF_INIT* init_id __attribute__ ((unused)), UDF_ARGS* args, char* message) {
    if (args->arg_count != 2 || args->arg_type[0] != STRING_RESULT || args->arg_type[1] != STRING_RESULT)
    {
        strncpy(message, "wrong argument(s); expecting two STRINGs", MYSQL_ERRMSG_SIZE);
        return 1;
    }

    return 0;
}

long long min_edit_distance(
        UDF_INIT* init_id __attribute__ ((unused)),
        UDF_ARGS* args,
        char* is_null,
        char* error __attribute__ ((unused)))
{
    const char *s1 = args->args[0], *s2 = args->args[1];

    if (!s1 || !s2)
    {
        *is_null = 1;
        return 0;
    }

    return min_edit_distance_dp(s1, s2, args->lengths[0], args->lengths[1]);
}
