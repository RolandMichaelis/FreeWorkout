package de.spas.freeworkout;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.speech.tts.TextToSpeech;

import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;


public class WorkoutActivity extends Activity implements View.OnClickListener, TextToSpeech.OnInitListener {
    private de.spas.freeworkout.workoutPack workoutPack;
    private de.spas.freeworkout.specialPack specialPack;
    private de.spas.freeworkout.exercisePack exercisePack;
    private String xmeter;
    private int counter_practice;
    private String TextName;
    private String TextType = "";
    private int type;
    private int quantity;
    private String[] Types = {"Endurance","Standard","Strength"};
    private int counter_rounds=0;
    //    private ArrayList<ArrayList> roundsList = new ArrayList<ArrayList>();
    private ArrayList[] roundList = new ArrayList[30];
    //private ArrayList<ArrayList> mArraysAdapterList = new ArrayList<ArrayList>();
    //private ArrayList[] mArrayAdapterList = new ArrayList[21];
    private Handler handler = new Handler();
    private int countup = 0;
    private int countdown = 5;
    private String timeString;
    private TextToSpeech tts;
    private int top;
    private int left;
    private int wo_pointer = 0; // Workout Zeiger
    private int start_pointer = 0; // ab dort wird Liste angezeigt
    private ArrayList<String> runList = new ArrayList<String>(); // Liste alles zusammen (Runden und WOs)
    private ArrayList<TextView> tvList = new ArrayList<TextView>(); // TextViews
    private ArrayList<Integer> propList = new ArrayList<Integer>(); //Art des Eintrags (0=normal; 1=Runde;2=Pause)
    private ArrayList<Long> statList = new ArrayList<Long>(); //Timestamps
    private ArrayList<Long> timeList = new ArrayList<Long>(); //Sekunden/Exercise
    private ArrayList<Integer> ghostList = new ArrayList<Integer>(); // Liste Ghostzeiten
    private ArrayList<Integer> ghostList2 = new ArrayList<Integer>(); // Liste Ghostzeiten LT
    private Long timestampStart=0L;
    private Long timestampAdd=0L;
    private Long timestampCurr;
    private ArrayList<Integer> colorList = new ArrayList<Integer>(); //Farbzuordnung
    private int quantElements=0; //Menge der Elemente in der runList
    private int defaultPeriod=30;
    private double secondWidth;
    private int lengthElement;
    private boolean rest=false; //keine Pause = false;
    public static final String LOG_TAG = MainActivity.class.getSimpleName();
    private WorkoutMemoDataSource dataSource;
    private ListView mWorkoutMemosListView;
    private boolean theEnd=false;
    private int wore;
    private int number;
    private String text_pb;
    private String text_lt;
    PowerManager pm;
    PowerManager.WakeLock wl;
    private String datasGhost="";
    private String datasGhost2=""; // Wenn LT und PB vorhanden sind
    final Context context = this;
    String roundlistLastRest="";
    int counter_rounds_pre_add;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workout_activity);
        getActionBar().setDisplayHomeAsUpEnabled(true);


        showView(R.id.container_1);

        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl =  pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My WakeLock");

        dataSource = new WorkoutMemoDataSource(this);

        try {
            InputStream source = getAssets().open("workouts.xml");
            Serializer serializer = new Persister();
            workoutPack = serializer.read(de.spas.freeworkout.workoutPack.class, source);
            //Toast.makeText(this, "Wow! Klappt!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            //Toast.makeText(this, "Oh oh! workoutPack", Toast.LENGTH_LONG).show();
            Log.e(getClass().getSimpleName(), "loading levels threw exception", e);
        }
        try {
            InputStream source = getAssets().open("exercises.xml");
            Serializer serializer = new Persister();
            exercisePack = serializer.read(de.spas.freeworkout.exercisePack.class, source);
            //Toast.makeText(this, "Wow! Klappt!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Oh oh! exercisePack", Toast.LENGTH_LONG).show();
            Log.e(getClass().getSimpleName(), "loading levels threw exception", e);
        }
        try {
            InputStream source = getAssets().open("specials.xml");
            Serializer serializer = new Persister();
            specialPack = serializer.read(de.spas.freeworkout.specialPack.class, source);
            //Toast.makeText(this, "Wow! Klappt!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            //Toast.makeText(this, "Oh oh! specialPack", Toast.LENGTH_LONG).show();
            Log.e(getClass().getSimpleName(), "loading levels threw exception", e);
        }

        Intent empfangenerIntent = this.getIntent();
        if (empfangenerIntent != null && empfangenerIntent.hasExtra(Intent.EXTRA_TEXT)) {
            //Workout/Special,Workout/Exercise-Nummer,Type(Strth,Std,End.),Anzahl
            String s = empfangenerIntent.getStringExtra(Intent.EXTRA_TEXT);
            wore = Integer.valueOf(s.substring(0,1)); //wore = Workout oder Exercise
            String s1=s.substring(2,s.length());
            int n = s1.indexOf(",");
            number = Integer.valueOf(s1.substring(0,n));
            n = s1.lastIndexOf(",");
            type = Integer.valueOf(s1.substring(n-1,n));
            quantity = Integer.valueOf(s1.substring(n+1,s1.length()));


            for (int i = 0; i < 30; i++){
                roundList[i] = new ArrayList();
                //mArrayAdapterList[i] = new ArrayList();
            }

            if(wore==0){
                Workout w = workoutPack.getWorkouts().get(number);
                TextName = w.getName();
                TextType = Types[type];
                this.setTitle(quantity+"x "+TextName+" "+TextType);
                switch (type) {
                    case 0: {
                        Endurance wo = workoutPack.getWorkouts().get(number).getEndurance();
                        for (int qidx = 0; qidx < quantity; qidx++) {
                            for (Round r : wo.getRounds()) { // alle Round-Knoten durchlaufen
                                counter_practice = 0;

                                for (Practice p : r.getPractice()) {  // alle Practice-Knoten durchlaufen
                                    xmeter = " x ";
                                    String xhalf = "";
                                    int q = p.getQuantity();
                                    if (p.getName().equals("Sprint")) xmeter = " m ";
                                    if (p.getName().equals("Run")) xmeter = " m ";
                                    if (p.getName().equals("Rest")) xmeter = " s ";
                                    if (xmeter.equals(" m ") && q < 100) {
                                        q = q / 2;
                                        xhalf = "2x ";
                                    }
                                    roundList[counter_rounds].add(xhalf + q + xmeter + " " + p.getName());
                                    counter_practice++;
                                }
                                counter_rounds++;
                            }
                        }
                        //Toast.makeText(this, "specialPack Anzahl: "+String.valueOf(counter_rounds)+" | "+String.valueOf(counter_practice)+" | "+String.valueOf(wo), Toast.LENGTH_LONG).show();
                        break;
                    }
                    case 1: {
                        Standard wo = workoutPack.getWorkouts().get(number).getStandard();
                        for (int qidx = 0; qidx < quantity; qidx++) {
                            for (Round r : wo.getRounds()) { // alle Round-Knoten durchlaufen
                                counter_practice = 0;

                                for (Practice p : r.getPractice()) {  // alle Practice-Knoten durchlaufen
                                    xmeter = " x ";
                                    String xhalf = "";
                                    int q = p.getQuantity();
                                    if (p.getName().equals("Sprint")) xmeter = " m ";
                                    if (p.getName().equals("Run")) xmeter = " m ";
                                    if (p.getName().equals("Rest")) xmeter = " s ";
                                    if (xmeter.equals(" m ") && q < 100) {
                                        q = q / 2;
                                        xhalf = "2x ";
                                    }
                                    roundList[counter_rounds].add(xhalf + q + xmeter + " " + p.getName());
                                    counter_practice++;
                                }
                                counter_rounds++;
                            }
                        }
                        //Toast.makeText(this, "specialPack Anzahl: "+String.valueOf(counter_rounds)+"|"+String.valueOf(counter_practice)+" | "+String.valueOf(wo), Toast.LENGTH_LONG).show();
                        break;
                    }
                    default: {
                        Strength wo = workoutPack.getWorkouts().get(number).getStrength();
                        for (int qidx = 0; qidx < quantity; qidx++) {

                            for (Round r : wo.getRounds()) { // alle Round-Knoten durchlaufen
                                counter_practice = 0;

                                for (Practice p : r.getPractice()) {  // alle Practice-Knoten durchlaufen
                                    xmeter = " x ";
                                    String xhalf = "";
                                    int q = p.getQuantity();
                                    if (p.getName().equals("Sprint")) xmeter = " m ";
                                    if (p.getName().equals("Run")) xmeter = " m ";
                                    if (p.getName().equals("Rest")) xmeter = " s ";
                                    // quantity wird bei Laufstrecken unter 100 m halbiert für neue Darstellung von 40 m auf 2x 20 m
                                    if (xmeter.equals(" m ") && q < 100) {
                                        q = q / 2;
                                        xhalf = "2x ";
                                    }
                                    roundList[counter_rounds].add(xhalf + q + xmeter + " " + p.getName());
                                    counter_practice++;
                                    //Toast.makeText(this, "specialPack Anzahl: "+String.valueOf(counter_rounds)+"|"+String.valueOf(counter_practice), Toast.LENGTH_LONG).show();
                                }
                                counter_rounds++;
                            }
                        }
                        //Toast.makeText(this, "specialPack Anzahl: "+String.valueOf(counter_rounds)+"|"+String.valueOf(counter_practice)+" | "+String.valueOf(wo), Toast.LENGTH_LONG).show();
                        break;
                    }

                }
                if(xmeter.equals(" s ")) {
                    roundlistLastRest=roundList[counter_rounds - 1].get(counter_practice - 1).toString();
                    roundList[counter_rounds - 1].remove(counter_practice - 1); //Allerletzte Runde raus, wenn "Rest"
                }
                if(counter_rounds>0) {
                    ArrayAdapter<String> adapter0 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, roundList[0]);
                    ListView listView0 = (ListView) findViewById(R.id.list_round0);
                    listView0.setAdapter(adapter0);
                    ListUtils.setDynamicHeight(listView0);
                    showView(R.id.text_workout0);
                    showView(R.id.list_round0);
                }
                if (counter_rounds>1) {
                    ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, roundList[1]);
                    ListView listView1 = (ListView) findViewById(R.id.list_round1);
                    listView1.setAdapter(adapter1);
                    ListUtils.setDynamicHeight(listView1);
                    showView(R.id.text_workout1);
                    showView(R.id.list_round1);
                }
                if (counter_rounds>2) {
                    ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, roundList[2]);
                    ListView listView2 = (ListView) findViewById(R.id.list_round2);
                    listView2.setAdapter(adapter2);
                    ListUtils.setDynamicHeight(listView2);
                    showView(R.id.text_workout2);
                    showView(R.id.list_round2);
                }
                if (counter_rounds>3) {
                    ArrayAdapter<String> adapter3 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, roundList[3]);
                    ListView listView3 = (ListView) findViewById(R.id.list_round3);
                    listView3.setAdapter(adapter3);
                    ListUtils.setDynamicHeight(listView3);
                    showView(R.id.text_workout3);
                    showView(R.id.list_round3);
                }
                if (counter_rounds>4) {
                    ArrayAdapter<String> adapter4 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, roundList[4]);
                    ListView listView4 = (ListView) findViewById(R.id.list_round4);
                    listView4.setAdapter(adapter4);
                    ListUtils.setDynamicHeight(listView4);
                    showView(R.id.text_workout4);
                    showView(R.id.list_round4);
                }
                if (counter_rounds>5) {
                    ArrayAdapter<String> adapter5 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, roundList[5]);
                    ListView listView5 = (ListView) findViewById(R.id.list_round5);
                    listView5.setAdapter(adapter5);
                    ListUtils.setDynamicHeight(listView5);
                    showView(R.id.text_workout5);
                    showView(R.id.list_round5);
                }
                if (counter_rounds>6) {
                    ArrayAdapter<String> adapter6 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, roundList[6]);
                    ListView listView6 = (ListView) findViewById(R.id.list_round6);
                    listView6.setAdapter(adapter6);
                    ListUtils.setDynamicHeight(listView6);
                    showView(R.id.text_workout6);
                    showView(R.id.list_round6);
                }
                if (counter_rounds>7) {
                    ArrayAdapter<String> adapter7 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, roundList[7]);
                    ListView listView7 = (ListView) findViewById(R.id.list_round7);
                    listView7.setAdapter(adapter7);
                    ListUtils.setDynamicHeight(listView7);
                    showView(R.id.text_workout7);
                    showView(R.id.list_round7);
                }
                if (counter_rounds>8) {
                    ArrayAdapter<String> adapter8 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, roundList[8]);
                    ListView listView8 = (ListView) findViewById(R.id.list_round8);
                    listView8.setAdapter(adapter8);
                    ListUtils.setDynamicHeight(listView8);
                    showView(R.id.text_workout8);
                    showView(R.id.list_round8);
                }
                if (counter_rounds>9) {
                    ArrayAdapter<String> adapter9 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, roundList[9]);
                    ListView listView9 = (ListView) findViewById(R.id.list_round9);
                    listView9.setAdapter(adapter9);
                    ListUtils.setDynamicHeight(listView9);
                    showView(R.id.text_workout9);
                    showView(R.id.list_round9);
                }
                if (counter_rounds>10) {
                    ArrayAdapter<String> adapter10 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, roundList[10]);
                    ListView listView10 = (ListView) findViewById(R.id.list_round10);
                    listView10.setAdapter(adapter10);
                    ListUtils.setDynamicHeight(listView10);
                    showView(R.id.text_workout10);
                    showView(R.id.list_round10);
                }
                if (counter_rounds>11) {
                    ArrayAdapter<String> adapter11 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, roundList[11]);
                    ListView listView11 = (ListView) findViewById(R.id.list_round11);
                    listView11.setAdapter(adapter11);
                    ListUtils.setDynamicHeight(listView11);
                    showView(R.id.text_workout11);
                    showView(R.id.list_round11);
                }
                if (counter_rounds>12) {
                    ArrayAdapter<String> adapter12 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, roundList[12]);
                    ListView listView12 = (ListView) findViewById(R.id.list_round12);
                    listView12.setAdapter(adapter12);
                    ListUtils.setDynamicHeight(listView12);
                    showView(R.id.text_workout12);
                    showView(R.id.list_round12);
                }
                if (counter_rounds>13) {
                    ArrayAdapter<String> adapter13 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, roundList[13]);
                    ListView listView13 = (ListView) findViewById(R.id.list_round13);
                    listView13.setAdapter(adapter13);
                    ListUtils.setDynamicHeight(listView13);
                    showView(R.id.text_workout13);
                    showView(R.id.list_round13);
                }
                if (counter_rounds>14) {
                    ArrayAdapter<String> adapter14 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, roundList[14]);
                    ListView listView14 = (ListView) findViewById(R.id.list_round14);
                    listView14.setAdapter(adapter14);
                    ListUtils.setDynamicHeight(listView14);
                    showView(R.id.text_workout14);
                    showView(R.id.list_round14);
                }
                if (counter_rounds>15) {
                    ArrayAdapter<String> adapter15 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, roundList[15]);
                    ListView listView15 = (ListView) findViewById(R.id.list_round15);
                    listView15.setAdapter(adapter15);
                    ListUtils.setDynamicHeight(listView15);
                    showView(R.id.text_workout15);
                    showView(R.id.list_round15);
                }
                if (counter_rounds>16) {
                    ArrayAdapter<String> adapter16 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, roundList[16]);
                    ListView listView16 = (ListView) findViewById(R.id.list_round16);
                    listView16.setAdapter(adapter16);
                    ListUtils.setDynamicHeight(listView16);
                    showView(R.id.text_workout16);
                    showView(R.id.list_round16);
                }
                if (counter_rounds>17) {
                    ArrayAdapter<String> adapter17 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, roundList[17]);
                    ListView listView17 = (ListView) findViewById(R.id.list_round17);
                    listView17.setAdapter(adapter17);
                    ListUtils.setDynamicHeight(listView17);
                    showView(R.id.text_workout17);
                    showView(R.id.list_round17);
                }
                if (counter_rounds>18) {
                    ArrayAdapter<String> adapter18 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, roundList[18]);
                    ListView listView18 = (ListView) findViewById(R.id.list_round18);
                    listView18.setAdapter(adapter18);
                    ListUtils.setDynamicHeight(listView18);
                    showView(R.id.text_workout18);
                    showView(R.id.list_round18);
                }
                if (counter_rounds>19) {
                    ArrayAdapter<String> adapter19 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, roundList[19]);
                    ListView listView19 = (ListView) findViewById(R.id.list_round19);
                    listView19.setAdapter(adapter19);
                    ListUtils.setDynamicHeight(listView19);
                    showView(R.id.text_workout19);
                    showView(R.id.list_round19);
                }
                if (counter_rounds>20) {
                    ArrayAdapter<String> adapter20 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, roundList[20]);
                    ListView listView20 = (ListView) findViewById(R.id.list_round20);
                    listView20.setAdapter(adapter20);
                    ListUtils.setDynamicHeight(listView20);
                    showView(R.id.text_workout20);
                    showView(R.id.list_round20);
                }
                if (counter_rounds>21) {
                    ArrayAdapter<String> adapter21 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, roundList[21]);
                    ListView listView21 = (ListView) findViewById(R.id.list_round21);
                    listView21.setAdapter(adapter21);
                    ListUtils.setDynamicHeight(listView21);
                    showView(R.id.text_workout21);
                    showView(R.id.list_round21);
                }
                if (counter_rounds>22) {
                    ArrayAdapter<String> adapter22 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, roundList[22]);
                    ListView listView22 = (ListView) findViewById(R.id.list_round22);
                    listView22.setAdapter(adapter22);
                    ListUtils.setDynamicHeight(listView22);
                    showView(R.id.text_workout22);
                    showView(R.id.list_round22);
                }
                if (counter_rounds>23) {
                    ArrayAdapter<String> adapter23 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, roundList[23]);
                    ListView listView23 = (ListView) findViewById(R.id.list_round23);
                    listView23.setAdapter(adapter23);
                    ListUtils.setDynamicHeight(listView23);
                    showView(R.id.text_workout23);
                    showView(R.id.list_round23);
                }
                if (counter_rounds>24) {
                    ArrayAdapter<String> adapter24 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, roundList[24]);
                    ListView listView24 = (ListView) findViewById(R.id.list_round24);
                    listView24.setAdapter(adapter24);
                    ListUtils.setDynamicHeight(listView24);
                    showView(R.id.text_workout24);
                    showView(R.id.list_round24);
                }
                if (counter_rounds>25) {
                    ArrayAdapter<String> adapter25 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, roundList[25]);
                    ListView listView25 = (ListView) findViewById(R.id.list_round25);
                    listView25.setAdapter(adapter25);
                    ListUtils.setDynamicHeight(listView25);
                    showView(R.id.text_workout25);
                    showView(R.id.list_round25);
                }
                if (counter_rounds>26) {
                    ArrayAdapter<String> adapter26 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, roundList[26]);
                    ListView listView26 = (ListView) findViewById(R.id.list_round26);
                    listView26.setAdapter(adapter26);
                    ListUtils.setDynamicHeight(listView26);
                    showView(R.id.text_workout26);
                    showView(R.id.list_round26);
                }
                if (counter_rounds>27) {
                    ArrayAdapter<String> adapter27 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, roundList[27]);
                    ListView listView27 = (ListView) findViewById(R.id.list_round27);
                    listView27.setAdapter(adapter27);
                    ListUtils.setDynamicHeight(listView27);
                    showView(R.id.text_workout27);
                    showView(R.id.list_round27);
                }
                if (counter_rounds>28) {
                    ArrayAdapter<String> adapter28 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, roundList[28]);
                    ListView listView28 = (ListView) findViewById(R.id.list_round28);
                    listView28.setAdapter(adapter28);
                    ListUtils.setDynamicHeight(listView28);
                    showView(R.id.text_workout28);
                    showView(R.id.list_round28);
                }
                if (counter_rounds>29) {
                    ArrayAdapter<String> adapter29 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, roundList[29]);
                    ListView listView29 = (ListView) findViewById(R.id.list_round29);
                    listView29.setAdapter(adapter29);
                    ListUtils.setDynamicHeight(listView29);
                    showView(R.id.text_workout29);
                    showView(R.id.list_round29);
                }
            }
            else{
                Exercise w = exercisePack.getExercises().get(number);
                TextName = w.getName();
            }
            /*for (int i = 0; i < 18; i++){
                roundsList.add(roundList[i]);
            }
          //  ((TextView) findViewById(R.id.text_fragment)).setText(quantity+"x "+TextName+" "+TextType+" | "+s);
            int test=0;
            roundList[test].add("Test1");
            roundList[test].add("Test2");
            roundList[test].add("Test3");*/




            //Exercise w = exercisePack.getExercises().get(number); // XML für Exercise laden, number = Exercisenummer
            //Toast.makeText(this, "specialPack Anzahl: "+String.valueOf(), Toast.LENGTH_LONG).show();



            /*for (int i = 0; i < 18; i++){
                roundsList.add(roundList[i]);
            }*/
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: // Activity Bar Back Button override to go to previous Activity
                onBackPressed();
                return true;
            //case R.id.menu_add_icon:
            //    Toast.makeText(this, "Plus: ", Toast.LENGTH_LONG).show();
            //    return(true);
        }
        return(super.onOptionsItemSelected(item));

    }
    public void dialog_cancel() {
        AlertDialog.Builder alertDialogBuilder =  new AlertDialog.Builder(
                new ContextThemeWrapper(this, android.R.style.Theme_DeviceDefault_Light_Dialog));
        // set title
        //alertDialogBuilder.setTitle("Your Title");

        // set dialog message
        alertDialogBuilder
                .setMessage(R.string.text_wo_cancel)
                .setCancelable(false)
                .setPositiveButton("Ja",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, close
                        // current activity
                        finish();
                    }
                })
                .setNegativeButton("Nein",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }
    public void dialog_add() {
        AlertDialog.Builder alertDialogBuilder =  new AlertDialog.Builder(
                new ContextThemeWrapper(this, android.R.style.Theme_DeviceDefault_Light_Dialog));
        // set title
        //alertDialogBuilder.setTitle("Your Title");

        // set dialog message
        alertDialogBuilder
                .setMessage(R.string.text_wo_add)
                .setCancelable(false)
                .setPositiveButton("Ja",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        workout_add();
                    }
                })
                .setNegativeButton("Nein",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        dialog_cancel();
    }
    // create an action bar button
