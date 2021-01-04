package com.hyphenate.calluikit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.EMValueCallBack;
import com.hyphenate.calluikit.Base.EaseBaseActivity;
import com.hyphenate.calluikit.Base.EaseCallMemberView;
import com.hyphenate.calluikit.Base.EaseCallMemberViewGroup;
import com.hyphenate.calluikit.Base.EaseCommingCallView;
import com.hyphenate.calluikit.Utils.EaseLocalBroadcastReceiver;
import com.hyphenate.calluikit.Utils.EaseMsgUtils;
import com.hyphenate.calluikit.Utils.EasePhoneStateManager;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConference;
import com.hyphenate.chat.EMConferenceStream;
import com.hyphenate.chat.EMStreamParam;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.util.EMLog;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import static com.hyphenate.calluikit.Utils.EaseMsgUtils.MSG_MAKE_CONFERENCE_VIDEO;


/**
 * Created by lijian on 2020.12.10.
 */
public class EaseMultipleVideoCallActivity extends EaseBaseActivity implements  View.OnClickListener {

    private AudioManager audioManager;
    private EMConference conference;
    private EMStreamParam normalParam;
    // 正在显示音视频Window的stream
    private static EMConferenceStream windowStream;

    private EaseCallMemberView localView;
    private EaseCommingCallView incomingCallView;
    private EaseCallMemberViewGroup callConferenceViewGroup;

    // tools panel的父view
    private View toolsPanelView;
    // tools panel中显示会议成员名称的TextView
    private TextView membersTV;
    // tools panel中显示会议成员数量的TextView
    private TextView memberCountTV;
    // tools panel中显示时间的TextView
    private TextView callTimeView;
    // 麦克风开关
    private ImageButton micSwitch;
    // 摄像头开关
    private ImageButton cameraSwitch;
    // 话筒开关
    private ImageButton speakerSwitch;
    // 屏幕分享开关
    private ImageButton screenShareSwitch;
    // 前后摄像头切换
    private ImageButton changeCameraSwitch;
    // 挂断按钮
    private ImageButton hangupBtn;
    // 显示debug信息按钮
    private ImageButton debugBtn;
    // 邀请其他成员加入的按钮
    private ImageView inviteBtn;
    // 全屏模式下改变视频显示模式的按钮,只在全屏模式下显示
    private ImageView scaleModeBtn;
    // 显示悬浮窗的按钮
    private ImageButton closeBtn;
    // 退出全屏模式的按钮,只在全屏模式下显示
    private ImageButton zoominBtn;
    // ------ tools panel relevant end ------


    // ------ full screen views start -------
    private View stateCoverMain;
    private View membersLayout;
    private TextView membersTVMain;
    private TextView memberCountTVMain;
    private TextView callTimeViewMain;
    private View talkingLayout;
    private ImageView talkingImage;
    private TextView talkerTV;
    private TextView tv_nick;
    // ------ full screen views end -------

    private TimeHandler timeHandler;
    private Activity activity;
    private EMConferenceStream localStream;

    EaseLocalBroadcastReceiver localReceiver;
    LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(EaseCallUIKit.getInstance().getContext());
    private HashMap<String, EaseCallMemberView> placesMemberMap = new HashMap<>();


