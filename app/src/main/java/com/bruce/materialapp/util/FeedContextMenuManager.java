package com.bruce.materialapp.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import com.bruce.materialapp.view.FeedContextMenu;

/**
 * Created by Bruce
 * On 2015/3/11
 * At 19:22
 * About MaterialApp
 * ContextMenu的管理类*
 */
public class FeedContextMenuManager implements View.OnAttachStateChangeListener {

    private static FeedContextMenuManager instance;

    private FeedContextMenu contextMenuView;
    private boolean isContextMenuDismissing;
    private boolean isContextMenuShowing;

    public static FeedContextMenuManager getInstance() {
        if (instance == null) {
            instance = new FeedContextMenuManager();
        }
        return instance;
    }

    @Override
    public void onViewAttachedToWindow(View v) {

    }

    @Override
    public void onViewDetachedFromWindow(View v) {
        contextMenuView = null;
    }

    public void toggleContextMenuFromView(View openingView, int feedItem, FeedContextMenu.OnFeedContextMenuItemClickListener listener) {
        if (contextMenuView == null) {
            showContextMenuFromView(openingView, feedItem, listener);
        } else {
            hideContextMenu();
        }
    }

    /**
     * 显示ContextMenu
     *
     * @param openingView
     * @param feedItem
     * @param listener
     */
    private void showContextMenuFromView(final View openingView, int feedItem, FeedContextMenu.OnFeedContextMenuItemClickListener listener) {
        if (!isContextMenuShowing) {
            isContextMenuShowing = true;
            contextMenuView = new FeedContextMenu(openingView.getContext());
            //绑定点击的item数据
            contextMenuView.bindToItem(feedItem);
            //将contextMenuView的AttachStateChangeListener监听事件的响应在该类中处理
            contextMenuView.addOnAttachStateChangeListener(this);
            //contextMenuView中每个Item的点击绑定
            contextMenuView.setOnFeedMenuItemClickListener(listener);
            //得到最顶层的view
            ((ViewGroup) openingView.getRootView().findViewById(android.R.id.content)).addView(contextMenuView);
            //contextMenuView 在绘制前,取消系统默认的绘制,自定义动画
            contextMenuView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    contextMenuView.getViewTreeObserver().removeOnPreDrawListener(this);
                    setupContextMenuInitialPosition(openingView);
                    performShowAnimation();
                    return false;
                }
            });
        }
    }

    /**
     * 设置contextMenu的初始化位置
     *
     * @param openingView
     */
    private void setupContextMenuInitialPosition(View openingView) {
        //获取点击view的位置
        final int[] openingViewLocation = new int[2];
        openingView.getLocationOnScreen(openingViewLocation);
        //额外添加BottomMargin
        int additionalBottomMargin = Utils.dpToPx(16);
        //contextMenu位置的微调
        contextMenuView.setTranslationX(openingViewLocation[0] - contextMenuView.getWidth() / 3);
        contextMenuView.setTranslationY(openingViewLocation[1] - contextMenuView.getHeight() - additionalBottomMargin);
    }

    /**
     * 执行 ContextMenu显示时的动画
     */
    private void performShowAnimation() {
        //设置contextMenuView的锚点在 中下方 ！
        contextMenuView.setPivotX(contextMenuView.getWidth() / 2);
        contextMenuView.setPivotY(contextMenuView.getHeight());
        //动画前先缩小到 0.1f
        contextMenuView.setScaleX(0.1f);
        contextMenuView.setScaleY(0.1f);
        contextMenuView.animate()
                .scaleX(1f).scaleY(1f)
                .setDuration(150)
                .setInterpolator(new OvershootInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        isContextMenuShowing = false;
                    }
                });
    }

    /**
     * 隐藏contextMenuView
     */
    public void hideContextMenu() {
        if (!isContextMenuDismissing) {
            isContextMenuDismissing = true;
            performDismissAnimation();
        }
    }

    /**
     * 隐藏动画
     */
    private void performDismissAnimation() {
        //contextMenuView锚点已经设置过,这里无需设置
//        contextMenuView.setPivotX(contextMenuView.getWidth() / 2);
//        contextMenuView.setPivotY(contextMenuView.getHeight());
        contextMenuView.animate()
                .scaleX(0.1f).scaleY(0.1f)
                .setDuration(150)
                .setInterpolator(new AccelerateInterpolator())
                .setStartDelay(100)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (contextMenuView != null) {
                            //如果该视图存在,则在隐藏的过后就将其从父布局中移除掉
                            contextMenuView.dismiss();
                        }
                        isContextMenuDismissing = false;
                    }
                });
    }

    /**
     * 滚动recyclerview的时候隐藏contextMenuView
     * @param recyclerView
     * @param dx
     * @param dy
     */
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        if (contextMenuView != null) {
            //因为在滚动的时候,contextMenuView相对屏幕的位置发生了变化,隐藏要动态的将其位置变化
            contextMenuView.setTranslationY(contextMenuView.getTranslationY() - dy);
            hideContextMenu();
        }
    }

}
