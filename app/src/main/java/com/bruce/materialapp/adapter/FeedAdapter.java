package com.bruce.materialapp.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bruce.materialapp.R;
import com.bruce.materialapp.util.Utils;
import com.bruce.materialapp.view.SquaredImageView;

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
        holder.btnLike.setTag(i);

        //more点击事件
        holder.btnMore.setOnClickListener(this);
        holder.btnMore.setTag(i);
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
        SquaredImageView ivFeedCenter;
        @InjectView(R.id.ivFeedBottom)
        ImageView ivFeedBottom;
        @InjectView(R.id.btnComments)
        ImageButton btnComments;
        @InjectView(R.id.btnMore)
        ImageButton btnMore;
        @InjectView(R.id.btnLike)
        ImageButton btnLike;

        public FeedViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }
    }


    public void updateItems() {
        itemsCount = 20;
        notifyDataSetChanged();
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
                onFeedItemClickListener.onLikeClick(v, (Integer) v.getTag());
            }else if(v.getId() == R.id.btnMore){
                onFeedItemClickListener.onMoreClick(v, (Integer) v.getTag());
            }

        }
    }


    /**
     * 点击事件接口
     */
    public interface OnFeedItemClickListener {
        public void onCommentsClick(View v, int position);

        public void onLikeClick(View v, int position);

        public void onMoreClick(View v, int position);
    }
}
