namespace py urpc

struct RegisterInfo {
    1: string student_id;
    2: string username;
    3: string email;
    4: string password;
}

struct LoginInfo {
    1: string student_id;
    2: string password;
}

struct PostInfo {
    1: string student_id;
    2: bool for_lost_item;
    3: string content;
    4: binary item_image;
    5: string item_type;
    6: string item_position;
    7: optional string item_desc="";
    8: optional string date;
}

struct ReplyInfo {
    1: string student_id;
    2: i32 post_id;
    3: string content;
    4: optional string date;
}

service UappService {
    bool register(1: RegisterInfo info);
    bool login(1: LoginInfo info);
    RegisterInfo getUserInfo(1: string student_id);
    bool uploadPost(1: PostInfo info);
    bool uploadReply(1: ReplyInfo info);
    list<ReplyInfo> getAllReply(1: i32 post_id);
    list<PostInfo> getAllPost();
}