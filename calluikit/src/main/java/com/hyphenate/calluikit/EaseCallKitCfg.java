package com.hyphenate.calluikit;

import com.hyphenate.calluikit.Utils.EaseCallKitUser;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by lijian on 2020.12.12.
 */


/**
 * 有关EaseCallkit的一些配置
 * callKitUserMap  用户信息
 * callTimeout  超时时间
 */
public class EaseCallKitCfg {
    private Map<String, EaseCallKitUser> callKitUserMap = new HashMap<>();
    private int callTimeout;

    public void EaseCallKitCfg(int callTimeout,EaseCallKitUser[] users){
        this.callTimeout = callTimeout;
        for(EaseCallKitUser user:users){
            callKitUserMap.put(user.getUserId(),user);
        }
    }

    public void EaseCallKitCfg(){

    }

    public Map<String, EaseCallKitUser> getCallKitUserMap() {
        return callKitUserMap;
    }

    public void setCallKitUserInfo(EaseCallKitUser[] users) {
        for(EaseCallKitUser user:users){
            callKitUserMap.put(user.getUserId(),user);
        }
    }

    public int getCallTimeout() {
        return callTimeout;
    }

    public void setCallTimeout(int callTimeout) {
        this.callTimeout = callTimeout;
    }
}
