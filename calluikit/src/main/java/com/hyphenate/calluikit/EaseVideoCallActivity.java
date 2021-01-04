package com.hyphenate.calluikit;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.EMValueCallBack;
import com.hyphenate.calluikit.Base.EaseBaseActivity;
import com.hyphenate.calluikit.Utils.EaseLocalBroadcastReceiver;
import com.hyphenate.calluikit.Utils.EaseMsgUtils;
import com.hyphenate.calluikit.widget.EaseImageView;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConferenceStream;
import com.hyphenate.chat.EMStreamParam;
import com.hyphenate.media.EMCallSurfaceView;
import com.hyphenate.util.EMLog;
import com.superrtc.sdk.VideoView;

import androidx.constraintlayout.widget.Group;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


/**
 * Created by lijian on 2020.12.01.
 */

public class EaseVideoCallActivity extends EaseBaseActivity implements  View.OnClickListener {

    private Group comingBtnContainer;
    private ImageButton refuseBtn;
    private ImageButton answerBtn;
    private ImageButton hangupBtn;
    private Group voiceContronlLayout;

    private Group groupHangUp;
    private Group groupUseInfo;
    private Group groupOngoingSettings;
    private TextView nickTextView;
    private EMConferenceStream oppositeStream;
    private boolean isMuteState = false;
    private boolean isHandsfreeState;
    private ImageView muteImage;
    private ImageView handsFreeImage;
    private ImageButton switchCameraBtn;
    private Chronometer chronometer;
    private boolean surfaceStateChange = false;
    private EaseImageView avatarView;
    private TextView call_stateView;

    private Group videoCallingGroup;
    private Group voiceCallingGroup;
    private TextView tv_nick_voice;

    private Group videoCalledGroup;
    private Group voiceCalledGroup;

    private RelativeLayout video_transe_layout;
    private RelativeLayout video_transe_comming_layout;
    private ImageButton btn_voice_trans;
    private TextView tv_call_state_voice;



