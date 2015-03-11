package com.bruce.materialapp.activity;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.bruce.materialapp.R;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;

/**
 * Created by Bruce
 * On 2015/3/11
 * At 9:42
 * About MaterialApp
 */
public class BaseActivity extends ActionBarActivity {
    
    // Optional except An exception will be thrown if the target view cannot be found
    @Optional
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    
    @Optional
    @InjectView(R.id.ivLogo)
    ImageView ivLogo;

    protected MenuItem inboxMenuItem;
    
    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.inject(this);
        setToolBar();
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
}
