package com.bruce.materialapp.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import com.bruce.materialapp.R;

/**
 * Created by Bruce
 * On 2015/3/16
 * At 20:35
 * About MaterialApp
 */
public class SendingProgressView extends View {
    //progress 执行的状态
    public static final int STATE_NOT_STARTED = 0;
    public static final int STATE_PROGRESS_STARTED = 1;
    public static final int STATE_DONE_STARTED = 2;
    public static final int STATE_FINISHED = 3;

    //progress 描边的宽度
    private static final int PROGRESS_STROKE_SIZE = 10;
    //progress 内外的距离
    private static final int INNER_CIRCLE_PADDING = 30;
    private static final int MAX_DONE_BG_OFFSET = 800;
    private static final int MAX_DONE_IMG_OFFSET = 400;

    private int state = STATE_NOT_STARTED;
    private float currentProgress = 0;
    private float currentDoneBgOffset = MAX_DONE_BG_OFFSET;
    private float currentCheckmarkOffset = MAX_DONE_IMG_OFFSET;

    private Paint progressPaint;
    private Paint doneBgPaint;
    private Paint maskPaint;

    private RectF progressBounds;

    private Bitmap checkmarkBitmap;
    private Bitmap innerCircleMaskBitmap;

    private int checkmarkXPosition = 0;
    private int checkmarkYPosition = 0;

    private Paint checkmarkPaint;
    private Bitmap tempBitmap;
    private Canvas tempCanvas;

    private ObjectAnimator simulateProgressAnimator;
    private ObjectAnimator doneBgAnimator;
    private ObjectAnimator checkmarkAnimator;

    private OnLoadingFinishedListener onLoadingFinishedListener;

    public SendingProgressView(Context context) {
        super(context);
        init();
    }

