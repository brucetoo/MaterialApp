package com.bruce.materialapp.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by Bruce
 * On 2015/3/12
 * At 15:35
 * About MaterialApp
 */
public class SquaredFrameLayout extends FrameLayout {
    public SquaredFrameLayout(Context context) {
        super(context);
    }

    public SquaredFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquaredFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SquaredFrameLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
