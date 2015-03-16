package com.bruce.materialapp.view;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.bruce.materialapp.R;
import com.bruce.materialapp.adapter.GlobalMenuAdapter;
import com.bruce.materialapp.transfromation.CircleTransformation;
import com.squareup.picasso.Picasso;

/**
 * Created by Bruce
 * On 2015/3/16
 * At 10:19
 * About MaterialApp
 */
public class GlobalMenuView extends ListView implements View.OnClickListener {

    private int avatarSize; //头像大小
    private String profilePhoto; //头像地址
    private ImageView ivUserProfilePhoto; //头像imageView

    private OnHeaderClickListener onHeaderClickListener; //头部点击事件
    private GlobalMenuAdapter globalMenuAdapter;
    
    public GlobalMenuView(Context context) {
        super(context);
        init();
    }

    private void init() {
        //设置只能单选
        setChoiceMode(CHOICE_MODE_SINGLE);
        //设置分割线
        setDivider(getResources().getDrawable(android.R.color.transparent));
        setDividerHeight(1);
        setBackgroundColor(Color.WHITE);
        
        setupHeader();
        setupAdapter();
    }

    /**
     * ListVIew设置adapter
     */
    private void setupAdapter() {
        globalMenuAdapter = new GlobalMenuAdapter(getContext());
        setAdapter(globalMenuAdapter);
    }

    //设置头部
    private void setupHeader() {
        this.avatarSize = getResources().getDimensionPixelSize(R.dimen.global_menu_avatar_size);
        this.profilePhoto = getResources().getString(R.string.user_profile_photo);

        setHeaderDividersEnabled(true);
        View vHeader = LayoutInflater.from(getContext()).inflate(R.layout.view_global_menu_header, null);
        ivUserProfilePhoto = (ImageView) vHeader.findViewById(R.id.ivUserProfilePhoto);

        Picasso.with(getContext())
                .load(profilePhoto)
                .resize(avatarSize,avatarSize)
                .centerCrop()
                .placeholder(R.drawable.img_circle_placeholder)
                .transform(new CircleTransformation())
                .into(ivUserProfilePhoto);
        //添加HeaderView
        addHeaderView(vHeader);
        vHeader.setOnClickListener(this);
        
    }

    @Override
    public void onClick(View v) {
        if(onHeaderClickListener != null){
            onHeaderClickListener.onGlobalMenuHeaderClick(v);
        }
    }

    public interface OnHeaderClickListener {
        public void onGlobalMenuHeaderClick(View v);
    }

    public void setOnHeaderClickListener(OnHeaderClickListener onHeaderClickListener) {
        this.onHeaderClickListener = onHeaderClickListener;
    }
}


















