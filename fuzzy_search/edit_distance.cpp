#include "edit_distance.hpp"

#include <string>
#include <vector>

int editDistanceDP(const std::string& s1, const std::string& s2) {
    std::vector<std::vector<int>> dp(s1.length() + 1, std::vector<int>(s2.length() + 1, 0));
    for (int i = 0; i < s1.length() + 1; ++i) {
        dp.at(i).front() = i;
    }
    for (int j = 0; j < s2.length() + 1; ++j) {
        dp.front().at(j) = j;
    }
    for (int i = 1; i < s1.length() + 1; ++i) {
        for (int j = 1; j < s2.length() + 1; ++j) {
            if (s1.at(i - 1) == s2.at(j - 1))
                dp.at(i).at(j) = dp.at(i - 1).at(j - 1);
            else
                dp.at(i).at(j) = 1 + std::min({dp.at(i - 1).at(j), dp.at(i).at(j - 1), dp.at(i - 1).at(j - 1)});
        }
    }
    return dp.back().back();
}

long long editDistance(const char* s1, const char* s2) {
    return editDistanceDP(std::string(s1), std::string(s2));
}
