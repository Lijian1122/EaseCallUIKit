package com.hyphenate.calluikit.Base;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import com.hyphenate.calluikit.R;

import androidx.annotation.Nullable;


/**
 * Created by lijian on 2020.12.09.
 */

public class EaseCallSquareRelativeLayout extends RelativeLayout {
    public EaseCallSquareRelativeLayout(Context context) {
        super(context);
    }

    public EaseCallSquareRelativeLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public EaseCallSquareRelativeLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
