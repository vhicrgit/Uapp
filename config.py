import logger
import udata.database

SERVER_IP = "127.0.0.1"
SERVER_PORT = 7860

ALL_ONLINE_USERS = []

ITEMS_IMG_ROOT = "C:\\Users\\Administrator\\Desktop\\Userver\\itemimgs\\"

LOG_SAVE_PATH = "server.log"

LOGGER = logger.UappLogger(LOG_SAVE_PATH)

DBSERVER = udata.database.DB(LOGGER)


