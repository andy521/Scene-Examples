package io.agora.superapp.model;

import java.util.HashMap;

public class UserInfo {

    public long expiredTime;
    public String userId;
    public String userName;
    public String roomId;

    public String objectId = "";


    public HashMap<String, Object> toMap(){
        HashMap<String, Object> map = new HashMap<>();
        map.put("expiredTime", expiredTime);
        map.put("userId", userId);
        map.put("userName", userName);
        map.put("roomId", roomId);
        return map;
    }
}
