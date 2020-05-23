#include <iostream>
#include "edit_distance.hpp"

int main()
{
    long long ret = editDistance("", "");
    std::cout << R"(editDistance("", ""): expected 0, actual )" << ret << std::endl;
    ret = editDistance("elephant", "relevant");
    std::cout << R"(editDistance("elephant", "relevant"): expected 3, actual )" << ret << std::endl;
    ret = editDistance("schwazeneger", "schwarzenegger");
    std::cout << R"(editDistance("schwazeneger", "schwarzenegger"): expected 2, actual )" << ret << std::endl;
    return 0;
}