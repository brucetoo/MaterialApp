package com.bruce.materialapp.adapter;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bruce.materialapp.R;
import com.bruce.materialapp.transfromation.CircleTransformation;
import com.bruce.materialapp.util.Utils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Bruce
 * On 2015/3/13
 * At 9:34
 * About MaterialApp
 */
public class UserProfileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    //UserProfile 三个不同的Item
    public static final int TYPE_PROFILE_HEADER = 0;
    public static final int TYPE_PROFILE_OPTIONS = 1;
    public static final int TYPE_PHOTO = 2;
    //区别其他两个Item的最小值
    private static final int MIN_ITEMS_COUNT = 2;
    
    //在每次adptar时候如果屏蔽了系统自身的绘制方式，加入自己的动画处理
    //需要lockedAnimations 来判断该 recycleView 是否在滚动，如果滚动停止动画
    private boolean lockedAnimations = false;

    private int lastAnimatedItem = 0;

    private long profileHeaderAnimationStartTime = 0;
    private static final DecelerateInterpolator INTERPOLATOR= new DecelerateInterpolator();
    private static final int USER_HEADER_ANIMATION_TIME = 300;
    private static final int USER_OPTIONS_ANIMATION_DELAY = 300;
    private static final int USER_PHOTO_ANIMATION_DELAY = 600;
    private Context context;
    private int cellSize;
    private int avatarSize;

    private String profilePhoto;
    private List<String> photos;
    
    public UserProfileAdapter(Context context) {
        this.context = context;
        this.cellSize = Utils.getScreenWidth(context) / 3;
        this.avatarSize = context.getResources().getDimensionPixelSize(R.dimen.user_profile_avatar_size);
        this.profilePhoto = context.getString(R.string.user_profile_photo);
        this.photos = Arrays.asList(context.getResources().getStringArray(R.array.user_photos));
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (TYPE_PROFILE_HEADER == viewType) {
            final View view = LayoutInflater.from(context).inflate(R.layout.view_user_profile_header, parent, false);
            /**
             * StaggeredGridLayoutManager 是杂乱错开的grid布局管理,
             * setFullSpan() 设置 view 是否占整个一个 row　width
             */
            StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams();
            layoutParams.setFullSpan(true);
            view.setLayoutParams(layoutParams);
            return new ProfileHeaderViewHolder(view);
        } else if (TYPE_PROFILE_OPTIONS == viewType) {
            final View view = LayoutInflater.from(context).inflate(R.layout.view_user_profile_options, parent, false);
            StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams();
            layoutParams.setFullSpan(true); //使当前item覆盖整个行
            view.setLayoutParams(layoutParams);
            return new ProfileOptionsViewHolder(view);
        } else if (TYPE_PHOTO == viewType) {
            final View view = LayoutInflater.from(context).inflate(R.layout.item_photo, parent, false);
            //加入以下代码更严谨的将 view分为了屏幕的1/3
//            StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams();
//            layoutParams.height = cellSize;
//            layoutParams.width = cellSize;
//            layoutParams.setFullSpan(false);
//            view.setLayoutParams(layoutParams);
            return new NormolViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        if (TYPE_PROFILE_HEADER == viewType) {
            bindProfileHeader((ProfileHeaderViewHolder) holder);
        } else if (TYPE_PROFILE_OPTIONS == viewType) {
            bindProfileOptions((ProfileOptionsViewHolder) holder);
        } else if (TYPE_PHOTO == viewType) {
            bindPhoto((NormolViewHolder) holder, position);
        }
    }

    /**
     * Header item的数据填充 
     * @param holder
     */
    private void bindProfileHeader(final ProfileHeaderViewHolder holder) {
        Picasso.with(context)
                .load(profilePhoto)
                .placeholder(R.drawable.img_circle_placeholder)
                .resize(avatarSize, avatarSize)
                .centerCrop()
                .transform(new CircleTransformation())
                .into(holder.ivUserProfilePhoto);
        //在执行完revealView后，draw vUserProfileRoot
        //在draw vUserProfileRoot前，取消系统默认的绘制方式
        holder.vUserProfileRoot.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                holder.vUserProfileRoot.getViewTreeObserver().removeOnPreDrawListener(this);
                animateUserProfileHeader(holder);
                return false;
            }
        });
    }

    /**
     * * UserProfileHeader的动画
     * @param holder
     */
    private void animateUserProfileHeader(ProfileHeaderViewHolder holder) {
        if(!lockedAnimations){
           profileHeaderAnimationStartTime = System.currentTimeMillis(); //记录开始时间
            //重新设置每个需要动画的初始位置/状态
            holder.vUserProfileRoot.setTranslationY(-holder.vUserProfileRoot.getHeight());
            holder.ivUserProfilePhoto.setTranslationY(-holder.ivUserProfilePhoto.getHeight());
            holder.vUserDetails.setTranslationY(-holder.vUserDetails.getHeight());
            holder.vUserStats.setAlpha(0);
            //1.整个userProfileHeader动画
            ObjectAnimator userProfileRoot = ObjectAnimator.ofFloat(holder.vUserProfileRoot,"translationY",-holder.vUserProfileRoot.getHeight(),0);
            userProfileRoot.setDuration(USER_HEADER_ANIMATION_TIME);
            userProfileRoot.setInterpolator(INTERPOLATOR);
            userProfileRoot.start(); //不延迟
            //2.hear中的userAvator动画
            ObjectAnimator userProfilePhoto = ObjectAnimator.ofFloat(holder.ivUserProfilePhoto,"translationY",-holder.ivUserProfilePhoto.getHeight(),0);
            userProfilePhoto.setDuration(USER_HEADER_ANIMATION_TIME);
            userProfilePhoto.setInterpolator(INTERPOLATOR);
            userProfilePhoto.setStartDelay(100); //延迟100MS
            userProfilePhoto.start();
            //3.hear中的userDetails动画
            ObjectAnimator userDetails = ObjectAnimator.ofFloat(holder.vUserDetails,"translationY",-holder.vUserDetails.getHeight(),0);
            userDetails.setDuration(USER_HEADER_ANIMATION_TIME);
            userDetails.setInterpolator(INTERPOLATOR);
            userDetails.setStartDelay(200); //延迟200MS
            userDetails.start();
            //4.hear中的userStates动画
            ObjectAnimator userStates = ObjectAnimator.ofFloat(holder.vUserStats, "alpha", 0, 1);
            userStates.setDuration(USER_HEADER_ANIMATION_TIME);
            userStates.setInterpolator(INTERPOLATOR);
            userStates.setStartDelay(400); //延迟300MS
            userStates.start();
        }
    }

    private void bindProfileOptions(final ProfileOptionsViewHolder holder) {
        holder.vButtons.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                holder.vButtons.getViewTreeObserver().removeOnPreDrawListener(this);
                //设置下划线的长度为 imageButton的长度
                holder.vUnderline.getLayoutParams().width = holder.btnGrid.getWidth();
                //在绘制时候 layout 数据变动了，需要调用该方法
                holder.vUnderline.requestLayout();
                animateUserProfileOptions(holder);
                return false;
            }
        });
    }

    /**
     *  UserProfileOptions动画
     * @param holder
     */
    private void animateUserProfileOptions(ProfileOptionsViewHolder holder) {
        if(!lockedAnimations){
            holder.vButtons.setTranslationY(-holder.vButtons.getHeight());
            holder.vUnderline.setScaleX(0);
            
            holder.vButtons.animate().translationY(0).setDuration(USER_HEADER_ANIMATION_TIME)
                    .setStartDelay(USER_HEADER_ANIMATION_TIME+USER_OPTIONS_ANIMATION_DELAY).setInterpolator(INTERPOLATOR).start();
            holder.vUnderline.animate().scaleX(1).setDuration(USER_HEADER_ANIMATION_TIME - 100)
                    .setStartDelay(USER_HEADER_ANIMATION_TIME + USER_OPTIONS_ANIMATION_DELAY).setInterpolator(INTERPOLATOR).start();
            
        }
    }

    private void bindPhoto(final NormolViewHolder holder, int position) {
        Picasso.with(context)
                .load(photos.get(position - MIN_ITEMS_COUNT))
                .resize(cellSize, cellSize)
                .centerCrop()
                .into(holder.ivPhoto, new Callback() {
                    @Override
                    public void onSuccess() {
                        animatePhoto(holder);
                    }

                    @Override
                    public void onError() {

                    }
                });
        if (lastAnimatedItem < position) lastAnimatedItem = position;
    }

    /**
     * 图片动画
     * @param viewHolder
     */
    private void animatePhoto(NormolViewHolder viewHolder) {
        if (!lockedAnimations) {
            //当前的posion已经执行过动画,注释代码对比效果
//            if (lastAnimatedItem == viewHolder.getPosition()) {
//                setLockedAnimations(true);
//            }
            
            long animationDelay = profileHeaderAnimationStartTime + USER_PHOTO_ANIMATION_DELAY - System.currentTimeMillis();
            if (profileHeaderAnimationStartTime == 0) {
                animationDelay = viewHolder.getPosition() * 30 + USER_PHOTO_ANIMATION_DELAY;
            } else if (animationDelay < 0) {  //处理滑动时 时间差为负的情况
                animationDelay = viewHolder.getPosition() * 30;
            } else {
                //每个Item的位置*30 就是其延迟时间
                animationDelay += viewHolder.getPosition() * 30;
            }

            viewHolder.flRoot.setScaleY(0);
            viewHolder.flRoot.setScaleX(0);
            viewHolder.flRoot.animate()
                    .scaleY(1)
                    .scaleX(1)
                    .setDuration(200)
                    .setInterpolator(INTERPOLATOR)
                    .setStartDelay(animationDelay)
                    .start();
        }
    }

    @Override
    public int getItemCount() {
        return MIN_ITEMS_COUNT + photos.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_PROFILE_HEADER;
        } else if (position == 1) {
            return TYPE_PROFILE_OPTIONS;
        } else {
            return TYPE_PHOTO;
        }
    }

    public static class ProfileHeaderViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.ivUserProfilePhoto)
        ImageView ivUserProfilePhoto;
        @InjectView(R.id.vUserDetails)
        View vUserDetails;
        @InjectView(R.id.btnFollow)
        Button btnFollow;
        @InjectView(R.id.vUserStats)
        View vUserStats;
        @InjectView(R.id.vUserProfileRoot)  //UserProfile的root布局
        View vUserProfileRoot;

        public ProfileHeaderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this,itemView);
        }
    }

    public static class ProfileOptionsViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.btnGrid)
        ImageButton btnGrid;
        @InjectView(R.id.btnList)
        ImageButton btnList;
        @InjectView(R.id.btnMap)
        ImageButton btnMap;
        @InjectView(R.id.btnTagged)
        ImageButton btnComments;
        @InjectView(R.id.vUnderline)
        View vUnderline;
        @InjectView(R.id.vButtons)
        View vButtons;

        public ProfileOptionsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this,itemView);
        }
    }

    public static class NormolViewHolder extends RecyclerView.ViewHolder {

        @InjectView(R.id.flRoot)
        FrameLayout flRoot;
        @InjectView(R.id.ivPhoto)
        ImageView ivPhoto;

        public NormolViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this,itemView);
        }
    }

    /**
     * 设置是否锁定动画*
     * @param lockedAnimations
     */
    public void setLockedAnimations(boolean lockedAnimations) {
        this.lockedAnimations = lockedAnimations;
    }
}
