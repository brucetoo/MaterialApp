package com.bruce.materialapp.activity;

import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.bruce.materialapp.R;
import com.bruce.materialapp.util.DrawerLayoutInstaller;
import com.bruce.materialapp.util.Utils;
import com.bruce.materialapp.view.GlobalMenuView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;

/**
 * Created by Bruce
 * On 2015/3/11
 * At 9:42
 * About MaterialApp
 */
public class BaseActivity extends ActionBarActivity implements GlobalMenuView.OnHeaderClickListener {
    
    // Optional except An exception will be thrown if the target view cannot be found
    @Optional
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    
    @Optional
    @InjectView(R.id.ivLogo)
    ImageView ivLogo;

    protected MenuItem inboxMenuItem;
    private DrawerLayout drawerLayout;
    
    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.inject(this);
        setToolBar();
        setupDrawer();
    }

    private void setupDrawer() {
        GlobalMenuView menuView = new GlobalMenuView(this);
        menuView.setOnHeaderClickListener(this);
        /**
         * 只支持像drawer_root一样 DrawerLayout作为根节点，且只有两个子view的情况
         * 实际情况也是这样的居多* *
         */
        drawerLayout = DrawerLayoutInstaller.from(this)
                .drawerRoot(R.layout.drawer_root)
                .drawerLeftView(menuView)
                .drawerLeftWidth(Utils.dpToPx(300))
                .withNavigationIconToggler(toolbar)
                .build();
    }

    private void setToolBar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu_white);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        //以下去除了 toolbar item点击时的背景高度和 toolbar的高度不匹配
        inboxMenuItem = menu.findItem(R.id.action_inbox);
        inboxMenuItem.setActionView(R.layout.menu_item_view);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * drawerlayout头部点击事件
     * @param v
     */
    @Override
    public void onGlobalMenuHeaderClick(View v) {
        //关闭drawerlayout
        drawerLayout.closeDrawer(Gravity.START);
        //启动个人页
        int[] startLocation = new int[2];
        v.getLocationOnScreen(startLocation);
        startLocation[1] += v.getHeight() / 2; //微调点击的坐标
        UserProfileActivity.startUserProfileFromLocation(startLocation,this);
        overridePendingTransition(0,0);
    }
}
