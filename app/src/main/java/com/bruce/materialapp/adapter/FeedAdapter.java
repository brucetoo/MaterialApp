package com.bruce.materialapp.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextSwitcher;

import com.bruce.materialapp.R;
import com.bruce.materialapp.util.Utils;
import com.bruce.materialapp.view.SendingProgressView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by n1007 on 2015/3/10.
 * RecyclerView的更新特新---局部更新,对单个Item的增加，删除，更新三个状态进行更新*
 * notifyItemChanged()  //只更新某个item的变化
 * notifyItemInserted()*
 * 关于局部更新的一个优化方案
 * 优化
   虽然只更新单个item，不会造成闪烁，但是，如果单个item都很复杂，
   比如，item中需要从网络上加载图片等等。为了避免多次刷新照成的闪烁，
   我们可以在加载的时候，为ImageView设置一个Tag，比如imageView.setTag(image_url),
   下一次再加载之前，首先获取Tag，比如imageUrl = imageView.getTag(),如果此时的地址和之前的地址一样，
   我们就不需要加载了，如果不一样，再加载。* * 
 */
public class FeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {
    private static final int ANIMATED_ITEMS_COUNT = 1; //动画执行的最少Item数
    private int lastAnimatedPosition = -1; //上一个执行动画的位置
    private int itemsCount = 0;
    private Context context;
    //recyclerView item的两种type,这里有些不规范
    private static final int VIEW_TYPE_DEFAULT = 1;
    private static final int VIEW_TYPE_LOADER = 2;

    private OnFeedItemClickListener onFeedItemClickListener;
    //记录每个item对应的likesCount数
    private final Map<Integer, Integer> likesCount = new HashMap<>();
    //记录每个item 的holder中 like对应的动画集
    private final Map<RecyclerView.ViewHolder, AnimatorSet> likeAnimations = new HashMap<>();
    //已经点赞的item
    private final ArrayList<Integer> likedPositions = new ArrayList<>();
    //judge item is LoadingView or not
    private boolean showLoadingView = false;
    // intent photo Uri
    private Uri photoUri;

