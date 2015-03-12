package com.bruce.materialapp.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextSwitcher;

import com.bruce.materialapp.R;
import com.bruce.materialapp.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by n1007 on 2015/3/10.
 */
public class FeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {
    private static final int ANIMATED_ITEMS_COUNT = 1; //动画执行的最少Item数
    private int lastAnimatedPosition = -1; //上一个执行动画的位置
    private int itemsCount = 0;
    private Context context;

    private OnFeedItemClickListener onFeedItemClickListener;
    //记录每个item对应的likesCount数
    private final Map<Integer, Integer> likesCount = new HashMap<>();
    //记录每个item 的holder中 like对应的动画集
    private final Map<RecyclerView.ViewHolder, AnimatorSet> likeAnimations = new HashMap<>();
    //已经点赞的item
    private final ArrayList<Integer> likedPositions = new ArrayList<>();

    public FeedAdapter(Context context) {
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_feed, viewGroup, false);
        return new FeedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {

        //绑定数据的时候执行动画
        runEnterAnimation(viewHolder.itemView, i);

        FeedViewHolder holder = (FeedViewHolder) viewHolder;
        if (i % 2 == 0) {
            holder.ivFeedCenter.setImageResource(R.drawable.img_feed_center_1);
            holder.ivFeedBottom.setImageResource(R.drawable.img_feed_bottom_1);
        } else {
            holder.ivFeedCenter.setImageResource(R.drawable.img_feed_center_2);
            holder.ivFeedBottom.setImageResource(R.drawable.img_feed_bottom_2);
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
     * @param holder
     */
    private void animatePhotoLike(final FeedViewHolder holder){
        
        if(!likeAnimations.containsKey(holder)){
            //图片上的 动画图 显示
            holder.vBgLike.setVisibility(View.VISIBLE);
            holder.ivLike.setVisibility(View.VISIBLE);

            AnimatorSet animatorSet = new AnimatorSet();
            likeAnimations.put(holder,animatorSet);

            //背景图 从小变大
            ObjectAnimator bgScaleYAnim = ObjectAnimator.ofFloat(holder.vBgLike,"scaleY",0.1f,1f);
            bgScaleYAnim.setDuration(500);
            bgScaleYAnim.setInterpolator(new DecelerateInterpolator());
            ObjectAnimator bgScaleXAnim = ObjectAnimator.ofFloat(holder.vBgLike,"scaleX",0.1f,1f);
            bgScaleXAnim.setDuration(500);
            bgScaleXAnim.setInterpolator(new DecelerateInterpolator());
            
            //背景图 从不透明变全透明
            ObjectAnimator bgAlphaAnim = ObjectAnimator.ofFloat(holder.vBgLike,"alpha",1f,0f);
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
                    likedPositions.remove((Object)holder.getPosition());
                    updateLikesCounter(holder, true, true);
                    updateHeartButton(holder,false); //不执行动画直接变灰
                }
            } else if (v.getId() == R.id.btnMore) {
                onFeedItemClickListener.onMoreClick(v, (Integer) v.getTag());
            }else if(v.getId() == R.id.ivFeedCenter){
                FeedViewHolder holder = (FeedViewHolder) v.getTag();
                if (!likedPositions.contains(holder.getPosition())) { //如果没赞过
                    likedPositions.add(holder.getPosition());
                    updateLikesCounter(holder, true, false);
                    //点击图片的时候就不执行心形button的动画，只是改变其图片
                    updateHeartButton(holder, false);  
                    animatePhotoLike(holder);
                } else { //如果赞过
                    likedPositions.remove((Object)holder.getPosition());
                    updateLikesCounter(holder, true, true);
                    updateHeartButton(holder,false); //不执行动画直接变灰
                }
            }

        }
    }


    /**
     * 点击事件接口
     */
    public interface OnFeedItemClickListener {
        public void onCommentsClick(View v, int position);

        public void onMoreClick(View v, int position);
    }
}
