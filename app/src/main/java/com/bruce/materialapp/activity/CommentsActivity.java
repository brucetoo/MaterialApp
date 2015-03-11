package com.bruce.materialapp.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.bruce.materialapp.R;
import com.bruce.materialapp.adapter.CommentsAdapter;
import com.bruce.materialapp.util.Utils;
import com.bruce.materialapp.view.SendCommentButton;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by n1007 on 2015/3/10.
 */
public class CommentsActivity extends BaseActivity implements SendCommentButton.OnSendClickListener{
    public static final String ARG_DRAWING_START_LOCATION = "arg_drawing_start_location";


    @InjectView(R.id.contentRoot)
    LinearLayout contentRoot;
    @InjectView(R.id.rvComments)
    RecyclerView rvComments;
    @InjectView(R.id.llAddComments)
    LinearLayout llAddComment;
    @InjectView(R.id.btnSendComment)
    SendCommentButton btnSendComment;
    @InjectView(R.id.etComment)
    EditText etComment;

    private int drawingStartLocation; //上个activity点击的位置
    private CommentsAdapter commentsAdapter;
    private static long COMMENT_SHOW_ANIMAITON_TIME = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        ButterKnife.inject(this);

        setComments();
        btnSendComment.setOnSendClickListener(this);

        //获取上个界面点击的Y轴点
        drawingStartLocation = getIntent().getIntExtra(ARG_DRAWING_START_LOCATION, 0);
        if (savedInstanceState == null) {
            //绘制视图树前 去除默认绘图Draw
            contentRoot.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    //移除系统的监听,此必须加上，相当于我们现在是要自己动画的将view显示
                    contentRoot.getViewTreeObserver().removeOnPreDrawListener(this);
                    startIntroAnimation();
                    return true;
                }
            });
        }
    }

    /**
     * 设置评论区的内容*
     */
    private void setComments() {

        commentsAdapter = new CommentsAdapter(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvComments.setLayoutManager(layoutManager);
        rvComments.setAdapter(commentsAdapter);
        rvComments.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                //拖动的时候终止动画,在此用处不大,可删除
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    commentsAdapter.setAnimationsLocked(true);
                }
            }
        });
    }


    /**
     * 取消系统默认的绘制,自定义动画执行绘制
     */
    public void startIntroAnimation() {
        //这两个设置非常重要
        contentRoot.setScaleY(0.1f); //把整个评论区缩小到 0.1f大
        contentRoot.setPivotY(drawingStartLocation); // 将缩放的 锚点Y轴 设置到上个界面点击的位置

        //将评论区向下移动100px 隐藏起来
        llAddComment.setTranslationY(100);

        //contentRoot 动画
        contentRoot.animate()
                .scaleY(1f)
                .setInterpolator(new AccelerateInterpolator())
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        //执行contentRoot动画完后,执行Comment中item的动画
                        animateComment();
                    }
                })
                .start();
    }

    /**
     * comment 详情页的动画
     */
    private void animateComment() {
        commentsAdapter.updateItems();
        //add comment 动画
        llAddComment.animate()
                .translationY(0)
                .setInterpolator(new DecelerateInterpolator())
                .setDuration(COMMENT_SHOW_ANIMAITON_TIME + 200)
                .start();
    }

    /**
     * 按系统回退键的时候先要执行  contentRoot 向下消失的动画
     * 在super.onBackPressed
     */
    @Override
    public void onBackPressed() {
        contentRoot.animate()
                .translationY(Utils.getScreenHeight(this))
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        CommentsActivity.super.onBackPressed();
                        overridePendingTransition(0, 0);
                    }
                })
                .start();
    }

    /**
     * 发送评论的点击处理 
     * @param v
     */
    @Override
    public void onSendClickListener(View v) {
        if(validateComment()) {
            commentsAdapter.addItem();
            commentsAdapter.setAnimationsLocked(false);
        /*
         comment区中的item的显示会出现两种情况
         1.第一次进入显示comment的时候,每个item会根据自己的位置来设置动画的延迟效果
         2.在加入评论的时候，这个时候就不需要每个item都有延迟效果了，可注释该代码对比效果
          */
            commentsAdapter.setDelayEnterAnimation(false);  //改方法是控制 当在添加评论的时候不执行 延迟动画
            //移动到最后一个item位置
            rvComments.smoothScrollBy(0, rvComments.getChildAt(0).getHeight() * commentsAdapter.getItemCount());
            //点击发送后改变状态，两秒后恢复
            btnSendComment.setCurrentState(SendCommentButton.STATE_DONE);
            etComment.setText("");
        }
    }

    /**
     * 判断edCOmment是否为空，为空就抖动 btnSendComment*
     * @return
     */
    private boolean validateComment() {
        if (TextUtils.isEmpty(etComment.getText())) {
            etComment.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake_error));
            btnSendComment.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake_error));
            return false;
        }
        return true;
    }
}
