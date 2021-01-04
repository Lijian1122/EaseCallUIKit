package com.hyphenate.calluikit.Utils;


/**
 * Created by lijian on 2020.12.12.
 */

/**
 * 设置用户 环信Id对应的头像 昵称
 * userId 环信Id
 * nickName 用户昵称
 * headUrl 用户头像地址
 */
public class EaseCallKitUser {
    private String userId;
    private String nickName;
    private String headUrl;

    public EaseCallKitUser(String userId,String nickName,String headUrl){
        this.userId = userId;
        this.nickName =nickName;
        this.headUrl = headUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getHeadIUrl() {
        return headUrl;
    }

    public void setHeadIUrl(String headIUrl) {
        this.headUrl = headIUrl;
    }
}
