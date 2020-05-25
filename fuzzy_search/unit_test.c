#include <stdio.h>
#include <string.h>

#include "min_edit_distance.h"

#define EXPECT_EQ(expected, actual, pass_count, test_count)\
do {\
    if ((expected) == (actual)) {\
        ++(pass_count);\
        printf("Test passed!\n");\
    } else {\
        printf("Test failed at line %d: expected=%d; actual=%ld\n", __LINE__, (expected), (actual));\
    }\
    ++(test_count);\
} while (0)

#define STR_MAX_LEN 100

static void init_udf(UDF_ARGS *args)
{
    args->args = calloc(2, sizeof(char *));
    args->lengths = calloc(2, sizeof(long));
    args->args[0] = calloc(STR_MAX_LEN + 1, sizeof(char));
    args->args[1] = calloc(STR_MAX_LEN + 1, sizeof(char));
}

static UDF_ARGS *set_udf_args(UDF_ARGS *args, const char *s1, const char *s2)
{
    strncpy(args->args[0], s1, STR_MAX_LEN);
    strncpy(args->args[1], s2, STR_MAX_LEN);
    args->lengths[0] = strlen(s1);
    args->lengths[1] = strlen(s2);
    return args;
}

static void free_udf(UDF_ARGS *args)
{
    free(args->args[0]);
    free(args->args[1]);
    free(args->args);
    free(args->lengths);
}

int main(void)
{
    printf("unit test starts\n");
    int pass_count = 0, test_count = 0;

    UDF_ARGS args;
    char is_null_ret;
    long ret;

    init_udf(&args);

    set_udf_args(&args, "", "");
    ret = min_edit_distance(NULL, &args, &is_null_ret, NULL);
    EXPECT_EQ(0, ret, pass_count, test_count);

    set_udf_args(&args, "elephant", "relevant");
    ret = min_edit_distance(NULL, &args, &is_null_ret, NULL);
    EXPECT_EQ(3, ret, pass_count, test_count);

    set_udf_args(&args, "schwazeneger", "schwarzenegger");
    ret = min_edit_distance(NULL, &args, &is_null_ret, NULL);
    EXPECT_EQ(2, ret, pass_count, test_count);

    set_udf_args(&args, "ABcD1234@@##", "abcd1234@@##");
    ret = min_edit_distance(NULL, &args, &is_null_ret, NULL);
    EXPECT_EQ(0, ret, pass_count, test_count);

    set_udf_args(&args, "Impossible", "Inpossible");
    ret = min_edit_distance(NULL, &args, &is_null_ret, NULL);
    EXPECT_EQ(1, ret, pass_count, test_count);

    set_udf_args(&args, "Impossible", "Inpossble");
    ret = min_edit_distance(NULL, &args, &is_null_ret, NULL);
    EXPECT_EQ(2, ret, pass_count, test_count);

    set_udf_args(&args, "Impossible", "impossible");
    ret = min_edit_distance(NULL, &args, &is_null_ret, NULL);
    EXPECT_EQ(0, ret, pass_count, test_count);

    set_udf_args(&args, "Impossible", "inpossible");
    ret = min_edit_distance(NULL, &args, &is_null_ret, NULL);
    EXPECT_EQ(1, ret, pass_count, test_count);

    set_udf_args(&args, "Impossible", "inpossble");
    ret = min_edit_distance(NULL, &args, &is_null_ret, NULL);
    EXPECT_EQ(2, ret, pass_count, test_count);

    free_udf(&args);

    printf("%d / %d tests passed (%.1f%%)\n", pass_count, test_count, (pass_count * 100.0) / test_count);

    return pass_count != test_count;
}
