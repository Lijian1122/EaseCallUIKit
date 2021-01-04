package com.hyphenate.calluikit.Base;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import com.hyphenate.calluikit.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public class EaseCommingCallView extends FrameLayout {

    private ImageButton mBtnReject;
    private ImageButton mBtnPickup;
    private TextView mInviterName;
    private OnActionListener mOnActionListener;


    public EaseCommingCallView(@NonNull Context context) {
        this(context, null);
    }

    public EaseCommingCallView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EaseCommingCallView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.activity_comming_call, this);
        mBtnReject = findViewById(R.id.btn_reject);
        mBtnPickup = findViewById(R.id.btn_pickup);
        mInviterName = findViewById(R.id.tv_nick);

        mBtnReject.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mOnActionListener != null) {
                    mOnActionListener.onRejectClick(v);
                }
            }
        });

        mBtnPickup.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mOnActionListener != null) {
                    mOnActionListener.onPickupClick(v);
                }
            }
        });
    }

    public void setInviteInfo(String username){
        mInviterName.setText(username);
    }


    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if(visibility == View.VISIBLE) {
 //           mIvCallAnim.setBackgroundResource(R.drawable.call_ring_anim);
//            mDrawableAnim = mIvCallAnim.getBackground();
//            if(mDrawableAnim instanceof AnimationDrawable) {
//                ((AnimationDrawable) mDrawableAnim).setOneShot(false);
//                ((AnimationDrawable) mDrawableAnim).start();
//            }
        }else {
//            if(mDrawableAnim instanceof AnimationDrawable) {
//                if(((AnimationDrawable) mDrawableAnim).isRunning()) {
//                    ((AnimationDrawable) mDrawableAnim).stop();
//                }
//                mDrawableAnim = null;
//            }
        }
    }



    public void setOnActionListener(OnActionListener listener) {
        this.mOnActionListener = listener;
    }

    public interface OnActionListener {
        void onRejectClick(View v);
        void onPickupClick(View v);
    }


    float[] getScreenInfo(Context context) {
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        float[] info = new float[5];
        if(manager != null) {
            DisplayMetrics dm = new DisplayMetrics();
            manager.getDefaultDisplay().getMetrics(dm);
            info[0] = dm.widthPixels;
            info[1] = dm.heightPixels;
            info[2] = dm.densityDpi;
            info[3] = dm.density;
            info[4] = dm.scaledDensity;
        }
        return info;
    }
}

