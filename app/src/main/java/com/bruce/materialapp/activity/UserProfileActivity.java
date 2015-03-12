package com.bruce.materialapp.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewTreeObserver;

import com.bruce.materialapp.R;
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
    }

    /**
     * 设置 reveal 背景动画启动
     *
     * @param savedInstanceState
     */
    private void setupRevealBackground(Bundle savedInstanceState) {
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

    @Override
    public void onStateChange(int state) {
        if (RevealBackgroundView.STATE_FINISHED == state) {
            rvUserProfile.setVisibility(View.VISIBLE);
            //  userPhotosAdapter = new UserProfileAdapter(this);
            // rvUserProfile.setAdapter(userPhotosAdapter);
        } else {
            rvUserProfile.setVisibility(View.INVISIBLE);
        }
    }
}
