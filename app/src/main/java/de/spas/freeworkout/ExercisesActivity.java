package de.spas.freeworkout;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class ExercisesActivity extends Activity {

    private de.spas.freeworkout.exercisePack exercisePack;
    private List<ExerciseClass> ExerciseListArray = new ArrayList<>();
    private List<String> WorkoutListArray0 = new ArrayList<String>();

    private List<String> WorkoutListArrayPrint0 = new ArrayList<String>();

    private int exerciseCount;
    private TextView light_text;
    private StringBuilder msg = new StringBuilder(2048);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workouts_activity);

        try {
            InputStream source = getAssets().open("exercises.xml");
            Serializer serializer = new Persister();
            exercisePack = serializer.read(de.spas.freeworkout.exercisePack.class, source);
            //Toast.makeText(this, "Wow! Klappt!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            //Toast.makeText(this, "Oh oh! exercisePack", Toast.LENGTH_LONG).show();
            Log.e(getClass().getSimpleName(), "loading levels threw exception", e);
        }
        light_text = (TextView) findViewById(R.id.light);
        generalList();
        printWorkouts();

    }
    public void printWorkouts() {
        //Collections.sort(WorkoutListArrayPrint0);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, WorkoutListArrayPrint0);
        ListView listView = (ListView) findViewById(R.id.listview_workouts);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Intent erzeugen und Starten der ExerciseActivity mit explizitem Intent
                Intent workoutFragmentIntent = new Intent(ExercisesActivity.this, ExerciseActivity.class);
                workoutFragmentIntent.putExtra(Intent.EXTRA_TEXT, WorkoutListArray0.get(position));
                startActivity(workoutFragmentIntent);
                //Toast.makeText(getApplicationContext(), "Click: "+String.valueOf(position), Toast.LENGTH_SHORT).show();

            }

        });
    }

    public void generalList() {
        // Erstellung der Liste aller Exercises
        WorkoutListArray0.clear();
        exerciseCount = exercisePack.getExercises().size();

        for(int i=0; i<exerciseCount; i++)
        {
            Exercise w = exercisePack.getExercises().get(i);

            //WorkoutListArray0.add("1,"+String.valueOf(i)+",3,10,1"); //Workout/Special,Workoutnummer,Type(Strth,Std,End.),Anzahl,FromWhere (ExercisesActivity))
            //WorkoutListArrayPrint0.add(w.getName());
            int q = 10;
            if(w.getQuantHidden()==1){
                q=1;
            }
            ExerciseListArray.add(new ExerciseClass(w.getName(),i,"1,"+String.valueOf(i)+",3,"+String.valueOf(q)+",1,-1,-1"));//Workout/Special,Workoutnummer,Type(Strth,Std,End.),Anzahl,FromWhere (ExercisesActivity))
         /*   msg.insert(0, "Add: "+ExerciseListArray.get(i).name+ "\n");
            light_text.setText(msg);
            light_text.invalidate();*/

        }


         //Sortieren nach Namen
        Collections.sort(ExerciseListArray, new Comparator<ExerciseClass>(){
            public int compare(ExerciseClass obj1, ExerciseClass obj2)
            {
                // TODO Auto-generated method stub
                return obj1.name.compareToIgnoreCase(obj2.name);
            }
        });
       for(int i=0; i<exerciseCount; i++) {
            WorkoutListArray0.add(ExerciseListArray.get(i).textIntent+",-1,-1"); //Workout/Special,Workoutnummer,Type(Strth,Std,End.),Anzahl,FromWhere (ExercisesActivity))
            WorkoutListArrayPrint0.add(ExerciseListArray.get(i).name);

        }

    }
}
