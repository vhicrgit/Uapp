from config import *
from thrift.transport import TSocket
from thrift.protocol import TBinaryProtocol
from thrift.server import TServer
from urpc.ttypes import *
from urpc import UappService as UService
from urpcimpl.myservice import UappService


if __name__ == '__main__':

    DBSERVER.connect()

    # 创建服务处理器
    handler = UappService()
    processor = UService.Processor(handler)

    # 创建服务器套接字
    transport = TSocket.TServerSocket(host=SERVER_IP, port=SERVER_PORT)
    tfactory = TTransport.TBufferedTransportFactory()
    pfactory = TBinaryProtocol.TBinaryProtocolFactory()

    # 创建服务器
    server = TServer.TThreadPoolServer(processor, transport, tfactory, pfactory)

    # 启动服务器
    LOGGER.info(f'Uapp Server start and is listening on IP: {SERVER_IP}, Port: {SERVER_PORT}')
    server.serve()






