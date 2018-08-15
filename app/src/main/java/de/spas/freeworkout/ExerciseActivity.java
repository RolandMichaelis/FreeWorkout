package de.spas.freeworkout;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.speech.tts.TextToSpeech;

import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;


public class ExerciseActivity extends Activity implements View.OnClickListener {
    private de.spas.freeworkout.specialPack specialPack;
    private de.spas.freeworkout.workoutPack workoutPack;
    private de.spas.freeworkout.exercisePack exercisePack;
    private String xmeter;
    private int counter_practice;
    private String TextName;
    private String TextType = "";
    private int type;
    private int quantity;
    private int rounds;
    private int time; // Festlegung ob Zeitmessung für PB positiv oder negativ gewertet wird: 0=je schneller desto besser, 1=je länger desto besser: für PB-Ausgabe
    private int exQuantHidden; // Festlegung ob Mengenauswahl (0) oder reine Zeitmessung (1)
    private int fromWhere;
    private String[] Types = {"Endurance", "Standard", "Strength",""};
    private int counter_rounds = 0;
    //private ArrayList<ArrayList> roundsList = new ArrayList<ArrayList>();
    private ArrayList[] roundList = new ArrayList[30];
    //private ArrayList<ArrayList> mArraysAdapterList = new ArrayList<ArrayList>();
    //private ArrayList[] mArrayAdapterList = new ArrayList[21];
    private Handler handler = new Handler();
    private int countup = 0;
    private int countdown = 5;
    private String timeString;
    //private TextToSpeech tts;
    private int top;
    private int left;
    private int wo_pointer = 0; // Workout Zeiger
    private int start_pointer = 0; // ab dort wird Liste angezeigt
    private ArrayList<String> runList = new ArrayList<String>(); // Liste alles zusammen (Runden und WOs)
    private ArrayList<TextView> tvList = new ArrayList<TextView>(); // TextViews
    private ArrayList<Integer> propList = new ArrayList<Integer>(); //Art des Eintrags (0=normal; 1=Runde; 2=Pause)
    private ArrayList<Long> statList = new ArrayList<Long>(); //Timestamps
    private ArrayList<Long> timeList = new ArrayList<Long>(); //Sekunden/Exercise
    private ArrayList<Integer> ghostList = new ArrayList<Integer>(); // Liste Ghostzeiten
    private ArrayList<Integer> ghostList2 = new ArrayList<Integer>(); // Liste Ghostzeiten LT
    private Long timestampStart = 0L;
    private Long timestampAdd = 0L;
    private Long timestampCurr;
    private ArrayList<Integer> colorList = new ArrayList<Integer>(); //Farbzuordnung
    private int quantElements = 0; //Menge der Elemente in der runList
    private int defaultPeriod = 30;
    private double secondWidth;
    private int lengthElement;
    private boolean rest = false; //keine Pause = false;
    public static final String LOG_TAG = ExerciseActivity.class.getSimpleName();
    private WorkoutMemoDataSource dataSource;
    private ListView mWorkoutMemosListView;
    private boolean theEnd = false;
    private int wore;
    private int number;
    private String text_pb;
    private String text_lt;
    PowerManager pm;
    PowerManager.WakeLock wl;
    private String datasGhost = "";
    private String datasGhost2 = ""; // Wenn LT und PB vorhanden sind
    final Context context = this;
    String roundlistLastRest = "";
    int counter_rounds_pre_add;
    private String spinnerQuantityListType[];
    private int checked_day; //Vom Coach übergebener Tag für anschließendes Demarkieren des absolvierten Workouts, Wert "-1" wenn nicht vom Coach
    private int checked_pos; //Vom Coach übergebene Position am Tag für anschließendes Demarkieren des absolvierten Workouts, Wert "-1" wenn nicht vom Coach
    private int positionOfQuantity;
    private Spinner spSpinnerType;
    private Boolean showPlus=false;
    private MediaPlayer mpMusic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.exercise_activity);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl =  pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My WakeLock");
        if(!wl.isHeld())wl.acquire();
        dataSource = new WorkoutMemoDataSource(this);
        rounds=0;

        try {
            InputStream source = getAssets().open("exercises.xml");
            Serializer serializer = new Persister();
            exercisePack = serializer.read(de.spas.freeworkout.exercisePack.class, source);
            //Toast.makeText(this, "Wow! Klappt!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Oh oh! exercisePack", Toast.LENGTH_LONG).show();
            Log.e(getClass().getSimpleName(), "loading levels threw exception", e);
        }
        Intent empfangenerIntent = this.getIntent();
        if (empfangenerIntent != null && empfangenerIntent.hasExtra(Intent.EXTRA_TEXT)) {
            //Workout/Special,Workout/Exercise-Nummer,Type(Strth,Std,End.),Anzahl
            String s = empfangenerIntent.getStringExtra(Intent.EXTRA_TEXT);
            wore = Integer.valueOf(s.substring(0, 1)); //wore = Workout oder Exercise
            String s1 = s.substring(2, s.length());
            int n = s1.indexOf(",");
            number = Integer.valueOf(s1.substring(0, n));

            s1 = s1.substring(n+1, s1.length());
            n = s1.indexOf(",");
            type = Integer.valueOf(s1.substring(n - 1, n));
            s1 = s1.substring(n+1, s1.length());
            n = s1.indexOf(",");
            quantity = Integer.valueOf(s1.substring(0, n));
            s1 = s1.substring(n+1, s1.length());
            n = s1.indexOf(",");
            fromWhere = Integer.valueOf(s1.substring(0, n));
            s1=s1.substring(n+1,s1.length());
            n = s1.indexOf(",");
            checked_day = Integer.valueOf(s1.substring(0,n));
            s1=s1.substring(n+1,s1.length());
            n = s1.indexOf(",");
            checked_pos = Integer.valueOf(s1.substring(0,n));
            //Toast.makeText(this, String.valueOf(type)+"|"+String.valueOf(quantity)+"|"+String.valueOf(fromWhere) , Toast.LENGTH_LONG).show();
        }
        Exercise w = exercisePack.getExercises().get(number);
        TextName = w.getName();
        time = w.getTime();
        exQuantHidden = w.getQuantHidden();
        printTitle();
        // Erzeugen einer Instanz von GetDataTask und starten des asynchronen Tasks
        GetDataTask getDataTask = new GetDataTask();
        getDataTask.execute("PBLT");
        //text_pb = dataSource.getMinDuration(quantity, TextName, type);
        /*if (!text_pb.equals("")) {
            showView(R.id.time_pb);
            ((TextView) findViewById(R.id.time_pb)).setText(text_pb);
            //Toast.makeText(this, "getMinDuration:"+text_pb, Toast.LENGTH_LONG).show();

        }
        text_lt = dataSource.getMaxStartTime(quantity, TextName, type); //Abfrage DB letzte EX
        //Toast.makeText(this, "getMaxStartTime:"+String.valueOf(quantity)+" "+TextName+" "+String.valueOf(type), Toast.LENGTH_LONG).show();
        if (!text_lt.equals("")) {
            showView(R.id.time_lt);
            ((TextView) findViewById(R.id.time_lt)).setText(text_lt);
            //Toast.makeText(this, "getMaxDurationGhost:"+String.valueOf(quantity)+" "+TextName+" "+String.valueOf(type), Toast.LENGTH_LONG).show();
        }*/

        //Toast.makeText(this, "getMinDuration:"+text_pb, Toast.LENGTH_LONG).show();
        if(xmeter.equals(" x ")) {
            spinnerQuantityListType = getResources().getStringArray(R.array.spinnerQuantityListType);
        }
        else if(xmeter.equals(" m ") && !TextName.equals("Run")){
            spinnerQuantityListType = getResources().getStringArray(R.array.spinnerQuantityListTypeMeter);
        }
        /*else if(xmeter.equals(" m ") && TextName.equals("Sprint")){
            spinnerQuantityListType = getResources().getStringArray(R.array.spinnerQuantityListTypeMeter);
        }*/
        else if(xmeter.equals(" m ") && TextName.equals("Run")){
            spinnerQuantityListType = getResources().getStringArray(R.array.spinnerQuantityListTypeRunMeter);
        }
        if(exQuantHidden==0){
        // exQuantHidden = Soll Spinner angezeigt werden? Mengenauswahl gewünscht?
            if(fromWhere==1) {
                showView(R.id.edit_spinner_quantity);
                ArrayAdapter<String> adapterSpinnerType;
                spSpinnerType = (Spinner) this.findViewById(R.id.edit_spinner_quantity);
                adapterSpinnerType = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinnerQuantityListType);
                adapterSpinnerType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                spSpinnerType.setAdapter(adapterSpinnerType);


                if(xmeter.equals(" x ")) {
                    int selectionPosition = adapterSpinnerType.getPosition(String.valueOf(quantity) + "x");
                    if (selectionPosition != -1) spSpinnerType.setSelection(selectionPosition);
                }
                else if(xmeter.equals(" m ")){
                    if(quantity==20)spSpinnerType.setSelection(0);
                    else if(quantity==40)spSpinnerType.setSelection(1);
                    else if(quantity==80)spSpinnerType.setSelection(2);
                    else if(quantity>80){
                        int selectionPosition = adapterSpinnerType.getPosition(String.valueOf(quantity) + "m");
                        if (selectionPosition != -1) spSpinnerType.setSelection(selectionPosition);
                    }

                }
                spSpinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapter, View v,
                                               int position, long id) {
                        // On selecting a spinner item

                        String s1=adapter.getItemAtPosition(position).toString();
                        s1 = s1.substring(0, s1.length()-1);
                        if(xmeter.equals(" m ")){
                            if(s1.equals("2x 10"))s1="20";
                            else if(s1.equals("2x 20"))s1="40";
                            else if(s1.equals("2x 40"))s1="80";
                        }
                        if (quantity != Integer.valueOf(s1)) {
                            quantity = Integer.valueOf(s1);
                            //Toast.makeText(MainActivity.this, "days = "+ String.valueOf(days), Toast.LENGTH_LONG).show();
                            printTitle();
                            GetDataTask holeDatenTask = new GetDataTask();
                            holeDatenTask.execute("PBLT");
                            check_for_add();
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {
                        //nothing to do
                    }
                });
            }
        }

        check_for_add();
    }
    private void check_for_add(){
        String s1=String.valueOf(quantity);
        if(xmeter.equals(" m ")){
            if(quantity==20)s1="2x 10";
            else if(quantity==40)s1="2x 20";
            else if(quantity==80)s1="2x 40";
            positionOfQuantity= Arrays.asList(spinnerQuantityListType).indexOf(s1 + "m");
        }
        else{
            positionOfQuantity= Arrays.asList(spinnerQuantityListType).indexOf(s1 + "x");
        }
        int size=Arrays.asList(spinnerQuantityListType).size();
        if(positionOfQuantity+1!=size){showView(R.id.button_ex_add);showPlus=true;}
        else{hideView(R.id.button_ex_add);showPlus=false;}
        //Toast.makeText(this, "spinnerQuantityListType = "+String.valueOf(n), Toast.LENGTH_LONG).show();


    }
    public class GetDataTask extends AsyncTask<String, Integer, String[]> {
        @Override
        protected String[] doInBackground(String... params) {
            return new String[0];
        }
        @Override
        protected void onPostExecute(String[] strings) {

            // Wir löschen den Inhalt des ArrayAdapters und fügen den neuen Inhalt ein
            // Der neue Inhalt ist der Rückgabewert von doInBackground(String...) also
            // der StringArray gefüllt mit Daten
            hideView(R.id.time_pb);
            hideView(R.id.time_lt);
            text_pb="";
            text_lt="";
            if(time==0){
                text_pb = dataSource.getMinDuration(quantity, TextName, type);
            }
            else
            {
                text_pb = dataSource.getMaxDuration(quantity, TextName, type);

            }
            if (!text_pb.equals("")) {
                showView(R.id.time_pb);
                ((TextView) findViewById(R.id.time_pb)).setText(text_pb);
                //Toast.makeText(this, "getMinDuration:"+text_pb, Toast.LENGTH_LONG).show();

            }
            text_lt = dataSource.getMaxStartTime(quantity, TextName, type); //Abfrage DB letzte EX
            //Toast.makeText(this, "getMaxStartTime:"+String.valueOf(quantity)+" "+TextName+" "+String.valueOf(type), Toast.LENGTH_LONG).show();
            if (!text_lt.equals("")) {
                showView(R.id.time_lt);
                ((TextView) findViewById(R.id.time_lt)).setText(text_lt);
                //Toast.makeText(this, "getMaxDurationGhost:"+String.valueOf(quantity)+" "+TextName+" "+String.valueOf(type), Toast.LENGTH_LONG).show();
            }
            //Toast.makeText(ExerciseActivity.this, "PB/LT vollständig geladen!",Toast.LENGTH_SHORT).show();
        }

    }

    private void countup() {
        if (countdown > 0) {
            handler.postDelayed(runnable, 1000);
            doSpeech(countdown);
            ((TextView) findViewById(R.id.clock)).setText(String.valueOf(countdown));
            countdown--;
        } else {
            if (timestampStart == 0L) timestampStart = System.currentTimeMillis();
            int cup = (int) ((System.currentTimeMillis() - timestampStart) / 1000);
            ((TextView) findViewById(R.id.clock)).setText(timeFormat(cup));
            if (countup == 0) {
                doSpeech(0);
            }
            handler.postDelayed(runnable, 1000);
            countup++;
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: // Activity Bar Back Button override to go to previous Activity
                dialog_cancel();
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
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        //if(wl.isHeld())wl.release();
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

    private void doSpeech(int ctn) {
        if(ctn==5) {
            cleanSounds();
            mpMusic = MediaPlayer.create(this, R.raw.five);
            mpMusic.start();
            //tts.speak(getString(R.string.speak5), TextToSpeech.QUEUE_FLUSH, null);
        } else  if(ctn==4) {
            cleanSounds();
            mpMusic = MediaPlayer.create(this, R.raw.four);
            mpMusic.start();
            //tts.speak(getString(R.string.speak4), TextToSpeech.QUEUE_FLUSH, null);
        } else if(ctn==3) {
            cleanSounds();
            mpMusic = MediaPlayer.create(this, R.raw.three);
            mpMusic.start();
            //tts.speak(getString(R.string.speak3), TextToSpeech.QUEUE_FLUSH, null);
        } else if(ctn==2) {
            cleanSounds();
            mpMusic = MediaPlayer.create(this, R.raw.two);
            mpMusic.start();
            //tts.speak(getString(R.string.speak2), TextToSpeech.QUEUE_FLUSH, null);
        } else if(ctn==1) {
            cleanSounds();
            mpMusic = MediaPlayer.create(this, R.raw.one);
            mpMusic.start();
            //tts.speak(getString(R.string.speak1), TextToSpeech.QUEUE_FLUSH, null);
        } else if(ctn==0) {
            cleanSounds();
            mpMusic = MediaPlayer.create(this, R.raw.go);
            mpMusic.start();
            //tts.speak(getString(R.string.speak0), TextToSpeech.QUEUE_FLUSH, null);
        }
    }
/*    @Override
    public void onInit(int i) {
        //tts.setLanguage(Locale.ENGLISH);
    }*/


    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.button_wo_start) {
            //Toast.makeText(ExerciseActivity.this, "Exercise starten", Toast.LENGTH_LONG).show();
            //statList.set(0,tsLong);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            handler.postDelayed(runnable, 1000);
            hideView(R.id.button_wo_start);
            hideView(R.id.edit_spinner_quantity);
            //tts = new TextToSpeech(ExerciseActivity.this,ExerciseActivity.this);
        }
        if(view.getId()==R.id.container) {
            //Ende Exercise nur wenn countup schon läuft:
            if(countup>0) {

                //Stopp!!!
                handler.removeCallbacks(runnable);
                timestampCurr=System.currentTimeMillis();
                int cup= (int) ((timestampCurr-timestampStart)/1000);
                ((TextView) findViewById(R.id.clock)).setText(timeFormat(cup));
                //hideView(R.id.button_wo_add);
                dialog_finish();
                //Toast.makeText(this, "Ich habe fertig!", Toast.LENGTH_LONG).show();

                //String tx=String.valueOf(lengthElement)+"|"+String.valueOf(secondWidth)+"|"+String.valueOf(wo_pointer)+"|"+String.valueOf(quantElements);
                //((TextView) findViewById(R.id.time_lt)).setText(String.valueOf(wo_pointer));

            }
        }
        if(view.getId()==R.id.button_ex_add) {
            exercise_add();
            check_for_add();
            //Toast.makeText(this, "Ich habe fertig! "+String.valueOf(positionOfQuantity+1), Toast.LENGTH_LONG).show();

        }
    }
    final void exercise_add(){
        String s1=spinnerQuantityListType[positionOfQuantity+1];
        s1 = s1.substring(0, s1.length()-1);
        if(xmeter.equals(" m ")){
            if(s1.equals("2x 10"))s1="20";
            else if(s1.equals("2x 20"))s1="40";
            else if(s1.equals("2x 40"))s1="80";
        }
        if (quantity != Integer.valueOf(s1)) {
            quantity = Integer.valueOf(s1);
            //Toast.makeText(MainActivity.this, "days = "+ String.valueOf(days), Toast.LENGTH_LONG).show();
            printTitle();
            GetDataTask holeDatenTask = new GetDataTask();
            holeDatenTask.execute("PBLT");
            if(fromWhere==1)spSpinnerType.setSelection(positionOfQuantity+1);
        }
    }
    public void dialog_finish() {
        AlertDialog.Builder alertDialogBuilder =  new AlertDialog.Builder(
                new ContextThemeWrapper(this, android.R.style.Theme_DeviceDefault_Light_Dialog));
        // set title
        //alertDialogBuilder.setTitle("Your Title");

        // set dialog message
        alertDialogBuilder
                .setMessage(R.string.text_wo_finish)
                .setCancelable(false)
                .setPositiveButton("Speichern",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, close
                        // current activity
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        if(wl.isHeld())wl.release();
                        String tlt="";
                        //Toast.makeText(ExerciseActivity.this, String.valueOf(wore)+"|"+ String.valueOf(number)+"|"+ String.valueOf(TextName)+"|"+ String.valueOf(type)+"|"+ String.valueOf(quantity)+"|"+ String.valueOf(timestampStart)+"|"+ String.valueOf(timestampCurr), Toast.LENGTH_LONG).show();
                        type=3;
                        dataSource.createWorkoutMemo(wore, number, TextName, type, quantity, rounds, timestampStart, timestampCurr, timestampCurr-timestampStart,tlt,false,false,false);
                        //text_pb = dataSource.getMinDuration(quantity, TextName, type);
                        //Toast.makeText(ExerciseActivity.this, "getMinDuration:"+text_pb, Toast.LENGTH_LONG).show();
                        //finish();
                        Intent workoutFragmentIntent;
                        if(checked_day!=-1) {
                            String dur = timeFormat ((int)((timestampCurr-timestampStart)/1000));
                            workoutFragmentIntent = new Intent(ExerciseActivity.this, MainActivity.class);
                            Boolean star=false; // Platzhalter für später
                            //Übergabe an Coach: wore, name, quantity, type, Startzeit, Länge Format hh:mm:ss, star, checked_day, checked_pos
                            workoutFragmentIntent.putExtra(Intent.EXTRA_TEXT, String.valueOf(wore)+","+TextName+","+String.valueOf(quantity)+","+String.valueOf(type)+","+String.valueOf(timestampStart)+","+dur+","+String.valueOf(star)+","+String.valueOf(checked_day)+","+String.valueOf(checked_pos));
                            startActivity(workoutFragmentIntent);
                        } else {
                            finish();
                        }

                    }
                })
                .setNegativeButton("ZURÜCK",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        //showView(R.id.button_wo_add);
                        handler.postDelayed(runnable, 1000);
                        dialog.cancel();
                    }
                })
                .setNeutralButton("+", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        exercise_add();
                        check_for_add();
                        handler.postDelayed(runnable, 1000);
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
        Button neutralButton = alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL);
        if(!showPlus)((AlertDialog)alertDialog).getButton(AlertDialog.BUTTON_NEUTRAL).setVisibility(View.GONE);

    }

    protected void hideView(int id) {
        findViewById(id).setVisibility(View.GONE);
    }
    protected void showView(int id) {
        findViewById(id).setVisibility(View.VISIBLE);
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
        if(wl.isHeld())wl.release();
        cleanSounds();
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
    private void printTitle(){
        xmeter = " x ";
        String xhalf = "";
        int q = quantity;
        if (TextName.equals("Sprint")) xmeter = " m ";
        if (TextName.equals("Run")) xmeter = " m ";
        if (TextName.equals("Lunge Walk"))  xmeter = " m ";
        if (TextName.equals("HH Lunge Walk"))  xmeter = " m ";
        if (TextName.equals("Sprawl Frogs"))  xmeter = " m ";
        if (TextName.equals("Burpee Frogs"))  xmeter = " m ";
        if (TextName.equals("Burpee Deep Frogs"))  xmeter = " m ";
        if (xmeter.equals(" m ") && q < 100) {
            q = q / 2;
            xhalf = "2x ";
        }
        if(exQuantHidden==0) {
            this.setTitle(xhalf + q + xmeter + TextName);
        }
        else {
            this.setTitle(TextName);
        }
    }
    private void cleanSounds(){
        if(mpMusic!=null) {
            mpMusic.stop();
            try {
                mpMusic.reset();
                mpMusic.prepare();
                mpMusic.stop();
                mpMusic.release();
                mpMusic=null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}