/*    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_icon, menu);
        return super.onCreateOptionsMenu(menu);
    }*/

    public void onClick(final View view) {

 /*       view.setOnTouchListener(new OnSwipeTouchListener(WorkoutActivity.this) {

            @Override
            public void onClick() {
                super.onClick();*/
                Toast.makeText(WorkoutActivity.this, "super.onClick()", Toast.LENGTH_LONG).show();

                // your on click here
                if(view.getId()==R.id.button_wo_display) {
                    Toast.makeText(WorkoutActivity.this, "Workout display", Toast.LENGTH_LONG).show();
                    hideView(R.id.container_1);
                    hideView(R.id.button_wo_display);
                    showView(R.id.container_2);
                    showView(R.id.button_wo_start);
                    if(quantity<3)showView(R.id.button_wo_add);
                    //showView(R.id.button_wo_cancel);

                    init_view();
                    calc_view();
                    if(!datasGhost.equals(""))run_view_ghost();
                }
                if(view.getId()==R.id.button_wo_start) {
                    Toast.makeText(WorkoutActivity.this, "Workout starten", Toast.LENGTH_LONG).show();
                    //statList.set(0,tsLong);
                    showView(R.id.button_wo_back);
                    wl.acquire();
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    handler.postDelayed(runnable, 1000);
                    hideView(R.id.button_wo_start);
                    tts = new TextToSpeech(WorkoutActivity.this,WorkoutActivity.this);
                }
                if(view.getId()==R.id.button_wo_back) {
                    //Toast.makeText(this, "button_wo_cancel", Toast.LENGTH_LONG).show();
                    Toast.makeText(WorkoutActivity.this, "button_wo_back", Toast.LENGTH_LONG).show();
                    if(wo_pointer>1) {
                        wo_pointer--;
                        start_pointer--;
                        workoutOrRoundMinus();
                        timeList.set(wo_pointer, 0L);
                        if (wo_pointer == 1) {
                            timestampAdd = 0L;
                        } else {
                            if (propList.get(wo_pointer - 1) == 1)
                                timestampAdd = statList.get(wo_pointer - 2);
                            else timestampAdd = statList.get(wo_pointer - 1);
                        }
                        if (rest) rest = false;
                    }
                    }
                if(view.getId()==R.id.container_2 && theEnd==false) {
                    //Nächstes Workout nur wenn countup schon läuft:
                    if(countup>0) {
                        if(rest==false ) {
                            timestampCurr = System.currentTimeMillis();
                            if (timestampAdd == 0L){ timeList.set(wo_pointer, timestampCurr - timestampStart);}
                            else {timeList.set(wo_pointer, timestampCurr - timestampAdd);}
                            timestampAdd = timestampCurr;
                            //timestampStart=statList.get(start_pointer+tvx);
                            //String ts = tsLong.toString();
                            statList.set(wo_pointer, timestampCurr);
                            if(!text_pb.equals("")) {
                                long a=0;
                                String minus;
                                for(int x=0;x<=wo_pointer;x++){
                                    a=a+(long)ghostList.get(x);
                                }
                                int timeDiff_pb = (int)(a-(timestampCurr-timestampStart))/1000;
                                if(timeDiff_pb<0){timeDiff_pb=timeDiff_pb*-1;minus="+";}
                                else {minus="-";}
                                String time_pb_suffix="";
                                time_pb_suffix=" "+minus+timeFormat(timeDiff_pb);
                                ((TextView) findViewById(R.id.time_pb)).setText(text_pb+time_pb_suffix);
                                long td=(timestampCurr-timestampStart)/1000;
                                //Toast.makeText(this, "timestampStart|Curr: "+String.valueOf(td)+"|"+String.valueOf(timeDiff_pb), Toast.LENGTH_LONG).show();

                            }
                            if(wo_pointer<quantElements-1) {
                                wo_pointer++;
                                start_pointer++;
                                workoutOrRound();
                                list_view();
                                calc_view();
                                run_view();
                                if(!datasGhost.equals(""))run_view_ghost();
                                //Toast.makeText(this, "timestamp; "+tsLong.toString(), Toast.LENGTH_LONG).show();
                            }
                            else {
                                //Stopp!!! Letzte Exercise fertig!
                                handler.removeCallbacks(runnable);
                                int cup= (int) ((timestampCurr-timestampStart)/1000);
                                ((TextView) findViewById(R.id.clock)).setText(timeFormat(cup));
                                theEnd=true;
                                hideView(R.id.button_wo_add);
                                hideView(R.id.button_wo_back);
                                list_view();
                                run_view();
                                if(!datasGhost.equals(""))run_view_ghost();
                                String tlt="";
                                for(int x=0;x<quantElements;x++){
                                    if(x<quantElements-1)tlt=tlt+timeList.get(x).toString()+"|";
                                    else tlt=tlt+timeList.get(x).toString();

                                }
                                dataSource.createWorkoutMemo(wore, number, TextName, type, quantity, timestampStart, timestampCurr, timestampCurr-timestampStart,tlt,false,false,false);
                                showView(R.id.button_wo_continue);
                                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                                wl.release();
                                //Toast.makeText(this, "Ich habe fertig!", Toast.LENGTH_LONG).show();

                                //String tx=String.valueOf(lengthElement)+"|"+String.valueOf(secondWidth)+"|"+String.valueOf(wo_pointer)+"|"+String.valueOf(quantElements);
                                //((TextView) findViewById(R.id.time_lt)).setText(String.valueOf(wo_pointer));
                            }
                        }
                    }
                }
                if(view.getId()==R.id.button_wo_add) {
                    //Toast.makeText(this, "button_wo_cancel", Toast.LENGTH_LONG).show();
                    dialog_add();
                }

/*            }

            @Override
            public void onDoubleClick() {
                super.onDoubleClick();
                // your on onDoubleClick here
                Toast.makeText(WorkoutActivity.this, "onDoubleClick", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onLongClick() {
                super.onLongClick();
                // your on onLongClick here
                Toast.makeText(WorkoutActivity.this, "onLongClick", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSwipeUp() {
                super.onSwipeUp();
                // your swipe up here
            }

            @Override
            public void onSwipeDown() {
                super.onSwipeDown();
                // your swipe down here.
            }

            @Override
            public void onSwipeLeft() {
                super.onSwipeLeft();
                // your swipe left here.
                Toast.makeText(WorkoutActivity.this, "Wischen links", Toast.LENGTH_LONG).show();
                if(wo_pointer>1) {
                    wo_pointer--;
                    start_pointer--;
                    workoutOrRoundMinus();
                    timeList.set(wo_pointer,0L);
                    if(wo_pointer==1){
                        timestampAdd=0L;
                    } else {
                        if(propList.get(wo_pointer-1) == 1) timestampAdd=statList.get(wo_pointer-2);
                        else timestampAdd=statList.get(wo_pointer-1);
                    }
                    if(rest)rest=false;
                }
                checkRest();
                list_view();
                calc_view();
                run_view();
                if(!datasGhost.equals(""))run_view_ghost();           }

            @Override
            public void onSwipeRight() {
                super.onSwipeRight();
                // your swipe right here.
            }
        });*/

    }
    private void workout_add() {
        //Nach Klick auf add-Button: Workout Wiederholungen um eins erhöhen
        if(!roundlistLastRest.equals("")) {
            roundList[counter_rounds - 1].add(roundlistLastRest);
            statList.add((long) 0); //Für die Pause dazu
            timeList.add((long) 0);//Für die Pause dazu
        }
        Toast.makeText(this,  String.valueOf(counter_rounds), Toast.LENGTH_LONG).show();
        counter_rounds_pre_add=counter_rounds;
        for(int x=0;x<(counter_rounds_pre_add/quantity);x++){
            roundList[counter_rounds].addAll(roundList[x]);
            counter_rounds++;
        }
        if(!roundlistLastRest.equals(""))roundList[counter_rounds - 1].remove(counter_practice - 1); //Allerletzte Runde raus, wenn "Rest"
        Toast.makeText(this,  String.valueOf(counter_rounds), Toast.LENGTH_LONG).show();

        for (int rLx = counter_rounds_pre_add; rLx < counter_rounds; rLx++) {
            int r=rLx+1;
            //runList.add(getString(R.string.round)+" "+r+"/"+counter_rounds); // Alter Teil muss extra gefüllt werden, da letzer Wert nun falsch ist
            statList.add((long) 0);
            timeList.add((long) 0);
            for (int wox = 0; wox < roundList[rLx].size(); wox++) {
                //runList.add(String.valueOf(roundList[rLx].get(wox)));
                statList.add((long) 0);
                timeList.add((long) 0);
            }
        }
        runList.clear();
        propList.clear();
        for (int runLx = 0; runLx < counter_rounds; runLx++) {
            int r=runLx+1;
            propList.add(1);
            runList.add(getString(R.string.round)+" "+r+"/"+counter_rounds); // Alter Teil muss extra gefüllt werden, da letzer Wert nun falsch ist
            for (int wox = 0; wox < roundList[runLx].size(); wox++) {
                runList.add(String.valueOf(roundList[runLx].get(wox)));
                propList.add(0);
            }
        }
        quantity++;
        this.setTitle(quantity+"x "+TextName+" "+TextType);
        if(quantity>2)hideView(R.id.button_wo_add);

        text_pb="";
        text_lt="";
        datasGhost="";
        defaultPeriod=30;
        hideView(R.id.time_pb);
        hideView(R.id.time_lt);

        text_pb=dataSource.getMinDuration(quantity,TextName,type); //Abfrage DB kürzestes WO
        if(!text_pb.equals("")) {
            showView(R.id.time_pb);
            ((TextView) findViewById(R.id.time_pb)).setText(text_pb);
            datasGhost=dataSource.getMinDurationGhost(quantity,TextName,type); //Abfrage Ghostdaten in DB für kürzestes WO
            //Toast.makeText(this, "getMinDurationGhost:"+String.valueOf(quantity)+" "+TextName+" "+String.valueOf(type), Toast.LENGTH_LONG).show();
        }
        text_lt=dataSource.getMaxStartTime(quantity,TextName,type); //Abfrage DB letztes WO
        Toast.makeText(this, "getMaxStartTime return:"+text_lt, Toast.LENGTH_LONG).show();
        if(!text_lt.equals("")) {
            showView(R.id.time_lt);
            ((TextView) findViewById(R.id.time_lt)).setText(text_lt);
            if(datasGhost.equals(""))datasGhost=dataSource.getMaxStartTimeGhost(quantity,TextName,type);
            //Toast.makeText(this, "getMinDurationGhost:"+String.valueOf(quantity)+" "+TextName+" "+String.valueOf(type), Toast.LENGTH_LONG).show();

        }
        if(!datasGhost.equals("")){
            ghostList.clear();
            exDatasGhostString(datasGhost);//Toast.makeText(this, "Ghost:"+ghostList.get(1), Toast.LENGTH_LONG).show();
        }
/*        else {
            datasGhost2=dataSource.getMaxStartTimeGhost(quantity,TextName,type);
            ghostList2.clear();
            exDatasGhostString2(datasGhost2);}*/

        //TextView text = (TextView) this.findViewById(R.id.practice_1);//text.setTextColor(Color.parseColor("#999999"))
        //Toast.makeText(this, "TextView: "+String.valueOf(getResources().getColor(R.color.colorWoTextViewNormal)), Toast.LENGTH_LONG).show();
        list_view();
        calc_view();
        if(!datasGhost.equals(""))run_view_ghost();


}


    /*    private void initRound() {
        ViewGroup container = (ViewGroup) findViewById(R.id.container);
        container.removeAllViews();
        RunClockView rcv = new RunClockView(this);
        container.addView(rcv, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        float scale = getResources().getDisplayMetrics().density;
        //FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(Math.round(64*scale),Math.round(61*scale));
        //lp.gravity = Gravity.TOP + Gravity.LEFT;

        //container.addView(frog, lp);
        //update();
        //handler.postDelayed(runnable, 1000);
    }*/
    private void countup() {
        if(countdown>0){
            handler.postDelayed(runnable, 1000);
            doSpeech(countdown);
            ((TextView) findViewById(R.id.clock)).setText(String.valueOf(countdown));
            countdown--;
        }
        else {
            if(timestampStart==0L)timestampStart=System.currentTimeMillis();
            int cup= (int) ((System.currentTimeMillis()-timestampStart)/1000);
            ((TextView) findViewById(R.id.clock)).setText(timeFormat(cup));
            if(countup==0){
                list_view();
                calc_view();
                doSpeech(0);
                run_view();
                if(!datasGhost.equals(""))run_view_ghost();
            }
            if(countup>0) {
                checkRest();
                list_view();
                calc_view();
                run_view();
                if(!datasGhost.equals(""))run_view_ghost();
            }
            /*String tlt="";
            for(int x=0;x<quantElements;x++){
                if(x<quantElements-1)tlt=tlt+statList.get(x).toString()+"|";
                else tlt=tlt+statList.get(x).toString();
            }
            String tx=String.valueOf(lengthElement)+"|"+String.valueOf(secondWidth)+"|"+String.valueOf(wo_pointer)+"|"+String.valueOf(quantElements);
            //((TextView) findViewById(R.id.time_lt)).setText(String.valueOf(wo_pointer));
            //((TextView) findViewById(R.id.time_pb)).setText(tlt);*/
            handler.postDelayed(runnable, 1000);
            countup++;

            //int t=fillTextView(R.id.practice_1);
            //Toast.makeText(this, "getTop: "+C, Toast.LENGTH_LONG).show();
            //this.findViewById(R.id.img_1).setTop(t);
        }


        /*update();
        if(countdown<=0) {
            frog.setOnClickListener(null);
            if(points>highscore) {
                saveHighscore(points);
                update();
            }
            showGameOverFragment();
        } else {
            handler.postDelayed(runnable, 1000);
        }*/
    }
    private void checkRest() {
        String s=runList.get(wo_pointer);
        String s1 = s.substring(s.length()-4,s.length());

        if (s1.equals("Rest")){
            int n = s.indexOf(" ");
            Integer restTime = Integer.valueOf(s.substring(0,n));
            rest=true;
            int t = (int) ((System.currentTimeMillis()-timestampAdd)/1000);
            int cdw=restTime-t;
            doSpeech(cdw);
            ((TextView) findViewById(R.id.clock)).setText(String.valueOf(cdw));
            if(cdw<=0){
                timestampCurr=System.currentTimeMillis();
                timeList.set(wo_pointer, (long)restTime*1000);
                timestampAdd = timestampCurr;
                statList.set(wo_pointer, timestampCurr);
                wo_pointer++;
                start_pointer++;
                rest=false;
                workoutOrRound();
                list_view();
                calc_view();
                run_view();
                if(!datasGhost.equals(""))run_view_ghost();
            }
        }
    }
    private void list_view() {
        //Ausgabe der Workouttexte auf die TextViews
        //Prüfung der Anzahl der anzuzeigenden Elemente
        workoutOrRound();
        int start_elements_top=4;
        int xq = 8;
        quantElements = runList.size();
        if(quantElements<8)  xq=quantElements;
        if(wo_pointer<=start_elements_top) start_pointer=0;
        if(wo_pointer>start_elements_top) start_pointer=wo_pointer-start_elements_top;
        if(wo_pointer>=(quantElements-xq+start_elements_top)) start_pointer=quantElements-xq;

        for (int x = 0; x < xq; x++) {
            tvList.get(x).setText(runList.get(x+ start_pointer));
            tvList.get(x).setTextColor(colorList.get(propList.get(x+ start_pointer)));
            if(statList.get(x+start_pointer)>(long)0)  tvList.get(x).setTextColor(colorList.get(2)); //Falls timestamp schon gesetzt, ausgrauen
        }
    }
    private void calc_view() {
        //Breite der Balken berechnen: Balkenlänge=secondWidth*Zeit des WOs
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int distanceLeft=tvList.get(7).getLeft();
        int calcWidth=screenWidth-distanceLeft-distanceLeft;
        if(distanceLeft==0)calcWidth=screenWidth*85/100;
        if(timestampStart!=0L) {
            if (timestampAdd == 0L) {
                if ((System.currentTimeMillis() / 1000) - (timestampStart / 1000) > defaultPeriod) {
                    defaultPeriod = defaultPeriod * 15 / 10;
                    secondWidth = calcWidth / defaultPeriod;
                }
            } else {
                if ((System.currentTimeMillis() / 1000) - (timestampAdd / 1000) > defaultPeriod) {
                    defaultPeriod = defaultPeriod * 15 / 10;
                    secondWidth = calcWidth / defaultPeriod;
                }
            }
        }
        secondWidth=(double)calcWidth/defaultPeriod;

        //Toast.makeText(this,  String.valueOf(distanceLeft)+"|"+String.valueOf(calcWidth)+"|"+String.valueOf(defaultPeriod), Toast.LENGTH_LONG).show();
    }
