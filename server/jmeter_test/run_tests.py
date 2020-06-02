from classes import RemoteServerError
from config import TEST_CASES
from log_processing import get_avg_time
import subprocess
import sys

if __name__ == '__main__':
    for test in TEST_CASES:
        # clear logs between tests

        print(f'[INFO] start clearing logs for test case "{test.name}"')
        for server in test.servers:
            try:
                server.clear_log()
            except RemoteServerError as e:
                stderr = e.ret.stderr.decode(encoding='utf-8')
                if not stderr.strip().endswith('No such file or directory'):
                    stdout = e.ret.stdout.decode(encoding='utf-8')
                    print(stdout)
                    print(stderr, file=sys.stderr)
                    print(f'[ERROR] failed to clear logs for test "{test.name}"; abort', file=sys.stderr)
                    exit(1)
        print(f'[INFO] finish clearing logs for test case "{test.name}"')

        # run JMeter

        print(f'[INFO] start JMeter for test case "{test.name}"')
        test_file_name_stem = test.get_test_case_file_name_stem()
        ret = subprocess.run(
            ['jmeter', '-n', '-t', test.jml_path, '-l', f'{test_file_name_stem}.jtl'],
            stdout=sys.stdout, stderr=sys.stderr
        )

        if ret.returncode != 0:
            print(f'[ERROR] JMeter exited abnormally in test case "{test.name}"; abort', file=sys.stderr)
            exit(1)

        # get results

        print(f'[INFO] start gathering logs for test case "{test.name}"')
        log = ''
        for server in test.servers:
            try:
                log += server.get_log()
            except RemoteServerError as e:
                stdout = e.ret.stdout.decode(encoding='utf-8')
                stderr = e.ret.stderr.decode(encoding='utf-8')
                print(stdout)
                print(stderr, file=sys.stderr)
                print(
                    f'[ERROR] failed to get logs for test "{test.name} from server {server.hostname}"; abort',
                    file=sys.stderr
                )
                exit(1)
        print(f'[INFO] all logs for test case "{test.name}" acquired')

        # get avg TS/TJ
        print(f'[INFO] start parsing log files for "{test.name}"')
        avg_ts, avg_tj = get_avg_time(log.split(sep='\n'))
        print(f'[INFO] avg_ts={avg_ts / 1e6}, avg_tj={avg_tj / 1e6}  (unit ms, for test case "{test.name}")')
