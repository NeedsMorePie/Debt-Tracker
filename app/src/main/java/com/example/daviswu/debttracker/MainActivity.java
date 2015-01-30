package com.example.daviswu.debttracker;

import android.app.Activity;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.transition.ChangeImageTransform;
import android.transition.Explode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Email;

import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private int currentIndex = 0;
    private String dir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        dir = Environment.getExternalStorageDirectory().toString();

        // transition settings
        getWindow().setExitTransition(new Explode());
        getWindow().setEnterTransition(new Explode());
        getWindow().setAllowEnterTransitionOverlap(false);
        getWindow().setAllowReturnTransitionOverlap(false);

        Thread thread = new Thread() {
            @Override
            public void run() {
                Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.money);
                File file = new File(dir, "money.jpg");
                try {
                    FileOutputStream outStream = new FileOutputStream(file);
                    bm.compress(Bitmap.CompressFormat.JPEG, 60, outStream);
                    outStream.flush();
                    outStream.close();
                }
                catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
        thread.start();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        Fragment fragment = null;
        FragmentManager fragmentManager = getFragmentManager();
        switch(position) {
            case 0:
                fragment = new AllDebtFragment();
                currentIndex = 0;
                fragmentManager.beginTransaction()
                        .replace(R.id.container, fragment, "tag")
                        .commit();
                break;
            case 1:
                fragment = new MyDebtFragment();
                currentIndex = 1;
                break;
            case 2:
                fragment = new OthersDebtFragment();
                currentIndex = 2;
                break;
            default:
                break;
        }
        if (fragment != null && position != 0) {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit();
        }
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void delete(int position) {
        FragmentManager fragmentManager = getFragmentManager();
        AllDebtFragment fragment = (AllDebtFragment) fragmentManager.findFragmentByTag("tag");
        fragment.deleteItem(position);
    }

    public void edit(int position, ImageView imageView) {
        FragmentManager fragmentManager = getFragmentManager();
        AllDebtFragment fragment = (AllDebtFragment) fragmentManager.findFragmentByTag("tag");
        fragment.editItem(position, imageView);
    }
}