//         1440 471 429196
    private void run_view() {
        ViewGroup container = (ViewGroup) findViewById(R.id.container);
        container.removeAllViews();
        runView rv = new runView(this);
        container.addView(rv, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }
    private void run_view_ghost() {
        ViewGroup container_ghost = (ViewGroup) findViewById(R.id.container_ghost);
        container_ghost.removeAllViews();
        runViewGhost rvg = new runViewGhost(this);
        container_ghost.addView(rvg, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    private void init_view() {
/*        ViewGroup container = (ViewGroup) findViewById(R.id.container);
        container.removeAllViews();
        runView rv = new runView(this);
        container.addView(rv, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);*/
        for (int rLx = 0; rLx < counter_rounds; rLx++) {
            int r=rLx+1;
            runList.add(getString(R.string.round)+" "+r+"/"+counter_rounds);
            propList.add(1);
            statList.add((long) 0);
            timeList.add((long) 0);
            for (int wox = 0; wox < roundList[rLx].size(); wox++) {
                runList.add(String.valueOf(roundList[rLx].get(wox)));
                propList.add(0);
                statList.add((long) 0);
                timeList.add((long) 0);
            }
        }
        tvList.add((TextView) this.findViewById(R.id.practice_1));
        tvList.add((TextView) this.findViewById(R.id.practice_2));
        tvList.add((TextView) this.findViewById(R.id.practice_3));
        tvList.add((TextView) this.findViewById(R.id.practice_4));
        tvList.add((TextView) this.findViewById(R.id.practice_5));
        tvList.add((TextView) this.findViewById(R.id.practice_6));
        tvList.add((TextView) this.findViewById(R.id.practice_7));
        tvList.add((TextView) this.findViewById(R.id.practice_8));


        colorList.add(getResources().getColor(R.color.colorWoTextViewNormal));
        colorList.add(getResources().getColor(R.color.colorWoTextViewRound));
        colorList.add(getResources().getColor(R.color.colorWoTextViewDone));

        hideView(R.id.time_pb);
        hideView(R.id.time_lt);

        text_pb=dataSource.getMinDuration(quantity,TextName,type); //Abfrage DB kürzestes WO
        //Toast.makeText(this, "getMinDuration:"+String.valueOf(quantity)+" "+TextName+" "+String.valueOf(type), Toast.LENGTH_LONG).show();
        if(!text_pb.equals("")) {
            showView(R.id.time_pb);
            ((TextView) findViewById(R.id.time_pb)).setText(text_pb);
            datasGhost=dataSource.getMinDurationGhost(quantity,TextName,type); //Abfrage Ghostdaten in DB für kürzestes WO
            Toast.makeText(this, "getMinDurationGhost:"+String.valueOf(quantity)+" "+TextName+" "+String.valueOf(type), Toast.LENGTH_LONG).show();
        }
        text_lt=dataSource.getMaxStartTime(quantity,TextName,type); //Abfrage DB letztes WO
        //Toast.makeText(this, "getMaxStartTime:"+String.valueOf(quantity)+" "+TextName+" "+String.valueOf(type), Toast.LENGTH_LONG).show();
        if(!text_lt.equals("")) {
            showView(R.id.time_lt);
            ((TextView) findViewById(R.id.time_lt)).setText(text_lt);
            if(datasGhost.equals(""))datasGhost=dataSource.getMaxStartTimeGhost(quantity,TextName,type);
            else datasGhost2=dataSource.getMaxStartTimeGhost(quantity,TextName,type);
            //Toast.makeText(this, "getMaxDurationGhost:"+String.valueOf(quantity)+" "+TextName+" "+String.valueOf(type), Toast.LENGTH_LONG).show();

        }
        if(!datasGhost.equals(""))exDatasGhostString(datasGhost);//Toast.makeText(this, "Ghost:"+ghostList.get(1), Toast.LENGTH_LONG).show();
        //TextView text = (TextView) this.findViewById(R.id.practice_1);//text.setTextColor(Color.parseColor("#999999"))
        //Toast.makeText(this, "TextView: "+String.valueOf(getResources().getColor(R.color.colorWoTextViewNormal)), Toast.LENGTH_LONG).show();
        list_view();
    }
    private void workoutOrRound() {
        if(propList.get(wo_pointer) == 1){wo_pointer++;start_pointer++;}
    }
    private void workoutOrRoundMinus() {
        if(propList.get(wo_pointer) == 1){wo_pointer--;start_pointer--;}
    }

    private void doSpeech(int ctn) {
        if(ctn==5) {
            tts.speak(getString(R.string.speak5), TextToSpeech.QUEUE_FLUSH, null);
        } else  if(ctn==4) {
            tts.speak(getString(R.string.speak4), TextToSpeech.QUEUE_FLUSH, null);
        } else if(ctn==3) {
            tts.speak(getString(R.string.speak3), TextToSpeech.QUEUE_FLUSH, null);
        } else if(ctn==2) {
            tts.speak(getString(R.string.speak2), TextToSpeech.QUEUE_FLUSH, null);
        } else if(ctn==1) {
            tts.speak(getString(R.string.speak1), TextToSpeech.QUEUE_FLUSH, null);
        } else if(ctn==0) {
            tts.speak(getString(R.string.speak0), TextToSpeech.QUEUE_FLUSH, null);
        }
    }
    @Override
    public void onInit(int i) {
        tts.setLanguage(Locale.ENGLISH);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            countup();
        }
    };
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "Die Datenquelle wird geöffnet.");
        dataSource.open();

       // Log.d(LOG_TAG, "Folgende Einträge sind in der Datenbank vorhanden:");
       // showAllListEntries();
    }
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "Die Datenquelle wird geschlossen.");
        dataSource.close();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //wl.release();
   }

    /*    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {

            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/
    class runView extends View
    {
        public runView(Context context) {
            super(context);

        }
        @Override
        protected void onDraw(Canvas canvas)
        {   //Dreieck zeichnen (Pointer)
            super.onDraw(canvas);
            //int y=fillTextView(R.id.practice_1);
            int t=tvList.get(wo_pointer-start_pointer).getTop();
            //left=getLeftList(R.id.practice_1);
            int l=tvList.get(wo_pointer-start_pointer).getLeft();
            left=tvList.get(wo_pointer-start_pointer).getLeft();
            //float h=getHeightList(R.id.practice_1);
            int h=tvList.get(wo_pointer-start_pointer).getHeight();
            //View start = getLayoutInflater().inflate(R.layout.workout_activity, null);
            //((TextView) start.findViewById(R.id.time_lt)).setText(String.valueOf(left));
            Paint paint=new Paint();
            if(theEnd==false) {
                // Use Color.parseColor to define HTML colors
                paint.setColor(Color.parseColor("#CD5C5C"));
                Path triPath = new Path();
                triPath.moveTo(l - (l / 2) - (l / 5), t);
                triPath.lineTo(l - (l / 5), t + (h / 2));
                triPath.lineTo(l - (l / 2) - (l / 5), t + h);
                triPath.close();
                canvas.drawPath(triPath, paint);
                if (timestampAdd == 0L)
                    lengthElement = (int) (secondWidth * ((System.currentTimeMillis()) - (timestampStart)) / 1000);
                else
                    lengthElement = (int) (secondWidth * ((System.currentTimeMillis()) - (timestampAdd)) / 1000);
                paint.setColor(Color.parseColor("#898CEB"));
                canvas.drawRect(l, t, left + lengthElement, t + h, paint);
            }

            if(quantElements>0){
                int quant=8;

                if(quantElements<8) quant=quantElements;
                for(int tvx = 0; tvx < quant; tvx++){
                    if(timeList.get(start_pointer+tvx)>0L){
                        //Toast.makeText(getContext(), "timestamp: "+statList.get(start_pointer+tvx).toString(), Toast.LENGTH_LONG).show();


                       lengthElement= (int) (secondWidth*(timeList.get(start_pointer+tvx)/1000));

                        paint.setColor(Color.parseColor("#ffba60"));
                        t=tvList.get(tvx).getTop();
                        //l=tvList.get(tvx).getLeft();
                        h=tvList.get(tvx).getHeight();
                        canvas.drawRect(left, t, left+lengthElement, t+h, paint);
                    }
                }
            }
            if(quantElements>0 && ghostList.size()>0){
                int quant=8;
                long ghostTime = 0L; //Variable für die Addition der Ghostzeiten
                long currTimeCounter;
                boolean oneGhost=false;
                for(int glx = 0; glx < start_pointer; glx++){
                    ghostTime=ghostTime+ghostList.get(glx);
                }
                if(quantElements<8) quant=quantElements;
                for(int tvx = 0; tvx < quant; tvx++){
                    if(ghostList.get(start_pointer+tvx)>0){
                        //Toast.makeText(getContext(), "timestamp: "+statList.get(start_pointer+tvx).toString(), Toast.LENGTH_LONG).show();



                        t=tvList.get(tvx).getTop();
                        l=tvList.get(tvx).getLeft();
                        h=tvList.get(tvx).getHeight();


                        ghostTime=ghostTime+ghostList.get(start_pointer+tvx);
                        currTimeCounter = (System.currentTimeMillis()) - (timestampStart);
                        if(ghostTime>currTimeCounter){
                            if(oneGhost==false) {
                                double x = secondWidth*((currTimeCounter-ghostTime+ghostList.get(start_pointer + tvx)) / 1000);
                                paint.setColor(Color.parseColor("#ff6060"));
                                canvas.drawRect(left + (int)x, t, left + (int)x+2, t + h, paint);
                                oneGhost=true;

                                Path triPath = new Path();
                                triPath.moveTo(l  + (int)x- (h /3), t-h/3);
                                triPath.lineTo(l  + (int)x+ (h /3), t-h/3);
                                triPath.lineTo(l  + (int)x, t);
                                triPath.close();
                                canvas.drawPath(triPath, paint);
                                Path triPath2 = new Path();
                                triPath2.moveTo(l  + (int)x- (h /3), t+h+h/3);
                                triPath2.lineTo(l  + (int)x, t+h);
                                triPath2.lineTo(l  + (int)x+ (h /3), t+h+h/3);
                                triPath2.close();
                                canvas.drawPath(triPath2, paint);


                            }

                        }

                    }
                }
            }

        }
    }
    class runViewGhost extends View
    {
        public runViewGhost(Context context) {
            super(context);

        }
        @Override
        protected void onDraw(Canvas canvas)
        {
            super.onDraw(canvas);
            left=tvList.get(wo_pointer-start_pointer).getLeft();
            Paint paint=new Paint();
            //Toast.makeText(getContext(), "Ghost:"+ghostList.get(1), Toast.LENGTH_LONG).show();
            if(quantElements>0){
                int quant=8;
                if(quantElements<8) quant=quantElements;
                for(int tvx = 0; tvx < quant; tvx++){
                    if(ghostList.get(start_pointer+tvx)>0){
                        //Toast.makeText(getContext(), "timestamp: "+statList.get(start_pointer+tvx).toString(), Toast.LENGTH_LONG).show();


                        lengthElement= (int) (secondWidth*(ghostList.get(start_pointer+tvx)/1000));

                        paint.setColor(Color.parseColor("#D0D0D0"));
                        int t=tvList.get(tvx).getTop();
                        //l=tvList.get(tvx).getLeft();
                        int h=tvList.get(tvx).getHeight();
                        canvas.drawRect(left, t, left+lengthElement, t+h, paint);
                    }
                }
            }
        }
    }
    private int getTopList(int id) {
        TextView tv = (TextView) findViewById(id);
        int s=tv.getTop();
        return s;
    }
    private int getLeftList(int id) {
        TextView tv = (TextView) findViewById(id);
        int s=tv.getLeft();
        return s;
    }
    private int getHeightList(int id) {
        TextView tv = (TextView) findViewById(id);
        int s=tv.getHeight();
        return s;
    }
    private int getWidthList(int id) {
        TextView tv = (TextView) findViewById(id);
        int s=tv.getWidth();
        return s;
    }
    public void exDatasGhostString(String s)
    // Extrahieren des Ghoststrings, die Einträge sind mit | getrennt
    // Output in ghostList
    {
        String s1;
        boolean cnc; // kommt "|" noch vor? Wenn nicht letzter Durchlauf der Schleife!
        do {
            int n = s.indexOf("|");
            if(n>0) {
                int wert=Integer.valueOf(s.substring(0, n));
                ghostList.add(wert); //kompletter String bis |
                if(wert/1000>defaultPeriod)defaultPeriod=wert/1000;
                cnc=true;
            }else {
                int wert=Integer.valueOf(s);
                ghostList.add(wert); //Rest String
                if(wert/1000>defaultPeriod)defaultPeriod=wert/1000;
                cnc=false;
            }
            if(n>0){s =  s.substring(n+1,s.length());}
        }while(cnc);
    }
    public void exDatasGhostString2(String s)
    // Extrahieren des Ghoststrings, die Einträge sind mit | getrennt
    // Output in ghostList
    {
        String s1;
        boolean cnc; // kommt "|" noch vor? Wenn nicht letzter Durchlauf der Schleife!
        do {
            int n = s.indexOf("|");
            if(n>0) {
                int wert=Integer.valueOf(s.substring(0, n));
                ghostList2.add(wert); //kompletter String bis |
                cnc=true;
            }else {
                int wert=Integer.valueOf(s);
                ghostList2.add(wert); //Rest String
                cnc=false;
            }
            if(n>0){s =  s.substring(n+1,s.length());}
        }while(cnc);
    }
    protected void hideView(int id) {
        findViewById(id).setVisibility(View.GONE);
    }
    protected void showView(int id) {
        findViewById(id).setVisibility(View.VISIBLE);
    }
    public static class ListUtils {
        public static void setDynamicHeight(ListView mListView) {
            ListAdapter mListAdapter = mListView.getAdapter();
            if (mListAdapter == null) {
                // when adapter is null
                return;
            }
            int height = 0;
            int desiredWidth = View.MeasureSpec.makeMeasureSpec(mListView.getWidth(), View.MeasureSpec.UNSPECIFIED);
            for (int i = 0; i < mListAdapter.getCount(); i++) {
                View listItem = mListAdapter.getView(i, null, mListView);
                listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
                height += listItem.getMeasuredHeight();
            }
            ViewGroup.LayoutParams params = mListView.getLayoutParams();
            params.height = height + (mListView.getDividerHeight() * (mListAdapter.getCount() - 1));
            mListView.setLayoutParams(params);
            mListView.requestLayout();
        }
    }
    public String timeFormat(int sec) {
        // Gibt Sekunden als hh:mm:ss aus
        String s;
        int timeSeconds=sec;
        int timeMinutes=timeSeconds/60;
        timeSeconds=timeSeconds-(timeMinutes*60);
        int timeHours=timeMinutes/60;
        timeMinutes=timeMinutes-(timeHours*60);
        if(timeHours>0) s=String.valueOf(timeHours)+":"+String.format("%02d",timeMinutes)+":"+String.format("%02d",timeSeconds);
        else s=String.format("%02d",timeMinutes)+":"+String.format("%02d",timeSeconds);
        return s;
    }

}
