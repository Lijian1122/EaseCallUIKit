package com.hyphenate.calluikit;


import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import com.hyphenate.EMCallBack;
import com.hyphenate.EMConferenceListener;
import com.hyphenate.EMError;
import com.hyphenate.EMMessageListener;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.calluikit.Base.EaseBaseActivity;
import com.hyphenate.calluikit.Utils.EaseCallKitUser;
import com.hyphenate.calluikit.Utils.EaseInviteInfo;
import com.hyphenate.calluikit.Utils.EaseMsgUtils;
import com.hyphenate.calluikit.Utils.EaseStringUtils;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConference;
import com.hyphenate.chat.EMConferenceAttribute;
import com.hyphenate.chat.EMConferenceManager;
import com.hyphenate.chat.EMConferenceMember;
import com.hyphenate.chat.EMConferenceStream;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMOptions;
import com.hyphenate.chat.EMStreamStatistics;
import com.hyphenate.util.EMLog;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by lijian on 2020.12.01.
 */

public class EaseCallUIKit {
    private static final String TAG = EaseCallUIKit.class.getSimpleName();
    private static EaseCallUIKit instance = null;
    private boolean sdkInited = false;
    private Context appContext = null;
    protected EMMessageListener messageListener = null;
    protected EMConferenceListener conferenceListener = null;

    private ArrayList<String> inviteeUsers = new ArrayList<>();
    private List<EMConferenceMember> members = new ArrayList<>();
    protected EMConference conference;
    protected EMConferenceStream localStream;


    //多人会议流
    private List<EMConferenceStream> conferenceStreams = Collections.synchronizedList(new ArrayList<EMConferenceStream>());
    private List<EMConferenceAttribute> conferenceAttributes = Collections.synchronizedList(new ArrayList<EMConferenceAttribute>());


    private int timeOutInterval = 5000; //被叫收到会议属性超时时间 毫秒
    public int callInterval = 30 * 1000; //主叫超时时间  毫秒
    private TimeHandler timeHandler = new TimeHandler();
    protected String conferneceId;
    protected String confernecePwd;


    private EaseBaseActivity callActivity;
    private EaseInviteInfo appInviteInfo;
    private EMConferenceAttribute conferenceAttribute;
    private EaseCallKitListener callListener;
    private EaseCallKitCfg callKitCfg;


    //配置项增加 振铃 震动 播放文件 通话中可以修改

    private EMConferenceStream oppositeStream;
    private EMConferenceStream old0ppositeStream = new EMConferenceStream();

    private EaseCallKitType callType;

    private EaseCallUIKit() {}

    public static EaseCallUIKit getInstance() {
        if(instance == null) {
            synchronized (EaseCallUIKit.class) {
                if(instance == null) {
                    instance = new EaseCallUIKit();
                }
            }
        }
        return instance;
    }


    /**
     * init 初始化
     * @param context
     * @param options
     * @return
     */
    public synchronized boolean init(Context context, EMOptions options) {
        if(sdkInited) {
            return true;
        }
        removeMessageListener();
        appContext = context.getApplicationContext();
        if (!isMainProcess(appContext)) {
            Log.e(TAG, "enter the service process!");
            return false;
        }
        if(options == null) {
            options = initChatOptions();
        }
        EMClient.getInstance().init(context, options);
        callKitCfg = new EaseCallKitCfg();

        //增加接收消息回调
        addMessageListener();
        addConferenceListener();
        sdkInited = true;
        return true;
    }

    /**
     * 设置用户信息及超时时间
     */
    public void setCallKitCfg(int timeOutinterval, EaseCallKitUser[] users){
        if(callKitCfg != null){
            callKitCfg.setCallTimeout(timeOutinterval);
            callKitCfg.setCallKitUserInfo(users);
        }
    }


    /**
     * 设置通话超时时间
     * @param interval
     */
    public void setCallTimeOutInterval(int interval) {
        this.timeOutInterval = interval;
    }

    /**
     * 更新用户信息
     */
    public void updataCallKitUsers(EaseCallKitUser[] users){
        if(callKitCfg != null){
            callKitCfg.setCallKitUserInfo(users);
        }
    }


    /***
     * 设置通话监听
     * @param listener
     * @return
     */
    public void setCallListener(EaseCallKitListener listener){
        this.callListener = listener;
    }

    /**
     * 设置邀请用户回调
     * @param
     * @return
     */
     public void onInviteeUser(){
         if(callListener != null){
             callListener.onInviteUsers(getContext());
         }
     }