    public FeedAdapter(Context context) {
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        /**
         * 在此解释下之用一个Item的原因
         * 第一个item只是在发布的时候才会出现动画效果，其实所有的显示东西都基本是差不多的
         * 因此只需要用一个布局文件就OK了， 只需要在布局文件中控制显示和隐藏Loading的视图就OK
         */
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_feed_loading, viewGroup, false);
//        if(i == VIEW_TYPE_DEFAULT) {
//            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_feed, viewGroup, false);
//        }else if(i==VIEW_TYPE_LOADER){
//            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_feed_loading,viewGroup,false);
//        }
        return new FeedViewHolder(view);
    }


    /**
     * 对外抛出何时需要显示Loading view的方法
     * showLoadingView
     * @param photoUri
     */
    public void showLoadingView(Uri photoUri) {
        showLoadingView = true;
        this.photoUri = photoUri;
      //  notifyItemInserted(0);
        notifyItemChanged(0); //只更新第一个Item
    }

    /**
     * itemType根据showLoadingView来判断是否是两个Item类型
     * @param position
     * @return
     */
    @Override
    public int getItemViewType(int position) {
        if (showLoadingView) {
            return VIEW_TYPE_LOADER;
        } else {
            return VIEW_TYPE_DEFAULT;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {

        //绑定数据的时候执行动画
        runEnterAnimation(viewHolder.itemView, i);

        final FeedViewHolder holder = (FeedViewHolder) viewHolder;
        if (getItemViewType(i) == VIEW_TYPE_DEFAULT) {
            holder.flLoadingRoot.setVisibility(View.GONE);
            if (i % 2 == 0) {
                holder.ivFeedCenter.setImageResource(R.drawable.img_feed_center_1);
                holder.ivFeedBottom.setImageResource(R.drawable.img_feed_bottom_1);
            } else {
                holder.ivFeedCenter.setImageResource(R.drawable.img_feed_center_2);
                holder.ivFeedBottom.setImageResource(R.drawable.img_feed_bottom_2);
            }
        } else if (getItemViewType(i) == VIEW_TYPE_LOADER) { //显示LoadingVIew的逻辑
            Picasso.with(context).load(photoUri).into(holder.ivFeedCenter);
       //     Log.i("picasso-photouri:",photoUri);
         //   holder.ivFeedCenter.setImageResource(R.drawable.img_feed_center_2);
            holder.ivFeedBottom.setImageResource(R.drawable.img_feed_bottom_2);
            holder.flLoadingRoot.setVisibility(View.VISIBLE);
            //remove sendingprogress's OnPreDrawListener
            holder.vSendingProgress.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    holder.vSendingProgress.getViewTreeObserver().removeOnPreDrawListener(this);
                    //simulate Progerss running
                    holder.vSendingProgress.simulateProgress();
                    return false;
                }
            });
            holder.vSendingProgress.setOnLoadingFinishedListener(new SendingProgressView.OnLoadingFinishedListener() {
                @Override
                public void onLoadingFinished() {
                    holder.vSendingProgress.animate().scaleY(0).scaleX(0).setDuration(200).setStartDelay(100);
                    holder.flLoadingRoot.animate().alpha(0).setDuration(200).setStartDelay(100)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    holder.vSendingProgress.setScaleX(0.1f);
                                    holder.vSendingProgress.setScaleY(0.1f);
                                    holder.vSendingProgress.setAlpha(1);
                                    showLoadingView = false;
                                    //   notifyItemChanged(0); //在第一条上插入数据
                                }
                            })
                            .start();
                }
            });
        }

        //comment点击事件
        holder.btnComments.setOnClickListener(this);
        holder.btnComments.setTag(i);

        //like点击事件
        holder.btnLike.setOnClickListener(this);
        holder.btnLike.setTag(holder); //此次tag传入的是holder

        //more点击事件
        holder.btnMore.setOnClickListener(this);
        holder.btnMore.setTag(i);

        //头像点击事件
        holder.ivUserProfile.setOnClickListener(this);

        //图片的点击事件
        holder.ivFeedCenter.setOnClickListener(this);
        holder.ivFeedCenter.setTag(holder);

        //textSwicher 123 Likes 的值设置
        updateLikesCounter(holder, false, false);
        updateHeartButton(holder, false);

        if (likeAnimations.containsKey(holder)) {
            likeAnimations.get(holder).cancel();
        }
        resetLikeAnimationState(holder);
    }

    /**
     * 更新likeCounter
     *
     * @param holder
     * @param animated 是否切换likeCount数据
     * @param isLiked  是否已经赞过
     */
    private void updateLikesCounter(FeedViewHolder holder, boolean animated, boolean isLiked) {
        int currentLikeCount = 0;
        if (!isLiked) {
            currentLikeCount = likesCount.get(holder.getPosition()) + 1;
        } else {
            currentLikeCount = likesCount.get(holder.getPosition()) - 1;
        }
        // 使用复数的表达形式赋值currentLikeCount
        String likesCountText =
                context.getResources().getQuantityString(R.plurals.likes_count, currentLikeCount, currentLikeCount);

        if (animated) {  // 需要动态改变下一个text的值
            holder.tsLikesCounter.setText(likesCountText);
        } else {       //不需要改变下一个text值
            holder.tsLikesCounter.setCurrentText(likesCountText);
        }

        //保存对应位置的 LikeCount数量
        likesCount.put(holder.getPosition(), currentLikeCount);
    }


    /**
     * 更新 HeardButton
     *
     * @param holder
     * @param animated 是否需要显示动画
     */
    private void updateHeartButton(final FeedViewHolder holder, boolean animated) {
        if (animated) {

            if (!likeAnimations.containsKey(holder)) { //如果该holder 对应的动画不存在
                AnimatorSet animatorSet = new AnimatorSet();
                likeAnimations.put(holder, animatorSet);

                //先旋转
                ObjectAnimator rotateAnim = ObjectAnimator.ofFloat(holder.btnLike, "rotation", 0f, 360f);
                rotateAnim.setDuration(300);
                rotateAnim.setInterpolator(new AccelerateInterpolator());

                //再先缩小 后 放大
                ObjectAnimator bounceAnimY = ObjectAnimator.ofFloat(holder.btnLike, "scaleY", 0.2f, 1f);
                bounceAnimY.setDuration(300);
                bounceAnimY.setInterpolator(new OvershootInterpolator());

                ObjectAnimator bounceAnimX = ObjectAnimator.ofFloat(holder.btnLike, "scaleX", 0.2f, 1f);
                bounceAnimX.setDuration(300);
                bounceAnimX.setInterpolator(new OvershootInterpolator());
                //执行拉伸动画前，先将背景图换成红色
                bounceAnimX.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        holder.btnLike.setImageResource(R.drawable.ic_heart_red);
                    }
                });

                animatorSet.play(rotateAnim);
                animatorSet.play(bounceAnimY).with(bounceAnimX).after(rotateAnim);
                //执行完动画后,将其移除
                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        resetLikeAnimationState(holder);
                    }
                });

                animatorSet.start();
            }

        } else {  //不要显示动画
            if (likedPositions.contains(holder.getPosition())) {  //如果已经操作点赞过
                holder.btnLike.setImageResource(R.drawable.ic_heart_red);
            } else {
                holder.btnLike.setImageResource(R.drawable.ic_heart_outline_grey);
            }
        }
    }

    private void resetLikeAnimationState(FeedViewHolder holder) {
        likeAnimations.remove(holder);
//        holder.vBgLike.setVisibility(View.GONE);
//        holder.ivLike.setVisibility(View.GONE);
    }


    /**
     * 点击 图片的响应 和 点击赞心形图片的动画形同，保存在同一个map 里面 likeAnimations
     *
     * @param holder
     */
    private void animatePhotoLike(final FeedViewHolder holder) {

        if (!likeAnimations.containsKey(holder)) {
            //图片上的 动画图 显示
            holder.vBgLike.setVisibility(View.VISIBLE);
            holder.ivLike.setVisibility(View.VISIBLE);

            AnimatorSet animatorSet = new AnimatorSet();
            likeAnimations.put(holder, animatorSet);

            //背景图 从小变大
            ObjectAnimator bgScaleYAnim = ObjectAnimator.ofFloat(holder.vBgLike, "scaleY", 0.1f, 1f);
            bgScaleYAnim.setDuration(500);
            bgScaleYAnim.setInterpolator(new DecelerateInterpolator());
            ObjectAnimator bgScaleXAnim = ObjectAnimator.ofFloat(holder.vBgLike, "scaleX", 0.1f, 1f);
            bgScaleXAnim.setDuration(500);
            bgScaleXAnim.setInterpolator(new DecelerateInterpolator());

            //背景图 从不透明变全透明
            ObjectAnimator bgAlphaAnim = ObjectAnimator.ofFloat(holder.vBgLike, "alpha", 1f, 0f);
            bgAlphaAnim.setDuration(500);
            bgAlphaAnim.setInterpolator(new DecelerateInterpolator());

            //背景Like 图片从小变大
            ObjectAnimator imgScaleUpYAnim = ObjectAnimator.ofFloat(holder.ivLike, "scaleY", 0.1f, 1f);
            imgScaleUpYAnim.setDuration(500);
            imgScaleUpYAnim.setInterpolator(new DecelerateInterpolator());
            ObjectAnimator imgScaleUpXAnim = ObjectAnimator.ofFloat(holder.ivLike, "scaleX", 0.1f, 1f);
            imgScaleUpXAnim.setDuration(500);
            imgScaleUpXAnim.setInterpolator(new DecelerateInterpolator());

            //背景LIke 图片从大变小 到消失
            ObjectAnimator imgScaleDownYAnim = ObjectAnimator.ofFloat(holder.ivLike, "scaleY", 1f, 0f);
            imgScaleDownYAnim.setDuration(500);
            imgScaleDownYAnim.setInterpolator(new DecelerateInterpolator());
            ObjectAnimator imgScaleDownXAnim = ObjectAnimator.ofFloat(holder.ivLike, "scaleX", 1f, 0f);
            imgScaleDownXAnim.setDuration(500);
            imgScaleDownXAnim.setInterpolator(new DecelerateInterpolator());

            animatorSet.playTogether(bgScaleYAnim, bgScaleXAnim, bgAlphaAnim, imgScaleUpYAnim, imgScaleUpXAnim);
            animatorSet.play(imgScaleDownXAnim).with(imgScaleDownYAnim).after(imgScaleUpXAnim);
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    resetLikeAnimationState(holder);
                }
            });
            animatorSet.start();
        }
    }

    @Override
    public int getItemCount() {
        return itemsCount;
    }

    /**
     * 每个item 进入的时候执行的动画
     *
     * @param itemView
     * @param position
     */
    private void runEnterAnimation(View itemView, int position) {
        //只有一个Item 不执行动画
        if (position >= ANIMATED_ITEMS_COUNT) {
            return;
        }

        //item执行动画条件是:现在item的位置大于上次动画item的位置
        if (position > lastAnimatedPosition) {
            lastAnimatedPosition = position;
            //事先设置itemview的高度
            itemView.setTranslationY(Utils.getScreenHeight(context));
            itemView.animate()
                    .translationY(0)
                    .setDuration(700)
                    .setInterpolator(new DecelerateInterpolator(3.f))
                    .start();
        }
    }


    /**
     * FeedViewHolder 是找到且设置对应的view
     */
    public static class FeedViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.ivFeedCenter)
        ImageView ivFeedCenter;
        @InjectView(R.id.ivFeedBottom)
        ImageView ivFeedBottom;
        @InjectView(R.id.btnComments)
        ImageButton btnComments;
        @InjectView(R.id.btnMore)
        ImageButton btnMore;
        @InjectView(R.id.btnLike)
        ImageButton btnLike;
        @InjectView(R.id.tsLikesCounter)
        TextSwitcher tsLikesCounter;
        @InjectView(R.id.vBgLike)
        View vBgLike;
        @InjectView(R.id.ivLike)
        ImageView ivLike;
        @InjectView(R.id.ivUserProfile)
        ImageView ivUserProfile;

        @InjectView(R.id.flLoadingRoot)
        View flLoadingRoot;
        @InjectView(R.id.vSendingProgress)
        SendingProgressView vSendingProgress;

        public FeedViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }
    }


    public void updateItems() {
        itemsCount = 20;
        fillLikesWithRandomValues();
        notifyDataSetChanged();
    }

    /**
     * 模拟填充每个Item 的 Likes
     */
    private void fillLikesWithRandomValues() {
        for (int i = 0; i < getItemCount(); i++) {
            likesCount.put(i, new Random().nextInt(100));
        }
    }

    /**
     * 对外抛出设置点击的事件
     *
     * @param onFeedItemClickListener
     */
    public void setOnFeedItemClickListener(OnFeedItemClickListener onFeedItemClickListener) {
        this.onFeedItemClickListener = onFeedItemClickListener;
    }

    @Override
    public void onClick(View v) {
        if (onFeedItemClickListener != null) {
            if (v.getId() == R.id.btnComments) {
                //v.getTag() 获取点击的Position
                onFeedItemClickListener.onCommentsClick(v, (Integer) v.getTag());
            } else if (v.getId() == R.id.btnLike) {
                FeedViewHolder holder = (FeedViewHolder) v.getTag();
                if (!likedPositions.contains(holder.getPosition())) { //如果没赞过
                    likedPositions.add(holder.getPosition());
                    updateLikesCounter(holder, true, false);
                    updateHeartButton(holder, true);
                } else { //如果赞过
                    likedPositions.remove((Object) holder.getPosition());
                    updateLikesCounter(holder, true, true);
                    updateHeartButton(holder, false); //不执行动画直接变灰
                }
            } else if (v.getId() == R.id.btnMore) {
                onFeedItemClickListener.onMoreClick(v, (Integer) v.getTag());
            } else if (v.getId() == R.id.ivFeedCenter) {
                FeedViewHolder holder = (FeedViewHolder) v.getTag();
                if (!likedPositions.contains(holder.getPosition())) { //如果没赞过
                    likedPositions.add(holder.getPosition());
                    updateLikesCounter(holder, true, false);
                    //点击图片的时候就不执行心形button的动画，只是改变其图片
                    updateHeartButton(holder, false);
                    animatePhotoLike(holder);
                } else { //如果赞过
                    likedPositions.remove((Object) holder.getPosition());
                    updateLikesCounter(holder, true, true);
                    updateHeartButton(holder, false); //不执行动画直接变灰
                }
            } else if (v.getId() == R.id.ivUserProfile) {
                onFeedItemClickListener.onProfileClick(v);
            }

        }
    }


    /**
     * 点击事件接口
     */
    public interface OnFeedItemClickListener {
        public void onCommentsClick(View v, int position);

        public void onMoreClick(View v, int position);

        public void onProfileClick(View v);
    }
}
