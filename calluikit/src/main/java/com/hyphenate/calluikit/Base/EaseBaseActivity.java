package com.hyphenate.calluikit.Base;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;

import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.hyphenate.EMCallBack;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.calluikit.EaseCallKitType;
import com.hyphenate.calluikit.Utils.EaseMsgUtils;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConference;
import com.hyphenate.chat.EMConferenceStream;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMStreamParam;
import com.hyphenate.util.EMLog;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 作为基础activity,放置一些公共的方法
 */
public abstract class EaseBaseActivity extends AppCompatActivity{
    /** 是否沉浸状态栏 **/
    private boolean isSetStatusBar = false;
    /** 是否允许全屏 **/
    private boolean mAllowFullScreen = true;
    /** 是否禁止旋转屏幕 **/
    private boolean isAllowScreenRoate = false;
    /** 当前Activity渲染的视图View **/
    protected View mContextView = null;
    /** 日志输出标志 **/
    protected final String TAG = this.getClass().getSimpleName();


    protected AudioManager audioManager;
    protected SoundPool soundPool;
    protected Ringtone ringtone;
    //用于判断fragment是否执行过onBackPressed()方法
    public boolean isBackPress;
    //判断是发起者还是被邀请
    protected boolean isInComingCall;
    protected String msgid;
    protected String username;
    protected String conferneceId;
    protected String confernecePwd;
    private boolean record =false;
    private boolean merge = false;
    protected EMConference conference;
    protected EMStreamParam normalParam;
    protected String localStreamId;
    protected static int exit_state = 0;
    protected static int destory_state =1;
    protected ArrayList<String> inviteeUsers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "BaseActivity-->onCreate()");
        Bundle bundle = getIntent().getExtras();
        initParms(bundle);
        View mView = bindView();
        if (null == mView) {
            mContextView = LayoutInflater.from(this)
                    .inflate(bindLayout(), null);
        } else
            mContextView = mView;
        if (mAllowFullScreen) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
        if (isSetStatusBar) {
            steepStatusBar();
        }
        setContentView(mContextView);
        if (!isAllowScreenRoate) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        initView(mContextView);

        audioManager = (AudioManager)this.getSystemService(Context.AUDIO_SERVICE);
    }



    /**
     * [沉浸状态栏]
     */
    private void steepStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // 透明状态栏
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // 透明导航栏
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

    /**
     * [初始化参数]
     *
     * @param parms
     */
    public abstract void initParms(Bundle parms);

    /**
     * [绑定视图]
     *
     * @return
     */
    public abstract View bindView();

    /**
     * [绑定布局]
     *
     * @return
     */
    public abstract int bindLayout();

    /**
     * [初始化控件]
     *
     * @param view
     */
    public abstract void initView(final View view);

    /**
     * [绑定控件]
     *
     * @param resId
     *
     * @return
     */
    protected    <T extends View> T $(int resId) {
        return (T) super.findViewById(resId);
    }



    /**
     * [页面跳转]
     *
     * @param clz
     */
    public void startActivity(Class<?> clz) {
        startActivity(new Intent(EaseBaseActivity.this,clz));
    }

    /**
     * [携带数据的页面跳转]
     *
     * @param clz
     * @param bundle
     */
    public void startActivity(Class<?> clz, Bundle bundle) {
        Intent intent = new Intent();
        intent.setClass(this, clz);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
    }

    /**
     * [含有Bundle通过Class打开编辑界面]
     *
     * @param cls
     * @param bundle
     * @param requestCode
     */
    public void startActivityForResult(Class<?> cls, Bundle bundle,
                                       int requestCode) {
        Intent intent = new Intent();
        intent.setClass(this, cls);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart()");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
    }

    /**
     * [简化Toast]
     * @param msg
     */
    protected void showToast(String msg){
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }

    /**
     * [是否允许全屏]
     *
     * @param allowFullScreen
     */
    public void setAllowFullScreen(boolean allowFullScreen) {
        this.mAllowFullScreen = allowFullScreen;
    }

    /**
     * [是否设置沉浸状态栏]
     *
     * @param isSetStatusBar
     */
    public void setSteepStatusBar(boolean isSetStatusBar) {
        this.isSetStatusBar = isSetStatusBar;
    }


    /**
     * [是否允许屏幕旋转]
     *
     * @param isAllowScreenRoate
     */
    public void setScreenRoate(boolean isAllowScreenRoate) {
        this.isAllowScreenRoate = isAllowScreenRoate;
    }

    /**
     * 处理异步消息
     */
    HandlerThread callHandlerThread = new HandlerThread("callHandlerThread");
    { callHandlerThread.start(); }
    protected Handler handler = new Handler(callHandlerThread.getLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 100: // 1V1语音通话
                    sendInviteeMsg(username, EaseCallKitType.SIGNAL_VOICE_CALL);
                    break;
                case 101: // 1V1视频通话
                    sendInviteeMsg(username, EaseCallKitType.SIGNAL_VIDEO_CALL);
                    break;
                case 102: // 多人音视频通话
                    if(inviteeUsers != null && inviteeUsers.size() > 0){
                        for(final String username:inviteeUsers){
                            sendInviteeMsg(username, EaseCallKitType.CONFERENCE_CALL);
                        }
                        inviteeUsers.clear();
                    }
                    break;
                case 301: //停止事件循环线程
                    callHandlerThread.quit();
                    break;
                default:
                    break;
            }
        }
    };


    /**
     * 发送通话邀请信息
     * @param username
     * @param callType
     */
    private void sendInviteeMsg(String username, EaseCallKitType callType){
        final EMMessage message = EMMessage.createTxtSendMessage( "邀请您进行通话", username);
        message.setAttribute(EaseMsgUtils.CONFRID_KEY,conferneceId);
        message.setAttribute(EaseMsgUtils.CONFRIDPWD_KEY,confernecePwd);
        message.setAttribute(EaseMsgUtils.CALLTYPE_KEY, callType.code);
        final EMConversation conversation = EMClient.getInstance().chatManager().getConversation(username, EMConversation.EMConversationType.Chat, true);
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




    /**
     * 取消订阅对端stream
     */
    protected void unsubscribe(EMConferenceStream stream) {
        EMClient.getInstance().conferenceManager().unsubscribe(stream, new EMValueCallBack<String>() {
            @Override
            public void onSuccess(String value) {
                EMLog.e(TAG, "subscribe Successed streamId:"+ stream.getStreamId());

                //结束通话
                exitConference();
            }

            @Override
            public void onError(int error, String errorMsg) {
                EMLog.e(TAG, "subscribe failed: error=" + error + ", msg=" + errorMsg + "  streamId:" + stream.getStreamId());
            }
        });
    }

    /**
     * 删除会议属性
     */
    public void deleteConfenceAttribute(String key,boolean exit){
        EMClient.getInstance().conferenceManager().deleteConferenceAttribute(key, new EMValueCallBack<Void>() {
            @Override
            public void onSuccess(Void value) {
                EMLog.d(TAG," deleteConferenceAttribute successed key:" + key);
                if(exit){
                    exitConference();
                }
            }

            @Override
            public void onError(int error, String errorMsg) {
                EMLog.d(TAG, " deleteConferenceAttribute failed key:" + key);
                if(exit){
                    exitConference();
                }
            }
        });
    }


    /**
     * unpub本地流
     */
    public void unpublish(int state){
        if(localStreamId != null){
            EMClient.getInstance().conferenceManager().unpublish(localStreamId, new EMValueCallBack<String>() {
                @Override
                public void onSuccess(String value) {
                    EMLog.d(TAG, "unpublish local stream success " + value);
                    if(state == exit_state){
                        exitConference();
                    }else{
                        destoryConference();
                    }
                }

                @Override
                public void onError(int error, String errorMsg) {
                    EMLog.e(TAG, "unpublish local stream failed " + error + ", " + errorMsg);
                    if(state == exit_state){
                        exitConference();
                    }else{
                        destoryConference();
                    }
                }
            });
        }else{
            EMLog.e(TAG, "unPublish local stream is null");
            if(state == exit_state){
                exitConference();
            }else{
                destoryConference();
            }
        }
    }

    /**
     * 销毁会议
     */
    public void destoryConference() {
        stopAudioTalkingMonitor();
        EMClient.getInstance().conferenceManager().destroyConference(new EMValueCallBack() {
            @Override
            public void onSuccess(Object value) {
                EMLog.d(TAG, "destroy conference success " + value);
                finish();
            }
            @Override
            public void onError(int error, String errorMsg) {
                EMLog.e(TAG, "destroy conference failed " + error + ", " + errorMsg);
                finish();
            }
        });
    }


    /**
     * 退出会议
     */
    public void exitConference() {
        stopAudioTalkingMonitor();
        EMClient.getInstance().conferenceManager().exitConference(new EMValueCallBack() {
            @Override
            public void onSuccess(Object value) {
                EMLog.e(TAG, "exit conference success " + value);
                finish();
            }
            @Override
            public void onError(int error, String errorMsg) {
                EMLog.e(TAG, "exit conference failed " + error + ", " + errorMsg);
                finish();
            }
        });
    }


    protected void startAudioTalkingMonitor() {
        EMClient.getInstance().conferenceManager().startMonitorSpeaker(300);
    }

    private void stopAudioTalkingMonitor() {
        EMClient.getInstance().conferenceManager().stopMonitorSpeaker();
    }

    public boolean isInComingCall() {
        return isInComingCall;
    }

    public String getLocalStreamId() {
        return localStreamId;
    }


    /**
     * 停止事件循环
     */
    protected  void releaseHandler() {
        handler.sendEmptyMessage(EaseMsgUtils.MSG_RELEASE_HANDLER);
    }
}