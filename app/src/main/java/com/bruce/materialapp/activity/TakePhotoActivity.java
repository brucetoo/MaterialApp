package com.bruce.materialapp.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ViewSwitcher;

import com.bruce.materialapp.R;
import com.bruce.materialapp.adapter.PhotoFiltersAdapter;
import com.bruce.materialapp.view.RevealBackgroundView;
import com.commonsware.cwac.camera.CameraHost;
import com.commonsware.cwac.camera.CameraHostProvider;
import com.commonsware.cwac.camera.CameraView;
import com.commonsware.cwac.camera.PictureTransaction;
import com.commonsware.cwac.camera.SimpleCameraHost;

import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by Bruce
 * On 2015/3/16
 * At 17:37
 * About MaterialApp
 */
public class TakePhotoActivity extends BaseActivity implements RevealBackgroundView.OnStateChangeListener, CameraHostProvider {
    public static final String ARG_REVEAL_START_LOCATION = "reveal_start_location";

    private static final Interpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
    private static final Interpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();
    //照片view显示的状态
    private static final int STATE_TAKE_PHOTO = 0;   //拍照
    private static final int STATE_SETUP_PHOTO = 1;  //设置照片

    @InjectView(R.id.vRevealBackground)
    RevealBackgroundView vRevealBackground; //用于切入的动画显示view
    @InjectView(R.id.vTakePhotoRoot)
    View contentRoot;         //content rootview 包含了所以需要显示的内容
    @InjectView(R.id.vPhotoRoot)
    View vTakePhotoRoot; // 包含了拍照显示的内容
    @InjectView(R.id.vShutter)
    View vShutter;  //shutter 拍照时模仿闪光的动画
    @InjectView(R.id.ivTakenPhoto)
    ImageView ivTakenPhoto;   //拍照后的图片显示imageView
    @InjectView(R.id.vUpperPanel)
    ViewSwitcher vUpperPanel;  // 上方切换viewSwicher
    @InjectView(R.id.vLowerPanel)
    ViewSwitcher vLowerPanel;  //下方切换 viewSwicher
    @InjectView(R.id.cameraView)
    CameraView cameraView;  //三方插件 camera
    @InjectView(R.id.rvFilters)
    RecyclerView rvFilters;  //图片滤镜 recyclerView
    @InjectView(R.id.btnTakePhoto)
    Button btnTakePhoto;  // 拍照按钮
    

    private boolean pendingIntro;
    private int currentState;

    /**
     * 从外activity中启动该activity
     *
     * @param startingLocation 启动触摸的点
     * @param startingActivity 启动触发的activity
     */
    public static void startCameraFromLocation(int[] startingLocation, Activity startingActivity) {
        Intent intent = new Intent(startingActivity, TakePhotoActivity.class);
        intent.putExtra(ARG_REVEAL_START_LOCATION, startingLocation);
        startingActivity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_photo);
        updateState(STATE_TAKE_PHOTO);
        setupRevealBackground(savedInstanceState);
        setupPhotoFilters();

