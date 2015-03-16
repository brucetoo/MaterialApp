package com.bruce.materialapp.util;

import android.app.Activity;
import android.content.res.Resources;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.bruce.materialapp.R;

/**
 * DrawerLayout installer which helps you to add left sliding panel to your activity
 * 轻易的实现了drawerLayout抽屉效果，包括了切换抽屉的动画*
 * 使用例子
 *  drawerLayout = DrawerLayoutInstaller.from(this)
                                        .drawerRoot(R.layout.drawer_root) //drawerLayout为跟布局的layout文件id
                                        .drawerLeftView(menuView)  //抽屉左侧显示的view
                                        .drawerLeftWidth(Utils.dpToPx(300))   //抽屉左侧的宽度
                                        .withNavigationIconToggler(toolbar)   //将toolBar绑定到此
                                        .build();* *
 */
public class DrawerLayoutInstaller {

    public static final int DEFAULT_LEFT_DRAWER_WIDTH_DP = 240;

    public static DrawerBuilder from(Activity activity) {
        return new DrawerBuilder(activity);
    }

    public static class DrawerBuilder {

        private Activity activity;
        private int drawerRootResId;
        private Toolbar toolbar;
        private DrawerLayout.DrawerListener drawerListener;
        private View drawerLeftView;
        private int drawerLeftWidth;

        public DrawerBuilder(Activity activity) {
            this.activity = activity;
        }

        @SuppressWarnings("UnusedDeclaration")
        private DrawerBuilder() {
            throw new RuntimeException("Not supported. Use DrawerBuilder(Activity activity) instead.");
        }

        /**
         * Root resource used as a base for DrawerLayout. The only supported structure built from
         * DrawerLayout as root element and two children.
         */
        public DrawerBuilder drawerRoot(int drawerRootResId) {
            this.drawerRootResId = drawerRootResId;
            return this;
        }

        /**
         * Add toggler for open/close DrawerLayout. It takes navigation icon from {@link android.support.v7.widget.Toolbar}
         */
        public DrawerBuilder withNavigationIconToggler(Toolbar toolbar) {
            this.toolbar = toolbar;
            return this;
        }

        /**
         * Add listener for DrawerLayout operations.
         */
        public DrawerBuilder drawerListener(DrawerLayout.DrawerListener drawerListener) {
            this.drawerListener = drawerListener;
            return this;
        }

        /**
         * Add custom view which will be put inside root of left panel in DrawerLayout.
         */
        public DrawerBuilder drawerLeftView(View drawerLeftView) {
            this.drawerLeftView = drawerLeftView;
            return this;
        }

        /**
         * Set custom width of left panel. By default 240dp translated to px is used.
         *
         * @param width in pixels
         */
        public DrawerBuilder drawerLeftWidth(int width) {
            this.drawerLeftWidth = width;
            return this;
        }

        /**
         * Build DrawerLayout and inject it into given {@link android.app.Activity}
         */
        public DrawerLayout build() {
            DrawerLayout drawerLayout = createDrawerLayout();
            addDrawerToActivity(drawerLayout);
            setupToggler(drawerLayout);
            //DrawerLayout的切换动画效果
            setupDrawerLeftView(drawerLayout);
            ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(activity,drawerLayout,toolbar,
                    R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawerLayout.setDrawerListener(drawerToggle);
            drawerToggle.syncState();
            return drawerLayout;
        }

        /**
         * *创建drawerLayout
         * @return
         */
        private DrawerLayout createDrawerLayout() {
            if (drawerRootResId != 0) {
                return (DrawerLayout) LayoutInflater.from(activity).inflate(drawerRootResId, null);
            } else {
                //如果给出的drawerLayout id 是不存在的，则手动创建一个
                DrawerLayout drawerLayout = new DrawerLayout(activity);

                FrameLayout contentView = new FrameLayout(activity);
                drawerLayout.addView(contentView, new DrawerLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                ));

                FrameLayout leftDrawer = new FrameLayout(activity);

                int drawerWidth = drawerLeftWidth != 0 ? drawerLeftWidth : DEFAULT_LEFT_DRAWER_WIDTH_DP;

                final ViewGroup.LayoutParams leftDrawerParams = new DrawerLayout.LayoutParams(
                        (int) (drawerWidth * Resources.getSystem().getDisplayMetrics().density),
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        Gravity.START
                );
                drawerLayout.addView(leftDrawer, leftDrawerParams);
                return drawerLayout;
            }
        }

        /**
         * 将drawerLayout 加入到activity
         * 主要是找到activity的根视图，然后将其替换*
         * @param drawerLayout
         */
        private void addDrawerToActivity(DrawerLayout drawerLayout) {
            //找到activity的根视图
            ViewGroup rootView = (ViewGroup) activity.findViewById(android.R.id.content);
            //将activity的根视图(android.R.id.content)替换成 drawer_root中的(content)内容视图
            ViewGroup drawerContentRoot = (ViewGroup) drawerLayout.getChildAt(0);
            View contentView = rootView.getChildAt(0);
            //移除acitivity中的根视图
            rootView.removeView(contentView);
            //drawer_root 添加视图
            drawerContentRoot.addView(contentView, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
            //将drawerLayout加入到rootView中
            rootView.addView(drawerLayout, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
        }

        /**
         *  drawerLayout的点击切换，左侧进左侧出 Gravity.START
         * @param drawerLayout
         */
        private void setupToggler(final DrawerLayout drawerLayout) {
            if (toolbar != null) {
                //toolBar NavigationIcon的点击事件
                toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (drawerLayout.isDrawerOpen(Gravity.START)) {
                            drawerLayout.closeDrawer(Gravity.START);
                        } else {
                            drawerLayout.openDrawer(Gravity.START);
                        }
                    }
                });
            }
        }

        /**
         * 设置drawerLayout的抽屉view*
         * @param drawerLayout
         */
        private void setupDrawerLeftView(DrawerLayout drawerLayout) {
            if (drawerLeftView != null) {
                ((ViewGroup) drawerLayout.getChildAt(1))
                        .addView(drawerLeftView, new DrawerLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                        ));
            }
        }
    }
}