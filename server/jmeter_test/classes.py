from dataclasses import dataclass
from pathlib import Path
import subprocess
from typing import List


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
        ret = subprocess.run(
            ['ssh', '-i', self.private_key, f'{self.username}@{self.hostname}'] +
            (['sudo'] if self.need_sudo else []) +
            ['rm', self.remote_log_file_path],
            capture_output=True
        )
        if ret.returncode != 0:
            raise RemoteServerError('failed to clear log', ret)

    def get_log(self) -> str:
        ret = subprocess.run(
            ['ssh', '-i', self.private_key, f'{self.username}@{self.hostname}'] +
            (['sudo'] if self.need_sudo else []) +
            ['cat', self.remote_log_file_path],
            capture_output=True
        )
        if ret.returncode != 0:
            raise RemoteServerError('failed to get log', ret)
        return ret.stdout.decode(encoding='utf-8')


@dataclass
class TestCaseInfo:
    name: str
    jml_path: str
    servers: List[ServerInfo]

    def get_test_case_file_name_stem(self) -> str:
        return str(Path(self.jml_path).stem)
