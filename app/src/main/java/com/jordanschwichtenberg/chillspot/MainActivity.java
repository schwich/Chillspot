package com.jordanschwichtenberg.chillspot;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.SupportMapFragment;
import com.jordanschwichtenberg.chillspot.sync.ChillspotSyncAdapter;


public class MainActivity extends ActionBarActivity implements EventListFragment.Callback, YourEventFragment.Callback {
    // removed implements ActionBar.TabListener

    AppSectionsPagerAdapter mAppSectionsPagerAdapter;
    ViewPager mViewPager;
    SharedPreferences mSharedPreferences;

    private SupportMapFragment mMapFragment;
    private Fragment mEventListFragment;
    private Fragment mYourEventFragment;

    //private final ActionBar mActionBar;

    public static final int MAP_TAB_INDEX = 0;
    public static final int EVENT_LIST_TAB_INDEX = 1;
    public static final int YOUR_EVENT_TAB_INDEX = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //SharedPreferences settings = getPreferences(0);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        //SharedPreferences.Editor settingsEditor = settings.edit();
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt("user_id", 1);
        editor.commit();


        mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager());

        final ActionBar actionBar = getSupportActionBar();
        //mActionBar = getSupportActionBar();

        actionBar.setHomeButtonEnabled(false);

        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        /*mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mAppSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        for (int i = 0; i < mAppSectionsPagerAdapter.getCount(); i++) {
            actionBar.addTab(actionBar.newTab()
                    .setText(mAppSectionsPagerAdapter.getPageTitle(i))
                    .setTabListener(this));
        }*/

        /**
         * Map Tab
         */
        ActionBar.Tab tab = actionBar.newTab()
                .setText("Nearby")
                .setTabListener(new ActionBar.TabListener() {
                    @Override
                    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
                        if (mMapFragment == null) {
                            mMapFragment = new MapFragment();
                            fragmentTransaction.add(R.id.container, mMapFragment, "your_map");
                        } else {
                            fragmentTransaction.attach(mMapFragment);
                        }
                    }

                    @Override
                    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

                        if (mMapFragment != null) {
                            fragmentTransaction.detach(mMapFragment);
                        }
                    }

                    @Override
                    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

                    }
                });

        actionBar.addTab(tab);

        /**
         * Event List Tab
         */
        tab = actionBar.newTab()
                .setText("Browse")
                .setTabListener(new ActionBar.TabListener() {
                    @Override
                    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
                        if (mEventListFragment == null) {
                            mEventListFragment = new EventListFragment();
                            fragmentTransaction.add(R.id.container, mEventListFragment, "event_list");
                        } else {
                            fragmentTransaction.attach(mEventListFragment);
                        }
                    }

                    @Override
                    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

                        if (mEventListFragment != null) {
                            fragmentTransaction.detach(mEventListFragment);
                        }
                    }

                    @Override
                    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

                    }
                });

        actionBar.addTab(tab);

        /**
         * Your Event Tab
         */
        tab = actionBar.newTab()
                .setText("Your Event")
                .setTabListener(new ActionBar.TabListener() {
                    @Override
                    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
                        if (mYourEventFragment == null) {
                            mYourEventFragment = new YourEventFragment();
                            fragmentTransaction.add(R.id.container, mYourEventFragment, "your_event");
                        } else {
                            fragmentTransaction.attach(mYourEventFragment);
                        }
                    }

                    @Override
                    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

                        if (mYourEventFragment != null) {
                            fragmentTransaction.detach(mYourEventFragment);
                        }
                    }

                    @Override
                    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
                        fragmentTransaction.detach(mYourEventFragment);
                        fragmentTransaction.attach(mYourEventFragment);
                    }
                });

        actionBar.addTab(tab);

        // which tab to select
        Intent intent = getIntent();
        int tabToOpen = intent.getIntExtra("tabPosition", -1);
        if (tabToOpen != -1) {
            // open the specific tab
            actionBar.setSelectedNavigationItem(tabToOpen);
        }

        ChillspotSyncAdapter.initializeSyncAdapter(this);
    }

    @Override
    public void refreshYourEventView() {
        YourEventFragment.updateViewFlag = true;
        getSupportActionBar().setSelectedNavigationItem(EVENT_LIST_TAB_INDEX);
        getSupportActionBar().setSelectedNavigationItem(YOUR_EVENT_TAB_INDEX);

    }

    @Override
    public void onItemSelected(Uri eventUri) {
        Intent intent = new Intent(this, EventDetailActivity.class)
                .setData(eventUri);
        startActivity(intent);
    }


    /*@Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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

    public static class AppSectionsPagerAdapter extends FragmentPagerAdapter {

        public AppSectionsPagerAdapter(android.support.v4.app.FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {

                // map fragment
                case 0:

                    SupportMapFragment mapFragment = new MapFragment();
                    return mapFragment;

                // event list fragment
                case 1:
                    Fragment listFragment = new EventListFragment();
                    return listFragment;

                // your event fragment
                case 2:
                    Fragment yourEventFragment = new YourEventFragment();
                    return yourEventFragment;

                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "nearby";
                case 1:
                    return "browse";
                case 2:
                    return "your event";
                default:
                    return null;
            }
        }
    }
}
