package de.spas.freeworkout;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Toast;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import tools.BaseGameActivity;

/**
 * Created by roland on 24.05.2017.
 */

public class WorkoutChooseActivity extends FragmentActivity  implements ActionBar.TabListener {
    private de.spas.freeworkout.workoutPack workoutPack;
    private List<String> WorkoutListArray0 = new ArrayList<String>();
    private List<String> WorkoutListArrayPrint0 = new ArrayList<String>();
    private int workoutCount;
    private ViewPager viewPager;
    private TabsPagerAdapter mAdapter;
    private ActionBar actionBar;
    // Tab titles
    private String[] tabs = { "Endurance", "Standard", "Strength" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workout_choose_activity);

        // Initilization
        viewPager = (ViewPager) findViewById(R.id.pager);
        actionBar = getActionBar();
        mAdapter = new TabsPagerAdapter(getSupportFragmentManager());

        viewPager.setAdapter(mAdapter);
        actionBar.setHomeButtonEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Adding Tabs
        for (String tab_name : tabs) {
            actionBar.addTab(actionBar.newTab().setText(tab_name).setTabListener(this));
        }


        try {
            InputStream source = getAssets().open("workouts.xml");
            Serializer serializer = new Persister();
            workoutPack = serializer.read(de.spas.freeworkout.workoutPack.class, source);
            //Toast.makeText(this, "Wow! Klappt!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Oh oh! workoutPack", Toast.LENGTH_LONG).show();
            Log.e(getClass().getSimpleName(), "loading levels threw exception", e);
        }
        printWorkout();

    }
    public void printWorkout() {
        Intent empfangenerIntent = this.getIntent();
        if (empfangenerIntent != null && empfangenerIntent.hasExtra(Intent.EXTRA_TEXT)) {
            //position
            String s = empfangenerIntent.getStringExtra(Intent.EXTRA_TEXT);
            Workout w = workoutPack.getWorkouts().get(Integer.valueOf(s));
            this.setTitle(w.getName());
            SharedPreferences sp = getPreferences(MODE_PRIVATE);
            SharedPreferences.Editor e = sp.edit();
            e.putInt("wo_choose", Integer.valueOf(s));
            e.commit();
        }

    }
/*    public void generalList() {
        // Erstellung der Liste aller  Workouts nach Type
        WorkoutListArray0.clear();
        workoutCount = workoutPack.getWorkouts().size();

        for(int i=0; i<workoutCount; i++)
        {
            Workout w = workoutPack.getWorkouts().get(i);
            WorkoutListArray0.add(String.valueOf(i) + ",0," + w.getDuration() + "," + w.getDifficulty() + "," +w.getEndurance().getPoints());
            WorkoutListArrayPrint0.add(w.getName());

*//*            for (int j = 0; j < 3; j++) {
                if (j == 0) {
                    //Workoutindex, Workouttyp, Duration, Difficulty,Punkte
                    WorkoutListArray0.add(String.valueOf(i) + ",0," + w.getDuration() + "," + w.getDifficulty() + "," +w.getEndurance().getPoints());
                    WorkoutListArrayPrint0.add(w.getName());
                }
                if (j == 1) {
                    WorkoutListArray1.add(String.valueOf(i) + ",1," + w.getDuration() + "," + w.getDifficulty() + "," +w.getStandard().getPoints());
                    WorkoutListArrayPrint1.add(w.getName());
                }
                if (j == 2) {
                    WorkoutListArray2.add(String.valueOf(i) + ",2," + w.getDuration() + "," + w.getDifficulty() + "," + w.getStrength().getPoints());
                    WorkoutListArrayPrint2.add(w.getName());
                    }
                }*/

/*        }
    }*/
    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        // on tab selected
        // show respected fragment view
        viewPager.setCurrentItem(tab.getPosition());
        /**
         * on swiping the viewpager make respective tab selected
         * */
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                // on changing the page
                // make respected tab selected
                actionBar.setSelectedNavigationItem(position);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

}
