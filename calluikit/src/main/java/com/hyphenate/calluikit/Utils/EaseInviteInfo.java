package com.hyphenate.calluikit.Utils;

/**
 * Created by lijian on 2020.12.01
 */

public class EaseInviteInfo {

    private String conferenceId;
    private String passWord;
    private int callType;
    private String userName;
    private boolean isComming;

    public EaseInviteInfo(String conferenceId, String passWord, int type, String userName, boolean isComming){
        this.conferenceId = conferenceId;
        this.passWord = passWord;
        this.callType = type;
        this.userName = userName;
        this.isComming = isComming;
    }

    public String getConferenceId() {
        return conferenceId;
    }

    public void setConferenceId(String conferenceId) {
        this.conferenceId = conferenceId;
    }

    public String getPassWord() {
        return passWord;
    }

    public int getCallType() { return callType; }

    public void setCallType(int callType) { this.callType = callType;}

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public String getUserName() { return userName; }

    public void setUserName(String userName) { this.userName = userName; }

    public boolean isComming() { return isComming; }

    public void setComming(boolean comming) { isComming = comming; }
}