    public SendingProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SendingProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SendingProgressView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setupProgressPaint();
        setupDonePaints();
        setupSimulateProgressAnimator();
        setupDoneAnimators();
    }

    /**
     * 绘制中progress动态白边的绘制(圆周)
     */
    private void setupProgressPaint() {
        progressPaint = new Paint();
        progressPaint.setAntiAlias(true); //抗锯齿
        progressPaint.setStyle(Paint.Style.STROKE); //实体笔
        progressPaint.setColor(0xffffffff);
        progressPaint.setStrokeWidth(PROGRESS_STROKE_SIZE); //画笔宽度
    }

    /**
     * 内部圆的绘制
     */
    private void setupDonePaints() {
        doneBgPaint = new Paint();
        doneBgPaint.setAntiAlias(true);
        doneBgPaint.setStyle(Paint.Style.FILL); //填充
        doneBgPaint.setColor(0xff39cb72);

        checkmarkPaint = new Paint();

        //PorterDuff.Mode.DST_IN 能将通过alpha的变化将Image剪裁出想要的形状
        maskPaint = new Paint();
        maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
    }

    /**
     * 模拟Progress的动画
     */
    private void setupSimulateProgressAnimator() {
        simulateProgressAnimator = ObjectAnimator.ofFloat(this, "currentProgress", 0, 100).setDuration(2000);
        simulateProgressAnimator.setInterpolator(new AccelerateInterpolator());
        simulateProgressAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                //执行动画完后
                changeState(STATE_DONE_STARTED);
            }
        });
    }


    /**
     * 外部白边绘制完后,中间结束动画 
     */
    private void setupDoneAnimators() {
        //背景动画
        doneBgAnimator = ObjectAnimator.ofFloat(this, "currentDoneBgOffset", MAX_DONE_BG_OFFSET, 0).setDuration(300);
        doneBgAnimator.setInterpolator(new DecelerateInterpolator());
        //剪裁背景的动画
        checkmarkAnimator = ObjectAnimator.ofFloat(this, "currentCheckmarkOffset", MAX_DONE_IMG_OFFSET, 0).setDuration(300);
        checkmarkAnimator.setInterpolator(new OvershootInterpolator());
        checkmarkAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                changeState(STATE_FINISHED);
            }
        });
    }

    /**
     * canvas 大小变化时回调 
     * 1.更新progress白边进度条显示*
     * 2.* 
     * @param w
     * @param h
     * @param oldw
     * @param oldh
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateProgressBounds();
        setupCheckmarkBitmap();
        setupDoneMaskBitmap();
        resetTempCanvas();
    }

    /**
     * 更新此时progress的边界 就是包裹圆周的矩形区域
     */
    private void updateProgressBounds() {
        progressBounds = new RectF(
                PROGRESS_STROKE_SIZE, PROGRESS_STROKE_SIZE,
                getWidth() - PROGRESS_STROKE_SIZE, getWidth() - PROGRESS_STROKE_SIZE
        );
    }

    private void setupCheckmarkBitmap() {
        checkmarkBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_done_white_48dp);
        checkmarkXPosition = getWidth() / 2 - checkmarkBitmap.getWidth() / 2;
        checkmarkYPosition = getWidth() / 2 - checkmarkBitmap.getHeight() / 2;
    }

    private void setupDoneMaskBitmap() {
        innerCircleMaskBitmap = Bitmap.createBitmap(getWidth(), getWidth(), Bitmap.Config.ARGB_8888);
        Canvas srcCanvas = new Canvas(innerCircleMaskBitmap);
        srcCanvas.drawCircle(getWidth() / 2, getWidth() / 2, getWidth() / 2 - INNER_CIRCLE_PADDING, new Paint());
    }

    /**
     * 重设临时画布 canvas*
     */
    private void resetTempCanvas() {
        tempBitmap = Bitmap.createBitmap(getWidth(), getWidth(), Bitmap.Config.ARGB_8888);
        tempCanvas = new Canvas(tempBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (state == STATE_PROGRESS_STARTED) {
            drawArcForCurrentProgress();
        } else if (state == STATE_DONE_STARTED) {
            drawFrameForDoneAnimation();
            postInvalidate();
        } else if (state == STATE_FINISHED) {
            drawFinishedState();
        }

        canvas.drawBitmap(tempBitmap, 0, 0, null);
    }

    /**
     * 绘制圆周 
     */
    private void drawArcForCurrentProgress() {
        /**
         * *oval :指定圆弧的外轮廓矩形区域。
         *  startAngle: 圆弧起始角度，单位为度。
            sweepAngle: 圆弧扫过的角度，顺时针方向，单位为度。
            useCenter: 如果为True时，在绘制圆弧时将圆心包括在内，通常用来绘制扇形。
            paint: 绘制圆弧的画板属性，如颜色，是否填充等。*
         */
        tempCanvas.drawArc(progressBounds, -90f, 360 * currentProgress / 100, false, progressPaint);
    }

    private void drawFrameForDoneAnimation() {
        tempCanvas.drawCircle(getWidth() / 2, getWidth() / 2 + currentDoneBgOffset, getWidth() / 2 - INNER_CIRCLE_PADDING, doneBgPaint);
        tempCanvas.drawBitmap(checkmarkBitmap, checkmarkXPosition, checkmarkYPosition + currentCheckmarkOffset, checkmarkPaint);
        tempCanvas.drawBitmap(innerCircleMaskBitmap, 0, 0, maskPaint);
        tempCanvas.drawArc(progressBounds, 0, 360f, false, progressPaint);
    }

    private void drawFinishedState() {
        tempCanvas.drawCircle(getWidth() / 2, getWidth() / 2, getWidth() / 2 - INNER_CIRCLE_PADDING, doneBgPaint);
        tempCanvas.drawBitmap(checkmarkBitmap, checkmarkXPosition, checkmarkYPosition, checkmarkPaint);
        tempCanvas.drawArc(progressBounds, 0, 360f, false, progressPaint);
    }

    /**
     * 绘制状态的变化
     * @param state
     */
    private void changeState(int state) {
        if (this.state == state) {
            return;
        }

        tempBitmap.recycle();
        resetTempCanvas();

        this.state = state;
        if (state == STATE_PROGRESS_STARTED) {
            //状态为开始,进度设置为0,开始模拟动画simulateProgressAnimator
            setCurrentProgress(0);
            simulateProgressAnimator.start();
        } else if (state == STATE_DONE_STARTED) {
            //白边progress绘制完成,执行背景 和 背景剪裁 的动画
            setCurrentDoneBgOffset(MAX_DONE_BG_OFFSET);
            setCurrentCheckmarkOffset(MAX_DONE_IMG_OFFSET);
            AnimatorSet animatorSet = new AnimatorSet();
            //顺序执行动画
            animatorSet.playSequentially(doneBgAnimator, checkmarkAnimator);
            animatorSet.start();
        } else if (state == STATE_FINISHED) {
            //执行完所有动画,回调执行完方法
            if (onLoadingFinishedListener != null) {
                onLoadingFinishedListener.onLoadingFinished();
            }
        }
    }

    public void simulateProgress() {
        changeState(STATE_PROGRESS_STARTED);
    }

    /**
     *   simulateProgressAnimator = ObjectAnimator.ofFloat(this, "currentProgress", 0, 100).setDuration(2000);
     *   对应该动画*   
     *   postInvalidate 和 Invalidate 区别 前者能在任何线程中调用，而Invalidate只能在UI中，否则需要加上handler*
     * @param currentProgress
     */
    public void setCurrentProgress(float currentProgress) {
        this.currentProgress = currentProgress;
        postInvalidate();
    }

    public void setCurrentDoneBgOffset(float currentDoneBgOffset) {
        this.currentDoneBgOffset = currentDoneBgOffset;
        postInvalidate();
    }

    public void setCurrentCheckmarkOffset(float currentCheckmarkOffset) {
        this.currentCheckmarkOffset = currentCheckmarkOffset;
        postInvalidate();
    }

    public void setOnLoadingFinishedListener(OnLoadingFinishedListener onLoadingFinishedListener) {
        this.onLoadingFinishedListener = onLoadingFinishedListener;
    }

    public interface OnLoadingFinishedListener {
        public void onLoadingFinished();
    }
}
