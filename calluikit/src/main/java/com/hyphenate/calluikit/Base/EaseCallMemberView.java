package com.hyphenate.calluikit.Base;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.calluikit.EaseCallUIKit;
import com.hyphenate.calluikit.R;

import com.hyphenate.calluikit.Utils.EaseMsgUtils;
import com.hyphenate.media.EMCallSurfaceView;
import com.superrtc.sdk.VideoView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;


/**
 * Created by lijian on 2020/12/09.
 */
public class EaseCallMemberView extends RelativeLayout {

    private Context context;

    private EMCallSurfaceView surfaceView;
    private ImageView avatarView;
    private ImageView audioOffView;
    private ImageView talkingView;
    private TextView nameView;

    private boolean isVideoOff = true;
    private boolean isAudioOff = false;
    private boolean isDesktop = false;
    private boolean isFullScreenMode = false;
    private String streamId;
    private TimeHandler timehandler;


    public EaseCallMemberView(Context context) {
        this(context, null);
    }

    public EaseCallMemberView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EaseCallMemberView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.avtivity_call_member, this);
        init();
    }

    private void init() {
        surfaceView = (EMCallSurfaceView) findViewById(R.id.item_surface_view);
        avatarView = (ImageView) findViewById(R.id.img_call_avatar);
        audioOffView = (ImageView) findViewById(R.id.icon_mute);
        talkingView = (ImageView) findViewById(R.id.icon_talking);
        nameView = (TextView) findViewById(R.id.text_name);

        surfaceView.setScaleMode(VideoView.EMCallViewScaleMode.EMCallViewScaleModeAspectFill);

        timehandler = new TimeHandler();
    }

    public EMCallSurfaceView getSurfaceView() {
        return surfaceView;
    }

    /**
     * 更新静音状态
     */
    public void setAudioOff(boolean state) {
        if (isDesktop) {
            return;
        }
        isAudioOff = state;

        if (isFullScreenMode) {
            return;
        }

        if (isAudioOff) {
            audioOffView.setVisibility(View.VISIBLE);

        } else {
            audioOffView.setVisibility(View.GONE);
        }
    }

    public boolean isAudioOff() {
        return isAudioOff;
    }

    /**
     * 更新视频显示状态
     */
    public void setVideoOff(boolean state) {
        isVideoOff = state;
        if (isVideoOff) {
            avatarView.setVisibility(View.VISIBLE);
            surfaceView.setVisibility(GONE);
        } else {
            avatarView.setVisibility(View.GONE);
            surfaceView.setVisibility(VISIBLE);
        }
    }

    public boolean isVideoOff() {
        return isVideoOff;
    }

    public void setDesktop(boolean desktop) {
        isDesktop = desktop;
        if (isDesktop) {
            avatarView.setVisibility(View.GONE);
        }
    }

    /**
     * 更新说话状态
     */
    public void setTalking(boolean talking) {
        if (isDesktop) {
            return;
        }

        if (isFullScreenMode) {
            return;
        }

        if (talking) {
            talkingView.setVisibility(VISIBLE);
        } else {
            talkingView.setVisibility(GONE);
        }
    }

    /**
     * 设置当前 view 对应的 stream 的用户，主要用来语音通话时显示对方头像
     */
    public void setUsername(String username) {
        avatarView.setBackgroundResource(R.drawable.call_memberview_background);
        nameView.setText(username);
    }

    /**
     * 设置当前控件显示的 Stream Id
     */
    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    public String getStreamId() {
        return streamId;
    }

    public void setFullScreen(boolean fullScreen) {
        isFullScreenMode = fullScreen;

        if (fullScreen) {
            talkingView.setVisibility(GONE);
            nameView.setVisibility(GONE);
            audioOffView.setVisibility(GONE);
        } else {
            nameView.setVisibility(VISIBLE);
            if (isAudioOff) {
                audioOffView.setVisibility(VISIBLE);
            }

            surfaceView.setScaleMode(VideoView.EMCallViewScaleMode.EMCallViewScaleModeAspectFill);
        }
    }

    public void setScaleMode(VideoView.EMCallViewScaleMode mode) {
        surfaceView.setScaleMode(mode);
    }

    public VideoView.EMCallViewScaleMode getScaleMode() {
        return surfaceView.getScaleMode();
    }


    public void startTimer() {
        timehandler.startTime();
    }

    public void stopTimer() {
        timehandler.stopTime();
    }

    private class TimeHandler extends Handler {
        private final int MSG_TIMER = 0;
        private DateFormat dateFormat = null;
        private int timePassed = 0;

        public TimeHandler() {
            dateFormat = new SimpleDateFormat("HH:mm:ss");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

        public void startTime() {
            timePassed = 0;
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
                if(timePassed *1000 == EaseCallUIKit.getInstance().callInterval){

                    String info = getContext().getString(R.string.The_other_is_not_recived);
                    Toast.makeText(getContext(), info, Toast.LENGTH_SHORT).show();
                    //已经超时删除占位符
                    String key = EaseMsgUtils.INVITEE_KEY + streamId;
                    EaseCallUIKit.getInstance().deleteConfenceAttribute(key);
                    timehandler.stopTime();
                }
                sendEmptyMessageDelayed(MSG_TIMER, 1000);
                return;
            }
            super.handleMessage(msg);
        }
    }
}

