package com.bruce.materialapp.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.bruce.materialapp.R;
import com.bruce.materialapp.util.Utils;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Bruce
 * On 2015/3/11
 * At 18:35
 * About MaterialApp
 */
public class FeedContextMenu extends LinearLayout {

    private static final int CONTEXT_MENU_WIDTH = Utils.dpToPx(240); //context menu宽
    private int feedItem = -1;
    private OnFeedContextMenuItemClickListener onItemClickListener;
    
    public FeedContextMenu(Context context) {
        super(context);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_context_menu, this, true);
        setBackgroundResource(R.drawable.bg_container_shadow);
        setOrientation(VERTICAL); //LinearLayout必须设置方向和LayoutParams
        setLayoutParams(new LayoutParams(CONTEXT_MENU_WIDTH, LayoutParams.WRAP_CONTENT));
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        //attach到window的时候注入注解
        ButterKnife.inject(this);
    }

    /**
     * 绑定数据（实际情况会很复杂）
     * @param feedItem
     */
    public void bindToItem(int feedItem) {
        this.feedItem = feedItem;
    }
    

    /**
     * 从父布局中移除该布局* 
     */
    public void dismiss() {
        ((ViewGroup) getParent()).removeView(FeedContextMenu.this);
    }
    
    @OnClick(R.id.btnReport)
    public void onReportClick(){
        if(onItemClickListener != null){
            onItemClickListener.onReportClick(feedItem);
        }
    }


    @OnClick(R.id.btnSharePhoto)
    public void onSharePhotoClick() {
        if (onItemClickListener != null) {
            onItemClickListener.onSharePhotoClick(feedItem);
        }
    }

    public void setOnFeedMenuItemClickListener(OnFeedContextMenuItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
    
    @OnClick(R.id.btnCopyShareUrl)
    public void onCopyShareUrlClick() {
        if (onItemClickListener != null) {
            onItemClickListener.onCopyShareUrlClick(feedItem);
        }
    }

    @OnClick(R.id.btnCancel)
    public void onCancelClick() {
        if (onItemClickListener != null) {
            onItemClickListener.onCancelClick(feedItem);
        }
    }
    
    /**
     * ContextMenu中的点击接口
     */
    public interface OnFeedContextMenuItemClickListener {

        public void onReportClick(int feedItem);

        public void onSharePhotoClick(int feedItem);

        public void onCopyShareUrlClick(int feedItem);

        public void onCancelClick(int feedItem);
    }
}
