#ifdef DEBUG
#include <stdio.h>
#include "min_edit_distance.h"

#define str_literal_len(str_literal) (sizeof(str_literal) - 1)
#endif

int main()
{
#ifdef DEBUG
    int ret = min_edit_distance_dp("", "", str_literal_len(""), str_literal_len(""));
    printf("\"\" vs \"\": expected 0, actual %d\n", ret);
    ret = min_edit_distance_dp("elephant", "relevant", str_literal_len("elephant"), str_literal_len("relevant"));
    printf("\"elephant\" vs \"relevant\": expected 3, actual %d\n", ret);
    ret = min_edit_distance_dp("schwazeNeger123", "sChwarzeneGGEr123", str_literal_len("schwazeNeger123"), str_literal_len("sChwarzeneGGEr123"));
    printf("\"schwazeNeger123\" vs \"sChwarzeneGGEr123\": expected 2, actual %d\n", ret);
#endif
    return 0;
}