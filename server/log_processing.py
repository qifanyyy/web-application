import json
from pathlib import Path
import sys
from typing import Optional, TextIO, Tuple


def get_avg_time(log_file: TextIO) -> Tuple[Optional[int], Optional[int]]:
    ts_times, tj_times = [], []

    for line in log_file:
        log_entry = json.loads(line)
        ts_times.append(log_entry['TS'])
        tj_times.append(log_entry['TJ'])

    return sum(ts_times) / len(ts_times) if len(ts_times) > 0 else None, \
        sum(tj_times) / len(tj_times) if len(tj_times) > 0 else None


if __name__ == '__main__':
    if len(sys.argv) != 2:
        print('error: wrong argument(s)', file=sys.stderr)
        print(f'usage: {sys.argv[0] if len(sys.argv) == 1 else "log_processing.py"} log_file_path', file=sys.stderr)
        exit(1)
    log_file_path = Path(sys.argv[1])

    with log_file_path.open() as f:
        avg_ts, avg_tj = get_avg_time(f)

    print(f'{avg_ts=}, {avg_tj=}')
