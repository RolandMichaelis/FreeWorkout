package de.spas.freeworkout;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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

public class WorkoutsActivity extends Activity {
    private de.spas.freeworkout.workoutPack workoutPack;
    private List<String> WorkoutListArray0 = new ArrayList<String>();
    private List<String> WorkoutListArray1 = new ArrayList<String>();
    private List<String> WorkoutListArray2 = new ArrayList<String>();
    private List<String> WorkoutListArrayPrint0 = new ArrayList<String>();
    private List<String> WorkoutListArrayPrint1 = new ArrayList<String>();
    private List<String> WorkoutListArrayPrint2 = new ArrayList<String>();
    private int workoutCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workouts_activity);

        try {
            InputStream source = getAssets().open("workouts.xml");
            Serializer serializer = new Persister();
            workoutPack = serializer.read(de.spas.freeworkout.workoutPack.class, source);
            //Toast.makeText(this, "Wow! Klappt!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Oh oh! workoutPack", Toast.LENGTH_LONG).show();
            Log.e(getClass().getSimpleName(), "loading levels threw exception", e);
        }
        generalList();
        printWorkouts();

    }
    public void printWorkouts() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, WorkoutListArrayPrint0);
        ListView listView = (ListView) findViewById(R.id.listview_workouts);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Intent erzeugen und Starten der AktiendetailActivity mit explizitem Intent
                Intent workoutFragmentIntent = new Intent(WorkoutsActivity.this, WorkoutChooseActivity.class);
                workoutFragmentIntent.putExtra(Intent.EXTRA_TEXT, String.valueOf(position));
                startActivity(workoutFragmentIntent);
                //Toast.makeText(getApplicationContext(), "Click: "+String.valueOf(position), Toast.LENGTH_SHORT).show();

            }

        });
    }

    public void generalList() {
        // Erstellung der Liste aller  Workouts
        WorkoutListArray0.clear();
        workoutCount = workoutPack.getWorkouts().size();

        for(int i=0; i<workoutCount; i++)
        {
            Workout w = workoutPack.getWorkouts().get(i);
            WorkoutListArray0.add(String.valueOf(i) + ",0," + w.getDuration() + "," + w.getDifficulty() + "," +w.getEndurance().getPoints());
            WorkoutListArrayPrint0.add(w.getName());

/*            for (int j = 0; j < 3; j++) {
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

        }
    }
}
