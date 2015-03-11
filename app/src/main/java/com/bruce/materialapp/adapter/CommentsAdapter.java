package com.bruce.materialapp.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bruce.materialapp.R;
import com.bruce.materialapp.transfromation.RoundedTransformation;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Bruce
 * On 2015/3/11
 * At 9:42
 * About MaterialApp
 * 此处总结下RecyclerView.Adapter的使用步奏
 * 1. 定义一个类CommentViewHolder extends RecyclerView.ViewHolder
 *  ① 如果不适用注解：只需要再构造方法中找到view 中对应的各个控件
 *  ② 使用注解  在构造方法中加入注解 Like： ButterKnife.inject(this, itemView); 然后在注入控件
 * 2. 在onCreateViewHolder()方法中 inflate view,返回上面定义的类 传入view作为参数
 * 3. 在onBindViewHolder()这个方法中将对应的控件绑定值,
 *    可能会出现对每个item进入动画效果的情况,所有操作也是在这里完成
 */
public class CommentsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private int itemsCount = 0;
    private int lastAnimatedPosition = -1;
    private int avatarSize;
    private boolean animationsLocked = false; //是否执行动画
    private boolean delayEnterAnimation = true;  //动画是否延长执行

    public CommentsAdapter(Context context) {
        this.context = context;
        avatarSize = context.getResources().getDimensionPixelSize(R.dimen.btn_fab_size);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comments, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        //执行每个item动画
        runEnterAnimation(holder.itemView, position);
        //填充数据
        CommentViewHolder viewHolder = (CommentViewHolder) holder;
        switch (position % 3) {
            case 0:
                viewHolder.tvComment.setText("This is comment item 1 !!!!");
                break;
            case 1:
                viewHolder.tvComment.setText("This is comment item 2 !!!!");
                break;
            case 2:
                viewHolder.tvComment.setText("This is comment item 3 !!!!");
                break;
        }

        Picasso.with(context)
                .load(R.mipmap.ic_launcher)
                .centerCrop()
                .resize(avatarSize, avatarSize)          //图片大小设置
                .transform(new RoundedTransformation()) //图片转换
                .into(viewHolder.ivUserAvatar);
    }

    /**
     * * 执行每个item动画
     *
     * @param itemView
     * @param position
     */
    private void runEnterAnimation(View itemView, int position) {
        if (animationsLocked) return;

        if (position > lastAnimatedPosition) {
            lastAnimatedPosition = position;
            itemView.setTranslationY(100);
            itemView.setAlpha(0.1f);
            itemView.animate()
                    .translationY(0)
                    .alpha(1f)
                    .setDuration(300)
                    //20 * position 这个的用意是让每个item 根据他的位置来决定它执行的延迟时间
                    .setStartDelay(delayEnterAnimation ? 20 * position : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            animationsLocked = true; //执行动画完后锁定
                        }
                    });
        }
    }

    @Override
    public int getItemCount() {
        return itemsCount;
    }

    public void updateItems() {
        itemsCount = 10;
        notifyDataSetChanged();
    }

    /**
     * 增加item 
     */
    public void addItem() {
        itemsCount++;
        notifyItemInserted(itemsCount - 1);
    }
    /**
     * 定义CommentViewHolder类
     */
    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.ivUserAvatar)
        ImageView ivUserAvatar;
        @InjectView(R.id.tvComment)
        TextView tvComment;

        public CommentViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }
    }

    /**
     * 对外抛出方法设置动画是否播放 
     * @param animationsLocked
     */
    public void setAnimationsLocked(boolean animationsLocked) {
        this.animationsLocked = animationsLocked;
    }

    /**
     * 对外抛出方法设置 播放item 是否根据位置不同而延迟不同
     * @param delayEnterAnimation
     */
    public void setDelayEnterAnimation(boolean delayEnterAnimation) {
        this.delayEnterAnimation = delayEnterAnimation;
    }
}