    // 视频通话画面显示控件，这里在新版中使用同一类型的控件，方便本地和远端视图切换
    protected RelativeLayout localSurface_layout;
    protected RelativeLayout oppositeSurface_layout;
    private EMCallSurfaceView localView;
    public EMCallSurfaceView oppositeView;
    private EaseCallKitType callType;
    private View Voice_View;
    EaseLocalBroadcastReceiver localReceiver;
    LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(EaseCallUIKit.getInstance().getContext());

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
            callType = EaseCallUIKit.getInstance().getCallType();
        }
    }


    public View bindView(){
        return null;
    }


    public int bindLayout(){
        return R.layout.activity_video_call;
    }


    public void initView(final View view){

        refuseBtn = findViewById(R.id.btn_refuse_call);
        answerBtn = findViewById(R.id.btn_answer_call);
        hangupBtn = findViewById(R.id.btn_hangup_call);
        voiceContronlLayout = findViewById(R.id.ll_voice_control);
        comingBtnContainer = findViewById(R.id.ll_coming_call);
        avatarView = findViewById(R.id.iv_avatar);

        muteImage = (ImageView) findViewById(R.id.iv_mute);
        handsFreeImage = (ImageView) findViewById(R.id.iv_handsfree);
        switchCameraBtn = (ImageButton) findViewById(R.id.btn_switch_camera);


        //呼叫中页面
        videoCallingGroup = findViewById(R.id.ll_video_calling);
        voiceCallingGroup = findViewById(R.id.ll_voice_calling);

        video_transe_layout = findViewById(R.id.bnt_video_transe);
        video_transe_comming_layout = findViewById(R.id.bnt_video_transe_comming);
        tv_nick_voice = findViewById(R.id.tv_nick_voice);
        tv_call_state_voice = findViewById(R.id.tv_call_state_voice);

        if(callType == EaseCallKitType.SIGNAL_VIDEO_CALL){
            videoCallingGroup.setVisibility(View.VISIBLE);
            voiceCallingGroup.setVisibility(View.GONE);
            if(isInComingCall){
                video_transe_layout.setVisibility(View.GONE);
                video_transe_comming_layout.setVisibility(View.VISIBLE);
            }else{
                video_transe_layout.setVisibility(View.VISIBLE);
                video_transe_comming_layout.setVisibility(View.GONE);
            }
        }else{
            if(!isInComingCall){
                voiceContronlLayout.setVisibility(View.VISIBLE);
            }
            videoCallingGroup.setVisibility(View.GONE);
            video_transe_layout.setVisibility(View.GONE);
            video_transe_comming_layout.setVisibility(View.GONE);
            voiceCallingGroup.setVisibility(View.VISIBLE);
            tv_nick_voice.setText(username);
        }

        video_transe_layout.setOnClickListener(this);
        video_transe_comming_layout.setOnClickListener(this);


        //通话中页面
        videoCalledGroup = findViewById(R.id.ll_video_called);
        voiceCalledGroup =findViewById(R.id.ll_voice_control);

        btn_voice_trans = findViewById(R.id.btn_voice_trans);
        btn_voice_trans.setOnClickListener(this);


        refuseBtn.setOnClickListener(this);
        answerBtn.setOnClickListener(this);
        hangupBtn.setOnClickListener(this);

        muteImage.setOnClickListener(this);
        handsFreeImage.setOnClickListener(this);
        switchCameraBtn.setOnClickListener(this);

        // local surfaceview
        localSurface_layout = (RelativeLayout) findViewById(R.id.local_surface_layout);
        // remote surfaceview
        oppositeSurface_layout = (RelativeLayout) findViewById(R.id.opposite_surface_layout);
        groupHangUp = findViewById(R.id.group_hang_up);
        groupUseInfo = findViewById(R.id.group_use_info);
        groupOngoingSettings = findViewById(R.id.group_ongoing_settings);
        nickTextView = (TextView) findViewById(R.id.tv_nick);
        chronometer = (Chronometer) findViewById(R.id.chronometer);
        call_stateView = (TextView)findViewById(R.id.tv_call_state) ;

        nickTextView.setText(username);
        localSurface_layout.setOnClickListener(this);


        //增加surfaceview
        localView = new EMCallSurfaceView(this);
        localView.setScaleMode(VideoView.EMCallViewScaleMode.EMCallViewScaleModeAspectFill);
        localView.setZOrderOnTop(true);
        localView.setZOrderMediaOverlay(true);
        RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params1.addRule(RelativeLayout.CENTER_IN_PARENT);
        localSurface_layout.addView(localView, params1);


        oppositeView = new EMCallSurfaceView(this);
        oppositeView.setScaleMode(VideoView.EMCallViewScaleMode.EMCallViewScaleModeAspectFill);
        RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params2.addRule(RelativeLayout.CENTER_IN_PARENT);
        oppositeSurface_layout.addView(oppositeView, params2);

        Voice_View = findViewById(R.id.view_ring);

        if(isInComingCall){
            call_stateView.setText("邀请你进行音视频通话");
            tv_call_state_voice.setText("邀请你进行音视频通话");
        }else{
            call_stateView.setText("正在等待对方接受邀请");
            tv_call_state_voice.setText("正在等待对方接受邀请");
        }


        //如果是语音通话
        if(callType == EaseCallKitType.SIGNAL_VOICE_CALL){
            view.setBackground(getResources().getDrawable(R.drawable.call_bg_voice));
            //sufaceview不可见
            localSurface_layout.setVisibility(View.GONE);
            oppositeSurface_layout.setVisibility(View.GONE);

            //语音通话UI可见
            Voice_View.setVisibility(View.VISIBLE);
            avatarView.setVisibility(View.VISIBLE);
        }else{
            avatarView.setVisibility(View.GONE);
        }

        registerInviteBroadCast();
        if(!isInComingCall){
            //拨打电话状态
            makeCallStatus();
            publish();
        }else{
            //被呼叫状态
            makeComingStatus();

            //开始振铃
            audioManager = (AudioManager)this.getSystemService(Context.AUDIO_SERVICE);
            Uri ringUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            audioManager.setMode(AudioManager.MODE_RINGTONE);
            audioManager.setSpeakerphoneOn(true);
            ringtone = RingtoneManager.getRingtone(this, ringUri);
            ringtone.play();
        }
    }


    /**
     * 来电话的状态
     */
    private void makeComingStatus() {
        voiceContronlLayout.setVisibility(View.INVISIBLE);
        comingBtnContainer.setVisibility(View.VISIBLE);
        groupUseInfo.setVisibility(View.VISIBLE);
        if(callType == EaseCallKitType.SIGNAL_VIDEO_CALL){
            groupOngoingSettings.setVisibility(View.INVISIBLE);
            localSurface_layout.setVisibility(View.INVISIBLE);
        }else{
            avatarView.setVisibility(View.VISIBLE);
            nickTextView.setVisibility(View.VISIBLE);
        }
        groupHangUp.setVisibility(View.INVISIBLE);
        groupRequestLayout();
    }


    /**
     * 通话中的状态
     */
    private void makeOngoingStatus() {
        voiceContronlLayout.setVisibility(View.VISIBLE);
        comingBtnContainer.setVisibility(View.INVISIBLE);
        groupUseInfo.setVisibility(View.INVISIBLE);
        groupHangUp.setVisibility(View.VISIBLE);

        if(callType == EaseCallKitType.SIGNAL_VIDEO_CALL){
            groupOngoingSettings.setVisibility(View.VISIBLE);
            localSurface_layout.setVisibility(View.VISIBLE);
            videoCalledGroup.setVisibility(View.VISIBLE);
            voiceCalledGroup.setVisibility(View.INVISIBLE);
            hangupBtn.setVisibility(View.VISIBLE);
            videoCallingGroup.setVisibility(View.GONE);
            voiceCallingGroup.setVisibility(View.GONE);
        }else{
            avatarView.setVisibility(View.VISIBLE);
            nickTextView.setVisibility(View.VISIBLE);
            videoCalledGroup.setVisibility(View.INVISIBLE);
            voiceCalledGroup.setVisibility(View.VISIBLE);
            hangupBtn.setVisibility(View.VISIBLE);

            videoCallingGroup.setVisibility(View.GONE);
            voiceCallingGroup.setVisibility(View.VISIBLE);
            tv_nick_voice.setText(username);
            tv_call_state_voice.setText("通话中");
        }

        video_transe_layout.setVisibility(View.GONE);
        video_transe_comming_layout.setVisibility(View.GONE);
        groupRequestLayout();
    }

    /**
     * 拨打电话的状态
     */
    public void makeCallStatus() {
        if(!isInComingCall && callType == EaseCallKitType.SIGNAL_VOICE_CALL){
            voiceContronlLayout.setVisibility(View.VISIBLE);
        }else{
            voiceContronlLayout.setVisibility(View.INVISIBLE);
        }
        comingBtnContainer.setVisibility(View.INVISIBLE);
        groupUseInfo.setVisibility(View.VISIBLE);
        groupOngoingSettings.setVisibility(View.INVISIBLE);
        localSurface_layout.setVisibility(View.INVISIBLE);
        groupHangUp.setVisibility(View.VISIBLE);
        groupRequestLayout();
    }

    public void groupRequestLayout() {
        comingBtnContainer.requestLayout();
        //voiceContronlLayout.requestLayout();
        groupHangUp.requestLayout();
        groupUseInfo.requestLayout();
        groupOngoingSettings.requestLayout();
    }

    private void changeSurface(){
        EMClient.getInstance().conferenceManager().setLocalSurfaceView(null);
        EMClient.getInstance().conferenceManager().updateRemoteSurfaceView(oppositeStream.getStreamId(),null);
        localView.getRenderer().dispose();
        oppositeView.getRenderer().dispose();
        localSurface_layout.removeAllViews();
        oppositeSurface_layout.removeAllViews();

        localView = new EMCallSurfaceView(this);
        localView.setScaleMode(VideoView.EMCallViewScaleMode.EMCallViewScaleModeAspectFill);
        localView.setZOrderOnTop(true);
        localView.setZOrderMediaOverlay(true);
        RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params1.addRule(RelativeLayout.CENTER_IN_PARENT);
        localSurface_layout.addView(localView, params1);

        oppositeView = new EMCallSurfaceView(this);
        oppositeView.setScaleMode(VideoView.EMCallViewScaleMode.EMCallViewScaleModeAspectFill);
        RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params2.addRule(RelativeLayout.CENTER_IN_PARENT);
        oppositeSurface_layout.addView(oppositeView, params2);

        if(!surfaceStateChange){
            EMClient.getInstance().conferenceManager().updateRemoteSurfaceView(oppositeStream.getStreamId(),localView);
            EMClient.getInstance().conferenceManager().setLocalSurfaceView(oppositeView);
        }else{
            EMClient.getInstance().conferenceManager().updateRemoteSurfaceView(oppositeStream.getStreamId(),oppositeView);
            EMClient.getInstance().conferenceManager().setLocalSurfaceView(localView);
        }
        surfaceStateChange = !surfaceStateChange;
    }




    @Override
    public void onClick(View view){

        int id = view.getId();
        if(id == R.id.btn_refuse_call) {
            if(ringtone != null){
                ringtone.stop();
            }
            if(isInComingCall){
                chronometer.stop();

                //接通通话删除会议属性
                String key = EaseMsgUtils.INVITEE_KEY +
                            EMClient.getInstance().getCurrentUser();

                deleteConfenceAttribute(key,true);
            }
        } else if (id == R.id.btn_answer_call) {
            if(isInComingCall){
              publish();
            }
        } else if (id == R.id.btn_hangup_call) {
            chronometer.stop();
            if(!isInComingCall){
                unpublish(destory_state);
            }else{
                //拒绝删除会议属性
                if (isInComingCall) {
                    //接通通话删除会议属性
                    String key = EaseMsgUtils.INVITEE_KEY +
                            EMClient.getInstance().getCurrentUser();

                    deleteConfenceAttribute(key,true);
                }
            }

        } else if(id == R.id.local_surface_layout){
            changeSurface();
        } else if (id == R.id.iv_mute) { // mute
            if (isMuteState) {
                // resume voice transfer
                muteImage.setImageResource(R.drawable.call_mute_normal);
                EMClient.getInstance().conferenceManager().openVoiceTransfer();
                isMuteState = false;
            } else {
                // pause voice transfer
                muteImage.setImageResource(R.drawable.call_mute_on);
                EMClient.getInstance().conferenceManager().closeVoiceTransfer();
                isMuteState = true;
            }
        } else if (id == R.id.iv_handsfree) { // handsfree
            if (isHandsfreeState) {
                handsFreeImage.setImageResource(R.drawable.em_icon_speaker_normal);
                closeSpeakerOn();
                isHandsfreeState = false;
            } else {
                handsFreeImage.setImageResource(R.drawable.em_icon_speaker_on);
                openSpeakerOn();
                isHandsfreeState = true;
            }
        }else if(id == R.id.btn_switch_camera){ //通话进行中转音频
            EMClient.getInstance().conferenceManager().switchCamera();
        }else if(id == R.id.btn_voice_trans){
           if(callType == EaseCallKitType.SIGNAL_VOICE_CALL){
               callType = EaseCallKitType.SIGNAL_VIDEO_CALL;
               EaseCallUIKit.getInstance().setCallType(EaseCallKitType.SIGNAL_VIDEO_CALL);
               changeVideoVoiceState();
               EMClient.getInstance().conferenceManager().openVideoTransfer();
           }else{
               callType = EaseCallKitType.SIGNAL_VOICE_CALL;
               EaseCallUIKit.getInstance().setCallType(EaseCallKitType.SIGNAL_VOICE_CALL);
               changeVideoVoiceState();
               EMClient.getInstance().conferenceManager().closeVideoTransfer();
           }
        }else if(id == R.id.bnt_video_transe_comming || id == R.id.bnt_video_transe){  //进入通话之前转音频

            callType = EaseCallKitType.SIGNAL_VOICE_CALL;
            EaseCallUIKit.getInstance().setCallType(EaseCallKitType.SIGNAL_VOICE_CALL);

            EMClient.getInstance().conferenceManager().closeVideoTransfer();

            localSurface_layout.setVisibility(View.GONE);
            oppositeSurface_layout.setVisibility(View.GONE);

            mContextView.setBackground(getResources().getDrawable(R.drawable.call_bg_voice));

            videoCallingGroup.setVisibility(View.GONE);
            video_transe_layout.setVisibility(View.GONE);
            video_transe_comming_layout.setVisibility(View.GONE);
            voiceCallingGroup.setVisibility(View.VISIBLE);
            tv_nick_voice.setText(username);
            if(!isInComingCall){
                voiceContronlLayout.setVisibility(View.VISIBLE);
            }

            if(isInComingCall){
                publish();
            }
        }
    }


    void changeVideoVoiceState(){
        if(callType == EaseCallKitType.SIGNAL_VIDEO_CALL){//切换到视频通话UI
            //语音通话UI可见
            Voice_View.setVisibility(View.GONE);
            avatarView.setVisibility(View.GONE);

            //sufaceview不可见
            localSurface_layout.setVisibility(View.VISIBLE);
            oppositeSurface_layout.setVisibility(View.VISIBLE);

            makeOngoingStatus();
        }else{ // 切换到音频通话UI
            localSurface_layout.setVisibility(View.GONE);
            oppositeSurface_layout.setVisibility(View.GONE);

            mContextView.setBackground(getResources().getDrawable(R.drawable.call_bg_voice));

            //已经在通话中
            if(videoCalledGroup.getVisibility() == View.VISIBLE){
                //语音通话UI可见
                Voice_View.setVisibility(View.VISIBLE);
                avatarView.setVisibility(View.VISIBLE);
                makeOngoingStatus();

            }else{
                localSurface_layout.setVisibility(View.GONE);
                oppositeSurface_layout.setVisibility(View.GONE);

                mContextView.setBackground(getResources().getDrawable(R.drawable.call_bg_voice));

                if(isInComingCall){
                    tv_call_state_voice.setText("邀请你进行音视频通话");
                }else{
                    tv_call_state_voice.setText("正在等待对方接受邀请");
                    if(!isInComingCall){
                        voiceContronlLayout.setVisibility(View.VISIBLE);
                    }
                }

                videoCallingGroup.setVisibility(View.GONE);
                video_transe_layout.setVisibility(View.GONE);
                video_transe_comming_layout.setVisibility(View.GONE);
                voiceCallingGroup.setVisibility(View.VISIBLE);
                tv_nick_voice.setText(username);
            }
        }
    }


    //通话中订阅对端的流
    public void subOppositeStream() {
        if (EaseCallUIKit.getInstance().getOppositeStream() != null) {
            oppositeStream = EaseCallUIKit.getInstance().getOppositeStream();
            if(!isInComingCall){
                //被叫中没有视频流 ，转音频UI
                if(oppositeStream.isVideoOff()){
                    callType = EaseCallKitType.SIGNAL_VOICE_CALL;
                    EaseCallUIKit.getInstance().setCallType(EaseCallKitType.SIGNAL_VOICE_CALL);
                    changeVideoVoiceState();
                    EMClient.getInstance().conferenceManager().closeVideoTransfer();
                }
            }
            subscribe(oppositeStream, oppositeView, new EMValueCallBack<String>() {
                @Override
                public void onSuccess(String value) {
                    EMLog.e(TAG, "subscribe Successed " + oppositeStream.getStreamId());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            //停止响铃
                            if (isInComingCall) {
                                if (ringtone != null)
                                    ringtone.stop();
                            }
                            //改为通话中状态
                            makeOngoingStatus();
                            EMClient.getInstance().conferenceManager().setLocalSurfaceView(localView);

                            //订阅对端成功开始计时
                            startAudioTalkingMonitor();

                            chronometer.setVisibility(View.VISIBLE);
                            chronometer.setBase(SystemClock.elapsedRealtime());
                            // call durations start
                            chronometer.start();

                            handsFreeImage.setImageResource(R.drawable.em_icon_speaker_on);
                            isHandsfreeState = true;
                            openSpeakerOn();
                            if (isInComingCall) {
                                //接通通话删除会议属性
                                String key = EaseMsgUtils.INVITEE_KEY +
                                        EMClient.getInstance().getCurrentUser();

                                deleteConfenceAttribute(key, false);
                            }
                        }
                    });
                }

                @Override
                public void onError(int error, String errorMsg) {
                    EMLog.e(TAG, "subscribe failed: error=" + error + ", msg=" + errorMsg + " " + oppositeStream.getStreamId());

                    //订阅失败销毁通话页面
                    finish();
                }
            });
        }
    }

    /**
     * 开始推自己的数据
     */

    protected void publish() {
        normalParam = new EMStreamParam();
        normalParam.setStreamType(EMConferenceStream.StreamType.NORMAL);
        //判断是video or voice
        if(callType == EaseCallKitType.SIGNAL_VIDEO_CALL){
            normalParam.setVideoOff(false);
        }else {
            normalParam.setVideoOff(true);
        }
        normalParam.setAudioOff(false);
        EMClient.getInstance().conferenceManager().publish(normalParam, new EMValueCallBack<String>() {
            @Override
            public void onSuccess(String value) {
                EMLog.e(TAG, "publish success: onSuccess");
                localStreamId = value;
                if(!isInComingCall){
                    //打开扬声器
                    openSpeakerOn();

                    //发送邀请信息
                    if(callType == EaseCallKitType.SIGNAL_VIDEO_CALL){
                        handler.sendEmptyMessage(EaseMsgUtils.MSG_MAKE_SIGNAL_VIDEO);
                    }else{
                        handler.sendEmptyMessage(EaseMsgUtils.MSG_MAKE_SIGNAL_VOICE);
                    }

                    //设置会议属性
                    String key = EaseMsgUtils.INVITEE_KEY + username;
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
                }else{
                    openSpeakerOn();
                    //发流成功订阅对端的流
                    subOppositeStream();
                }

            }

            @Override
            public void onError(int error, String errorMsg) {
                EMLog.e(TAG, "publish failed: error=" + error + ", msg=" + errorMsg);
                if(isInComingCall){
                    unpublish(exit_state);
                }else{
                    unpublish(destory_state);
                }

            }
        });
    }

    /**
     * 监听收到对端流
     */
    private void registerInviteBroadCast() {
        localReceiver = new EaseLocalBroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getStringExtra("action");
                EMLog.i(TAG, "Get action:" + action);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                       if(action.equals("add-stream")){
                           if(!isInComingCall){
                               subOppositeStream();
                           }
                       }else if(action.equals("all-member-remove")){
                           if(!isInComingCall){
                              destoryConference();
                           }else{
                               if(ringtone != null){
                                   ringtone.stop();
                               }
                               unpublish(exit_state);
                           }
                       }else if(action.equals("delete-attribute")){
                           if(isInComingCall){
                               if(localStreamId == null){
                                   //未发流退出会议
                                   if(ringtone != null){
                                       ringtone.stop();
                                   }
                                   exitConference();
                               }
                           }
                       }else if(action.equals("opposite-busy")){
                           if(!isInComingCall){
                               //提示对方正在忙碌
                               String info = getString(R.string.The_other_is_on_the_phone);
                               Toast.makeText(EaseCallUIKit.getInstance().getContext(), info, Toast.LENGTH_SHORT).show();
                               //销毁会议
                               unpublish(destory_state);
                           }

                       }else if(action.equals("video-voice-change")){
                           boolean isToVideo = intent.getBooleanExtra("isToVideo",false);
                           if(isToVideo){
                               callType = EaseCallKitType.SIGNAL_VIDEO_CALL;
                               EMClient.getInstance().conferenceManager().openVideoTransfer();
                               changeVideoVoiceState();

                           }else{
                               callType = EaseCallKitType.SIGNAL_VOICE_CALL;
                               EMClient.getInstance().conferenceManager().closeVideoTransfer();
                               changeVideoVoiceState();

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
     * 订阅对端stream
     */
    public void subscribe(EMConferenceStream stream,EMCallSurfaceView surfaceView,EMValueCallBack<String> callBack) {
        EMClient.getInstance().conferenceManager().subscribe(stream,surfaceView, new EMValueCallBack<String>() {
            @Override
            public void onSuccess(String value) {
                callBack.onSuccess(value);
            }

            @Override
            public void onError(int error, String errorMsg) {
                callBack.onError(error,errorMsg);
            }
        });
    }

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