from dataclasses import dataclass
import json
from pathlib import Path
import subprocess
import sys
from typing import Iterable, List, Optional, Tuple


class RemoteServerError(RuntimeError):
    def __init__(self, message: str, ret: subprocess.CompletedProcess):
        super().__init__(message)
        self.ret = ret


@dataclass
class ServerInfo:
    username: str
    private_key: str
    hostname: str
    remote_log_file_path: str
    need_sudo: bool

    def clear_log(self) -> None:
        completed_process = subprocess.run(
            ['ssh', '-i', self.private_key, f'{self.username}@{self.hostname}'] +
            (['sudo'] if self.need_sudo else []) +
            ['rm', self.remote_log_file_path],
            capture_output=True
        )
        if completed_process.returncode != 0:
            raise RemoteServerError('failed to clear log', completed_process)

    def get_log(self) -> str:
        completed_process = subprocess.run(
            ['ssh', '-i', self.private_key, f'{self.username}@{self.hostname}'] +
            (['sudo'] if self.need_sudo else []) +
            ['cat', self.remote_log_file_path],
            capture_output=True
        )
        if completed_process.returncode != 0:
            raise RemoteServerError('failed to get log', completed_process)
        return completed_process.stdout.decode(encoding='utf-8')


@dataclass
class TestCaseInfo:
    name: str
    jml_path: str
    servers: List[ServerInfo]

    def get_test_case_file_name_stem(self) -> str:
        return str(Path(self.jml_path).stem)


def get_avg_time(log_lines: Iterable[str]) -> Tuple[Optional[int], Optional[int]]:
    """get average ts/tj time from log lines (unit is nanosecond)"""
    ts_times, tj_times = [], []

    for line_no, line in enumerate(log_lines):
        line = line.strip()
        if len(line) == 0:
            continue
        try:
            log_entry = json.loads(line)
            ts_times.append(log_entry['TS'])
            tj_times.append(log_entry['TJ'])
        except json.JSONDecodeError:
            print(f'[WARNING] bad line {line_no + 1} when parsing JSON; skipped')

    return sum(ts_times) / len(ts_times) if len(ts_times) > 0 else None, \
        sum(tj_times) / len(tj_times) if len(tj_times) > 0 else None


TEST_CASES = [
    TestCaseInfo('single_case_1_http_1_thread', 'single_case_1_http_1_thread.jmx', [
        ServerInfo(
            'ubuntu',
            '~/.ssh/US_Amazon.pem',
            '52.53.193.39',
            '/home/ubuntu/tomcat/webapps/ROOT/moviesServletLog.txt',
            True
        )
    ]),
    TestCaseInfo('single_case_2_http_10_threads', 'single_case_2_http_10_threads.jmx', [
        ServerInfo(
            'ubuntu',
            '~/.ssh/US_Amazon.pem',
            '52.53.193.39',
            '/home/ubuntu/tomcat/webapps/ROOT/moviesServletLog.txt',
            True
        )
    ]),
    TestCaseInfo('single_case_3_https_10_threads', 'single_case_3_https_10_threads.jmx', [
        ServerInfo(
            'ubuntu',
            '~/.ssh/US_Amazon.pem',
            '52.53.193.39',
            '/home/ubuntu/tomcat/webapps/ROOT/moviesServletLog.txt',
            True
        )
    ]),
    TestCaseInfo('single_case_4_http_10_threads_no_cp', 'single_case_4_http_10_threads_no_cp.jmx', [
        ServerInfo(
            'ubuntu',
            '~/.ssh/US_Amazon.pem',
            '52.52.53.4',
            '/home/ubuntu/tomcat/webapps/ROOT/moviesServletLog.txt',
            True
        )
    ]),
    TestCaseInfo('scaled_case_1_http_1_thread', 'scaled_case_1_http_1_thread.jmx', [
        ServerInfo(
            'ubuntu',
            '~/.ssh/US_Amazon.pem',
            'master.fabflix.live',
            '/home/ubuntu/tomcat/webapps/ROOT/moviesServletLog.txt',
            True
        ),
        ServerInfo(
            'ubuntu',
            '~/.ssh/US_Amazon.pem',
            'slave.fabflix.live',
            '/home/ubuntu/tomcat/webapps/ROOT/moviesServletLog.txt',
            True
        )
    ]),
    TestCaseInfo('scaled_case_2_http_10_threads', 'scaled_case_2_http_10_threads.jmx', [
        ServerInfo(
            'ubuntu',
            '~/.ssh/US_Amazon.pem',
            'master.fabflix.live',
            '/home/ubuntu/tomcat/webapps/ROOT/moviesServletLog.txt',
            True
        ),
        ServerInfo(
            'ubuntu',
            '~/.ssh/US_Amazon.pem',
            'slave.fabflix.live',
            '/home/ubuntu/tomcat/webapps/ROOT/moviesServletLog.txt',
            True
        )
    ]),
    TestCaseInfo('scaled_case_3_http_10_threads_no_cp', 'scaled_case_3_http_10_threads_no_cp.jmx', [
        ServerInfo(
            'ubuntu',
            '~/.ssh/US_Amazon.pem',
            '54.151.78.141',
            '/home/ubuntu/tomcat/webapps/ROOT/moviesServletLog.txt',
            True
        ),
        ServerInfo(
            'ubuntu',
            '~/.ssh/US_Amazon.pem',
            '54.215.94.69',
            '/home/ubuntu/tomcat/webapps/ROOT/moviesServletLog.txt',
            True
        )
    ]),
]


if __name__ == '__main__':
    log_folder = Path('./logs')
    log_folder.mkdir(exist_ok=True)

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
                    print(f'[ERROR] failed to clear logs for test "{test.name}"; abort', file=sys.stderr)
                    print('====stdout====')
                    print(stdout)
                    print('====stderr====', file=sys.stderr)
                    print(stderr, file=sys.stderr)
                    exit(1)

        # run JMeter

        print(f'[INFO] start JMeter for test case "{test.name}"')
        test_file_name_stem = test.get_test_case_file_name_stem()
        jmeter_ret = subprocess.run(
            ['jmeter', '-n', '-t', test.jml_path, '-l', f'{test_file_name_stem}.jtl'],
            stdout=sys.stdout, stderr=sys.stderr
        )

        if jmeter_ret.returncode != 0:
            print(f'[ERROR] JMeter exited abnormally in test case "{test.name}"; abort', file=sys.stderr)
            exit(1)

        # get results

        print(f'[INFO] start gathering logs for test case "{test.name}"')
        log = ''

        test_log_folder = log_folder / test.name
        test_log_folder.mkdir(exist_ok=True)

        for server in test.servers:
            try:
                server_log = server.get_log()
                with (test_log_folder / f'{server.hostname}.txt').open('w') as log_file:
                    log_file.write(server_log)
                log += server_log
            except RemoteServerError as e:
                stdout = e.ret.stdout.decode(encoding='utf-8')
                stderr = e.ret.stderr.decode(encoding='utf-8')
                print(
                    f'[ERROR] failed to get logs for test "{test.name} from server {server.hostname}"; skipped',
                    file=sys.stderr
                )
                print('====stdout====')
                print(stdout)
                print('====stderr====', file=sys.stderr)
                print(stderr, file=sys.stderr)

        # get avg TS/TJ
        print(f'[INFO] start parsing log files for "{test.name}"')
        avg_ts, avg_tj = get_avg_time(log.split(sep='\n'))
        print(f'[INFO] avg_ts={avg_ts / 1e6}, avg_tj={avg_tj / 1e6}  (unit ms, for test case "{test.name}")')