    public boolean isMainProcess(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : activityManager.getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return context.getApplicationInfo().packageName.equals(appProcess.processName);
            }
        }
        return false;
    }

    protected EMOptions initChatOptions(){
        Log.d(TAG, "init HuanXin Options");

        EMOptions options = new EMOptions();
        // change to need confirm contact invitation
        options.setAcceptInvitationAlways(false);
        // set if need read ack
        options.setRequireAck(true);
        // set if need delivery ack
        options.setRequireDeliveryAck(false);

        return options;
    }


    private void addMessageListener() {
        this.messageListener = new EMMessageListener() {
            @Override
            public void onMessageReceived(List<EMMessage> messages) {
                for (EMMessage message : messages) {
                    String confId = message.getStringAttribute(EaseMsgUtils.CONFRID_KEY, "");
                    //收到会议邀请
                    if(confId != null && confId.length() != 0){
                        String result = message.getStringAttribute(EaseMsgUtils.RESULT_KEY, "");
                        String userName = message.getFrom();
                        int mcallType = message.getIntAttribute(EaseMsgUtils.CALLTYPE_KEY, 0);

                        if(result.isEmpty() || result == ""){

                            if(callListener != null){
                                callListener.onRecivedCall(EaseCallKitType.getfrom(mcallType),userName);
                            }
                            String confrPwd = message.getStringAttribute(EaseMsgUtils.CONFRIDPWD_KEY, "");

                            EaseInviteInfo inviteInfo = new EaseInviteInfo(confId,confrPwd,mcallType,userName,true);

                            //收到邀请加入会议
                            joinConference(inviteInfo);

                        }else{

                            String key = EaseMsgUtils.INVITEE_KEY + userName;

                            deleteConfenceAttribute(key);

                            if(!isDestroy(callActivity)){
                                if(callListener != null){
                                    callListener.onEndCallWithReason(callType,"对方忙碌",0);
                                }
                                    //收到被踢的通知退出会议
                                    Intent streamIntent = new Intent("com.OppositeStream.LOCAL_BROADCAST");
                                    streamIntent.putExtra("action", "opposite-busy");
                                streamIntent.putExtra("userName", userName);

                                    LocalBroadcastManager.getInstance(appContext).sendBroadcast(streamIntent);
                                }
                        }
                    }
                }
            }

            @Override
            public void onCmdMessageReceived(List<EMMessage> messages) {

            }

            @Override
            public void onMessageRead(List<EMMessage> messages) {

            }

            @Override
            public void onMessageDelivered(List<EMMessage> messages) {

            }

            @Override
            public void onMessageRecalled(List<EMMessage> messages) {

            }

            @Override
            public void onMessageChanged(EMMessage message, Object change) {

            }
        };
        EMClient.getInstance().chatManager().addMessageListener(messageListener);
    }

    private void removeMessageListener() {
        EMClient.getInstance().chatManager().removeMessageListener(messageListener);
        messageListener = null;
    }


    private void addConferenceListener(){
         conferenceListener = new EMConferenceListener() {
            @Override
            public void onMemberJoined(EMConferenceMember member) {
                if(!members.contains(member)){
                    members.add(member);
                }
            }

            @Override
            public void onMemberExited(EMConferenceMember member) {
                if(members.size() > 0){
                    for(EMConferenceMember num :members){
                        if(num.memberName.equals(member.memberName)){
                            members.remove(num);
                            break;
                        }
                    }

                    //会议中已经没有其他人 退出会议
                    if(members.size() == 0){
                        //主叫退出会议
                        Intent streamIntent = new Intent
                                       ("com.OppositeStream.LOCAL_BROADCAST");                         streamIntent.putExtra("action", "all-member-remove");
                                LocalBroadcastManager.getInstance(appContext)
                                       .sendBroadcast(streamIntent);
                    }
                }
            }

            @Override
            public void onStreamAdded(EMConferenceStream stream) {
                if(callType != EaseCallKitType.CONFERENCE_CALL){
                    if(oppositeStream != null){
                        //收到同样的发流事件 其他设备已处理
                        if(oppositeStream.getUsername().equals(EMClient.getInstance().getCurrentUser())){
                            if(callListener != null){
                                callListener.onEndCallWithReason(callType,"已在其他设备处理",0);
                            }
                            EMLog.i(TAG, "The_other_is_recived  111111");
                            String info = getContext().getString(R.string.The_other_is_recived);
                            Toast.makeText(getContext(), info, Toast.LENGTH_SHORT).show();
                            //退出会议
                            eixitConference();
                        }
                    }
                    oppositeStream = stream;
                    old0ppositeStream.setAudioOff(stream.isAudioOff());
                    old0ppositeStream.setVideoOff(stream.isVideoOff());
                    if(callActivity instanceof EaseVideoCallActivity){
                        if(!callActivity.isInComingCall()){
                            if(callListener != null){
                                callListener.onEndCallWithReason(callType,"已在其他设备处理",0);
                            }
                            Intent streamIntent = new Intent
                                    ("com.OppositeStream.LOCAL_BROADCAST");
                            streamIntent.putExtra("action", "add-stream");
                            LocalBroadcastManager.getInstance(appContext).
                                    sendBroadcast(streamIntent);
                        }
                    }
                }else{

                    boolean flag = addMuliteStream(stream);
                    if(!flag){
                        if(!isDestroy(callActivity)){
                            EMLog.i(TAG, "The_other_is_recived  333");
                            String info = getContext().getString(R.string.The_other_is_recived);
                            Toast.makeText(getContext(), info, Toast.LENGTH_SHORT).show();
                            //收到被踢的通知退出会议
                            Intent streamIntent = new Intent("com.OppositeStream.LOCAL_BROADCAST");
                            streamIntent.putExtra("action", "all-member-remove");
                            LocalBroadcastManager.getInstance(appContext).sendBroadcast(streamIntent);
                        }else{
                            //退出会议
                            eixitConference();
                        }
                    }
                }
            }

            @Override
            public void onStreamRemoved(EMConferenceStream stream) {
                if(callType == EaseCallKitType.CONFERENCE_CALL){
                    removeMuliteStream(stream);
                }
            }

            @Override
            public void onStreamUpdate(EMConferenceStream stream) {
                if(callType == EaseCallKitType.CONFERENCE_CALL){
                    updataMuliteStream(stream);
                }else{
                    if(stream.getStreamId().equals(oppositeStream.getStreamId())){
                        //发送流的更新
                        boolean isToVideo = false;
                        if(old0ppositeStream.isVideoOff() && !stream.isVideoOff()){
                            if(callType != EaseCallKitType.SIGNAL_VIDEO_CALL){
                                //音频转接视频
                                isToVideo = true;
                                callType = EaseCallKitType.SIGNAL_VIDEO_CALL;
                                Intent streamIntent = new Intent("com.OppositeStream.LOCAL_BROADCAST");
                                streamIntent.putExtra("action", "video-voice-change");
                                streamIntent.putExtra("isToVideo",isToVideo);
                                LocalBroadcastManager.getInstance(appContext).sendBroadcast(streamIntent);
                            }

                        }else if(!old0ppositeStream.isVideoOff() && stream.isVideoOff()){
                            if(callType != EaseCallKitType.SIGNAL_VOICE_CALL){
                                //视频转音频
                                isToVideo = false;
                                callType = EaseCallKitType.SIGNAL_VOICE_CALL;
                                Intent streamIntent = new Intent("com.OppositeStream.LOCAL_BROADCAST");
                                streamIntent.putExtra("action", "video-voice-change");
                                streamIntent.putExtra("isToVideo",isToVideo);
                                LocalBroadcastManager.getInstance(appContext).sendBroadcast(streamIntent);
                            }
                        }
                        old0ppositeStream.setAudioOff(stream.isAudioOff());
                        old0ppositeStream.setVideoOff(stream.isVideoOff());

                        oppositeStream = stream;
                    }
                }
            }

            @Override
            public void onPassiveLeave(int error, String message) {
                EMLog.i(TAG, "onPassiveLeave  error :" + error + " message:" + message);
                //其他设备发流被踢 error :-410 message:reason-enter-other-device
                if(error == -410){
                    EMLog.i(TAG, "The_other_is_recived  22222");
                    String info = getContext().getString(R.string.The_other_is_recived);
                    Toast.makeText(getContext(), info, Toast.LENGTH_SHORT).show();
                    if(callListener != null){
                        callListener.onEndCallWithReason(callType,"已在其他设备处理",0);
                    }
                }

                //activity not destory
                if(!isDestroy(callActivity)){
                    //收到被踢的通知退出会议
                    Intent streamIntent = new Intent("com.OppositeStream.LOCAL_BROADCAST");
                    streamIntent.putExtra("action", "all-member-remove");
                    LocalBroadcastManager.getInstance(appContext).sendBroadcast(streamIntent);
                }else{
                    eixitConference();
                }
            }

            @Override
            public void onConferenceState(ConferenceState state) {

            }

            @Override
            public void onStreamStatistics(EMStreamStatistics statistics) {

            }

            @Override
            public void onStreamSetup(String streamId) {

            }

            @Override
            public void onSpeakers(List<String> speakers) {

            }

            @Override
            public void onReceiveInvite(String confId, String password, String extension) {

            }

            @Override
            public void onGetLocalStreamId(String rtcId, String streamId){
                if(appInviteInfo.isComming()){
                    if(localStream.getStreamId().equals(rtcId)){
                        //删除会议属性
                        String key = EaseMsgUtils.INVITEE_KEY + EMClient.getInstance().getCurrentUser();
                        deleteConfenceAttribute(key);
                    }
                }
            }


            @Override
            public void onAttributesUpdated(EMConferenceAttribute[] attributes) {
                EMLog.i(TAG, " onAttributesUpdated started ");
                int size = attributes.length;
                boolean deleteflag = true;
                for (int i = 0; i < size; i++) {
                    EMConferenceAttribute attribute = attributes[i];
                    EMLog.i(TAG, " onAttributesUpdated Get key:" + attribute.key + " " + attribute.value);
                    if(callType != EaseCallKitType.CONFERENCE_CALL){
                        if (attribute.action.equals(EMConferenceAttribute.Action.ADD)) {
                            conferenceAttribute = attribute;
                        } else if (attribute.action.equals(EMConferenceAttribute.Action.DELETE)) {
                            //会议属性已经删除 被其他端接听
                            conferenceAttribute = null;
                            if (appInviteInfo != null) {
                                if (!appInviteInfo.isComming()) {
                                    timeHandler.stopTime();
                                } else {
                                    if (callActivity != null) {
                                            //检测到会议属性删除
                                            Intent streamIntent = new Intent("com.OppositeStream.LOCAL_BROADCAST");
                                            streamIntent.putExtra("action", "delete-attribute");
                                            LocalBroadcastManager.getInstance(appContext).sendBroadcast(streamIntent);
                                        } else {
                                            timeHandler.stopTime();
                                            //直接退出会议
                                            eixitConference();
                                        }
                                    }
                                }
                            }
                    }else {
                        if(attribute.key.contains(EaseMsgUtils.INVITEE_KEY)){
                            String username = attribute.key.substring(EaseMsgUtils.INVITEE_KEY.length());
                            if (attribute.action.equals(EMConferenceAttribute.Action.ADD)) {
                                if(attribute.key.contains(EMClient.getInstance().getCurrentUser())){
                                    conferenceAttribute = attribute;
                                }else{
                                    conferenceAttributes.add(attribute);
                                    //检测到会议属性增加
                                    Intent streamIntent = new Intent("com.OppositeStream.LOCAL_BROADCAST");
                                    streamIntent.putExtra("action", "add-attribute");
                                    streamIntent.putExtra("userName", username);
                                    LocalBroadcastManager.getInstance(appContext).sendBroadcast(streamIntent);

                                }
                            } else if (attribute.action.equals(EMConferenceAttribute.
                                    Action.DELETE)){
                                //自己的会议属性
                                if(attribute.key.contains(EMClient.getInstance().getCurrentUser())){
                                    conferenceAttribute = null;
                                }else{
                                    conferenceAttributes.remove(attribute);
                                }

                                if(!isDestroy(callActivity)){
                                    //检测到会议属性删除
                                    Intent streamIntent = new Intent("com.OppositeStream.LOCAL_BROADCAST");
                                    streamIntent.putExtra("action", "delete-attribute");
                                    streamIntent.putExtra("userName", username);
                                    LocalBroadcastManager.getInstance(appContext).sendBroadcast(streamIntent);
                                    if(!appInviteInfo.isComming()){
                                        if(members.size() == 0){
                                            //检测会议成员
                                            Intent userIntent = new Intent("com.OppositeStream.LOCAL_BROADCAST");
                                            userIntent.putExtra("action", "all-member-remove");
                                            LocalBroadcastManager.getInstance(appContext).sendBroadcast(userIntent);
                                        }
                                    }
                                }else{
                                    if(appInviteInfo.isComming()){
                                        eixitConference();
                                    }
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onRoleChanged(EMConferenceManager.EMConferenceRole role) {

            }
        };

        EMClient.getInstance().conferenceManager().addConferenceListener(conferenceListener);
    }

    public EaseInviteInfo getInviteInfo() {
        return appInviteInfo;
    }


    /**
     *加入1V1通话
     */
    public void startSignleCall(final EaseCallKitType type, final String userName){
        callType = type;
        createAndJoinConference(userName);
    }

    /**
     * 加入多人视频通话
     */
    public void startInviteMuitupleCall(final String[] users){
        if(users != null){
            if(users.length > 11 - members.size()){
                if(callListener != null){
                    callListener.onInviteerIsFull(12,members.size() + 1);
                }
                return;
            }
            callType = EaseCallKitType.CONFERENCE_CALL;
            inviteeUsers.clear();
            for(String user:users){
                inviteeUsers.add(user);
            }
        }
        //还没有加入会议 创建会议
        if(isDestroy(callActivity)){
            if(users != null && users.length > 0){
                //创建加入会议
                createAndJoinConference("");
            }
        }else{

            //邀请成员加入
            Intent intent = new Intent(appContext, callActivity.getClass())
                    .addFlags(FLAG_ACTIVITY_NEW_TASK);
            appContext.startActivity(intent);
        }
    }



    /**
     *被邀请用户名
     * @param userName
     */
    public void createAndJoinConference(String userName) {
        confernecePwd = EaseStringUtils.getRandomString(8);
        members.clear();
        conferenceStreams.clear();
        conferenceAttributes.clear();
        oppositeStream = null;
        EMClient.getInstance().conferenceManager().createAndJoinConference(EMConferenceManager.EMConferenceType.SmallCommunication,
                confernecePwd, true, false, false, new EMValueCallBack<EMConference>() {
                    @Override
                    public void onSuccess(final EMConference value) {
                        EMLog.e(TAG, "create and join conference success");
                        conference = value;
                        conferneceId = conference.getConferenceId();

                        if(callType != EaseCallKitType.CONFERENCE_CALL) { //1V1视频通话
                            appInviteInfo = new EaseInviteInfo(conferneceId, confernecePwd, callType.code, userName, false);
                            //加入会议成功
                            callActivity = new EaseVideoCallActivity();
                            Intent intent = new Intent(appContext, callActivity.getClass()).addFlags(FLAG_ACTIVITY_NEW_TASK);
                            Bundle bundle = new Bundle();
                            bundle.putBoolean("isComingCall", false);
                            bundle.putString("username", userName);
                            bundle.putString(EaseMsgUtils.CONFRID_KEY, conferneceId);
                            bundle.putString(EaseMsgUtils.CONFRIDPWD_KEY, confernecePwd);
                            intent.putExtras(bundle);
                            appContext.startActivity(intent);

                            //开始定时
                            timeHandler.startTime();
                        }else{ //多人视频通话

                            appInviteInfo = new EaseInviteInfo(conferneceId, confernecePwd, EaseCallKitType.CONFERENCE_CALL.code, userName, false);
                            localStream = new EMConferenceStream();
                            localStream.setUsername(EMClient.getInstance().getCurrentUser());
                            localStream.setStreamId("local-stream");
                            EaseCallUIKit.getInstance().getConferenceStreams().add(localStream);

                            callActivity = new EaseMultipleVideoCallActivity();
                            Intent intent = new Intent(appContext, callActivity.getClass()).addFlags(FLAG_ACTIVITY_NEW_TASK);
                            Bundle bundle = new Bundle();
                            bundle.putString(EaseMsgUtils.CONFRID_KEY, conferneceId);
                            bundle.putString(EaseMsgUtils.CONFRIDPWD_KEY, confernecePwd);
                            bundle.putStringArrayList(EaseMsgUtils.CONFRINVITEE_KEY,inviteeUsers);
                            intent.putExtras(bundle);
                            appContext.startActivity(intent);
                        }
                    }

                    @Override
                    public void onError(final int error, final String errorMsg) {
                        EMLog.e(TAG, "Create and join conference failed error " + error + ", msg " + errorMsg);
                    }
                });
    }


    /**
     * 被呼叫人根据 confId 和 password 加入会议
     */
    protected void joinConference(EaseInviteInfo inviteInfo) {
        if(inviteInfo == null){
            EMLog.e(TAG, "inviteInfo is null");
            return;
        }
        if(conference != null && conference.getConferenceId().equals(inviteInfo.getConferenceId())){
            EMLog.e(TAG, "already conference successed  conferenceId" + conference.getConferenceId());
            return;
        }
        String conferenceId = inviteInfo.getConferenceId();
        String conferencePwd = inviteInfo.getPassWord();
        members.clear();
        conferenceStreams.clear();
        conferenceAttributes.clear();
        oppositeStream = null;
        EMClient.getInstance().conferenceManager().joinConference(conferenceId, conferencePwd, new EMValueCallBack<EMConference>() {
            @Override
            public void onSuccess(EMConference value) {
                EMLog.e(TAG, "join conference successed  conferenceId" + value.getConferenceId());

                //增加本地流
                localStream = new EMConferenceStream();
                localStream.setUsername(EMClient.getInstance().getCurrentUser());
                localStream.setStreamId("local-stream");
                EaseCallUIKit.getInstance().getConferenceStreams().add(localStream);

                conference = value;
                appInviteInfo = inviteInfo;
                callType = EaseCallKitType.getfrom(inviteInfo.getCallType());
                //等待会议属性
                conferenceAttribute = null;
                timeHandler.startTime();
            }

            @Override
            public void onError(final int error, final String errorMsg) {
                EMLog.e(TAG, "join conference failed error " + error + ", msg " + errorMsg);

                //判断当时是否忙碌状态，发送忙碌消息
                if(error == EMError.CALL_ALREADY_JOIN){
                    if(conference != null && conference.getConferenceId().equals(inviteInfo.getConferenceId())){
                        EMLog.e(TAG, "already conference successed  conferenceId" + conference.getConferenceId());
                        return;
                    }
                    final EMMessage message = EMMessage.createTxtSendMessage( "对方正在通话中", inviteInfo.getUserName());
                    message.setAttribute(EaseMsgUtils.CONFRID_KEY,inviteInfo.getConferenceId());
                    message.setAttribute(EaseMsgUtils.RESULT_KEY,"busy");
                    final EMConversation conversation = EMClient.getInstance().chatManager().getConversation(inviteInfo.getUserName(), EMConversation.EMConversationType.Chat, true);
                    message.setMessageStatusCallback(new EMCallBack() {
                        @Override
                        public void onSuccess() {
                            EMLog.d(TAG, "Invite call success");
                            conversation.removeMessage(message.getMsgId());
                        }

                        @Override
                        public void onError(int code, String error) {
                            EMLog.e(TAG, "Invite call error " + code + ", " + error);
                            conversation.removeMessage(message.getMsgId());
                        }

                        @Override
                        public void onProgress(int progress, String status) {

                        }
                    });
                    EMClient.getInstance().chatManager().sendMessage(message);
                }
            }
        });
    }


    protected class TimeHandler extends Handler {
        private final int MSG_TIMER = 0;
        private DateFormat dateFormat = null;
        private int timePassed = 0;

        public TimeHandler() {
            dateFormat = new SimpleDateFormat("HH:mm:ss");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

        public void startTime() {
            timePassed = 0;
            conferenceAttribute = null;
            sendEmptyMessageDelayed(MSG_TIMER, 1000);
        }

        public void stopTime() {
            removeMessages(MSG_TIMER);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_TIMER) {
                // TODO: update calling time.
                timePassed++;
                String time = dateFormat.format(timePassed * 1000);
                //updateConferenceTime(time);
                sendEmptyMessageDelayed(MSG_TIMER, 1000);

                if (appInviteInfo != null) {
                    if (appInviteInfo.isComming()) {
                        if(conferenceAttribute != null){
                            //收到邀请自己的会议属性
                            if (conferenceAttribute.key.equals(EaseMsgUtils.INVITEE_KEY +
                                    EMClient.getInstance().getCurrentUser())) {

                                //停止计时
                                timeHandler.stopTime();
                                if(appInviteInfo.getCallType() != EaseCallKitType.
                                        CONFERENCE_CALL.code){ //1v1通话

                                    //加入会议成功
                                    callActivity = new EaseVideoCallActivity();
                                    Intent intent = new Intent(appContext, callActivity.getClass())
                                            .addFlags(FLAG_ACTIVITY_NEW_TASK);
                                    Bundle bundle = new Bundle();
                                    bundle.putBoolean("isComingCall", true);
                                    bundle.putString("username", appInviteInfo.getUserName());

                                    bundle.putString(EaseMsgUtils.CONFRID_KEY, appInviteInfo.getConferenceId());
                                    bundle.putString(EaseMsgUtils.CONFRIDPWD_KEY, appInviteInfo.getPassWord());
                                    intent.putExtras(bundle);
                                    appContext.startActivity(intent);
                                }else{ //多人视频
                                    //加入会议成功
                                    if(!isDestroy(callActivity)){
                                        EMLog.d(TAG,"EaseMultipleVideoCallActivity is create");
                                        return;
                                    }

                                    callActivity = new EaseMultipleVideoCallActivity();
                                    Intent intent = new Intent(appContext, callActivity.getClass())
                                            .addFlags(FLAG_ACTIVITY_NEW_TASK);
                                    Bundle bundle = new Bundle();
                                    bundle.putBoolean("isComingCall", true);
                                    bundle.putString(EaseMsgUtils.CONFRID_KEY, appInviteInfo.getConferenceId());
                                    bundle.putString("username", appInviteInfo.getUserName());
                                    bundle.putString(EaseMsgUtils.CONFRIDPWD_KEY, appInviteInfo.getPassWord());
                                    intent.putExtras(bundle);
                                    appContext.startActivity(intent);
                                }
                            } else{
                                //收到的会议属性不对
                                eixitConference();
                            }
                        } else {
                            //超过5s 未能收到会议属性
                            if(timePassed * 1000 == callInterval){
                                //超时未收到会议属性 退出会议
                                timeHandler.stopTime();
                                eixitConference();
                            }
                        }
                    } else {
                        //只处理单人的超时
                        if(appInviteInfo.getCallType() != EaseCallKitType.
                                CONFERENCE_CALL.code){
                            if (timePassed * 1000 == timeOutInterval) {
                                timeHandler.stopTime();
                                if (conferenceAttribute != null) {
                                    //超时时候删除会议属性
                                    deleteConfenceAttribute(conferenceAttribute.key);
                                    String info = getContext().getString(R.string.The_other_is_not_recived);
                                    Toast.makeText(getContext(), info, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                    }
                }
                super.handleMessage(msg);
            }
        }
    }


    public EMConferenceStream getOppositeStream() {
        return oppositeStream;
    }


    public void setInviteInfo(EaseInviteInfo inviteInfo) {
        this.appInviteInfo = inviteInfo;
    }

    public Context getContext() {
        return appContext;
    }


    //退出会议
    private void eixitConference(){
        EMClient.getInstance().conferenceManager().exitConference(new EMValueCallBack() {
            @Override
            public void onSuccess(Object value) {
                EMLog.d(TAG,"exit conference scuessed");
                if(callActivity != null) {
                    if (!callActivity.isFinishing()) {
                        if (callActivity instanceof EaseVideoCallActivity) {
                            callActivity.finish();
                        }
                    }
                }
            }

            @Override
            public void onError(int error, String errorMsg) {
                EMLog.d(TAG,"exit conference failed error:" + error + " errorMsg" +errorMsg);
                if(callActivity != null) {
                    if (!callActivity.isFinishing()) {
                        if (callActivity instanceof EaseVideoCallActivity) {
                            callActivity.finish();
                        }
                    }
                }
            }
        });

    }


    /**
     * 删除会议属性
     */
    public void deleteConfenceAttribute(String key){
        EMClient.getInstance().conferenceManager().deleteConferenceAttribute(key, new EMValueCallBack<Void>() {
            @Override
            public void onSuccess(Void value) {
                EMLog.d(TAG," deleteConferenceAttribute successed key:" + key);
            }

            @Override
            public void onError(int error, String errorMsg) {
                EMLog.d(TAG, " deleteConferenceAttribute failed key:" + key);
            }
        });
    }

    /**
     * 判断Activity是否Destroy
     * @param mActivity
     * @return true:已销毁
     */
    public static boolean isDestroy(Activity mActivity) {
        if (mActivity == null ||
                mActivity.isFinishing() ||
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && mActivity.isDestroyed())) {
            return true;
        } else {
            return false;
        }
    }

    /**
     *增加多人会议流
     */
    public boolean addMuliteStream(EMConferenceStream conferenceStream){
        boolean addflag = true;
        if(conferenceStreams.size()> 0){
            for(EMConferenceStream stream:conferenceStreams){
                if(stream.getStreamId().equals(conferenceStream.getStreamId())){
                    Log.e(TAG,"The_other_is_recived:" + stream.getStreamId());
                    addflag = false;
                }
                if(stream.getUsername().equals(conferenceStream.getUsername())){
                    //处理占位符的情况
                    if(stream.getStreamId().equals(conferenceStream.getUsername())){
                        int index  = conferenceStreams.indexOf(stream);
                        conferenceStreams.set(index,conferenceStream);
                        addflag = false;
                    }else{
                        //收到同样用户的流 已经在其他设备处理
                        return false;
                    }
                }

            }
        }

        if(addflag){
            conferenceStreams.add(conferenceStream);
        }

        //已经发流进行广播 发送本地广播

        if(!isDestroy(callActivity)){
            if(!localStream.getStreamId().equals("local-stream")){
                Intent streamIntent = new Intent
                        ("com.OppositeStream.LOCAL_BROADCAST");
                streamIntent.putExtra("action", "add-stream");
                streamIntent.putExtra("streamId", conferenceStream.getStreamId());
                LocalBroadcastManager.getInstance(appContext).
                        sendBroadcast(streamIntent);
            }
            return  true;
        }

        return true;
    }


    /**
     *移除多人会议流
     */
    private boolean removeMuliteStream(EMConferenceStream conferenceStream){
        if(conferenceStreams.size()> 0){
            for(EMConferenceStream stream:conferenceStreams){
                if(stream.getStreamId().equals(conferenceStream.getStreamId())){

                    //发送本地广播
                    if(callActivity instanceof EaseMultipleVideoCallActivity){
                        if(!localStream.getStreamId().equals("local-stream")){
                            Intent streamIntent = new Intent
                                    ("com.OppositeStream.LOCAL_BROADCAST");
                            streamIntent.putExtra("action", "remove-stream");
                            streamIntent.putExtra("streamId", conferenceStream.getStreamId());
                            LocalBroadcastManager.getInstance(appContext).
                                    sendBroadcast(streamIntent);
                        }else{
                            //直接移除流
                            conferenceStreams.remove(stream);
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }

    /**
     *更新多人会议流
     */
    private void updataMuliteStream(EMConferenceStream conferenceStream){
        if(conferenceStreams.size()> 0){
            for(EMConferenceStream stream:conferenceStreams){
                if(stream.getStreamId().equals(conferenceStream.getStreamId())){

                    //发送本地广播
                    if(callActivity instanceof EaseMultipleVideoCallActivity){
                        if(!localStream.getStreamId().equals("local-stream")){
                            Intent streamIntent = new Intent
                                    ("com.OppositeStream.LOCAL_BROADCAST");
                            streamIntent.putExtra("action", "updata-stream");
                            streamIntent.putExtra("streamId", conferenceStream.getStreamId());
                            LocalBroadcastManager.getInstance(appContext).
                                    sendBroadcast(streamIntent);
                        }else{
                            int index  = conferenceStreams.indexOf(stream);
                            conferenceStreams.set(index,conferenceStream);
                        }
                    }
                }
            }
        }
    }



    /**
     *获取当前流
     */
    protected EMConferenceStream getMuliteStream(String streamId){
        synchronized (conferenceStreams) {
            Iterator iterator = conferenceStreams.iterator();
            // Must be in synchronized block
            while(iterator.hasNext()){
                EMConferenceStream stream  = (EMConferenceStream) iterator.next();
                if(stream.getStreamId().equals(streamId)){
                    return stream;
                }
            }
        }
        return  null;
    }

    /**
     *获取当前流
     */
    protected EMConferenceStream getUserStream(String userName){
//        if(conferenceStreams.size()> 0){
//            for(EMConferenceStream stream:conferenceStreams){
//                if(stream.getUsername().equals(userName)){
//                    return  stream;
//                }
//            }
//        }
//        return null;
        synchronized (conferenceStreams) {
            Iterator iterator = conferenceStreams.iterator();
            // Must be in synchronized block
            while(iterator.hasNext()){
                EMConferenceStream stream  = (EMConferenceStream) iterator.next();
                if(stream.getUsername().equals(userName)){
                    return stream;
                }
            }
        }
        return null;
    }

    public EaseCallKitType getCallType() {
        return callType;
    }

    public void setCallType(EaseCallKitType type) {
        this.callType = type;
    }

    public ArrayList<String> getInviteeUsers() {
        return inviteeUsers;
    }

    public EMConferenceStream getLocalStream() {
        return localStream;
    }

    public List<EMConferenceStream> getConferenceStreams() {
        return conferenceStreams;
    }

    public void initCallActivity() {
        this.callActivity = null;
        this.conference = null;
    }
}
