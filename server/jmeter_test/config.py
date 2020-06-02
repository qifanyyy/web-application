from classes import TestCaseInfo, ServerInfo

TEST_CASES = [
    TestCaseInfo('Single Case 1: HTTP 1 Thread', 'single_case_1_http_1_thread.jmx', [
        ServerInfo(
            'ubuntu',
            '~/.ssh/US_Amazon.pem',
            '52.53.193.39',
            '/home/ubuntu/tomcat/webapps/ROOT/moviesServletLog.txt',
            True
        )
    ]),
    TestCaseInfo('Single Case 2: HTTP 10 Threads', 'single_case_2_http_10_threads.jmx', [
        ServerInfo(
            'ubuntu',
            '~/.ssh/US_Amazon.pem',
            '52.53.193.39',
            '/home/ubuntu/tomcat/webapps/ROOT/moviesServletLog.txt',
            True
        )
    ]),
    TestCaseInfo('Single Case 3: HTTPS 10 Threads', 'single_case_3_https_10_threads.jmx', [
        ServerInfo(
            'ubuntu',
            '~/.ssh/US_Amazon.pem',
            '52.53.193.39',
            '/home/ubuntu/tomcat/webapps/ROOT/moviesServletLog.txt',
            True
        )
    ]),
    TestCaseInfo('Single Case 4: HTTP 10 Threads w/o Connection Pooling', 'single_case_4_http_10_threads_no_cp.jmx', [
        ServerInfo(
            'ubuntu',
            '~/.ssh/US_Amazon.pem',
            '52.52.53.4',
            '/home/ubuntu/tomcat/webapps/ROOT/moviesServletLog.txt',
            True
        )
    ]),
]
