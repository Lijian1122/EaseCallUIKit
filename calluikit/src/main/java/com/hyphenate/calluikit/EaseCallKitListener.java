package com.hyphenate.calluikit;


import android.content.Context;

/**
 * Created by lijian on 2020.12.01.
 */


public interface EaseCallKitListener {

    /**
     *邀请好友通话
     */
    void onInviteUsers(Context context);


    /**
     * 通话结束
     * @param callType
     * @param reason
     * @param callTime
     */
    void onEndCallWithReason(EaseCallKitType callType, String reason, int callTime);


    /**
     * 收到通话邀请
     * @param callType
     * @param userId
     */
     void onRecivedCall(EaseCallKitType callType, String userId);

    /**
     * 邀请人数太多
     * @param viodeCount
     * @param currentCount
     */
    void onInviteerIsFull(int viodeCount,int currentCount);
}
