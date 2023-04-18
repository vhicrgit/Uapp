import logging
import sys
from logging.handlers import RotatingFileHandler

class ColoredFormatter(logging.Formatter):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self._colors = {
            'DEBUG': '\033[32m', # green
            'INFO': '\033[34m', # blue
            'WARNNING': '\033[33m', # yellow
            'ERROR': '\033[31m', # red
            'CRITICAL': '\033[41m' # red background
        }
        self._reset = '\033[0m'

    def format(self, record):
        levelname = record.levelname
        message = super().format(record)
        color = self._colors.get(levelname, '')
        return f'{color}{message}{self._reset}'


class UappLogger(logging.Logger):
    def __init__(self, log_path, file_level=logging.INFO, console_level=logging.DEBUG):
        super().__init__(__name__, logging.INFO)
        file_handler = RotatingFileHandler(log_path, maxBytes=10000, backupCount=1)
        file_handler.setFormatter(logging.Formatter('%(asctime)s - %(levelname)s - %(message)s'))
        file_handler.setLevel(file_level)

        console_handler = logging.StreamHandler(sys.stdout)
        console_handler.setFormatter(ColoredFormatter('%(asctime)s - %(levelname)s - %(message)s'))
        console_handler.setLevel(console_level)

        self.addHandler(file_handler)
        self.addHandler(console_handler)



