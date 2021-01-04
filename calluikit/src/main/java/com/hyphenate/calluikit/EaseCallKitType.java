package com.hyphenate.calluikit;


/**
 * Created by lijian on 2020.12.01.
 */


/**
 * 通话类型说明
 */
public enum EaseCallKitType {
    SIGNAL_VOICE_CALL(0), //1v1语音通话
    SIGNAL_VIDEO_CALL(1), //1v1视频通话
    CONFERENCE_CALL(2);   //多人音视频

    public int code;

    EaseCallKitType(int code) {
        this.code = code;
    }

    public static EaseCallKitType getfrom(int code) {
        switch (code) {
            case 0:
                return SIGNAL_VOICE_CALL;
            case 1:
                return SIGNAL_VIDEO_CALL;
            case 2:
                return CONFERENCE_CALL;
            default:
                return SIGNAL_VIDEO_CALL;
        }
    }
};