    @Override
     protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     }

     public void initParms(Bundle bundle){
         if(bundle != null) {
             isInComingCall = bundle.getBoolean("isComingCall", false);
             username = bundle.getString("username");
             conferneceId = bundle.getString(EaseMsgUtils.CONFRID_KEY);
             confernecePwd = bundle.getString(EaseMsgUtils.CONFRIDPWD_KEY);
         }
     }

     public View bindView(){
        return null;
     }


     public int bindLayout(){
        return R.layout.activity_multiply_video_call;
     }


     public void initView(final View view){
         activity = this;
         incomingCallView = (EaseCommingCallView)findViewById(R.id.incoming_call_view);
         callConferenceViewGroup = (EaseCallMemberViewGroup) view.findViewById(R.id.surface_view_group);

//         toolsPanelView = view.findViewById(R.id.layout_tools_panel);

         inviteBtn = (ImageView) view.findViewById(R.id.btn_invite);
//         membersTV = (TextView) view.findViewById(R.id.tv_members);
//         memberCountTV = (TextView) view.findViewById(R.id.tv_member_count);
         callTimeView = (TextView) view.findViewById(R.id.tv_call_time);
         micSwitch = (ImageButton) view.findViewById(R.id.btn_mic_switch);
         cameraSwitch = (ImageButton) view.findViewById(R.id.btn_camera_switch);
         speakerSwitch = (ImageButton) view.findViewById(R.id.btn_speaker_switch);
//         screenShareSwitch = (ImageButton)view.findViewById(R.id.btn_desk_share);
         changeCameraSwitch = (ImageButton)view.findViewById(R.id.btn_change_camera_switch);
         hangupBtn = (ImageButton) view.findViewById(R.id.btn_hangup);
//         debugBtn = (ImageButton) view.findViewById(R.id.btn_debug);
         scaleModeBtn = (ImageView) view.findViewById(R.id.btn_float);

         talkingImage = (ImageView) view.findViewById(R.id.icon_talking);
         tv_nick = (TextView)findViewById(R.id.tv_nick);

         incomingCallView.setOnActionListener(onActionListener);
         callConferenceViewGroup.setOnItemClickListener(onItemClickListener);
         callConferenceViewGroup.setOnScreenModeChangeListener(onScreenModeChangeListener);
         callConferenceViewGroup.setOnPageStatusListener(onPageStatusListener);
         inviteBtn.setOnClickListener(this);
         micSwitch.setOnClickListener(this);
         speakerSwitch.setOnClickListener(this);
         cameraSwitch.setOnClickListener(this);
         changeCameraSwitch.setOnClickListener(this);
         hangupBtn.setOnClickListener(this);
         scaleModeBtn.setOnClickListener(this);
         audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

         normalParam = new EMStreamParam();
         normalParam.setStreamType(EMConferenceStream.StreamType.NORMAL);
         normalParam.setVideoOff(true);
         normalParam.setAudioOff(false);

         micSwitch.setActivated(normalParam.isAudioOff());
         cameraSwitch.setActivated(normalParam.isVideoOff());
         speakerSwitch.setActivated(true);
         openSpeakerOn();

         timeHandler = new TimeHandler();

         localStream = EaseCallUIKit.getInstance().getLocalStream();

         //注册本地广播接听
         registerInviteBroadCast();

         //被邀请的话弹出邀请界面
         if(isInComingCall){

             audioManager = (AudioManager)this.getSystemService(Context.AUDIO_SERVICE);
             Uri ringUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
             audioManager.setMode(AudioManager.MODE_RINGTONE);
             audioManager.setSpeakerphoneOn(true);
             ringtone = RingtoneManager.getRingtone(this, ringUri);
             ringtone.play();

             incomingCallView.setInviteInfo(username);
             incomingCallView.setVisibility(View.VISIBLE);

         }else{
             incomingCallView.setVisibility(View.GONE);
             initLocalConferenceView();
         }

     }

    @Override
    public void onClick(View view){

        if(view.getId() == R.id.btn_mic_switch){
            voiceSwitch();
        }else if(view.getId() == R.id.btn_speaker_switch){
            if (speakerSwitch.isActivated()) {
                speakerSwitch.setActivated(false);
                speakerSwitch.setBackground(getResources().getDrawable(R.drawable.voice_off));
                closeSpeakerOn();
            }else{
                speakerSwitch.setActivated(true);
                speakerSwitch.setBackground(getResources().getDrawable(R.drawable.voice_on));
                openSpeakerOn();
            }
        }else if(view.getId() == R.id.btn_camera_switch){
            videoSwitch();
        }else if(view.getId() == R.id.btn_change_camera_switch){
            changeCamera();
        }else if(view.getId() == R.id.btn_hangup){
            exitConference();
        }else if(view.getId() == R.id.btn_invite){
            EaseCallUIKit.getInstance().onInviteeUser();
        }
    }

    /**
     * 语音开关
     */
    private void voiceSwitch() {
        if (normalParam.isAudioOff()) {
            normalParam.setAudioOff(false);
            micSwitch.setBackground(getResources().getDrawable(R.drawable.audio_unmute));
            EMClient.getInstance().conferenceManager().openVoiceTransfer();
        } else {
            normalParam.setAudioOff(true);
            micSwitch.setBackground(getResources().getDrawable(R.drawable.audio_mute));
            EMClient.getInstance().conferenceManager().closeVoiceTransfer();
        }
        micSwitch.setActivated(normalParam.isAudioOff());
        localView.setAudioOff(normalParam.isAudioOff());
    }

    /**
     * 视频开关
     */
    private void videoSwitch() {
        if (normalParam.isVideoOff()) {
            normalParam.setVideoOff(false);
            cameraSwitch.setBackground(getResources().getDrawable(R.drawable.video_on));
            EMClient.getInstance().conferenceManager().openVideoTransfer();
        } else {
            normalParam.setVideoOff(true);
            cameraSwitch.setBackground(getResources().getDrawable(R.drawable.video_0ff));
            EMClient.getInstance().conferenceManager().closeVideoTransfer();
        }
        cameraSwitch.setActivated(normalParam.isVideoOff());
        localView.setVideoOff(normalParam.isVideoOff());
    }

    /**
     * 切换摄像头
     */
    private void changeCamera() {
        EMClient.getInstance().conferenceManager().switchCamera();
    }


    /**
     * 初始化多人音视频画面管理控件
     */
    private void initLocalConferenceView() {
        localView = new EaseCallMemberView(activity);
        localView.setVideoOff(normalParam.isVideoOff());
        localView.setAudioOff(normalParam.isAudioOff());
        localView.setUsername(EMClient.getInstance().getCurrentUser());
        EMClient.getInstance().conferenceManager().setLocalSurfaceView(localView.getSurfaceView());

        callConferenceViewGroup.addView(localView);

        //开始发流
        publish();
    }




    /**
     * 开始推自己的数据
     */
    private void publish() {
        startAudioTalkingMonitor();
        timeHandler.startTime();

        EMClient.getInstance().conferenceManager().publish(normalParam, new EMValueCallBack<String>() {
            @Override
            public void onSuccess(String value) {
                        localView.setStreamId(value);
                        localStream.setStreamId(value);
                        EaseCallUIKit.getInstance().getConferenceStreams().get(0).setStreamId(value);
//                        //如果是被邀请 发流后删除会议属性
//                        if(isInComingCall){
//                            //删除会议属性
//                            String key = EaseMsgUtils.INVITEE_KEY + EMClient.getInstance().getCurrentUser();
//                            deleteConfenceAttribute(key,false);
//                        }

                        // Start to watch the phone call state.
                        EasePhoneStateManager.get(EaseMultipleVideoCallActivity.this).addStateCallback(phoneStateCallback);

                //设置会议属性

                inviteeUsers = EaseCallUIKit.getInstance().getInviteeUsers();
                if(inviteeUsers.size() > 0){
                    //设置会议属性
                    for(final String userName:inviteeUsers){
                        String key = EaseMsgUtils.INVITEE_KEY + userName;
                        setConferenceAttribute(key);
                    }

                    //发送邀请信息
                    handler.sendEmptyMessage(MSG_MAKE_CONFERENCE_VIDEO);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //开始sub流
                        List<EMConferenceStream> conferenceStreams = EaseCallUIKit.getInstance().getConferenceStreams();
                        if(conferenceStreams !=null && conferenceStreams.size()>1){
                            for(EMConferenceStream stream:conferenceStreams){
                                if(!stream.getStreamId().equals(localStream.getStreamId())){
                                    //开始订阅流
                                   addCallMemberView(stream);
                                }

                            }
                        }

//                       //增加view
//                        for(int i = 0 ; i < 10;i++){
//                            final CallMemberView memberView = new CallMemberView(activity);
//                            callConferenceViewGroup.addView(memberView);
//                        }
                    }
                });
            }

            @Override
            public void onError(int error, String errorMsg) {
                EMLog.e(TAG, "publish failed: error=" + error + ", msg=" + errorMsg);
                exitConference();
            }
        });
    }



    /**
     * 订阅对端stream
     */
    protected void subscribe(EMConferenceStream stream, EaseCallMemberView memberView) {
        EMClient.getInstance().conferenceManager().subscribe(stream,memberView.getSurfaceView(), new EMValueCallBack<String>() {
            @Override
            public void onSuccess(String value) {
                EMLog.d(TAG, "add conference successed memberName:" + stream.getMemberName());
            }

            @Override
            public void onError(int error, String errorMsg) {
                EMLog.d(TAG, "add conference failed error:" + error +  " errorMsg"  + errorMsg + "  memberName" + stream.getMemberName());
            }
        });
    }


    /**
     * 添加一个展示远端画面的 view
     */
    private void addCallMemberView(EMConferenceStream stream) {
        //检查有没有占位符
        String username = stream.getUsername();
        EMLog.e(TAG,"oncall66 addCallMemberView11 " + username);
        EaseCallMemberView placememberView = placesMemberMap.get(username);
        if(placememberView == null){ //没有占位符
            EMLog.d(TAG, "add conference view -start- " + stream.getMemberName());
            EMLog.e(TAG,"oncall66 addCallMemberView22 " + username);
            final EaseCallMemberView memberView = new EaseCallMemberView(activity);
            callConferenceViewGroup.addView(memberView);
            memberView.setUsername(stream.getUsername());
            memberView.setStreamId(stream.getStreamId());
            memberView.setAudioOff(stream.isAudioOff());
            memberView.setVideoOff(stream.isVideoOff());
            memberView.setDesktop(stream.getStreamType() == EMConferenceStream.StreamType.DESKTOP);
            subscribe(stream, memberView);
            EMLog.d(TAG, "add conference view -end-" + stream.getMemberName());
        }else{ //之前有占位符
            //停止计时
            placememberView.setStreamId(stream.getStreamId());
            placememberView.setAudioOff(stream.isAudioOff());
            placememberView.setVideoOff(stream.isVideoOff());
            placememberView.stopTimer();
            subscribe(stream, placememberView);
            placesMemberMap.remove(stream.getUsername());
        }
    }


    /**
     * 移除指定位置的 View，移除时如果已经订阅需要取消订阅
     */
    private void removeCallMemberView(EMConferenceStream stream) {
        int index = EaseCallUIKit.getInstance().getConferenceStreams().indexOf(stream);
        final EaseCallMemberView memberView = (EaseCallMemberView) callConferenceViewGroup.getChildAt(index);
        EaseCallUIKit.getInstance().getConferenceStreams().remove(stream);
        callConferenceViewGroup.removeView(memberView);
    }

    /**
     * 更新指定 View
     */
    private void updateCallMemberView(EMConferenceStream stream) {

        int position = EaseCallUIKit.getInstance().getConferenceStreams().indexOf(stream);
        EaseCallMemberView conferenceMemberView = (EaseCallMemberView) callConferenceViewGroup.getChildAt(position);
        conferenceMemberView.setAudioOff(stream.isAudioOff());
        conferenceMemberView.setVideoOff(stream.isVideoOff());
    }

    /**
     * 增加占位符
     */
    private void addplaceView(String userName) {
        EMLog.e(TAG,"oncall66 addplaceView11 " + userName + " size:" + placesMemberMap.size());
        EMConferenceStream userStream =EaseCallUIKit.getInstance().getUserStream(userName);
        EMLog.e(TAG,"oncall66 addplaceView11  userStream:" + userStream == null?"11":"22");
        if(!placesMemberMap.containsKey(userName) && userStream == null){

            //增加占位符的流
            EMConferenceStream stream = new EMConferenceStream();
            stream.setStreamId(userName);;
            stream.setUsername(userName);
            EaseCallUIKit.getInstance().getConferenceStreams().add(stream);

            EMLog.e(TAG,"oncall66 addplaceView22 " + userName);
            final EaseCallMemberView memberView = new EaseCallMemberView(activity);
            placesMemberMap.put(userName,memberView);
            callConferenceViewGroup.addView(memberView);
            memberView.setUsername(userName);
            memberView.setStreamId(userName);
            memberView.setAudioOff(true);
            memberView.setVideoOff(true);

            //开始计时
            memberView.startTimer();
        }
    }


    /**
     * 移除占位符
     */
    private void removeplaceView(String userName){
        final EaseCallMemberView memberView = placesMemberMap.get(userName);
        EMLog.e(TAG,"oncall66 removeplaceView " + userName);
        if(memberView != null){
            //停止计时
            memberView.stopTimer();
            //增加占位符的流
            EMConferenceStream stream = EaseCallUIKit.getInstance().getMuliteStream(userName);
            if(stream != null){
                removeCallMemberView(stream);
                placesMemberMap.remove(userName);
            }
        }
    }


    private EaseCommingCallView.OnActionListener onActionListener = new EaseCommingCallView.OnActionListener() {
        @Override
        public void onPickupClick(View v) {
            //停止震铃
            if(ringtone != null){
                ringtone.stop();
            }

            incomingCallView.setVisibility(View.GONE);

            //开始初始化
            initLocalConferenceView();
        }

        @Override
        public void onRejectClick(View v) {
            //停止震铃
            if(ringtone != null){
                ringtone.stop();
            }
            //拒绝删除会议属性 退出会议
            String key = EaseMsgUtils.INVITEE_KEY + EMClient.getInstance().getCurrentUser();
            deleteConfenceAttribute(key,true);
        }
    };


    private EaseCallMemberViewGroup.OnScreenModeChangeListener onScreenModeChangeListener = new EaseCallMemberViewGroup.OnScreenModeChangeListener() {
        @Override
        public void onScreenModeChange(boolean isFullScreenMode, @Nullable View fullScreenView) {
            if (isFullScreenMode) { // 全屏模式
            } else { // 非全屏模式
            }
        }
    };

    private EaseCallMemberViewGroup.OnItemClickListener onItemClickListener = new EaseCallMemberViewGroup.OnItemClickListener() {
        @Override
        public void onItemClick(View v, int position) {
        }
    };

    private EaseCallMemberViewGroup.OnPageStatusListener onPageStatusListener = new EaseCallMemberViewGroup.OnPageStatusListener() {
        @Override
        public void onPageCountChange(int count) {
            // 多于1页时显示indicator.
           // pageIndicator.setup(count > 1 ? count : 0);
        }

        @Override
        public void onPageScroll(int page) {
            //pageIndicator.setItemChecked(page);
        }
    };

    /**
     * 监听收到对端流
     */
    private void registerInviteBroadCast() {
        localReceiver = new EaseLocalBroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getStringExtra("action");
                String streamId = intent.getStringExtra("streamId");
                String userName = intent.getStringExtra("userName");
                EMLog.i(TAG, "Get action:" + action);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(action.equals("add-stream")){
                            final EMConferenceStream stream =  EaseCallUIKit.getInstance().getMuliteStream(streamId);
                            if(stream != null){
                                //开始订阅流
                                addCallMemberView(stream);
                            }
                        }else if(action.equals("remove-stream")){
                            final EMConferenceStream stream =  EaseCallUIKit.getInstance().getMuliteStream(streamId);
                            if(stream != null){
                                //移除流
                                removeCallMemberView(stream);
                            }
                        }else if(action.equals("updata-stream")){
                            final EMConferenceStream stream =  EaseCallUIKit.getInstance().getMuliteStream(streamId);
                            if(stream != null){
                                //更新流
                                updateCallMemberView(stream);
                            }
                        }else if(action.equals("all-member-remove")){
                            if(ringtone != null){
                                ringtone.stop();
                            }
                            //退出会议
                            unpublish(exit_state);
                        }else if(action.equals("add-attribute")){
                            if(userName != null && userName.length() != 0){
                                addplaceView(userName);
                            }
                        }else if(action.equals("delete-attribute")){

                            if(userName != null && userName.length() != 0){
                                //收到自己的会议属性删除
                                if(userName.equals(EMClient.getInstance().getCurrentUser())){
                                   //没有发流
                                   if(localStream.getStreamId().equals("local-stream")){
                                       if(ringtone != null){
                                           ringtone.stop();
                                       }
                                       //退出会议
                                       exitConference();
                                   }
                                }else{
                                    removeplaceView(userName);
                                }
                            }
                        }else if(action.equals("opposite-busy")){
                            if(!isInComingCall){
                                //提示对方正在忙碌
                                String info = getString(R.string.The_other_is_on_the_phone);
                                if(userName != null && userName.length() > 0){
                                    info = userName +getString(R.string.The_other_is_on_the_phone_whithUserName);
                                }
                                Toast.makeText(EaseCallUIKit.getInstance().getContext(), info, Toast.LENGTH_SHORT).show();
                            }
                        }else if(action.equals("invitee-member")){
                            inviteeUsers = EaseCallUIKit.getInstance().getInviteeUsers();
                            //设置会议属性
                            if(inviteeUsers.size() > 0){
                                handler.sendEmptyMessage(MSG_MAKE_CONFERENCE_VIDEO);
                                for(final String userName:inviteeUsers){
                                    String key = EaseMsgUtils.INVITEE_KEY + userName;
                                    setConferenceAttribute(key);
                                }
                            }
                        }
                    }
                });
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.OppositeStream.LOCAL_BROADCAST");
        localBroadcastManager.registerReceiver(localReceiver, intentFilter);
    }

    /**
     * 开启扬声器
     */
    protected void openSpeakerOn() {
        try {
            if (!audioManager.isSpeakerphoneOn())
                audioManager.setSpeakerphoneOn(true);
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭扬声器
     */
    protected void closeSpeakerOn() {
        try {
            if (audioManager != null) {
                if (audioManager.isSpeakerphoneOn())
                    audioManager.setSpeakerphoneOn(false);
                audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //更新会议时间
    private void updateConferenceTime(String time) {
        callTimeView.setText(time);
        //callTimeViewMain.setText(time);
    }


    private class TimeHandler extends Handler {
        private final int MSG_TIMER = 0;
        private DateFormat dateFormat = null;
        private int timePassed = 0;

        public TimeHandler() {
            dateFormat = new SimpleDateFormat("mm:ss");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

        public void startTime() {
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
                updateConferenceTime(time);
                sendEmptyMessageDelayed(MSG_TIMER, 1000);
                return;
            }
            super.handleMessage(msg);
        }
    }

    /**
     * 设置会议属性
     * @param key
     */
    private  void setConferenceAttribute(String key){
        String msgInfo = EMClient.getInstance().getCurrentUser() +" invitee you a videocall";
        EMClient.getInstance().conferenceManager().setConferenceAttribute(key, msgInfo, new EMValueCallBack<Void>() {
            @Override
            public void onSuccess(Void value) {
                EMLog.d(TAG,key + " setConferenceAttribute successed");
            }

            @Override
            public void onError(int error, String errorMsg) {
                EMLog.d(TAG,key + " setConferenceAttribute failed  error" + error + "  errorMsg:" + errorMsg);
            }
        });
    }


    // 当前设备通话状态监听器
    EasePhoneStateManager.PhoneStateCallback phoneStateCallback = new EasePhoneStateManager.PhoneStateCallback() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:   // 电话响铃
                    break;
                case TelephonyManager.CALL_STATE_IDLE:      // 电话挂断
                    // resume current voice conference.
                    if (normalParam.isAudioOff()) {
                        try {
                            EMClient.getInstance().callManager().resumeVoiceTransfer();
                        } catch (HyphenateException e) {
                            e.printStackTrace();
                        }
                    }
                    if (normalParam.isVideoOff()) {
                        try {
                            EMClient.getInstance().callManager().resumeVideoTransfer();
                        } catch (HyphenateException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:   // 来电接通 或者 去电，去电接通  但是没法区分
                    // pause current voice conference.
                    if (!normalParam.isAudioOff()) {
                        try {
                            EMClient.getInstance().callManager().pauseVoiceTransfer();
                        } catch (HyphenateException e) {
                            e.printStackTrace();
                        }
                    }
                    if (!normalParam.isVideoOff()) {
                        try {
                            EMClient.getInstance().callManager().pauseVideoTransfer();
                        } catch (HyphenateException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        }
    };

    @Override
    protected void onRestart() {
        super.onRestart();
        EMLog.d(TAG, "onRestart()");
    }

    @Override
    protected void onStart() {
        super.onStart();
        EMLog.d(TAG, "onStart()");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        inviteeUsers = EaseCallUIKit.getInstance().getInviteeUsers();
        if(inviteeUsers != null && inviteeUsers.size()> 0){
            //设置会议属性
            for(final String userName:inviteeUsers) {
                String key = EaseMsgUtils.INVITEE_KEY + userName;
                setConferenceAttribute(key);
            }
            handler.sendEmptyMessage(MSG_MAKE_CONFERENCE_VIDEO);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        EMLog.d(TAG, "onResume()");
    }

    @Override
    protected void onPause() {
        super.onPause();
        EMLog.d(TAG, "onPause()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        EMLog.d(TAG, "onStop()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseHandler();
        if (localReceiver != null) {
            localBroadcastManager.unregisterReceiver(localReceiver);
        }
        EaseCallUIKit.getInstance().initCallActivity();
        EMLog.d(TAG, "onDestroy()");
    }
}