        vUpperPanel.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                vUpperPanel.getViewTreeObserver().removeOnPreDrawListener(this);
                pendingIntro = true;
                vUpperPanel.setTranslationY(-vUpperPanel.getHeight());
                vLowerPanel.setTranslationY(vLowerPanel.getHeight());
                return true;
            }
        });
    }

    private void setupRevealBackground(Bundle savedInstanceState) {
        vRevealBackground.setFillPaintColor(0xFF16181a);
        vRevealBackground.setOnStateChangeListener(this);
        if (savedInstanceState == null) {
            final int[] startingLocation = getIntent().getIntArrayExtra(ARG_REVEAL_START_LOCATION);
            vRevealBackground.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    vRevealBackground.getViewTreeObserver().removeOnPreDrawListener(this);
                    vRevealBackground.startFromLocation(startingLocation);
                    return true;
                }
            });
        } else {
            vRevealBackground.setToFinishedFrame();
        }
    }

    private void setupPhotoFilters() {
        PhotoFiltersAdapter photoFiltersAdapter = new PhotoFiltersAdapter(this);
        rvFilters.setHasFixedSize(true);
        rvFilters.setAdapter(photoFiltersAdapter);
        rvFilters.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    }

    @Override
    protected boolean shouldInstallDrawer() {
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.onPause();
    }

    @OnClick(R.id.btnTakePhoto)
    public void onTakePhotoClick() {
        btnTakePhoto.setEnabled(false);
        cameraView.takePicture(true, false);
        animateShutter();
    }

    /**
     * 拍照时的闪光效果
     * 主要也就是将view 的透明度从 0-0.8f（透明到不透明） 再 从0.8f-0（不透明到透明） 执行一个序列
     */
    private void animateShutter() {
        vShutter.setVisibility(View.VISIBLE);
        vShutter.setAlpha(0.f);

        ObjectAnimator alphaInAnim = ObjectAnimator.ofFloat(vShutter, "alpha", 0f, 0.8f);
        alphaInAnim.setDuration(100);
        alphaInAnim.setStartDelay(100);
        alphaInAnim.setInterpolator(ACCELERATE_INTERPOLATOR);

        ObjectAnimator alphaOutAnim = ObjectAnimator.ofFloat(vShutter, "alpha", 0.8f, 0f);
        alphaOutAnim.setDuration(200);
        alphaOutAnim.setInterpolator(DECELERATE_INTERPOLATOR);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(alphaInAnim, alphaOutAnim);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                //执行完动画后，将view 隐藏
                vShutter.setVisibility(View.GONE);
            }
        });
        animatorSet.start();
    }

    /**
     * * RevealBackgroundView 绘制状态的回调函数
     * 绘制前：隐藏 vTakePhotoRoot(需要显示所有view的rootview)*
     * 绘制后：执行显示 vTakePhotoRoot*
     *
     * @param state
     */
    @Override
    public void onStateChange(int state) {
        if (RevealBackgroundView.STATE_FINISHED == state) {
            vTakePhotoRoot.setVisibility(View.VISIBLE);
            if (pendingIntro) {
                startIntroAnimation();
            }
        } else {
            vTakePhotoRoot.setVisibility(View.INVISIBLE);
        }
    }

    private void startIntroAnimation() {
        vUpperPanel.animate().translationY(0).setDuration(400).setInterpolator(DECELERATE_INTERPOLATOR);
        vLowerPanel.animate().translationY(0).setDuration(400).setInterpolator(DECELERATE_INTERPOLATOR).start();
    }

    @Override
    public CameraHost getCameraHost() {
        return new MyCameraHost(this);
    }

    /**
     * * CameraHost 用于返回拍照的数据
     */
    class MyCameraHost extends SimpleCameraHost {

        private Camera.Size previewSize;

        public MyCameraHost(Context ctxt) {
            super(ctxt);
        }

        @Override
        public boolean useFullBleedPreview() {
            return true;
        }

        @Override
        public Camera.Size getPictureSize(PictureTransaction xact, Camera.Parameters parameters) {
            return previewSize;
        }

        @Override
        public Camera.Parameters adjustPreviewParameters(Camera.Parameters parameters) {
            Camera.Parameters parameters1 = super.adjustPreviewParameters(parameters);
            previewSize = parameters1.getPreviewSize();
            return parameters1;
        }

        @Override
        public void saveImage(PictureTransaction xact, final Bitmap bitmap) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showTakenPicture(bitmap);
                }
            });
        }
    }

    /**
     * 照片拍照完后
     * * vUpperPanel vLowerPanel都切换到下一个view
     * 更改现在的状态
     *
     * @param bitmap
     */
    private void showTakenPicture(Bitmap bitmap) {
        vUpperPanel.showNext();
        vLowerPanel.showNext();
        ivTakenPhoto.setImageBitmap(bitmap);
        updateState(STATE_SETUP_PHOTO);
    }

    @Override
    public void onBackPressed() {
        //在操作系统返回键时，如果状态是正在处理图片
        if (currentState == STATE_SETUP_PHOTO) {
            btnTakePhoto.setEnabled(true);
            //均显示下一个view
            vUpperPanel.showNext();
            vLowerPanel.showNext();
            updateState(STATE_TAKE_PHOTO);
        } else {
            super.onBackPressed();
//            contentRoot.animate()
//                    .translationY(Utils.getScreenHeight(this))
//                    .setDuration(300)
//                    .setListener(new AnimatorListenerAdapter() {
//                        @Override
//                        public void onAnimationEnd(Animator animation) {
//                            TakePhotoActivity.super.onBackPressed();
//                            overridePendingTransition(0, 0);
//                        }
//                    })
//                    .start();
        }
    }

    /**
     * 照片设置状态：分拍照和处理*
     *
     * @param state
     */
    private void updateState(int state) {
        currentState = state;
        //拍照时 切换vUpperPanel vLowerPanel两个viewSwither
        //vUpperPanel 右进,左出
        //vLowerPanel 右进,左出
        if (currentState == STATE_TAKE_PHOTO) {
            vUpperPanel.setInAnimation(this, R.anim.slide_in_from_right);
            vLowerPanel.setInAnimation(this, R.anim.slide_in_from_right);
            vUpperPanel.setOutAnimation(this, R.anim.slide_out_to_left);
            vLowerPanel.setOutAnimation(this, R.anim.slide_out_to_left);
            //拍照状态下：隐藏需要显示照片的imagView
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ivTakenPhoto.setVisibility(View.GONE);
                }
            }, 400);
        } else if (currentState == STATE_SETUP_PHOTO) {
            //处理图片时 切换vUpperPanel vLowerPanel两个viewSwither
            //vUpperPanel 左进,右出
            //vLowerPanel 左进,右出
            vUpperPanel.setInAnimation(this, R.anim.slide_in_from_left);
            vLowerPanel.setInAnimation(this, R.anim.slide_in_from_left);
            vUpperPanel.setOutAnimation(this, R.anim.slide_out_to_right);
            vLowerPanel.setOutAnimation(this, R.anim.slide_out_to_right);
            //处理照片时：显示需要显示照片的imageView
            ivTakenPhoto.setVisibility(View.VISIBLE);
        }
    }
}





















