import json
from typing import Iterable, Optional, Tuple


def get_avg_time(log_lines: Iterable[str]) -> Tuple[Optional[int], Optional[int]]:
    """get average ts/tj time from log lines (unit is nanosecond)"""
    ts_times, tj_times = [], []

    for line in log_lines:
        line = line.strip()
        if len(line) == 0:
            continue
        log_entry = json.loads(line)
        ts_times.append(log_entry['TS'])
        tj_times.append(log_entry['TJ'])

    return sum(ts_times) / len(ts_times) if len(ts_times) > 0 else None, \
        sum(tj_times) / len(tj_times) if len(tj_times) > 0 else None

