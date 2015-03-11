package com.bruce.materialapp.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ViewAnimator;

import com.bruce.materialapp.R;

/**
 * Created by Bruce
 * On 2015/3/11
 * At 15:59
 * About MaterialApp
 * 发送评论的button  ViewAnimator可以实现同一个FrameLayout perform animations
 * when switching between its views.
 */
public class SendCommentButton extends ViewAnimator implements View.OnClickListener {

    public static final int STATE_SEND = 0; //发送状态
    public static final int STATE_DONE = 1; //发完状态
    private static final long RESET_STATE_DELAY_MILLIS = 2000;
    private int currentState; //现在的状态
    private OnSendClickListener onSendClickListener;

    public SendCommentButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SendCommentButton(Context context) {
        super(context);
        init();
    }


    private Runnable revertStateRunnable = new Runnable() {
        @Override
        public void run() {
            setCurrentState(STATE_SEND);
        }
    };

    /**
     * 动态设置viewAnimatior button的状态
     * @param state
     */
    public void setCurrentState(int state) {
        if (state == currentState) {
            return;
        }

        currentState = state;
        if (state == STATE_DONE) {
            setEnabled(false);
            postDelayed(revertStateRunnable, RESET_STATE_DELAY_MILLIS);
            setInAnimation(getContext(), R.anim.slide_in_done);
            setOutAnimation(getContext(), R.anim.slide_out_send);
        } else if (state == STATE_SEND) {
            setEnabled(true);
            setInAnimation(getContext(), R.anim.slide_in_send);
            setOutAnimation(getContext(), R.anim.slide_out_done);
        }
        showNext();
    }
    
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        //显示的时候状态为发送
        currentState = STATE_SEND;
        //让父布局的点击事件，响应到此处
        super.setOnClickListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        //退出界面的时候讲状态改变回来  you destroy the Acitivty before button state switches back
        removeCallbacks(revertStateRunnable);
        super.onDetachedFromWindow();
    }
    
    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_send_comment_button, this, true);
    }

    @Override
    public void onClick(View v) {
        if (onSendClickListener != null) {
            onSendClickListener.onSendClickListener(this);
        }
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        //Do nothing, you have you own onClickListener implementation (OnSendClickListener)
        //此处 //让父布局的点击事件，这个方法被覆盖
        //super.setOnClickListener(this);
    }

    public void setOnSendClickListener(OnSendClickListener onSendClickListener) {
        this.onSendClickListener = onSendClickListener;
    }
    
    public interface OnSendClickListener {
        public void onSendClickListener(View v);
    }
}
