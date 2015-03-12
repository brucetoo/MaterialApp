package com.bruce.materialapp.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

/**
 * Created by Bruce
 * On 2015/3/12
 * At 18:50
 * About MaterialApp
 * 自定义视图实现 ripple效果*
 */
public class RevealBackgroundView extends View {
    // 绘制reveal的状态
    public static final int STATE_NOT_STARTED = 0;  //未开始
    public static final int STATE_FILL_STARTED = 1; //开始
    public static final int STATE_FINISHED = 2;     //结束
    private static final int FILL_TIME = 400; //绘制时间
    private Paint fillPaint; //绘制的画笔
    private int state = STATE_NOT_STARTED; //最初的状态
    private int currentRadius; // 绘制时的半径(是又系统绘图的时候自己调用赋值)

    //绘制的起始坐标
    private int startLocationX;
    private int startLocationY;

    private OnStateChangeListener onStateChangeListener;
    private ObjectAnimator revealAnimator; //绘制动画

    public RevealBackgroundView(Context context) {
        super(context);
        init();
    }

    public RevealBackgroundView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RevealBackgroundView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RevealBackgroundView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        fillPaint = new Paint();
        fillPaint.setStyle(Paint.Style.FILL); //绘笔分割 为fill
        fillPaint.setColor(Color.WHITE); //绘笔颜色为白色
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (state == STATE_FINISHED) { //如果状态为绘制完成,绘制方形
            canvas.drawRect(0, 0, getWidth(), getHeight(), fillPaint);
        } else { //如果是其他状态,绘制圆形
            canvas.drawCircle(startLocationX, startLocationY, currentRadius, fillPaint);
        }
    }

    /**
     * 对外抛出设置开始绘制的方法
     *
     * @param location 点击的点
     */
    public void startFromLocation(int[] location) {
        //改变状态为开始
        changeState(STATE_FILL_STARTED);
        //设置绘制起始点
        startLocationX = location[0];
        startLocationY = location[1];
        //开始执行绘制动画
        /**
         * 自定义属性currentRadius 绘制
         * * getWidth() + getHeight() 的值表示动画能保证能把长宽都覆盖完
         *   再此之用 getHeight()也是可以的
         */
        revealAnimator = ObjectAnimator.ofInt(this, "currentRadius", 0, getWidth() + getHeight()).setDuration(FILL_TIME);
        revealAnimator.setInterpolator(new AccelerateInterpolator());
        revealAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                //绘制完后改变状态
                changeState(STATE_FINISHED);
            }
        });
        revealAnimator.start();
    }

    private void changeState(int state) {
        if (this.state == state) return;

        this.state = state;
        if (onStateChangeListener != null) {
            onStateChangeListener.onStateChange(state);
        }
    }

    /**
     * 设置状态变化的监听*
     *
     * @param onStateChangeListener
     */
    public void setOnStateChangeListener(OnStateChangeListener onStateChangeListener) {
        this.onStateChangeListener = onStateChangeListener;
    }

    /**
     * 设置当前绘制的半径,然后在重绘*
     * 自定义属性绘制*
     * 改方法是被系统调用，ObjectAnimator.ofInt(this, "currentRadius", 0, getWidth() + getHeight()).setDuration(FILL_TIME);*
     * 这句代码已经限定了currentRadius这个属性,相当于 translationY 调用 ——- setTranslationY() *
     *
     * @param currentRadius
     */
    public void setCurrentRadius(int currentRadius) {
        this.currentRadius = currentRadius;
        invalidate();
    }

    /**
     * 状态变化时的监听接口*
     */
    public static interface OnStateChangeListener {
        void onStateChange(int state);
    }
}
