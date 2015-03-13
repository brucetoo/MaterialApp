package com.bruce.materialapp.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;

import com.bruce.materialapp.R;
import com.bruce.materialapp.adapter.UserProfileAdapter;
import com.bruce.materialapp.util.Utils;
import com.bruce.materialapp.view.RevealBackgroundView;

import butterknife.InjectView;

/**
 * Created by Bruce
 * On 2015/3/12
 * At 17:06
 * About MaterialApp
 */
public class UserProfileActivity extends BaseActivity implements RevealBackgroundView.OnStateChangeListener {
    public static final String ARG_REVEAL_START_LOCATION = "reveal_start_location";
    @InjectView(R.id.vRevealBackground)
    RevealBackgroundView vRevealBackground;
    @InjectView(R.id.rvUserProfile)
    RecyclerView rvUserProfile;
    private UserProfileAdapter userPhotosAdapter;

    /**
     * 起动 UserProfileActivity
     *
     * @param startingLocation
     * @param startingActivity
     */
    public static void startUserProfileFromLocation(int[] startingLocation, Activity startingActivity) {
        Intent intent = new Intent(startingActivity, UserProfileActivity.class);
        intent.putExtra(ARG_REVEAL_START_LOCATION, startingLocation);
        startingActivity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        setupRevealBackground(savedInstanceState);
        setupUserProfileGrid();
    }

    private void setupUserProfileGrid() {
        /**在此使用StaggeredGridLayoutManager的原因如下：
         显示view 具有三个 item,而每个item显示的方式不同,
         要想把不同的item整合到一起,如果用GridLayoutManager
         就不能单独的对某个item进行排版的操作
         */
        final StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        rvUserProfile.setLayoutManager(layoutManager);
        rvUserProfile.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                //滚动 recycler的时候停止动画的播放
                 userPhotosAdapter.setLockedAnimations(true);
            }
        });
    }

    /**
     * 设置 reveal 背景动画启动
     *
     * @param savedInstanceState
     */
    private void setupRevealBackground(Bundle savedInstanceState) {
        vRevealBackground.setVisibility(View.VISIBLE);
        //设置 reveal绘制状态变化的监听
        vRevealBackground.setOnStateChangeListener(this);
        if (savedInstanceState == null) {
            //获取 intent 中传递的坐标
            final int[] startingLocation = getIntent().getIntArrayExtra(ARG_REVEAL_START_LOCATION);
            //vRevealBackground 在此只是执行一个动画效果而已
            vRevealBackground.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    vRevealBackground.getViewTreeObserver().removeOnPreDrawListener(this);
                    vRevealBackground.startFromLocation(startingLocation);
                    return false;
                }
            });
        } else {
//            userPhotosAdapter.setLockedAnimations(true);
            //如果activity有保存的状态（Activity is restoring）,在次进入时不执行动画
            vRevealBackground.setToFinishedFrame();
        }
    }

    /**
     * * RevealBackgroundView 的绘制状态变化时调用
     * @param state
     */
    @Override
    public void onStateChange(int state) {
        if (RevealBackgroundView.STATE_FINISHED == state) {
            rvUserProfile.setVisibility(View.VISIBLE);
            //adapter的初始化必须是在revealview的状态在绘制完过后才进行,使其具有自然过渡的效果
            //可将代码提前到onCreate方法中对比
            userPhotosAdapter = new UserProfileAdapter(UserProfileActivity.this);
            rvUserProfile.setAdapter(userPhotosAdapter);
        } else {
            rvUserProfile.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        vRevealBackground.setVisibility(View.INVISIBLE);
        rvUserProfile.animate().translationY(Utils.getScreenHeight(this)).setDuration(400).setInterpolator(new DecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        UserProfileActivity.super.onBackPressed();
                        //删除系统默认动画
                        overridePendingTransition(0, 0);
                    }
                }).start();

    }
}
