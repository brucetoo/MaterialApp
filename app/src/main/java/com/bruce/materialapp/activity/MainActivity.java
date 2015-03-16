package com.bruce.materialapp.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;

import com.bruce.materialapp.R;
import com.bruce.materialapp.adapter.FeedAdapter;
import com.bruce.materialapp.util.FeedContextMenuManager;
import com.bruce.materialapp.util.Utils;
import com.bruce.materialapp.view.FeedContextMenu;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class MainActivity extends BaseActivity implements FeedAdapter.OnFeedItemClickListener
        , FeedContextMenu.OnFeedContextMenuItemClickListener {

    @InjectView(R.id.rvPost)
    RecyclerView rvPost;
    @InjectView(R.id.ibtCamera)
    ImageButton ibtCamera;
//    @InjectView(R.id.btnCreate)
//    ImageButton btnCreate;

    private FeedAdapter adapter;
    private boolean pendingIntroAnimation;   //界面进入的执行动画
    private static final int TOOLBAR_ANIMATE_TIME = 500;  //toolbar 动画时间
    private static final int CONTENT_ANIMATE_TIME = 500;  //recycler动画时间

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            pendingIntroAnimation = true;
        }
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        setPost();
    }

    private void setPost() {
        //LayoutManager预加载时为每个item增加额外空间layout space
        //保证了当每个item的高度已经够覆盖屏幕,第一次加载时RecyclerView只会加载一个item
        //但是在滑动的时候,RecyclerView会重用上一个view,但是现在没有可重用的view,导致最初滑动的时候会有点卡顿。
        // 所以给每个item额外的空间能保证加载的时候多加载一个Item 保证了重用
        /*
        Another problem I found in project is feed scroll smoothness right after we launch the app.
         Almost every time we scroll to the second item, layout stutters for a while. 
         Fortunately the reason of this problem is quite simple. 
         As you probably know RecyclerView (and other adapter views like ListView, GridView etc.) 
         uses recycle mechanism for reusing views (in short, system keeps in memory layout only for items which are visible on screen,
          and reuse them instead of creating new ones if you scroll it).

         The problem with our project is that we have only one feed element visible on the screen after we start the application. 
         When we start scrolling, there are no views to reuse by RecyclerView, 
         so in the moment when we reach the second element it has to be layed out. 
         Unfortunately it takes some time, so our RecyclerView stutters for this moment. 
         After that scroll becomes smooth because we had at least two elements in memory to reuse (which we don’t need to create from scratch)
         */
        LinearLayoutManager manager = new LinearLayoutManager(this) {
            @Override
            protected int getExtraLayoutSpace(RecyclerView.State state) {
                return 300;
            }
        };
        rvPost.setLayoutManager(manager);
        adapter = new FeedAdapter(this);
        adapter.setOnFeedItemClickListener(this); //设置item的点击事件 回调
        rvPost.setAdapter(adapter);
        //recyclerview滚动的时候如果存在contextMenuView就将其隐藏
        rvPost.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                FeedContextMenuManager.getInstance().onScrolled(recyclerView, dx, dy);
            }
        });
    }

    /**
     * 开始toolBar 动画*
     */
    private void startToolBarAnimation() {
        //悬浮照相按钮移动到下方隐藏
        ibtCamera.setTranslationY(2 * getResources().getDimensionPixelSize(R.dimen.btn_fab_size));

        int actionBarSize = Utils.dpToPx(56);
        //toolbar元素隐藏
        toolbar.setTranslationY(-actionBarSize);
        ivLogo.setTranslationY(-actionBarSize);
        inboxMenuItem.getActionView().setTranslationY(-actionBarSize);

        //执行动画
        toolbar.animate().translationY(0).setDuration(TOOLBAR_ANIMATE_TIME).setInterpolator(new DecelerateInterpolator()).setStartDelay(300);
        ivLogo.animate().translationY(0).setDuration(TOOLBAR_ANIMATE_TIME).setInterpolator(new DecelerateInterpolator()).setStartDelay(400);
        /**
         在此出用 AnimatorListenerAdapter 的原因是避免很多没必要的方法必须重写
         避免了 new Animate.AnimatorListener的冗余
         */
        inboxMenuItem.getActionView().animate().translationY(0).setDuration(TOOLBAR_ANIMATE_TIME).setInterpolator(new DecelerateInterpolator())
                .setStartDelay(500)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        //执行完 toolBar动画后执行
                        startContentAnimation();
                    }
                }).start();
    }

    /**
     * recyclerview 和 FAB 的动画
     */
    private void startContentAnimation() {
        ibtCamera.animate()
                .translationY(0)
                .setInterpolator(new OvershootInterpolator(1.f))
                .setDuration(CONTENT_ANIMATE_TIME)
                .setStartDelay(300);
        adapter.updateItems();
    }

    //If you want to be rich, Don't work for money, make money work hard for you
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        //在第一次创建menu的时候执行动画
        if (pendingIntroAnimation) {
            pendingIntroAnimation = false;
            startToolBarAnimation();
        }
        return true;
    }

    /**
     * comment点击事件
     *
     * @param v
     * @param position
     */
    @Override
    public void onCommentsClick(View v, int position) {

        Intent intent = new Intent(MainActivity.this, CommentsActivity.class);

        //获取点击view的位置,传递给CommentsActivity 使其 expand effect 有起点
        int[] clickLocation = new int[2];
        v.getLocationOnScreen(clickLocation);
        intent.putExtra(CommentsActivity.ARG_DRAWING_START_LOCATION, clickLocation[1]);
        startActivity(intent);
        //此方法去掉activity切换的默认动画
        overridePendingTransition(0, 0);
    }

    /**
     * more点击事件
     *
     * @param v
     * @param position
     */
    @Override
    public void onMoreClick(View v, int position) {
        FeedContextMenuManager.getInstance().toggleContextMenuFromView(v, position, this);
    }

    /**
     * 头像点击事件*
     * @param v
     */
    @Override
    public void onProfileClick(View v) {
        int[] startLocation = new int[2];
        v.getLocationOnScreen(startLocation);
        startLocation[0] += v.getWidth() / 2; //微调点击的坐标
        UserProfileActivity.startUserProfileFromLocation(startLocation,this);
        overridePendingTransition(0,0);
    }

    /**
     * FAB点击进行拍照处理
     */
    @OnClick(R.id.ibtCamera)
    public void onTakePhotoClick(){
        int[] startingLocation = new int[2];
        ibtCamera.getLocationOnScreen(startingLocation);
        startingLocation[0] += ibtCamera.getWidth() / 2;
        TakePhotoActivity.startCameraFromLocation(startingLocation, this);
        overridePendingTransition(0, 0);
    }

    /*************************ContextMenuView的点回调方法******************************/
    @Override
    public void onReportClick(int feedItem) {
        FeedContextMenuManager.getInstance().hideContextMenu();
    }

    @Override
    public void onSharePhotoClick(int feedItem) {
        FeedContextMenuManager.getInstance().hideContextMenu();
    }

    @Override
    public void onCopyShareUrlClick(int feedItem) {
        FeedContextMenuManager.getInstance().hideContextMenu();
    }

    @Override
    public void onCancelClick(int feedItem) {
        FeedContextMenuManager.getInstance().hideContextMenu();
    }
}
