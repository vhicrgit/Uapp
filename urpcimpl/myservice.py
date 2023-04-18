from config import *
import urpc.UappService as UService
from urpc.ttypes import *
from io import BytesIO
from PIL import Image
import os
import time


class UappService(UService.Iface):


    def register(self, info):
        print(info.student_id, info.username, info.email, info.password)
        return DBSERVER.insert_user(info.student_id, info.username, info.email, info.password)


    def login(self, info):
        column = DBSERVER.query_user(info.student_id)
        password = column[3]

        if password == info.password:
            ALL_ONLINE_USERS.append(info.student_id)
            return True
        else:
            return False

    def getUserInfo(self, student_id):

        if student_id in ALL_ONLINE_USERS:
            column = DBSERVER.query_user(student_id)
            return RegisterInfo(column[0], column[1], column[2], column[3])
        else:
            return RegisterInfo()


    def uploadPost(self, info):
        student_id = info.student_id
        img_data = BytesIO(info.item_image)
        img = Image.open(img_data)
        img_path = ITEMS_IMG_ROOT++info.student_id+'-'+time.strftime('%Y-%m-%d-%H-%M-%S', time.localtime())+'.png'
        img.save(img_path, 'PNG')

        item_id = DBSERVER.insert_item(info.item_type, img_path, info.item_position, info.item_desc)
        return item_id
        # if item_id:
        #     return DBSERVER.insert_post(student_id, item_id, info.for_lost_item, info.content)

        # if student_id in ALL_ONLINE_USERS:
        #     item_id = DBSERVER.insert_item(info.item_type, img_path, info.item_position, info.item_desc)
        #     if item_id:
        #         return DBSERVER.insert_post(student_id, item_id, info.for_lost_item, info.content)
        # return False



    def uploadReply(self, info):
        student_id = info.student_id
        if student_id in ALL_ONLINE_USERS:
            return DBSERVER.insert_reply(info.post_id, student_id, info.content)

        return False


    def getAllReply(self, id):
        result = DBSERVER.query_all_reply(id)
        if result:
            return [ReplyInfo(i[0], i[1]) for i in result]
        else:
            return []

    def getAllPost(self):
        result = DBSERVER.query_all_post()
        if result:
            return [PostInfo(i[0], i[1], i[2], i[3], i[4], i[5]) for i in result]
        else:
            return []