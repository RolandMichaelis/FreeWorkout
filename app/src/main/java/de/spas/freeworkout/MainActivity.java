package de.spas.freeworkout;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;

import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import android.widget.ProgressBar;

public class MainActivity extends Activity implements View.OnClickListener{

    public static final String LOG_TAG = MainActivity.class.getSimpleName();
    private de.spas.freeworkout.workoutPack workoutPack;
    private de.spas.freeworkout.workoutNewPack workoutNewPack;
    private de.spas.freeworkout.specialPack specialPack;
    private de.spas.freeworkout.exercisePack exercisePack;
    private int workoutCount;
    private int specialCount;
    private int specialProcent = 33; // Wahrscheinlichkeit das ein Special ausgewählt wird;
    private String[] Types = {"Endurance","Standard","Strength",""};
    int[] checked = {0,0,0,0,0,0,0};
    int[] binaerArray = {1, 2, 4, 8, 16, 32, 64, 128, 256};

    private int datestamp;
    private int fokus = 1;
    private int skills = 9;
    private int days = 3;
    private int hardness = 2;
    private boolean changeprefs = false;
    private int sp_changeprefs;
    private Button neutralButton;
    private int current_days=3; // Verwendung in der printWorkout() damit nicht days aus Prefs verwendet wird
    private boolean calc=false; // Berechnen der neuen Coachwoche auf dem Server==true! Hinterlegt für Testuser, da bisher Berechnung nur auf dem Client möglich ist


    //private String [] WorkoutListArray1;
    private List<String> WorkoutListArray0 = new ArrayList<String>();
    private List<String> WorkoutListArray1 = new ArrayList<String>();
    private List<String> WorkoutListArray2 = new ArrayList<String>();
    private List<String> WorkoutListArray3 = new ArrayList<String>();
    private List<String> WorkoutListArray4 = new ArrayList<String>();
    private List<String> WorkoutListArray5 = new ArrayList<String>();
    private List<String> WorkoutListArray6 = new ArrayList<String>();
    private List<String> WorkoutListArray7 = new ArrayList<String>();
    private List<String> WorkoutListArrayX = new ArrayList<String>();
    private List<String> WorkoutListArrayP = new ArrayList<String>();
    private List<String> WorkoutListArrayTest = new ArrayList<String>();
    private List<String> WorkoutListArraySpecial = new ArrayList<String>();
    private List<String> WorkoutListArraySpecialX = new ArrayList<String>();

    private List<String> WorkoutListArrayPShadow = new ArrayList<String>();
    private List<String> WorkoutListArrayShadow1 = new ArrayList<String>();
    private List<String> WorkoutListArrayShadow2 = new ArrayList<String>();
    private List<String> WorkoutListArrayShadow3 = new ArrayList<String>();
    private List<String> WorkoutListArrayShadow4 = new ArrayList<String>();
    private List<String> WorkoutListArrayShadow5 = new ArrayList<String>();
    private List<String> WorkoutListArrayShadow6 = new ArrayList<String>();
    private List<String> WorkoutListArrayShadow7 = new ArrayList<String>();

    private String spWorkoutList1;
    private String spWorkoutList2;
    private String spWorkoutList3;
    private String spWorkoutList4;
    private String spWorkoutList5;
    private String spWorkoutList6;
    private String spWorkoutList7;

    private ArrayList<String> spWO_strlist = new ArrayList<String>(); // Strings

    private int ch_wore;
    private String ch_name;
    private int ch_quantity;
    private int ch_type;
    long ch_startTime;
    String ch_duration;
    Boolean ch_star;
    int ch_checked_day;
    int ch_checked_pos;
    private List<Object> spListArray = new ArrayList<Object>();
    private String authCode;
    private Long lastAuthDatestamp=0L;
    private boolean coachUser;
    private String editTextEmail;
    private String editTextPassword;
    private int customID;
    private String authkey = "koBF89p0KGoO"; //auf Server ausweisen
    private long lastUpdate;
    private boolean history2update = false; // Wenn true dann liegt ein neues WO in der History (SQLite) zum Abspeichern bereit
    private String method;
    private String methodWO;
    private WorkoutMemoDataSource dataSource;
    final Context context = this;
    private List<WorkoutMemo> workoutMemoList;
    private int howMuchWOsClient;
    private int restOf; //zum Zählen der noch herunterzuladenden Workouts vom Server
    private int howMuchWOsServer;
    private int counterWO;
    private int wore; //Workout oder Exercise: 0=WO, 1=Ex
    private int number;
    private String name;
    private int type;
    private int quantity;
    private int rounds;
    private long startTime;
    private long endTime;
    private int duration;
    private String exTimes;
    private boolean star;
    private boolean checkedWO;
    private long startTimeDownload;
    private ProgressBar progressBar;
    private int progressStatus = 0;



/*
Binärwerte für Skills:
1 Pullups
2 HS Pushups
4 OH Pushups
8 Toes to Bar
16 Pistol Squats
32 Muscleups
*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);
        dataSource = new WorkoutMemoDataSource(this);
        Log.i(LOG_TAG, "Die Datenquelle wird geöffnet.");
        dataSource.open();
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
            InputStream source = getAssets().open("workouts_new.xml");
            Serializer serializer = new Persister();
            workoutNewPack = serializer.read(de.spas.freeworkout.workoutNewPack.class, source);
            //Toast.makeText(this, "Wow! Klappt!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            //Toast.makeText(this, "Oh oh! workoutNewPack", Toast.LENGTH_LONG).show();
            Log.e(getClass().getSimpleName(), "loading levels threw exception", e);
        }
        try {
            InputStream source = getAssets().open("exercises.xml");
            Serializer serializer = new Persister();
            exercisePack = serializer.read(de.spas.freeworkout.exercisePack.class, source);
            //Toast.makeText(this, "Wow! Klappt!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            //Toast.makeText(this, "Oh oh! exercisePack", Toast.LENGTH_LONG).show();
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
       // ActionBar actionBar = getActionBar();
// hide the action bar
        //actionBar.hide();

// show the action bar
       // actionBar.show();
        loadDate();
        checkForExtraText();
        this.findViewById(R.id.button_prefs).setOnClickListener(this);
        this.findViewById(R.id.button_calc).setOnClickListener(this);

//        List<String> WorkoutListArrayShadow7 = new ArrayList<String>();

//        ArrayList<ArrayList<String>> x=new ArrayList<ArrayList<String>>(10);
//        for(int i=0; i<10; i++) x.set(i,new ArrayList<String>());



/*        JTextfield[] arrayName = new JTextfield[anzahlDerFelder];
        for (int i = 1; i <= anzahl; i++) {
            JTextfield arrayName[i] = new JTextField(15);
        }

        String Test[];
        Test = new String[10];
        for(int i = 0; i < 10; i++) Test[i] = "Testliste_"+i;

        for(int i = 0; i < 10; i++) JTextfield arrayName[i] = new JTextField(15);;*/

        checkForLogin();
        //updateData();



        // Testbereich Anfang

        //Toast.makeText(this, "Test: " + w.getEndurance().getRounds().get(0).getPractice().get(1).getName(), Toast.LENGTH_LONG).show();
        //Toast.makeText(this, "datestamp: "+String.valueOf(current_days)+" "+String.valueOf(datestamp)+" "+String.valueOf(Integer.parseInt(getDateAsString())), Toast.LENGTH_LONG).show();


        // Ende Testbereich

    }

        @Override
    public void onClick(View view) {
/*            if(view.getId()==R.id.forgot_pw) {
                //Toast.makeText(getBaseContext(), "Button forgot pw", Toast.LENGTH_LONG).show();
                this.cancel();
            }*/
            if(view.getId()==R.id.button_prefs) {
                //Toast.makeText(getBaseContext(), "Button ", Toast.LENGTH_LONG).show();
                dialog_settings();
            }
            if(view.getId()==R.id.button_calc) {
                //Toast.makeText(getBaseContext(), "Button Calc", Toast.LENGTH_LONG).show();
                generalList();
                checkForCalc(); // Prüfen ob auf Server oder beim Client die Workoutwoche berechnet werden soll
                //WorkoutCalc();
                saveDate();
                printWorkout();
            }
        }
    private void checkForCalc() {
        Log.i(LOG_TAG, "calc="+calc);

        if(isConnectingToInternet(MainActivity.this)) {
            if(calc){
                // Berechnung der neuen Coachwoche auf dem Server, da für den User so auf DB eingestellt (Wert CALC auf TRUE)
                method="calc";

                 String[] inquiry ={"authcode="+authCode+"&customid="+String.valueOf(customID)+"&days="+String.valueOf(days)+"&focus="+String.valueOf(fokus)};
                //Toast.makeText(MainActivity.this, "emailPass = "+ emailPass[0]+" "+ emailPass[1], Toast.LENGTH_LONG).show();
                GetServerTask getServerTask = new GetServerTask();
                new GetServerTask().execute(inquiry);

            }
            else {
                // Berechnung der neuen Coachwoche auf dem Client, da für den User so auf DB eingestellt (Wert CALC auf FALSE)
                WorkoutCalc();
            }
        }
        else {
            // Berechnung der neuen Coachwoche auf dem Client, da kein Internet verfügbar ist
            WorkoutCalc();
        }
    }
        private void checkForLogin(){
        // authCode vom Server erhalten nach erfolgreichem Login
        //lastAuthDatestamp = hinterlegter datestamp nach letztem erfolgreichem Login
        //SharedPreferences sp = getPreferences(MODE_PRIVATE);
        if(authCode.equals("")){
            //ToDo: Login oder Registrierung
            Toast.makeText(getApplicationContext(),"Login oder Registrierung erforderlich!",Toast.LENGTH_LONG).show();
            dialog_login();
        }
        else
        {
            if(isConnectingToInternet(MainActivity.this)) {
                //ToDo: hier dann AuthCode auf dem Server checken wenn lastAuthDatestamp älter als 1h
                //Toast.makeText(getApplicationContext(),"internet is available "+String.valueOf(Long.parseLong(getDateAsString())-360000L),Toast.LENGTH_LONG).show();
                /*if(lastAuthDatestamp>Long.parseLong(getDateAsString())-360000L){
                    printWorkout();
                }*/
                /*else {*/
                    //Toast.makeText(getApplicationContext(),"Neu einloggen "+String.valueOf(Long.parseLong(getDateAsString())-360000L),Toast.LENGTH_LONG).show();
                    //Prüfung des AuthCodes auf dem Server
                    updateCheckData();
                    updateHistory();
                    downloadHistory();
                    printWorkout();
                //}
            }
            else {
                //ToDo: hier dann lastAuthDatestamp check (4 weeks)
                Toast.makeText(getApplicationContext(),"Internet is not available!",Toast.LENGTH_LONG).show();
                printWorkout();
            }

        }

    }
    @Override
    protected void onResume() {
        super.onResume();
        //checkForLogin();
        // Log.d(LOG_TAG, "Folgende Einträge sind in der Datenbank vorhanden:");
        // showAllListEntries();
    }
    @Override
    protected void onPause() {
        super.onPause();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "Die Datenquelle wird geschlossen.");
        dataSource.close();
    }
    private class WOdaten2clientTask extends AsyncTask<Integer, Integer, String> {
    // Download der Workout Daten vom Server zum Client

        //WorkoutMemo workoutMemo;
        //WorkoutMemo memo = (WorkoutMemo) workoutMemoList.get(0);

        private void flexWOstring(String s){
            wore=Integer.valueOf(s.substring(0,1));
            int n = s.indexOf(";");
            s=s.substring(n+1,s.length());
            n = s.indexOf(";");
            number = Integer.valueOf(s.substring(0,n));
            s=s.substring(n+1,s.length());
            n = s.indexOf(";");
            name=s.substring(0,n);
            s=s.substring(n+1,s.length());
            n = s.indexOf(";");
            type = Integer.valueOf(s.substring(0,n));
            s=s.substring(n+1,s.length());
            n = s.indexOf(";");
            quantity = Integer.valueOf(s.substring(0,n));
            s=s.substring(n+1,s.length());
            n = s.indexOf(";");
            startTime = Long.valueOf(s.substring(0,n));
            s=s.substring(n+1,s.length());
            n = s.indexOf(";");
            endTime = Long.valueOf(s.substring(0,n));
            s=s.substring(n+1,s.length());
            n = s.indexOf(";");
            duration = Integer.valueOf(s.substring(0,n));
            s=s.substring(n+1,s.length());
            n = s.indexOf(";");
            exTimes=s.substring(0,n);
            s=s.substring(n+1,s.length());
            n = s.indexOf(";");
            star = (!s.substring(0,n).equals("0"));
            s=s.substring(n+1,s.length());
            n = s.indexOf(";");
            checkedWO = (!s.substring(0,n).equals("0"));
            /*memo.setWore(wore);
            memo.setNumber(number);
            memo.setName(name);
            memo.setType(type);
            memo.setQuantity(quantity);
            memo.setStartTime(startTime);
            memo.setEndTime(endTime);
            memo.setDuration(duration);
            memo.setExTimes(exTimes);
            memo.setStar(star);
            memo.setChecked(checked);*/
        }

        protected String doInBackground(Integer... params) {
            //Lädt einzelne Workouts herunter S2C mit laufender Nummer

            //WorkoutMemo memo = (WorkoutMemo) workoutMemoList[0];
            //int n = params[0];
            //MainActivity.this.setTitle(String.valueOf(memo.getName()));
            //WorkoutMemo memo = (WorkoutMemo) workoutMemoList.getItemAtPosition(0);
            String inquiry;

            inquiry = "authcode=" + authCode + "&customid=" + String.valueOf(customID) + "&count=" + counterWO;
            final String URL_PARAMETER = "https://www.myphysiodoc.com/reg.php?method=";

            String getString = URL_PARAMETER;
            getString += "WOdownloadCount";
            getString += "&authkey=" + authkey;
            getString += "&" + inquiry;

            Log.i(LOG_TAG, "Zusammengesetzter Anfrage-String: " + getString);
            // Die URL-Verbindung und der BufferedReader, werden im finally-Block geschlossen
            HttpURLConnection httpURLConnection = null;
            BufferedReader bufferedReader = null;

            String RegisterString = "";

            try {
                URL url = new URL(getString);

                // Aufbau der Verbindung zu Server
                httpURLConnection = (HttpURLConnection) url.openConnection();

                InputStream inputStream = httpURLConnection.getInputStream();

                if (inputStream == null) { // Keinen Daten-Stream erhalten, daher Abbruch
                    return null;
                }
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    RegisterString += line;
                }
                if (RegisterString.length() == 0) { // Keine Daten ausgelesen, Abbruch
                    return null;
                }
                //Log.v(LOG_TAG, "Register-String: " + RegisterString);


            } catch (IOException e) { // Beim Holen der Daten trat ein Fehler auf, daher Abbruch
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            if ((counterWO%10 == 0) && (counterWO != 0)) {
                publishProgress(counterWO, howMuchWOsServer);
            }

            return RegisterString;
        }

        protected void onProgressUpdate(Integer... progress) {

            // gebe aktuellen Fortschritt aus
            Toast.makeText(MainActivity.this, progress[0] + " von " + progress[1] + " geladen",
                    Toast.LENGTH_SHORT).show();
        }

        protected void onPostExecute(String string) {
            // Task abgeschlossen, Ergebnis kann verwendet werden

            //Log.d(LOG_TAG, "Count-Rückgabe: " + string);
            //Hier Prüfung ob das Workout schon auf Client DB vorhanden ist (number, starttime, endtime)
            //Wenn nicht, restOf=restOf-1 (und speichern des WOs), damit nicht mehr runtergeladen wird, wenn bereits alle fehlenden WOs in Client DB gespeichert wurden;
                if(string.substring(0,3).equals("w2c")){
                    flexWOstring(string.substring(3,string.length())); //Übergabe ohne w2c nach Kontrolle
                    //Log.d(LOG_TAG, "workoutMemo: " + workoutMemo.getName());
                    if(!dataSource.getWOexist(number,startTime,endTime)){
                        Log.i(LOG_TAG, "WO unbekannt: " + string);
                        dataSource.createWorkoutMemo(wore, number, name, type, quantity, rounds, startTime, endTime, duration, exTimes,star,checkedWO,true);
                        restOf--;
                    }
                    else Log.i(LOG_TAG, "WO bekannt!!!: " + string);
                }
               // else Log.d(LOG_TAG, "Error return value w2c: " + string);
                Log.i(LOG_TAG, "restOf: " + restOf);
            if(restOf>0 && counterWO<howMuchWOsServer-1) {
                counterWO++;


                WOdaten2clientTask wOdaten2clientTask = new WOdaten2clientTask();
                new WOdaten2clientTask().execute(counterWO);
            }
            else  {
                Log.i(LOG_TAG, "restOf Ende: " + restOf+" counterWO: "+counterWO+" howMuchWOsServer: "+howMuchWOsServer);
                // TimeStamp nehmen um Zeit zu stoppen bis Daten holen beendet ist:
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                int gTd  = (int) (timestamp.getTime() - startTimeDownload)/1000;
                Toast.makeText(MainActivity.this, counterWO+1 + " von " + howMuchWOsServer + " geladen",Toast.LENGTH_SHORT).show();

                Log.i(LOG_TAG, "Download Zeit: " + timeFormat(gTd));

            }
        }
    }

    private class WOdaten2serverTask extends AsyncTask<Void, Integer, Integer> {


        protected Integer doInBackground(Void... params) {

            int n=workoutMemoList.size();
            String inquiry;

            for (int i=0; i < n; i++) {
                WorkoutMemo memo = (WorkoutMemo) workoutMemoList.get(i);
                String s="0";
                if(memo.getStar()) s="1";
                String c="0";
                if(memo.getStar()) c="1";
                inquiry = "authcode="+authCode+"&customid="+String.valueOf(customID)+"&wore="+String.valueOf(memo.getWore())+"&number="+String.valueOf(memo.getNumber())+"&name="+String.valueOf(memo.getName())+"&type="+String.valueOf(memo.getType())+"&quant="+String.valueOf(memo.getQuantity())+"&start="+String.valueOf(memo.getStartTime())+"&end="+String.valueOf(memo.getEndTime())+"&dura="+String.valueOf(memo.getDuration())+"&extimes="+String.valueOf(memo.getExTimes())+"&star="+s+"&check="+c;
                final String URL_PARAMETER = "https://www.myphysiodoc.com/reg.php?method=";

                String getString = URL_PARAMETER;
                getString += methodWO;
                getString += "&authkey=" + authkey;
                getString += "&" + inquiry;

                Log.i(LOG_TAG, "Zusammengesetzter Anfrage-String: " + getString);
                // Die URL-Verbindung und der BufferedReader, werden im finally-Block geschlossen
                HttpURLConnection httpURLConnection = null;
                BufferedReader bufferedReader = null;

                // In diesen String speichern wir die Aktiendaten im XML-Format
                String RegisterString = "";

                try {
                    URL url = new URL(getString);

                    // Aufbau der Verbindung zu Server
                    httpURLConnection = (HttpURLConnection) url.openConnection();

                    InputStream inputStream = httpURLConnection.getInputStream();

                    if (inputStream == null) { // Keinen Daten-Stream erhalten, daher Abbruch
                        return null;
                    }
                    bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;

                    while ((line = bufferedReader.readLine()) != null) {
                        RegisterString += line ;
                    }
                    if (RegisterString.length() == 0) { // Keine Daten ausgelesen, Abbruch
                        return null;
                    }
                    //Log.v(LOG_TAG, "Register-String: " + RegisterString);
                    //publishProgress(1, 1);

                } catch (IOException e) { // Beim Holen der Daten trat ein Fehler auf, daher Abbruch
                    Log.e(LOG_TAG, "Error ", e);
                    return null;
                } finally {
                    if (httpURLConnection != null) {
                        httpURLConnection.disconnect();
                    }
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (final IOException e) {
                            Log.e(LOG_TAG, "Error closing stream", e);
                        }
                    }
                }

                Log.i(LOG_TAG, "memo: "+i+" "+inquiry);
                if (i%5 == 4) {
                    publishProgress(i + 1, n);
                }
                if(RegisterString.equals("Error: Row is not updated")) {
                    memo.setUpload(false);
                }
                else{
                    memo.setUpload(true);
                }
            }


            //MainActivity.this.setTitle(String.valueOf(quant));
            return n;
        }

        protected void onProgressUpdate(Integer... progress) {

            // gebe aktuellen Fortschritt aus
            Toast.makeText(MainActivity.this, progress[0] + " von " + progress[1] + " geladen",
                    Toast.LENGTH_SHORT).show();
        }

        protected void onPostExecute(Integer n) {
            // Task abgeschlossen, Ergebnis kann verwendet werden
            for (int i=0; i < n; i++) {
                WorkoutMemo memo = (WorkoutMemo) workoutMemoList.get(i);
                if(memo.isUpload()) {
                    WorkoutMemo updatedWorkoutMemo = dataSource.updateWorkoutMemoUpload(memo.getId(), memo.getWore(), memo.getNumber(), memo.getName(), memo.getType(), memo.getQuantity(), memo.getRounds(), memo.getStartTime(), memo.getEndTime(), memo.getDuration(), memo.getExTimes(), memo.getStar(), true, memo.isChecked());
                    Log.i(LOG_TAG, "Uploaded-Status von Eintrag: " + updatedWorkoutMemo.toString() + " ist: " + updatedWorkoutMemo.isUpload());
                } else {
                    Log.i(LOG_TAG, "Error: Uploaded-Status von Eintrag: " + memo.toString() + " ist: " + memo.isUpload());
                }
            }
        }
    }
    public void downloadHistory() {
        //Checken ob Download erforderlich ist:
        //Anzahl der Datensätze auf Client ermitteln
        //Anzahl der Datensätze auf Server ermitteln
        //Wenn #Client<#Server dann Download der fehlenden Datensätze
        howMuchWOsClient=dataSource.getAllWorkoutEntrys();
        Log.i(LOG_TAG, "howMuchWOsClient: "+howMuchWOsClient);

        //String[] inquiry ={"authcode="+authCode+"&customid="+String.valueOf(customID)};
        GetQuantityEntrysTask getQuantityEntrysTask = new GetQuantityEntrysTask();
        new GetQuantityEntrysTask().execute();
    }
    private class GetQuantityEntrysTask extends AsyncTask<Void, Void, String> {


        protected String doInBackground(Void... strings) {

            //WorkoutMemo memo = (WorkoutMemo) workoutMemoList[0];
            //int n=workoutMemoList.size();
            //MainActivity.this.setTitle(String.valueOf(memo.getName()));
            //WorkoutMemo memo = (WorkoutMemo) workoutMemoList.getItemAtPosition(0);
            String inquiry = "authcode="+authCode+"&customid="+String.valueOf(customID);


                //inquiry = "authcode="+authCode+"&customid="+String.valueOf(customID)+"&wore="+String.valueOf(memo.getWore())+"&number="+String.valueOf(memo.getNumber())+"&name="+String.valueOf(memo.getName())+"&type="+String.valueOf(memo.getType())+"&quant="+String.valueOf(memo.getQuantity())+"&start="+String.valueOf(memo.getStartTime())+"&end="+String.valueOf(memo.getEndTime())+"&dura="+String.valueOf(memo.getDuration())+"&extimes="+String.valueOf(memo.getExTimes())+"&star="+s+"&check="+c;
                final String URL_PARAMETER = "https://www.myphysiodoc.com/reg.php?method=";

                String getString = URL_PARAMETER;
                getString += "getQuantityEntrys";
                getString += "&authkey=" + authkey;
                getString += "&" + inquiry;

                Log.i(LOG_TAG, "Zusammengesetzter Anfrage-String: " + getString);
                // Die URL-Verbindung und der BufferedReader, werden im finally-Block geschlossen
                HttpURLConnection httpURLConnection = null;
                BufferedReader bufferedReader = null;

                // In diesen String speichern wir die Aktiendaten im XML-Format
                String RegisterString = "";

                try {
                    URL url = new URL(getString);

                    // Aufbau der Verbindung zu Server
                    httpURLConnection = (HttpURLConnection) url.openConnection();

                    InputStream inputStream = httpURLConnection.getInputStream();

                    if (inputStream == null) { // Keinen Daten-Stream erhalten, daher Abbruch
                        return null;
                    }
                    bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;

                    while ((line = bufferedReader.readLine()) != null) {
                        RegisterString += line ;
                    }
                    if (RegisterString.length() == 0) { // Keine Daten ausgelesen, Abbruch
                        return null;
                    }
                    //Log.v(LOG_TAG, "Register-String: " + RegisterString);
                    //publishProgress(1, 1);

                } catch (IOException e) { // Beim Holen der Daten trat ein Fehler auf, daher Abbruch
                    Log.e(LOG_TAG, "Error ", e);
                    return null;
                } finally {
                    if (httpURLConnection != null) {
                        httpURLConnection.disconnect();
                    }
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (final IOException e) {
                            Log.e(LOG_TAG, "Error closing stream", e);
                        }
                    }
                }

            //MainActivity.this.setTitle(String.valueOf(quant));
            return RegisterString;
        }



        protected void onPostExecute(String strings) {
            // Task abgeschlossen, Ergebnis kann verwendet werden

                  howMuchWOsServer=Integer.valueOf(strings);

                //MainActivity.this.setTitle("Anzahl: "+String.valueOf(howMuchWOsServer));
                   if(howMuchWOsClient<howMuchWOsServer){
                       //Toast.makeText(MainActivity.this, "howMuchEntrys(C/S) = "+ String.valueOf(howMuchWOsClient)+"/"+String.valueOf(howMuchWOsServer), Toast.LENGTH_LONG).show();
                       Log.i(LOG_TAG, "Download Server 2 Client da howMuchEntrys(C/S) = "+ String.valueOf(howMuchWOsClient)+"/"+String.valueOf(howMuchWOsServer));
                       restOf=howMuchWOsServer-howMuchWOsClient;
                       counterWO=0;
                       // TimeStamp nehmen um Zeit zu stoppen bis Daten holen beendet ist:
                       Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                       startTimeDownload = timestamp.getTime();
                       WOdaten2clientTask wOdaten2clientTask = new WOdaten2clientTask();
                       new WOdaten2clientTask().execute(counterWO);
                   }
                    else  Log.i(LOG_TAG, "Kein Download da howMuchEntrys(C/S) = "+ String.valueOf(howMuchWOsClient)+"/"+String.valueOf(howMuchWOsServer));

            }

    }

    public void serverForgotPassword(String email) {

        method="forgotPassword";

        String[] inquiry ={"email="+email};
        //Toast.makeText(MainActivity.this, "emailPass = "+ emailPass[0]+" "+ emailPass[1], Toast.LENGTH_LONG).show();

        GetServerTask getServerTask = new GetServerTask();
        new GetServerTask().execute(inquiry);
    }
    public void serverCheckRegister(String email, String password) {

        method="register";

        String[] inquiry ={"email="+email+"&password="+password};
        //Toast.makeText(MainActivity.this, "emailPass = "+ emailPass[0]+" "+ emailPass[1], Toast.LENGTH_LONG).show();

        GetServerTask getServerTask = new GetServerTask();
        new GetServerTask().execute(inquiry);
    }
    public void serverCheckLogin(String email, String password) {

        method="loginNew";

        String[] inquiry ={"email="+email+"&password="+password};

        Log.i(LOG_TAG,"Abfrage loginNew gestartet: "+inquiry[0]);
        //Toast.makeText(MainActivity.this, "emailPass = "+ emailPass[0]+" "+ emailPass[1], Toast.LENGTH_LONG).show();

        GetServerTask getServerTask = new GetServerTask();
        new GetServerTask().execute(inquiry);
    }
    public void updateCheckData() {
        method="update";

        String[] inquiry ={"authcode="+authCode+"&customid="+String.valueOf(customID)+"&lastupdate="+String.valueOf(lastUpdate)};
        //Toast.makeText(MainActivity.this, "emailPass = "+ emailPass[0]+" "+ emailPass[1], Toast.LENGTH_LONG).show();

        GetServerTask getServerTask = new GetServerTask();
        new GetServerTask().execute(inquiry);
    }
    public void updateHistory() {
        methodWO="upHistory";
        //workoutMemoList.clear();
        workoutMemoList = dataSource.getIsUploadedFalse();
        int quant=workoutMemoList.size();
        Log.i(LOG_TAG, "UpHistory quant: " + String.valueOf(quant));
        if(quant>0){
            WOdaten2serverTask wodaten2servertask = new WOdaten2serverTask();
            new WOdaten2serverTask().execute();
        }
        //WorkoutMemo memo = (WorkoutMemo) workoutMemoList.get(0);

        //int anzahl=workoutMemoList.size();

        //MainActivity.this.setTitle(String.valueOf(memo.getName()));


        //WorkoutMemo memo = (WorkoutMemo) workoutMemoList.getItemAtPosition(0);



        /*String[] inquiry ={"authcode="+authCode+"&customid="+String.valueOf(customID)+"&lastupdate="+String.valueOf(lastUpdate)};
        //Toast.makeText(MainActivity.this, "emailPass = "+ emailPass[0]+" "+ emailPass[1], Toast.LENGTH_LONG).show();

        GetServerTask getServerTask = new GetServerTask();
        new GetServerTask().execute(inquiry);*/
    }

    public void update2server() throws UnsupportedEncodingException {
        method="u2s";

        String enc ="utf-8";
        String[] inquiry ={"authcode="+authCode+"&customid="+String.valueOf(customID)+"&lastupdate="+String.valueOf(lastUpdate)+"&l1="+ URLEncoder.encode(String.valueOf(spWorkoutList1),enc)+"&l2="+URLEncoder.encode(String.valueOf(spWorkoutList2),enc)+"&l3="+URLEncoder.encode(String.valueOf(spWorkoutList3),enc)+"&l4="+URLEncoder.encode(String.valueOf(spWorkoutList4),enc)+"&l5="+URLEncoder.encode(String.valueOf(spWorkoutList5),enc)+"&l6="+URLEncoder.encode(String.valueOf(spWorkoutList6),enc)+"&l7="+URLEncoder.encode(String.valueOf(spWorkoutList7),enc)+"&c1="+String.valueOf(checked[0])+"&c2="+String.valueOf(checked[1])+"&c3="+String.valueOf(checked[2])+"&c4="+String.valueOf(checked[3])+"&c5="+String.valueOf(checked[4])+"&c6="+String.valueOf(checked[5])+"&c7="+String.valueOf(checked[6])+"&datestamp="+datestamp+"&skills="+String.valueOf(skills)+"&days="+String.valueOf(days)+"&hardness="+String.valueOf(hardness)+"&fokus="+String.valueOf(fokus)+"&changeprefs="+String.valueOf(changeprefs)+"&currentdays="+String.valueOf(current_days)};
        //Toast.makeText(MainActivity.this, "emailPass = "+ emailPass[0]+" "+ emailPass[1], Toast.LENGTH_LONG).show();
        GetServerTask getServerTask = new GetServerTask();
        new GetServerTask().execute(inquiry);
    }
    public void u2sCWOs(){
        // Zusammenbauen der completedWorkouts. Wenn vorhanden mit neuem Task auf Server speichern
        method="u2sCWOs";
        SharedPreferences sp1 = getPreferences(MODE_PRIVATE);
        String completedString = "";
        for(int day=0; day<days+1; day++) {

            switch (day) {
                case (0): {
                    for(int i=0;i<WorkoutListArray1.size();i++){
                        if(!sp1.getString("completedWorkouts"+day+i, "").equals(""))completedString=completedString+"|"+sp1.getString("completedWorkouts"+day+i, "");
                        Log.i(LOG_TAG, "u2sCWOs CWO "+ sp1.getString("completedWorkouts"+day+i, ""));
                        }
                    break;
                }
                case (1): {
                    for(int i=0;i<WorkoutListArray2.size();i++){
                        if(!sp1.getString("completedWorkouts"+day+i, "").equals(""))completedString=completedString+"|"+sp1.getString("completedWorkouts"+day+i, "");
                        Log.i(LOG_TAG, "u2sCWOs CWO "+ sp1.getString("completedWorkouts"+day+i, ""));
                    }
                    break;
                }
                case (2): {
                    for(int i=0;i<WorkoutListArray3.size();i++){
                        if(!sp1.getString("completedWorkouts"+day+i, "").equals(""))completedString=completedString+"|"+sp1.getString("completedWorkouts"+day+i, "");
                        Log.i(LOG_TAG, "u2sCWOs CWO "+ sp1.getString("completedWorkouts"+day+i, ""));
                    }
                    break;
                }
                case (3): {
                    for(int i=0;i<WorkoutListArray4.size();i++){
                        if(!sp1.getString("completedWorkouts"+day+i, "").equals(""))completedString=completedString+"|"+sp1.getString("completedWorkouts"+day+i, "");
                        Log.i(LOG_TAG, "u2sCWOs CWO "+ sp1.getString("completedWorkouts"+day+i, ""));
                    }
                    break;
                }
                case (4): {
                    for(int i=0;i<WorkoutListArray5.size();i++){
                        if(!sp1.getString("completedWorkouts"+day+i, "").equals(""))completedString=completedString+"|"+sp1.getString("completedWorkouts"+day+i, "");
                        Log.i(LOG_TAG, "u2sCWOs CWO "+ sp1.getString("completedWorkouts"+day+i, ""));
                    }
                    break;
                }
                case (5): {
                    for(int i=0;i<WorkoutListArray6.size();i++){
                        if(!sp1.getString("completedWorkouts"+day+i, "").equals(""))completedString=completedString+"|"+sp1.getString("completedWorkouts"+day+i, "");
                        Log.i(LOG_TAG, "u2sCWOs CWO "+ sp1.getString("completedWorkouts"+day+i, ""));
                    }
                    break;
                }
                case (6): {
                    for(int i=0;i<WorkoutListArray7.size();i++){
                        if(!sp1.getString("completedWorkouts"+day+i, "").equals(""))completedString=completedString+"|"+sp1.getString("completedWorkouts"+day+i, "");
                        Log.i(LOG_TAG, "u2sCWOs CWO "+ sp1.getString("completedWorkouts"+day+i, ""));
                    }
                    break;
                }
            }
        }
        if(!completedString.equals("")) {
            completedString=completedString.substring(1,completedString.length());
            Log.i(LOG_TAG, "u2sCWOs completedString "+ completedString);
        }
        String[] inquiry1 = {"authcode=" + authCode + "&customid=" + String.valueOf(customID) + "&cWos=" + completedString};
        GetServerTask getServerTask = new GetServerTask();
        new GetServerTask().execute(inquiry1);
    }




    public class GetServerTask extends AsyncTask<String, Void, String> {

        private final String LOG_TAG = GetServerTask.class.getSimpleName();

        private String saveAuthCodeCustomID(String s){

            int n = s.indexOf(":");

            authCode = s.substring(0, n); //kompletter String bis :
            customID = Integer.valueOf(s.substring(n+1, s.length()));

            SharedPreferences sp = getPreferences(MODE_PRIVATE);
            SharedPreferences.Editor e = sp.edit();
            e.putInt("customID", customID);
            e.putString("authCode", authCode);
            e.commit();

            return authCode;
        }

        @Override
        protected String doInBackground(String... strings) {

            if (strings.length == 0) { // Keine Eingangsparameter erhalten, daher Abbruch
                return null;
            }

            // Wir konstruieren die Anfrage-URL für reg.php auf dem Server
            //strings[0] ist immer die method (register,login usw.)
            // https://www.myphysiodoc.com/reg.php?method=loginNew&authkey=koBF89p0KGoO&email=Tester2&password=test123
            final String URL_PARAMETER = "https://www.myphysiodoc.com/reg.php?method=";

            String getString = URL_PARAMETER;
            getString += method;
            getString += "&authkey=" + authkey;
            getString += "&" + strings[0];

            Log.i(LOG_TAG, "Zusammengesetzter Anfrage-String: " + getString);
            // Die URL-Verbindung und der BufferedReader, werden im finally-Block geschlossen
            HttpURLConnection httpURLConnection = null;
            BufferedReader bufferedReader = null;

            String RegisterString = "";

            try {
                URL url = new URL(getString);

                // Aufbau der Verbindung zu Server
                httpURLConnection = (HttpURLConnection) url.openConnection();

                InputStream inputStream = httpURLConnection.getInputStream();

                if (inputStream == null) { // Keinen Daten-Stream erhalten, daher Abbruch
                    return null;
                }
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    RegisterString += line ;
                }
                if (RegisterString.length() == 0) { // Keine Daten ausgelesen, Abbruch
                    return null;
                }
                //Log.v(LOG_TAG, "Register-String: " + RegisterString);
                //publishProgress(1, 1);

            } catch (IOException e) { // Beim Holen der Daten trat ein Fehler auf, daher Abbruch
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            return RegisterString;
        }
        @Override
        protected void onPostExecute(String strings) {
            //Toast.makeText(MainActivity.this, "serverCheckLogin!",Toast.LENGTH_SHORT).show();
            // Hintergrundberechnungen sind jetzt beendet, darüber informieren wir den Benutzer

            switch (method) {
                case ("forgotPassword"): {
                    if(strings.substring(0, 6).equals("Error:")){
                        Toast.makeText(MainActivity.this, strings,Toast.LENGTH_SHORT).show();
                        //dialog_login();
                    }
                    else {
                        //Toast.makeText(MainActivity.this, strings,Toast.LENGTH_SHORT).show();
                        Toast.makeText(MainActivity.this, getString(R.string.send_password),Toast.LENGTH_SHORT).show();
                        //Log.i(LOG_TAG,customID+"|"+strings);
                        //dialog_login();
                    }
                    break;
                }
                case ("register"): {
                    if(strings.substring(0, 6).equals("Error:")){
                        Toast.makeText(MainActivity.this, strings,Toast.LENGTH_SHORT).show();
                        dialog_login();
                    }
                    else {
                        saveAuthCodeCustomID(strings);
                        Toast.makeText(MainActivity.this, "User registriert!",Toast.LENGTH_SHORT).show();
                        //SharedPreferences sp = getPreferences(MODE_PRIVATE);
                        //int customID = sp.getInt("customID", 0);

                        //MainActivity.this.setTitle(customID+"|"+strings);
                        Log.i(LOG_TAG,"register: "+customID+"|"+strings+" ... days"+days);
                        //checkForCalc();
                        printWorkout();
                        Log.i(LOG_TAG,"register2: "+customID+"|"+strings+" ... days"+days);
                    }
                    break;
                }
                case ("loginNew"): {
                    if(strings.substring(0, 6).equals("Error:")){
                        Log.i(LOG_TAG,"loginNew: "+strings);
                        Toast.makeText(MainActivity.this, strings,Toast.LENGTH_SHORT).show();
                        dialog_login();
                    }
                    else {
                        saveAuthCodeCustomID(strings);
                        Log.i(LOG_TAG,"loginNew: "+customID+"|"+strings);
                        Toast.makeText(MainActivity.this, "User angemeldet!",Toast.LENGTH_SHORT).show();

                        //MainActivity.this.setTitle(customID+"|"+strings);
                        if(isConnectingToInternet(MainActivity.this)){
                            updateCheckData();
                            updateHistory();
                            downloadHistory();
                        }
                    }
                    break;
                }
                case ("update"): {

                    //MainActivity.this.setTitle(strings+"|"+ String.valueOf(customID));
                    Log.i(LOG_TAG, strings+"|"+ String.valueOf(customID));
                    if(strings.equals("Error: send me update")){
                        // Daten an Server senden
                        //Toast.makeText(MainActivity.this, "s "+strings,Toast.LENGTH_SHORT).show();
                        try {
                            update2server();
                            printWorkout();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                    else if(strings.substring(0,3).equals("u2c")){
                        // Daten an Client senden
                        Log.i(LOG_TAG, "Aufruf: update2client: " + strings.substring(3,strings.length()));
                        update2client(strings.substring(3,strings.length()));
                    }
                    else if(strings.equals("Error: authcode different")){
                        // Daten an Server senden
                        //Toast.makeText(MainActivity.this, "a "+strings,Toast.LENGTH_SHORT).show();
                        dialog_login();

                    }
                    else if(strings.equals("Error: no update")){
                        // Daten an Server senden
                        //MainActivity.this.setTitle(String.valueOf(lastUpdate)+"nu");
                        printWorkout();
                    }
                    break;
                }
                case ("u2s"): {
                    if(strings.substring(0, 6).equals("Error:")){
                        //Toast.makeText(MainActivity.this, strings,Toast.LENGTH_SHORT).show();
                        printWorkout();
                    }
                    else {
                           // MainActivity.this.setTitle(String.valueOf(lastUpdate)+"u2s");
                            u2sCWOs();

                    }
                    break;
                }
                case ("u2sCWOs"): {
                    if(strings.substring(0, 6).equals("Error:")){
                        //Toast.makeText(MainActivity.this, strings,Toast.LENGTH_SHORT).show();
                        //MainActivity.this.setTitle(String.valueOf(lastUpdate)+"|"+customID+"|"+strings);
                        Log.i(LOG_TAG,String.valueOf(lastUpdate)+"|"+customID+"|"+strings);
                        printWorkout();
                    }
                    else {
                        //MainActivity.this.setTitle(String.valueOf(lastUpdate)+"u2sCWOs");
                        Log.i(LOG_TAG,String.valueOf(lastUpdate)+"u2sCWOs");
                        printWorkout();
                    }
                    break;
                }
                case ("calc"): {
                    if(strings.substring(0, 6).equals("Error:")){
                        //Toast.makeText(MainActivity.this, strings,Toast.LENGTH_SHORT).show();
                        //MainActivity.this.setTitle(String.valueOf(lastUpdate)+"|"+customID+"|"+strings);
                        Log.i(LOG_TAG,customID+": "+strings);
                        printWorkout();
                    }
                    else {
                        //MainActivity.this.setTitle(String.valueOf(lastUpdate)+"u2sCWOs");
                        Log.i(LOG_TAG,"Calc: "+customID+" ... "+strings);
                        //ToDo: Hier Empfang und Aktualisierung der neuen Coachwoche
                        delCompletedWorkouts();
                        WorkoutListArray1.clear();
                        WorkoutListArray2.clear();
                        WorkoutListArray3.clear();
                        WorkoutListArray4.clear();
                        WorkoutListArray5.clear();
                        WorkoutListArray6.clear();
                        WorkoutListArray7.clear();

                        spWorkoutList1 = "";
                        spWorkoutList2 = "";
                        spWorkoutList3 = "";
                        spWorkoutList4 = "";
                        spWorkoutList5 = "";
                        spWorkoutList6 = "";
                        spWorkoutList7 = "";

                        SharedPreferences sp = getPreferences(MODE_PRIVATE);
                        SharedPreferences.Editor e = sp.edit();
                        String s = strings;
                        int p = s.indexOf("|");
                        lastUpdate = Long.valueOf(s.substring(4, p));
                        e.putLong("lastUpdate", lastUpdate);

                        s=s.substring(p+1, s.length());

                        String s2;
                        boolean cnc; // kommt "|" noch vor? Wenn nicht letzter Durchlauf der Schleife!
                        int c1=0;
                        do {
                            int n = s.indexOf("|");
                            if(n>0) {
                                s2 = s.substring(0, n); //kompletter String bis |
                                cnc=true;
                            }else {
                                s2 = s;
                                cnc=false;
                            }
                            if(s2.substring(0,1).equals("l")) {
                                c1++;
                                e.putString("spWorkoutList"+c1, s2.substring(1,s2.length()));
                            }
                            if(n>0){s =  s.substring(n+1,s.length());}
                        }while(cnc);
                        e.commit();
                        loadDate();
                        printWorkout();
                    }
                    break;
                }


        }

        }
    }

    public void update2client(String s){
        Log.i(LOG_TAG, "update2client aufgerufen");
        delCompletedWorkouts(); //checked löschen um sie aktualisieren zu können
        datestamp=Integer.parseInt(getDateAsString());
        SharedPreferences sp = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor e = sp.edit();

        int p = s.indexOf("|");
        lastUpdate = Long.valueOf(s.substring(0, p));
        e.putLong("lastUpdate", lastUpdate);

        s=s.substring(p+1, s.length());

        String s2;
        boolean cnc; // kommt "|" noch vor? Wenn nicht letzter Durchlauf der Schleife!
        int c1=0;
        int c2=0;
        do {
            int n = s.indexOf("|");
            if(n>0) {
                s2 = s.substring(0, n); //kompletter String bis |
                cnc=true;
            }else {
                s2 = s;
                cnc=false;
            }
            if(s2.substring(0,1).equals("l")) {
                //this.setTitle(s2);
                c1++;
                e.putString("spWorkoutList"+c1, s2.substring(1,s2.length()));
            }else if(s2.substring(0,1).equals("c")){
                e.putInt("checked"+c2, Integer.valueOf(s2.substring(1,s2.length())));
                c2++;
            }
            else if(s2.substring(0,1).equals("a")){
                String st = s2.substring(1,s2.length());

                if(st.equals("1")){
                    e.putBoolean("calc", true);
                    Log.i(LOG_TAG, "update2client true: calc st"+st);
                }
                else {
                    e.putBoolean("calc", false);
                    Log.i(LOG_TAG, "update2client false: calc st"+st);
                }
            }
            else if(s2.substring(0,1).equals("d")){
                e.putInt("datestamp", Integer.valueOf(s2.substring(1,s2.length())));
            }
            else if(s2.substring(0,1).equals("s")){
                e.putInt("sp_skills", Integer.valueOf(s2.substring(1,s2.length())));
            }
            else if(s2.substring(0,1).equals("t")){
                e.putInt("sp_days", Integer.valueOf(s2.substring(1,s2.length())));
            }
            else if(s2.substring(0,1).equals("h")){
                e.putInt("sp_hardness", Integer.valueOf(s2.substring(1,s2.length())));
            }
            else if(s2.substring(0,1).equals("f")){
                e.putInt("sp_fokus", Integer.valueOf(s2.substring(1,s2.length())));
            }
            else if(s2.substring(0,1).equals("r")){
                e.putInt("sp_current_days", Integer.valueOf(s2.substring(1,s2.length())));
            }
            else if(s2.substring(0,1).equals("n")){
                e.putInt("sp_changeprefs", Integer.valueOf(s2.substring(1,s2.length())));
            }
            else if(s2.substring(0,1).equals("o")){
                //String st=s2.substring(1,s2.length());
                int nn = s.indexOf("o"); //Jetzt gesamten String von |o bis Ende da hier auch | verwendet wurde
                String st=s.substring(nn+1,s.length());
                Log.i(LOG_TAG, "update2client: completedWorkouts st"+st);
                String st2="";
                boolean cnc2;
                do {
                    int nx = st.indexOf("|");
                    if(nx>0) {
                        st2 = st.substring(0, nx); //kompletter String bis |
                        cnc2=true;
                        Log.i(LOG_TAG, "update2client: completedWorkouts nx"+nx+cnc2);
                    }else {
                        st2 = st;
                        cnc2=false;
                        Log.i(LOG_TAG, "update2client: completedWorkouts nx"+nx+cnc2);
                    }
                    String[] input = new String[2];
                    if(!st2.equals(""))input = ex2CheckForExtraText(st2);
                    //Hier weitermachen. Nach Import wird nur beim ersten WO die Zeit ausgegeben :-((
                    if(!st2.equals("")){
                        e.putString("completedWorkouts"+input[0]+input[1], st2); //Tag und Position
                        Log.i(LOG_TAG, "update2client: completedWorkouts"+input[0]+input[1]);

                    }

                    if(nx>0){st =  st.substring(nx+1,st.length());}
                }while(cnc2);

            }
            if(n>0){s =  s.substring(n+1,s.length());}
        }while(cnc);
        e.commit();
        loadDate();
        printWorkout();
    }
    public static boolean isConnectingToInternet(Context context)
    {
        ConnectivityManager connectivity =
                (ConnectivityManager) context.getSystemService(
                        Context.CONNECTIVITY_SERVICE);
        if (connectivity != null)
        {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }
        }
        return false;
    }

    private String[] ex2CheckForExtraText(String s){
        //Übergabe an Coach: wore, name, quantity, type, Startzeit, Länge Format hh:mm:ss, star, checked_day, checked_pos
        //ch_wore = Integer.valueOf(s.substring(0, 1)); //wore = Workout oder Exercise
        String[] output = new String[2];
        String s1 = s.substring(2, s.length());
        int n = s1.indexOf(",");
        //ch_name = s1.substring(0, n);
        s1 = s1.substring(n + 1, s1.length());
        n = s1.indexOf(",");
        //ch_quantity = Integer.valueOf(s1.substring(0, n));
        s1 = s1.substring(n + 1, s1.length());
        n = s1.indexOf(",");
        //ch_type = Integer.valueOf(s1.substring(0, n));
        s1 = s1.substring(n + 1, s1.length());
        n = s1.indexOf(",");
        //ch_startTime = Long.valueOf(s1.substring(0, n));
        s1 = s1.substring(n + 1, s1.length());
        n = s1.indexOf(",");
        //ch_duration = s1.substring(0, n);
        s1 = s1.substring(n + 1, s1.length());
        n = s1.indexOf(",");
        //ch_star = Boolean.valueOf(s1.substring(0, n));
        s1 = s1.substring(n + 1, s1.length());
        n = s1.indexOf(",");
        output[0] = s1.substring(0, n);
        s1 = s1.substring(n + 1, s1.length());
        output[1] = s1.substring(0, s1.length());
        return output;
    }
    private void exCheckForExtraText(String s){
        //Übergabe an Coach: wore, name, quantity, type, Startzeit, Länge Format hh:mm:ss, star, checked_day, checked_pos
        ch_wore = Integer.valueOf(s.substring(0, 1)); //wore = Workout oder Exercise
        String s1 = s.substring(2, s.length());
        int n = s1.indexOf(",");
        ch_name = s1.substring(0, n);
        s1 = s1.substring(n + 1, s1.length());
        n = s1.indexOf(",");
        ch_quantity = Integer.valueOf(s1.substring(0, n));
        s1 = s1.substring(n + 1, s1.length());
        n = s1.indexOf(",");
        ch_type = Integer.valueOf(s1.substring(0, n));
        s1 = s1.substring(n + 1, s1.length());
        n = s1.indexOf(",");
        ch_startTime = Long.valueOf(s1.substring(0, n));
        s1 = s1.substring(n + 1, s1.length());
        n = s1.indexOf(",");
        ch_duration = s1.substring(0, n);
        s1 = s1.substring(n + 1, s1.length());
        n = s1.indexOf(",");
        ch_star = Boolean.valueOf(s1.substring(0, n));
        s1 = s1.substring(n + 1, s1.length());
        n = s1.indexOf(",");
        ch_checked_day = Integer.valueOf(s1.substring(0, n));
        s1 = s1.substring(n + 1, s1.length());
        ch_checked_pos = Integer.valueOf(s1.substring(0, s1.length()));

    }
    private void checkForExtraText() {
        //kontrollieren ob MainActivity von WorkoutActivity aus aufgerufen wurde um den Checked-Status eines WOs zu setzen
        //Übergabe an Coach: wore, name, quantity, type, Startzeit, Länge Format hh:mm:ss, star, checked_day, checked_pos
        Intent receivedIntent = this.getIntent();
        if (receivedIntent != null && receivedIntent.hasExtra(Intent.EXTRA_TEXT)) {
            String s = receivedIntent.getStringExtra(Intent.EXTRA_TEXT);
            exCheckForExtraText(s);
//            history2update = true;
            if(ch_checked_day>=0 && ch_wore==0){
                checked[ch_checked_day]=checked[ch_checked_day]+binaerArray[ch_checked_pos];
                saveChecked();
                SharedPreferences sp = getPreferences(MODE_PRIVATE);
                String spWo = sp.getString("spWorkoutList"+(ch_checked_day+1), "");
                Log.i(LOG_TAG, "checkForExtraText: completedWorkouts"+"ch_checked: "+spWo);

                s = spWo.substring(1,spWo.length());
                String s2;
                boolean cnc; // kommt "#" noch vor? Wenn nicht letzter Durchlauf der Schleife!
                int c = 0; // Counter für die Position
                String newString ="";

                //Toast.makeText(this, "sp_skills a: "+String.valueOf(s), Toast.LENGTH_LONG).show();
                Log.i(LOG_TAG, "checkForExtraText: sp_skills a: "+String.valueOf(s));
                do {
                    int n = s.indexOf("#");
                    if(n>0) {
                        s2 = s.substring(0, n); //kompletter String bis #
                        cnc=true;
                    }else {
                        s2 = s;
                        cnc=false;
                    }
                    if(c==ch_checked_pos) {
                        //this.setTitle(s2);
                        int n1 = s2.lastIndexOf(",");
                        String r = s2.substring(0, n1-1);
                        r=r+String.valueOf(ch_quantity);
                        r =r+s2.substring(n1,s2.length());
                        newString=newString+"#"+r;

                    }else{
                        newString=newString+"#"+s2;
                    }
                    if(n>0){s =  s.substring(n+1,s.length());}
                    c++;
                }while(cnc);
                //SharedPreferences sp2 = getPreferences(MODE_PRIVATE);
                SharedPreferences.Editor e = sp.edit();
                e.putString("spWorkoutList"+(ch_checked_day+1), newString);
                e.putString("completedWorkouts"+ch_checked_day+ch_checked_pos, receivedIntent.getStringExtra(Intent.EXTRA_TEXT));
                Log.i(LOG_TAG, "checkForExtraText: completedWorkouts"+ch_checked_day+ch_checked_pos+"|"+ receivedIntent.getStringExtra(Intent.EXTRA_TEXT));
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                e.putLong("lastUpdate",timestamp.getTime());
                e.commit();



                //Toast.makeText(this, "ch_checked: "+String.valueOf(ch_checked_day)+"|"+String.valueOf(ch_checked_pos), Toast.LENGTH_LONG).show();
                //Toast.makeText(this, "ch_checked: "+newString, Toast.LENGTH_LONG).show();

            }
            else {
                checked[ch_checked_day]=checked[ch_checked_day]+binaerArray[ch_checked_pos];
                saveChecked();
                SharedPreferences sp = getPreferences(MODE_PRIVATE);
                SharedPreferences.Editor e = sp.edit();
                e.putString("completedWorkouts"+ch_checked_day+ch_checked_pos, receivedIntent.getStringExtra(Intent.EXTRA_TEXT));
                Log.i(LOG_TAG, "checkForExtraText: completedWorkouts"+ch_checked_day+ch_checked_pos+"|"+ receivedIntent.getStringExtra(Intent.EXTRA_TEXT));
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                e.putLong("lastUpdate",timestamp.getTime());
                e.commit();
            }

            loadDate();
            if(isConnectingToInternet(MainActivity.this)) {
                updateCheckData();
                //updateHistory();
            }

        } else {
            return;
        }

    }
    public void generalList() {
        // Erstellung der Liste aller erlaubten Workouts nach Skills
        WorkoutListArray0.clear();
        workoutCount = workoutPack.getWorkouts().size();
        String binary = Integer.toBinaryString(skills);
        // für Auswertung binär in int umwandeln
        Integer s = Integer.valueOf(binary);
        // führende Nullen wenn nötig
        String formatted = String.format("%06d",s);
        String skill1 = String.valueOf(formatted.charAt(5));
        String skill2 = String.valueOf(formatted.charAt(4));
        String skill3 = String.valueOf(formatted.charAt(3));
        String skill4 = String.valueOf(formatted.charAt(2));
        String skill5 = String.valueOf(formatted.charAt(1));
        String skill6 = String.valueOf(formatted.charAt(0));

        for(int i=0; i<workoutCount; i++)
        {
            Workout w = workoutPack.getWorkouts().get(i);

            for (int j = 0; j < 3; j++) {
                    if (j == 0) {
                        boolean workout_taker = true;
                        Integer skillValue = w.getEndurance().getSkill();
                        if ((skill6.equals("0")) && (skillValue > 31)) {
                            workout_taker = false;
                        }
                        if (skillValue > 31) {
                            skillValue = skillValue - 32;
                        }
                        if ((skill5.equals("0")) && (skillValue > 15)) {
                            workout_taker = false;
                        }
                        if (skillValue > 15) {
                            skillValue = skillValue - 16;
                        }
                        if ((skill4.equals("0")) && (skillValue > 7)) {
                            workout_taker = false;
                        }
                        if (skillValue > 7) {
                            skillValue = skillValue - 8;
                        }
                        if ((skill3.equals("0")) && (skillValue > 3)) {
                            workout_taker = false;
                        }
                        if (skillValue > 3) {
                            skillValue = skillValue - 4;
                        }
                        if ((skill2.equals("0")) && (skillValue > 1)) {
                            workout_taker = false;
                        }
                        if (skillValue > 1) {
                            skillValue = skillValue - 2;
                        }
                        if ((skill1.equals("0")) && (skillValue > 0)) {
                            workout_taker = false;
                        }
                        //Wenn Fokus == Strength: Keine Endurance Workouts!
                        if (fokus == 2) {
                            workout_taker = false;
                        }
                        if (workout_taker == true) {
                            //Workoutindex, Workouttyp, Duration, Difficulty
                            WorkoutListArray0.add(String.valueOf(i) + ",0," + w.getDuration() + "," + w.getDifficulty());
                            if (fokus == 0) {
                                //Wenn Fokus == Endurance: Doppelte Anzahl Endurance Workouts!
                                WorkoutListArray0.add(String.valueOf(i) + ",0," + w.getDuration() + "," + w.getDifficulty());
                            }
                        }
                    }
                    if (j == 1) {
                        boolean workout_taker = true;
                        Integer skillValue = w.getStandard().getSkill();
                        if ((skill6.equals("0")) && (skillValue > 31)) {
                            workout_taker = false;
                        }
                        if (skillValue > 31) {
                            skillValue = skillValue - 32;
                        }
                        if ((skill5.equals("0")) && (skillValue > 15)) {
                            workout_taker = false;
                        }
                        if (skillValue > 15) {
                            skillValue = skillValue - 16;
                        }
                        if ((skill4.equals("0")) && (skillValue > 7)) {
                            workout_taker = false;
                        }
                        if (skillValue > 7) {
                            skillValue = skillValue - 8;
                        }
                        if ((skill3.equals("0")) && (skillValue > 3)) {
                            workout_taker = false;
                        }
                        if (skillValue > 3) {
                            skillValue = skillValue - 4;
                        }
                        if ((skill2.equals("0")) && (skillValue > 1)) {
                            workout_taker = false;
                        }
                        if (skillValue > 1) {
                            skillValue = skillValue - 2;
                        }
                        if ((skill1.equals("0")) && (skillValue > 0)) {
                            workout_taker = false;
                        }
                        if (workout_taker == true) {
                            WorkoutListArray0.add(String.valueOf(i) + ",1," + w.getDuration() + "," + w.getDifficulty());
                            if (fokus == 1) {
                                //Wenn Fokus == Standard: Doppelte Anzahl Standard Workouts!
                                WorkoutListArray0.add(String.valueOf(i) + ",1," + w.getDuration() + "," + w.getDifficulty());
                            }
                        }
                    }
                    if (j == 2) {
                        boolean workout_taker = true;
                        Integer skillValue = Integer.valueOf(w.getStrength().getSkill());
                        if ((skill6.equals("0")) && (skillValue > 31)) {
                            workout_taker = false;
                        }
                        if (skillValue > 31) {
                            skillValue = skillValue - 32;
                        }
                        if ((skill5.equals("0")) && (skillValue > 15)) {
                            workout_taker = false;
                        }
                        if (skillValue > 15) {
                            skillValue = skillValue - 16;
                        }
                        if ((skill4.equals("0")) && (skillValue > 7)) {
                            workout_taker = false;
                        }
                        if (skillValue > 7) {
                            skillValue = skillValue - 8;
                        }
                        if ((skill3.equals("0")) && (skillValue > 3)) {
                            workout_taker = false;
                        }
                        if (skillValue > 3) {
                            skillValue = skillValue - 4;
                        }
                        if ((skill2.equals("0")) && (skillValue > 1)) {
                            workout_taker = false;
                        }
                        if (skillValue > 1) {
                            skillValue = skillValue - 2;
                        }
                        if ((skill1.equals("0")) && (skillValue > 0)) {
                            workout_taker = false;
                        }
                        //Wenn Fokus == Endurance: Keine Strength Workouts!
                        if (fokus == 0) {
                            workout_taker = false;
                        }
                        if (workout_taker == true) {
                            WorkoutListArray0.add(String.valueOf(i) + ",2," + w.getDuration() + "," + w.getDifficulty());
                            if (fokus == 2) {
                                //Wenn Fokus == Strength: Doppelte Anzahl Strength Workouts!
                                WorkoutListArray0.add(String.valueOf(i) + ",2," + w.getDuration() + "," + w.getDifficulty());
                            }
                        }
                    }
                }

        }
// Liste nach Type durchgehen und überflüssige Workouts entfernen
        if(fokus==0){
            Iterator<String> iterator = WorkoutListArray0.iterator();
            while(iterator.hasNext()) // überprüfen, ob noch ein Element folgt
            {
                String s0 = iterator.next(); // das nächste Element holen und die aktuelle Position dahinter setzen
                Workout w = workoutPack.getWorkouts().get(exIndexString(s0));
                if (w.getType()==2)
                {
                    iterator.remove(); // entfernt das zuletzt gelesene Element
                }
            }
        }


        if(fokus==2){
            Iterator<String> iterator = WorkoutListArray0.iterator();
            while(iterator.hasNext()) // überprüfen, ob noch ein Element folgt
            {
                String s0 = iterator.next(); // das nächste Element holen und die aktuelle Position dahinter setzen
                Workout w = workoutPack.getWorkouts().get(exIndexString(s0));
                if (w.getType()==0)
                {
                    iterator.remove(); // entfernt das zuletzt gelesene Element
                }
            }
        }


        // Erstellung der Liste aller erlaubten Specials nach Skills
        WorkoutListArraySpecial.clear();
        specialCount = specialPack.getSpecials().size();
        binary = Integer.toBinaryString(skills);
        // für Auswertung binär in int umwandeln
        s = Integer.valueOf(binary);
        // führende Nullen wenn nötig
        formatted = String.format("%06d",s);
        skill1 = String.valueOf(formatted.charAt(5));
        skill2 = String.valueOf(formatted.charAt(4));
        skill3 = String.valueOf(formatted.charAt(3));
        skill4 = String.valueOf(formatted.charAt(2));
        skill5 = String.valueOf(formatted.charAt(1));
        skill6 = String.valueOf(formatted.charAt(0));

        for(int i=0; i<specialCount; i++)
        {
            Special w = specialPack.getSpecials().get(i);
            for(int j=0; j<3; j++) {
                if (j==0){
                    boolean workout_taker = true;
                    Integer skillValue = w.getEndurance().getSkill();
                    if ((skill6.equals("0")) && (skillValue>31)){workout_taker = false;}
                    if (skillValue>31){skillValue=skillValue-32;}
                    if ((skill5.equals("0")) && (skillValue>15)){workout_taker = false;}
                    if (skillValue>15){skillValue=skillValue-16;}
                    if ((skill4.equals("0")) && (skillValue>7)){workout_taker = false;}
                    if (skillValue>7){skillValue=skillValue-8;}
                    if ((skill3.equals("0")) && (skillValue>3)){workout_taker = false;}
                    if (skillValue>3){skillValue=skillValue-4;}
                    if ((skill2.equals("0")) && (skillValue>1)){workout_taker = false;}
                    if (skillValue>1){skillValue=skillValue-2;}
                    if ((skill1.equals("0")) && (skillValue>0)){workout_taker = false;}
                    //Wenn Fokus == Strength: Keine Endurance Workouts!
                    if(fokus==2){workout_taker=false;}

                        if (workout_taker == true) {
                            //Workoutindex, Workouttyp, Duration, Difficulty
                            WorkoutListArraySpecial.add(String.valueOf(i) + ",0," + w.getDuration() + "," + w.getDifficulty());
                            if (fokus == 0) {
                                //Wenn Fokus == Endurance: Doppelte Anzahl Endurance Workouts!
                                WorkoutListArraySpecial.add(String.valueOf(i) + ",0," + w.getDuration() + "," + w.getDifficulty());
                            }
                        }
                }
                if (j==1){
                    boolean workout_taker = true;
                    Integer skillValue = w.getStandard().getSkill();
                    if ((skill6.equals("0")) && (skillValue>31)){workout_taker = false;}
                    if (skillValue>31){skillValue=skillValue-32;}
                    if ((skill5.equals("0")) && (skillValue>15)){workout_taker = false;}
                    if (skillValue>15){skillValue=skillValue-16;}
                    if ((skill4.equals("0")) && (skillValue>7)){workout_taker = false;}
                    if (skillValue>7){skillValue=skillValue-8;}
                    if ((skill3.equals("0")) && (skillValue>3)){workout_taker = false;}
                    if (skillValue>3){skillValue=skillValue-4;}
                    if ((skill2.equals("0")) && (skillValue>1)){workout_taker = false;}
                    if (skillValue>1){skillValue=skillValue-2;}
                    if ((skill1.equals("0")) && (skillValue>0)){workout_taker = false;}
                    if(workout_taker==true){

                            WorkoutListArraySpecial.add(String.valueOf(i) + ",1," + w.getDuration() + "," + w.getDifficulty());
                            if (fokus == 1) {
                                //Wenn Fokus == Standard: Doppelte Anzahl Standard Workouts!
                                WorkoutListArraySpecial.add(String.valueOf(i) + ",1," + w.getDuration() + "," + w.getDifficulty());
                            }

                    }
                }
                if (j==2){
                    boolean workout_taker = true;
                    Integer skillValue = Integer.valueOf(w.getStrength().getSkill());
                    if ((skill6.equals("0")) && (skillValue>31)){workout_taker = false;                    }
                    if (skillValue>31){skillValue=skillValue-32;}
                    if ((skill5.equals("0")) && (skillValue>15)){workout_taker = false;}
                    if (skillValue>15){skillValue=skillValue-16;}
                    if ((skill4.equals("0")) && (skillValue>7)){workout_taker = false;}
                    if (skillValue>7){skillValue=skillValue-8;}
                    if ((skill3.equals("0")) && (skillValue>3)){workout_taker = false;}
                    if (skillValue>3){skillValue=skillValue-4;}
                    if ((skill2.equals("0")) && (skillValue>1)){workout_taker = false;}
                    if (skillValue>1){skillValue=skillValue-2;}
                    if ((skill1.equals("0")) && (skillValue>0)){workout_taker = false;}
                    //Wenn Fokus == Endurance: Keine Strength Workouts!
                    if(fokus==0){workout_taker=false;}

                        if (workout_taker == true) {
                            WorkoutListArraySpecial.add(String.valueOf(i) + ",2," + w.getDuration() + "," + w.getDifficulty());
                            if (fokus == 2) {
                                //Wenn Fokus == Strength: Doppelte Anzahl Strength Workouts!
                                WorkoutListArraySpecial.add(String.valueOf(i) + ",2," + w.getDuration() + "," + w.getDifficulty());
                            }
                        }
                }
            }
        }
        // Liste nach Type durchgehen und überflüssige Specials entfernen
        if(fokus==0){
            Iterator<String> iterator = WorkoutListArraySpecial.iterator();
            while(iterator.hasNext()) // überprüfen, ob noch ein Element folgt
            {
                String s0 = iterator.next(); // das nächste Element holen und die aktuelle Position dahinter setzen
                Special w = specialPack.getSpecials().get(exIndexString(s0));
                if (w.getType()==2)
                {
                    iterator.remove(); // entfernt das zuletzt gelesene Element
                }
            }
        }


        if(fokus==2){
            Iterator<String> iterator = WorkoutListArraySpecial.iterator();
            while(iterator.hasNext()) // überprüfen, ob noch ein Element folgt
            {
                String s0 = iterator.next(); // das nächste Element holen und die aktuelle Position dahinter setzen
                Special w = specialPack.getSpecials().get(exIndexString(s0));
                if (w.getType()==0)
                {
                    iterator.remove(); // entfernt das zuletzt gelesene Element
                }
            }
        }

    }
    public void delCompletedWorkouts(){
        SharedPreferences sp1 = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ee = sp1.edit();
        //Toast.makeText(this, "size WorkoutListArray1 "+String.valueOf(WorkoutListArray1.size()), Toast.LENGTH_LONG).show();
        for(int i=0; i<7;i++){
            checked[i]=0;
        }
        for(int day=0; day<days+1; day++) {

            switch (day) {
                case (0): {
                    for(int i=0;i<WorkoutListArray1.size();i++){
                        ee.remove("completedWorkouts"+day+i);
                        Log.i(LOG_TAG, "delCompletedWorkouts: "+day+i);
                    }
                    break;
                }
                case (1): {
                    for(int i=0;i<WorkoutListArray2.size();i++){
                        ee.remove("completedWorkouts"+day+i);
                        Log.i(LOG_TAG, "delCompletedWorkouts: "+day+i);
                    }
                    break;
                }
                case (2): {
                    for(int i=0;i<WorkoutListArray3.size();i++){
                        ee.remove("completedWorkouts"+day+i);
                        Log.i(LOG_TAG, "delCompletedWorkouts: "+day+i);
                    }
                    break;
                }
                case (3): {
                    for(int i=0;i<WorkoutListArray4.size();i++){
                        ee.remove("completedWorkouts"+day+i);
                        Log.i(LOG_TAG, "delCompletedWorkouts: "+day+i);
                    }
                    break;
                }
                case (4): {
                    for(int i=0;i<WorkoutListArray5.size();i++){
                        ee.remove("completedWorkouts"+day+i);
                        Log.i(LOG_TAG, "delCompletedWorkouts: "+day+i);
                    }
                    break;
                }
                case (5): {
                    for(int i=0;i<WorkoutListArray6.size();i++){
                        ee.remove("completedWorkouts"+day+i);
                        Log.i(LOG_TAG, "delCompletedWorkouts: "+day+i);
                    }
                    break;
                }
                case (6): {
                    for(int i=0;i<WorkoutListArray7.size();i++){
                        ee.remove("completedWorkouts"+day+i);
                        Log.i(LOG_TAG, "delCompletedWorkouts: "+day+i);
                    }
                    break;
                }
            }
        }
        //if(ee.commit()==true)Log.i(LOG_TAG, "delCompletedWorkouts: commit==true");
        //else Log.i(LOG_TAG, "delCompletedWorkouts: commit==false");
    }
    public void WorkoutCalc(){

        delCompletedWorkouts();
        WorkoutListArray1.clear();
        WorkoutListArray2.clear();
        WorkoutListArray3.clear();
        WorkoutListArray4.clear();
        WorkoutListArray5.clear();
        WorkoutListArray6.clear();
        WorkoutListArray7.clear();
        WorkoutListArrayTest.clear();

        spWorkoutList1 = "";
        spWorkoutList2 = "";
        spWorkoutList3 = "";
        spWorkoutList4 = "";
        spWorkoutList5 = "";
        spWorkoutList6 = "";
        spWorkoutList7 = "";


        for(int day=1; day<days+1; day++) {
            boolean special_trigger = false; // Pro Tag prüfen ob schon ein Special ausgewählt wurde
            int duration_adder = hardness;
            WorkoutListArrayX.clear();
            WorkoutListArraySpecialX.clear();
            // Die Arbeitsliste mit allen der Hardness entsprechenden Workouts füllen
            for(int w=0; w<WorkoutListArray0.size(); w++){
                if(exDuration(w)<=hardness){WorkoutListArrayX.add(String.valueOf(WorkoutListArray0.get(w)));
                }
            }
            // Auswahl der Workouts:
            do {
                if(duration_adder==2){

                    Iterator<String> iterator = WorkoutListArrayX.iterator();
                    while(iterator.hasNext()) // überprüfen, ob noch ein Element folgt
                    {
                        String s = iterator.next(); // das nächste Element holen und die aktuelle Position dahinter setzen
                        if (exDurationString(s)==3)
                        {
                            iterator.remove(); // entfernt das zuletzt gelesene Element
                        }
                    }
                }
                if(duration_adder==1){

                    Iterator<String> iterator = WorkoutListArrayX.iterator();
                    while(iterator.hasNext()) // überprüfen, ob noch ein Element folgt
                    {
                        String s = iterator.next(); // das nächste Element holen und die aktuelle Position dahinter setzen
                        if (exDurationString(s)==2 || exDurationString(s)==3)
                        {
                            iterator.remove(); // entfernt das zuletzt gelesene Element
                        }
                    }
                }
                int rndcount = (int) (Math.random() * WorkoutListArrayX.size());
                // Wiederholungsrate des Workouts (1-3x):
                int rndx = 1;
                int d=exDurationX(rndcount);
                if (d < duration_adder) {
                    switch (d) {
                        case 1:
                            rndx = (int) (Math.random() * 3 + 1);
                            break;
                        case 2:
                            rndx = (int) (Math.random() * 2 + 1);
                            break;
                    }
                }
                if(duration_adder < (rndx*d)) rndx=1;

                duration_adder = duration_adder - (d * rndx);

                switch (day) {
                    case (1): {
                        //WorkoutListArray1.add(String.valueOf(WorkoutListArrayX.get(rndcount)) + "," + String.valueOf(rndx));
                        spWorkoutList1 = spWorkoutList1+"#"+String.valueOf(WorkoutListArrayX.get(rndcount)) + "," + String.valueOf(rndx) + ",0"; // Letzter Wert 0 = Workout, 1 = Special
                        break;
                    }
                    case (2): {
                        //WorkoutListArray2.add(String.valueOf(WorkoutListArrayX.get(rndcount)) + "," + String.valueOf(rndx));
                        spWorkoutList2 = spWorkoutList2+"#"+String.valueOf(WorkoutListArrayX.get(rndcount)) + "," + String.valueOf(rndx) + ",0"; // Letzter Wert 0 = Workout, 1 = Special
                        break;
                    }
                    case (3): {
                        //WorkoutListArray3.add(String.valueOf(WorkoutListArrayX.get(rndcount)) + "," + String.valueOf(rndx));
                        spWorkoutList3 = spWorkoutList3+"#"+String.valueOf(WorkoutListArrayX.get(rndcount)) + "," + String.valueOf(rndx) + ",0"; // Letzter Wert 0 = Workout, 1 = Special
                        break;
                    }
                    case (4): {
                        //WorkoutListArray4.add(String.valueOf(WorkoutListArrayX.get(rndcount)) + "," + String.valueOf(rndx));
                        spWorkoutList4 = spWorkoutList4+"#"+String.valueOf(WorkoutListArrayX.get(rndcount)) + "," + String.valueOf(rndx) + ",0"; // Letzter Wert 0 = Workout, 1 = Special
                        break;
                    }
                    case (5): {
                        //WorkoutListArray5.add(String.valueOf(WorkoutListArrayX.get(rndcount)) + "," + String.valueOf(rndx));
                        spWorkoutList5 = spWorkoutList5+"#"+String.valueOf(WorkoutListArrayX.get(rndcount)) + "," + String.valueOf(rndx) + ",0"; // Letzter Wert 0 = Workout, 1 = Special
                        break;
                    }
                    case (6): {
                        //WorkoutListArray6.add(String.valueOf(WorkoutListArrayX.get(rndcount)) + "," + String.valueOf(rndx));
                        spWorkoutList6 = spWorkoutList6+"#"+String.valueOf(WorkoutListArrayX.get(rndcount)) + "," + String.valueOf(rndx) + ",0"; // Letzter Wert 0 = Workout, 1 = Special
                        break;
                    }
                    case (7): {
                        //WorkoutListArray7.add(String.valueOf(WorkoutListArrayX.get(rndcount)) + "," + String.valueOf(rndx));
                        spWorkoutList7 = spWorkoutList7+"#"+String.valueOf(WorkoutListArrayX.get(rndcount)) + "," + String.valueOf(rndx) + ",0"; // Letzter Wert 0 = Workout, 1 = Special
                        break;
                    }
                }
                // Entfernen des aktuellen Workouts aus der Arbeitsliste, damit dieses nicht noch einmal am selben Tag ausgewählt wird
                int r = exIndex(rndcount);
                Iterator<String> iterator = WorkoutListArrayX.iterator();
                while(iterator.hasNext()) // überprüfen, ob noch ein Element folgt
                {
                    String s = iterator.next(); // das nächste Element holen und die aktuelle Position dahinter setzen
                    if (exIndexString(s)==r)
                    {
                        iterator.remove(); // entfernt das zuletzt gelesene Element
                    }
                }
                if(special_trigger == false && duration_adder>0){
                    short rnd = (short) (Math.random() * 100 + 1);
                    //Toast.makeText(this, "rnd "+rnd, Toast.LENGTH_LONG).show();
                    if(rnd<=specialProcent){ // Special sollen nur mit vorgegebener Wahrscheinlichkeit gesetzt werden
                        for(int w=0; w<WorkoutListArraySpecial.size(); w++){ //nach Hardnesswert in Arbeitsliste übertragen
                            if(exDuration(w)<=hardness){WorkoutListArraySpecialX.add(String.valueOf(WorkoutListArraySpecial.get(w)));
                            }
                        }
                        if(duration_adder==1) {

                            Iterator<String> iterator2 = WorkoutListArraySpecialX.iterator();
                            while (iterator.hasNext()) // überprüfen, ob noch ein Element folgt
                            {
                                String s = iterator2.next(); // das nächste Element holen und die aktuelle Position dahinter setzen
                                if (exDurationString(s) == 2 || exDurationString(s) == 3) {
                                    iterator2.remove(); // entfernt das zuletzt gelesene Element
                                }
                            }
                        }
                        if(duration_adder==2) {

                            Iterator<String> iterator2 = WorkoutListArraySpecialX.iterator();
                            while (iterator.hasNext()) // überprüfen, ob noch ein Element folgt
                            {
                                String s = iterator2.next(); // das nächste Element holen und die aktuelle Position dahinter setzen
                                if (exDurationString(s) == 3) {
                                    iterator2.remove(); // entfernt das zuletzt gelesene Element
                                }
                            }
                        }
                        int rndSpecialCount = (int) (Math.random() * WorkoutListArraySpecialX.size());
                        duration_adder = duration_adder-exDurationString(WorkoutListArraySpecialX.get(rndSpecialCount));
                        special_trigger = true; // nur maximal ein Special pro Tag, daher Trigger von 0 auf 1
                        //Toast.makeText(this, "Special: "+WorkoutListArraySpecialX.get(rndSpecialCount), Toast.LENGTH_LONG).show();
                        switch (day) {
                            case (1): {
                                //WorkoutListArray1.add(String.valueOf(WorkoutListArrayX.get(rndcount)) + "," + String.valueOf(rndx));
                                spWorkoutList1 = spWorkoutList1+"#"+String.valueOf(WorkoutListArraySpecialX.get(rndSpecialCount)) + ",0,1"; // Letzter Wert 0 = Workout, 1 = Special
                                break;
                            }
                            case (2): {
                                //WorkoutListArray2.add(String.valueOf(WorkoutListArrayX.get(rndcount)) + "," + String.valueOf(rndx));
                                spWorkoutList2 = spWorkoutList2+"#"+String.valueOf(WorkoutListArraySpecialX.get(rndSpecialCount)) + ",0,1"; // Letzter Wert 0 = Workout, 1 = Special
                                break;
                            }
                            case (3): {
                                //WorkoutListArray3.add(String.valueOf(WorkoutListArrayX.get(rndcount)) + "," + String.valueOf(rndx));
                                spWorkoutList3 = spWorkoutList3+"#"+String.valueOf(WorkoutListArraySpecialX.get(rndSpecialCount)) + ",0,1"; // Letzter Wert 0 = Workout, 1 = Special
                                break;
                            }
                            case (4): {
                                //WorkoutListArray4.add(String.valueOf(WorkoutListArrayX.get(rndcount)) + "," + String.valueOf(rndx));
                                spWorkoutList4 = spWorkoutList4+"#"+String.valueOf(WorkoutListArraySpecialX.get(rndSpecialCount)) + ",0,1"; // Letzter Wert 0 = Workout, 1 = Special
                                break;
                            }
                            case (5): {
                                //WorkoutListArray5.add(String.valueOf(WorkoutListArrayX.get(rndcount)) + "," + String.valueOf(rndx));
                                spWorkoutList5 = spWorkoutList5+"#"+String.valueOf(WorkoutListArraySpecialX.get(rndSpecialCount)) + ",0,1"; // Letzter Wert 0 = Workout, 1 = Special
                                break;
                            }
                            case (6): {
                                //WorkoutListArray6.add(String.valueOf(WorkoutListArrayX.get(rndcount)) + "," + String.valueOf(rndx));
                                spWorkoutList6 = spWorkoutList6+"#"+String.valueOf(WorkoutListArraySpecialX.get(rndSpecialCount)) + ",0,1"; // Letzter Wert 0 = Workout, 1 = Special
                                break;
                            }
                            case (7): {
                                //WorkoutListArray7.add(String.valueOf(WorkoutListArrayX.get(rndcount)) + "," + String.valueOf(rndx));
                                spWorkoutList7 = spWorkoutList7+"#"+String.valueOf(WorkoutListArraySpecialX.get(rndSpecialCount)) + ",0,1"; // Letzter Wert 0 = Workout, 1 = Special
                                break;
                            }
                        }
                    }
                }
            }
            while(duration_adder > 0);
        }
        SharedPreferences sp = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor e = sp.edit();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        lastUpdate=timestamp.getTime();
        e.putLong("lastUpdate",lastUpdate);
        e.putInt("sp_changeprefs", 0);
        e.putInt("sp_current_days", days);
        e.putString("spWorkoutList1", spWorkoutList1);
        e.putString("spWorkoutList2", spWorkoutList2);
        e.putString("spWorkoutList3", spWorkoutList3);
        e.putString("spWorkoutList4", spWorkoutList4);
        e.putString("spWorkoutList5", spWorkoutList5);
        e.putString("spWorkoutList6", spWorkoutList6);
        e.putString("spWorkoutList7", spWorkoutList7);
        e.putInt("checked0", 0);
        e.putInt("checked1", 0);
        e.putInt("checked2", 0);
        e.putInt("checked3", 0);
        e.putInt("checked4", 0);
        e.putInt("checked5", 0);
        e.putInt("checked6", 0);
        e.commit();
        current_days=days;
        changeprefs = false;
        if(isConnectingToInternet(MainActivity.this)) updateCheckData();
    }
    public String exWorkoutString(String s, long p)
    // Extrahieren des Workoutstrings nach Position, die Einträge sind mit # getrennt
    //Input: s=String des Trainingstags (spWorkoutList1), p = Position des Workouts oder Specials
    // Output: String des Workouts
    {
        s = s.substring(1,s.length());
        String r = "";
        String s1;
        boolean cnc; // kommt "#" noch vor? Wenn nicht letzter Durchlauf der Schleife!
        int c = 0; // Counter für die Position
        //Toast.makeText(this, "sp_skills a: "+String.valueOf(s), Toast.LENGTH_LONG).show();
        do {
            int n = s.indexOf("#");
            if(n>0) {
                s1 = s.substring(0, n); //kompletter String bis #
                cnc=true;
            }else {
                s1 = s;
                cnc=false;
            }
            if(c==p){r =  s1;}
            if(n>0){s =  s.substring(n+1,s.length());}
            c++;
        }while(cnc);

        return r;
    }
    public int exDuration(int a)
    // Extrahieren des Duration Werts aus der Generalliste
    {
        String s =  (String) WorkoutListArray0.get(a);
        int n = s.lastIndexOf(",");
        Integer e = Integer.valueOf(s.substring(n-1,n));
        return e;
    }
    public int exDurationSpecial(int a)
    // Extrahieren des Duration Werts aus der Generalliste
    {
        String s =  (String) WorkoutListArraySpecial.get(a);
        int n = s.lastIndexOf(",");
        Integer e = Integer.valueOf(s.substring(n-1,n));
        return e;
    }
    public int exDurationString(String s)
    // Extrahieren des Index Werts aus der Arbeitsliste (Workout Nummer)
    {
        int n = s.lastIndexOf(",");
        Integer e = Integer.valueOf(s.substring(n-1,n));
        return e;
    }
    public int exDurationX(int a)
    // Extrahieren des Duration Werts aus der Arbeitsliste
    {
        String s =  (String) WorkoutListArrayX.get(a);
        int n = s.lastIndexOf(",");
        Integer e = Integer.valueOf(s.substring(n-1,n));
        return e;
    }
    public int exIndex(int a)
    // Extrahieren des Index Werts aus der Arbeitsliste (Workout Nummer)
    {
        String s =  (String) WorkoutListArrayX.get(a);
        int n = s.indexOf(",");
        Integer e = Integer.valueOf(s.substring(0,n));
        return e;
    }
    public int exIndexString(String s)
    // Extrahieren des Index Werts aus der Arbeitsliste (Workout Nummer)
    {
        int n = s.indexOf(",");
        Integer e = Integer.valueOf(s.substring(0,n));
        return e;
    }
    public boolean exChecked(int b, int position){
        boolean r;
        // int in binär umwandeln
        String binary = Integer.toBinaryString(b);
        // für Auswertung binär in int umwandeln
        Integer i = Integer.valueOf(binary);
        // führende Nullen wenn nötig
        String formatted = String.format("%08d",i);
        //Toast.makeText(this, "Prefs: "+formatted, Toast.LENGTH_LONG).show();
        String e = String.valueOf(formatted.charAt(formatted.length()-position-1));
        //Toast.makeText(this, "Prefs: "+String.valueOf(e), Toast.LENGTH_LONG).show();

        if(e.equals("0")) {r=false;} else {r=true;}
        return r;
    }
    public void printWorkout() {

        if(datestamp==0){
            current_days=0;
            showView(R.id.button_calc);
            showView(R.id.button_prefs);
            hideView(R.id.text_days_till_new_week);}
        else {
            if (datestamp > Integer.parseInt(getDateAsString())-current_days) {
                hideView(R.id.button_calc);
                hideView(R.id.button_prefs);
                showView(R.id.text_days_till_new_week);
            } else {

                showView(R.id.button_calc);
                hideView(R.id.button_prefs);
                hideView(R.id.text_days_till_new_week);
            }
        }

        WorkoutListArray1.clear();
        WorkoutListArray2.clear();
        WorkoutListArray3.clear();
        WorkoutListArray4.clear();
        WorkoutListArray5.clear();
        WorkoutListArray6.clear();
        WorkoutListArray7.clear();

        WorkoutListArrayShadow1.clear();
        WorkoutListArrayShadow2.clear();
        WorkoutListArrayShadow3.clear();
        WorkoutListArrayShadow4.clear();
        WorkoutListArrayShadow5.clear();
        WorkoutListArrayShadow6.clear();
        WorkoutListArrayShadow7.clear();

        for(int rmx=0;rmx<7;rmx++) {
            Log.i(LOG_TAG, "printWorkout checked["+rmx+"]: " + checked[rmx]);
        }

        if(!String.valueOf(spWorkoutList1).equals("")) {
            exWorkoutList(String.valueOf(spWorkoutList1), 1);
            ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, WorkoutListArray1)
            {
                @Override
                public View getView(int position, View convertView, ViewGroup parent)
                {
                    View view = super.getView(position, convertView, parent);
                    TextView text = (TextView) view.findViewById(android.R.id.text1);


                    if(exChecked(checked[0],position)==true) {
                        text.setTextColor(Color.parseColor("#999999"));
                        text.setPaintFlags(text.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    }
                    /*else {
                        text.setTextColor(Color.parseColor("#000000"));
                        text.setPaintFlags(text.getPaintFlags()  & (~ Paint.STRIKE_THRU_TEXT_FLAG));
                    }*/
                    return view;
                }
            };
            ListView listView1 = (ListView) findViewById(R.id.list_workout_day1);
            listView1.setAdapter(adapter1);
            ListUtils.setDynamicHeight(listView1);
            showView(R.id.text_workout1);
            showView(R.id.list_workout_day1);
            listView1.setOnItemClickListener(new AdapterView.OnItemClickListener(){

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                {
                    // Hier check-Status prüfen
                    //Toast.makeText(getApplicationContext(), "checked[0]: "+String.valueOf(checked[0]) + "|" + String.valueOf(id), Toast.LENGTH_SHORT).show();

                    // Intent erzeugen und Starten der ...Activity mit explizitem Intent
                    if(exChecked(checked[0],position)==false) {
                        int n = Integer.valueOf(WorkoutListArrayShadow1.get(position).substring(0, 1));
                        Intent workoutFragmentIntent;
                        if (n == 0) {
                            workoutFragmentIntent = new Intent(MainActivity.this, WorkoutActivity.class);
                        } else {
                            workoutFragmentIntent = new Intent(MainActivity.this, ExerciseActivity.class);
                        }
                        workoutFragmentIntent.putExtra(Intent.EXTRA_TEXT, WorkoutListArrayShadow1.get(position) + ",0," + position + ",0");
                        startActivity(workoutFragmentIntent);
                    }
/*                    if(exChecked(checked[0],position)==false) {
                        checked[0]=checked[0]+binaerArray[position];
                        saveChecked();
                        //Toast.makeText(getApplicationContext(), "checked[0]: "+String.valueOf(checked[0]) + "|" + String.valueOf(id), Toast.LENGTH_SHORT).show();
                    } else {
                        checked[0]=checked[0]-binaerArray[position];
                        saveChecked();
                        //Toast.makeText(getApplicationContext(), "checked[0]: "+String.valueOf(checked[0]) + "|" + String.valueOf(id), Toast.LENGTH_SHORT).show();
                    }
                    printWorkout();*/
                }

            });

        }
        else {
            hideView(R.id.text_workout1);
            hideView(R.id.list_workout_day1);
        }
        if(current_days>1) {
            exWorkoutList(String.valueOf(spWorkoutList2),2);
            ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, WorkoutListArray2)
            {
                @Override
                public View getView(int position, View convertView, ViewGroup parent)
                {
                    View view = super.getView(position, convertView, parent);
                    TextView text = (TextView) view.findViewById(android.R.id.text1);
                    if(exChecked(checked[1],position)==true) {
                        text.setTextColor(Color.parseColor("#999999"));
                        text.setPaintFlags(text.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    }
                    return view;
                }
            };

            ListView listView2 = (ListView) findViewById(R.id.list_workout_day2);
            listView2.setAdapter(adapter2);
            showView(R.id.text_workout2);
            showView(R.id.list_workout_day2);
            ListUtils.setDynamicHeight(listView2);
            listView2.setOnItemClickListener(new AdapterView.OnItemClickListener(){

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                {
                    // Hier check-Status prüfen
                    // Intent erzeugen und Starten der WorkoutActivity mit explizitem Intent
                    if(exChecked(checked[1],position)==false) {
                        int n = Integer.valueOf(WorkoutListArrayShadow2.get(position).substring(0, 1));
                        Intent workoutFragmentIntent;
                        if (n == 0) {
                            workoutFragmentIntent = new Intent(MainActivity.this, WorkoutActivity.class);
                        } else {
                            workoutFragmentIntent = new Intent(MainActivity.this, ExerciseActivity.class);
                        }
                        workoutFragmentIntent.putExtra(Intent.EXTRA_TEXT, WorkoutListArrayShadow2.get(position) + ",1," + position + ",0");
                        startActivity(workoutFragmentIntent);
                    }
                    /*if(exChecked(checked[1],position)==false) {
                        checked[1]=checked[1]+binaerArray[position];
                        saveChecked();
                        //Toast.makeText(getApplicationContext(), "checked[1]: "+String.valueOf(checked[1]) + "|" + String.valueOf(id), Toast.LENGTH_SHORT).show();
                    } else {
                        checked[1]=checked[1]-binaerArray[position];
                        saveChecked();
                        //Toast.makeText(getApplicationContext(), "checked[1]: "+String.valueOf(checked[1]) + "|" + String.valueOf(id), Toast.LENGTH_SHORT).show();
                    }*/
                    //printWorkout();
                }

            });

        }
        else {
            hideView(R.id.text_workout2);
            hideView(R.id.list_workout_day2);

        }        if(current_days>2) {

            exWorkoutList(String.valueOf(spWorkoutList3),3);
            ArrayAdapter<String> adapter3 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, WorkoutListArray3)
            {
                @Override
                public View getView(int position, View convertView, ViewGroup parent)
                {
                    View view = super.getView(position, convertView, parent);
                    TextView text = (TextView) view.findViewById(android.R.id.text1);
                    if(exChecked(checked[2],position)==true) {
                        text.setTextColor(Color.parseColor("#999999"));
                        text.setPaintFlags(text.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    }
                    return view;
                }
            };

            ListView listView3 = (ListView) findViewById(R.id.list_workout_day3);
            listView3.setAdapter(adapter3);
            showView(R.id.text_workout3);
            showView(R.id.list_workout_day3);
            ListUtils.setDynamicHeight(listView3);
            listView3.setOnItemClickListener(new AdapterView.OnItemClickListener(){

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                {
                    // Hier check-Status prüfen
                    // Intent erzeugen und Starten der WorkoutActivity mit explizitem Intent
                    if(exChecked(checked[2],position)==false) {
                        int n = Integer.valueOf(WorkoutListArrayShadow3.get(position).substring(0, 1));
                        Intent workoutFragmentIntent;
                        if (n == 0) {
                            workoutFragmentIntent = new Intent(MainActivity.this, WorkoutActivity.class);
                        } else {
                            workoutFragmentIntent = new Intent(MainActivity.this, ExerciseActivity.class);
                        }
                        workoutFragmentIntent.putExtra(Intent.EXTRA_TEXT, WorkoutListArrayShadow3.get(position) + ",2," + position + ",0");
                        startActivity(workoutFragmentIntent);
                    }
/*

                    if(exChecked(checked[2],position)==false) {
                        checked[2]=checked[2]+binaerArray[position];
                        saveChecked();
                        //Toast.makeText(getApplicationContext(), "checked[2]: "+String.valueOf(checked[2]) + "|" + String.valueOf(id), Toast.LENGTH_SHORT).show();
                    } else {
                        checked[2]=checked[2]-binaerArray[position];
                        saveChecked();
                        //Toast.makeText(getApplicationContext(), "checked[2]: "+String.valueOf(checked[2]) + "|" + String.valueOf(id), Toast.LENGTH_SHORT).show();
                    }
                    printWorkout();*/
                }

            });

        }
        else {
            hideView(R.id.text_workout3);
            hideView(R.id.list_workout_day3);
        }
        if(current_days>3) {
            exWorkoutList(String.valueOf(spWorkoutList4),4);
            ArrayAdapter<String> adapter4 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, WorkoutListArray4)
            {
                @Override
                public View getView(int position, View convertView, ViewGroup parent)
                {
                    View view = super.getView(position, convertView, parent);
                    TextView text = (TextView) view.findViewById(android.R.id.text1);
                    if(exChecked(checked[3],position)==true) {
                        text.setTextColor(Color.parseColor("#999999"));
                        text.setPaintFlags(text.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    }
                    return view;
                }
            };

            ListView listView4 = (ListView) findViewById(R.id.list_workout_day4);
            listView4.setAdapter(adapter4);
            showView(R.id.text_workout4);
            showView(R.id.list_workout_day4);
            ListUtils.setDynamicHeight(listView4);
            listView4.setOnItemClickListener(new AdapterView.OnItemClickListener(){

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                {
                    // Hier check-Status prüfen
                    // Intent erzeugen und Starten der WorkoutActivity mit explizitem Intent
                    if(exChecked(checked[3],position)==false) {
                        int n = Integer.valueOf(WorkoutListArrayShadow4.get(position).substring(0, 1));
                        Intent workoutFragmentIntent;
                        if (n == 0) {
                            workoutFragmentIntent = new Intent(MainActivity.this, WorkoutActivity.class);
                        } else {
                            workoutFragmentIntent = new Intent(MainActivity.this, ExerciseActivity.class);
                        }
                        workoutFragmentIntent.putExtra(Intent.EXTRA_TEXT, WorkoutListArrayShadow4.get(position) + ",3," + position + ",0");
                        startActivity(workoutFragmentIntent);
                    }
/*

                    if(exChecked(checked[3],position)==false) {
                        checked[3]=checked[3]+binaerArray[position];
                        saveChecked();
                        //Toast.makeText(getApplicationContext(), "checked[3]: "+String.valueOf(checked[3]) + "|" + String.valueOf(id), Toast.LENGTH_SHORT).show();
                    } else {
                        checked[3]=checked[3]-binaerArray[position];
                        saveChecked();
                        //Toast.makeText(getApplicationContext(), "checked[3]: "+String.valueOf(checked[3]) + "|" + String.valueOf(id), Toast.LENGTH_SHORT).show();
                    }
                    printWorkout();*/
                }

            });
        }
        else {
            hideView(R.id.text_workout4);
            hideView(R.id.list_workout_day4);
        }
        if(current_days>4) {
            exWorkoutList(String.valueOf(spWorkoutList5),5);
            ArrayAdapter<String> adapter5 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, WorkoutListArray5)
            {
                @Override
                public View getView(int position, View convertView, ViewGroup parent)
                {
                    View view = super.getView(position, convertView, parent);
                    TextView text = (TextView) view.findViewById(android.R.id.text1);
                    if(exChecked(checked[4],position)==true) {
                        text.setTextColor(Color.parseColor("#999999"));
                        text.setPaintFlags(text.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    }

                    return view;
                }
            };

            ListView listView5 = (ListView) findViewById(R.id.list_workout_day5);
            listView5.setAdapter(adapter5);
            showView(R.id.text_workout5);
            showView(R.id.list_workout_day5);
            ListUtils.setDynamicHeight(listView5);
            listView5.setOnItemClickListener(new AdapterView.OnItemClickListener(){

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                {
                    // Hier check-Status prüfen
                    // Intent erzeugen und Starten der WorkoutActivity mit explizitem Intent
                    if(exChecked(checked[4],position)==false) {
                        int n = Integer.valueOf(WorkoutListArrayShadow5.get(position).substring(0, 1));
                        Intent workoutFragmentIntent;
                        if (n == 0) {
                            workoutFragmentIntent = new Intent(MainActivity.this, WorkoutActivity.class);
                        } else {
                            workoutFragmentIntent = new Intent(MainActivity.this, ExerciseActivity.class);
                        }
                        workoutFragmentIntent.putExtra(Intent.EXTRA_TEXT, WorkoutListArrayShadow5.get(position) + ",4," + position + ",0");
                        startActivity(workoutFragmentIntent);
                    }
/*

                    if(exChecked(checked[4],position)==false) {
                        checked[4]=checked[4]+binaerArray[position];
                        saveChecked();
                        //Toast.makeText(getApplicationContext(), "checked[4]: "+String.valueOf(checked[4]) + "|" + String.valueOf(id), Toast.LENGTH_SHORT).show();
                    } else {
                        checked[4]=checked[4]-binaerArray[position];
                        saveChecked();
                        //Toast.makeText(getApplicationContext(), "checked[4]: "+String.valueOf(checked[4]) + "|" + String.valueOf(id), Toast.LENGTH_SHORT).show();
                    }
                    printWorkout();*/
                }

            });
        }
        else {
            hideView(R.id.text_workout5);
            hideView(R.id.list_workout_day5);
        }
        if(current_days>5) {
            exWorkoutList(String.valueOf(spWorkoutList6),6);
            ArrayAdapter<String> adapter6 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, WorkoutListArray6)
            {
                @Override
                public View getView(int position, View convertView, ViewGroup parent)
                {
                    View view = super.getView(position, convertView, parent);
                    TextView text = (TextView) view.findViewById(android.R.id.text1);
                    if(exChecked(checked[5],position)==true) {
                        text.setTextColor(Color.parseColor("#999999"));
                        text.setPaintFlags(text.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    }

                    return view;
                }
            };

            ListView listView6 = (ListView) findViewById(R.id.list_workout_day6);
            listView6.setAdapter(adapter6);
            showView(R.id.text_workout6);
            showView(R.id.list_workout_day6);
            ListUtils.setDynamicHeight(listView6);
            listView6.setOnItemClickListener(new AdapterView.OnItemClickListener(){

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                {
                    // Hier check-Status prüfen
                    // Intent erzeugen und Starten der WorkoutActivity mit explizitem Intent
                    if(exChecked(checked[5],position)==false) {

                        int n = Integer.valueOf(WorkoutListArrayShadow6.get(position).substring(0, 1));
                        Intent workoutFragmentIntent;
                        if (n == 0) {
                            workoutFragmentIntent = new Intent(MainActivity.this, WorkoutActivity.class);
                        } else {
                            workoutFragmentIntent = new Intent(MainActivity.this, ExerciseActivity.class);
                        }
                        workoutFragmentIntent.putExtra(Intent.EXTRA_TEXT, WorkoutListArrayShadow6.get(position) + ",5," + position + ",0");
                        startActivity(workoutFragmentIntent);
                    }
/*

                    if(exChecked(checked[5],position)==false) {
                        checked[5]=checked[5]+binaerArray[position];
                        saveChecked();
                        //Toast.makeText(getApplicationContext(), "checked[5]: "+String.valueOf(checked[5]) + "|" + String.valueOf(id), Toast.LENGTH_SHORT).show();
                    } else {
                        checked[5]=checked[5]-binaerArray[position];
                        saveChecked();
                        //Toast.makeText(getApplicationContext(), "checked[5]: "+String.valueOf(checked[5]) + "|" + String.valueOf(id), Toast.LENGTH_SHORT).show();
                    }
                    printWorkout();*/
                }

            });

        }
        else {
            hideView(R.id.text_workout6);
            hideView(R.id.list_workout_day6);
        }
        if(current_days>6) {
            exWorkoutList(String.valueOf(spWorkoutList7),7);
            ArrayAdapter<String> adapter7 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, WorkoutListArray7)
            {
                @Override
                public View getView(int position, View convertView, ViewGroup parent)
                {
                    View view = super.getView(position, convertView, parent);
                    TextView text = (TextView) view.findViewById(android.R.id.text1);
                    if(exChecked(checked[6],position)==true) {
                        text.setTextColor(Color.parseColor("#999999"));
                        text.setPaintFlags(text.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    }

                    return view;
                }
            };

            ListView listView7 = (ListView) findViewById(R.id.list_workout_day7);
            listView7.setAdapter(adapter7);
            showView(R.id.text_workout7);
            showView(R.id.list_workout_day7);
            ListUtils.setDynamicHeight(listView7);
            listView7.setOnItemClickListener(new AdapterView.OnItemClickListener(){

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                {
                    // Hier check-Status prüfen
                    // Intent erzeugen und Starten der WorkoutActivity mit explizitem Intent
                    if(exChecked(checked[6],position)==false) {

                        int n = Integer.valueOf(WorkoutListArrayShadow7.get(position).substring(0, 1));
                        Intent workoutFragmentIntent;
                        if (n == 0) {
                            workoutFragmentIntent = new Intent(MainActivity.this, WorkoutActivity.class);
                        } else {
                            workoutFragmentIntent = new Intent(MainActivity.this, ExerciseActivity.class);
                        }
                        workoutFragmentIntent.putExtra(Intent.EXTRA_TEXT, WorkoutListArrayShadow7.get(position) + ",6," + position + ",0");
                        startActivity(workoutFragmentIntent);
                    }
/*

                    if(exChecked(checked[6],position)==false) {
                        checked[6]=checked[6]+binaerArray[position];
                        saveChecked();
                        //Toast.makeText(getApplicationContext(), "checked[6]: "+String.valueOf(checked[6]) + "|" + String.valueOf(id), Toast.LENGTH_SHORT).show();
                    } else {
                        checked[6]=checked[6]-binaerArray[position];
                        saveChecked();
                        //Toast.makeText(getApplicationContext(), "checked[6]: "+String.valueOf(checked[6]) + "|" + String.valueOf(id), Toast.LENGTH_SHORT).show();
                    }
                    printWorkout();*/
                }

            });

        }
        else {
            hideView(R.id.text_workout7);
            hideView(R.id.list_workout_day7);
        }
        setCountdown();

    }
    public void setCountdown(){
        int countdown = Math.abs(Integer.parseInt(getDateAsString())-current_days-datestamp);
        TextView text = (TextView) findViewById(R.id.text_days_till_new_week);
        text.setText("Noch "+String.valueOf(countdown)+" Tage bis zur neuen Woche.");
    }
    private void exWorkoutList(String s, int d){
        WorkoutListArrayP.clear();
        WorkoutListArrayPShadow.clear();
        if(s.length()>0){
            s = s.substring(1,s.length());
            String s1;
            boolean cnc;
            int counter = 0;
            //Toast.makeText(this, "sp_skills a: "+String.valueOf(s), Toast.LENGTH_LONG).show();
            do {
                int n = s.indexOf("#");
                if(n>0) {
                    s1 = s.substring(0, n); //kompletter String bis #
                    cnc=true;
                }else {
                    s1 = s;
                    cnc=false;
                }
                int k = s1.indexOf(",");

                Integer z = Integer.valueOf(s1.substring(0,k)); // Workoutnummer


                String x = String.valueOf(s1.charAt(s1.length() - 3)); //Suche nach letztem Eintrag = Anzahl (Wiederholung)

                String t = String.valueOf(s1.charAt(s1.length() - 9)); //
                String t1 = String.valueOf(s1.charAt(s1.length() - 7)); //
                String t2 = String.valueOf(s1.charAt(s1.length() - 5)); //
                String t3 = String.valueOf(s1.charAt(s1.length() - 1)); // Workout = 0, Special = 1

                String ListText;
                String ListTextShadow;
                SharedPreferences sp = getPreferences(MODE_PRIVATE);


                if(t3.equals("0")) {
                    Workout w = workoutPack.getWorkouts().get(z); // XML für Workoutname laden, z = Workoutnummer
                    if(exChecked(checked[d-1],counter)==true && !sp.getString("completedWorkouts"+(d-1)+counter, "").equals("")){
                        exCheckForExtraText(sp.getString("completedWorkouts"+(d-1)+counter, ""));ListText = x + " x " + w.getName() + " " + Types[Integer.valueOf(t)] + " " + ch_duration;/* + " (" + t1 + "," + t2 + ")*/
                        Log.i(LOG_TAG, "exWorkoutList Workout: "+ListText);
                    }
                    else {ListText = x + " x " + w.getName() + " " + Types[Integer.valueOf(t)];/* + " (" + t1 + "," + t2 + ")*/}
                    //ListText = x + " x " + w.getName() + " " + Types[Integer.valueOf(t)]+" 10:45";
                    ListTextShadow = "0,"+z+","+t+","+x; //Workout/Special,Workoutnummer,Type(Strth,Std,End.),Anzahl
                    WorkoutListArrayP.add(ListText);
                    WorkoutListArrayPShadow.add(ListTextShadow);
                    //this.setTitle("completedWorkouts"+(d-1)+counter);
                    counter++;
               }else{
                    Special w = specialPack.getSpecials().get(z); // XML für Workoutname laden, z = Workoutnummer
                    //Toast.makeText(this, "specialPack Anzahl: "+String.valueOf(), Toast.LENGTH_LONG).show();
                    for(Unit u : w.getUnits()) { // alle Unit-Knoten durchlaufen
                        String xmeter = " x ";
                        if(u.getName().equals("Sprint")) xmeter=" m ";
                        if(exChecked(checked[d-1],counter)==true && !sp.getString("completedWorkouts"+(d-1)+counter, "").equals("")){
                            exCheckForExtraText(sp.getString("completedWorkouts"+(d-1)+counter, ""));ListText = ch_quantity + xmeter + u.getName() + " " + ch_duration;/* + " (" + t1 + "," + t2 + ")*/
                        }
                        else {ListText = u.getQuantity() + xmeter + u.getName();}
                        Log.i(LOG_TAG, "exWorkoutList Exercise: "+"completedWorkouts"+(d-1)+counter+"|"+sp.getString("completedWorkouts"+(d-1)+counter, "")+"|"+exChecked(checked[d-1],counter)+"|"+ListText);
                        ListTextShadow = "1,"+u.getExercise()+",3,"+u.getQuantity()+",0"; //Workout/Special,Workoutnummer,Type(Strth,Std,End.),Anzahl,FromWhere (MainActivity)
                        //ListTextShadow = "1,1,3,"+u.getQuantity()+",0"; //Workout/Special,Workoutnummer,Type(Strth,Std,End.),Anzahl,FromWhere (MainActivity)
                        WorkoutListArrayP.add(ListText);
                        WorkoutListArrayPShadow.add(ListTextShadow);
                        counter++;
                    }
                }
                if(n>0){s =  s.substring(n+1,s.length());}

            }while(cnc);
            switch(d){
                case(1):
                    WorkoutListArray1.addAll(WorkoutListArrayP);
                    WorkoutListArrayShadow1.addAll(WorkoutListArrayPShadow);
                    break;
                case(2):
                    WorkoutListArray2.addAll(WorkoutListArrayP);
                    WorkoutListArrayShadow2.addAll(WorkoutListArrayPShadow);
                    break;
                case(3):
                    WorkoutListArray3.addAll(WorkoutListArrayP);
                    WorkoutListArrayShadow3.addAll(WorkoutListArrayPShadow);
                    break;
                case(4):
                    WorkoutListArray4.addAll(WorkoutListArrayP);
                    WorkoutListArrayShadow4.addAll(WorkoutListArrayPShadow);
                    break;
                case(5):
                    WorkoutListArray5.addAll(WorkoutListArrayP);
                    WorkoutListArrayShadow5.addAll(WorkoutListArrayPShadow);
                    break;
                case(6):
                    WorkoutListArray6.addAll(WorkoutListArrayP);
                    WorkoutListArrayShadow6.addAll(WorkoutListArrayPShadow);
                    break;
                case(7):
                    WorkoutListArray7.addAll(WorkoutListArrayP);
                    WorkoutListArrayShadow7.addAll(WorkoutListArrayPShadow);
                    break;
            }

        }
    }
    private void loadDate() {
        SharedPreferences sp = getPreferences(MODE_PRIVATE);
        lastUpdate = sp.getLong("lastUpdate", 0L);
        customID = sp.getInt("customID", 0);
        datestamp = sp.getInt("datestamp", 0);
        authCode = sp.getString("authCode", "");
        coachUser = sp.getBoolean("coachuser", false);
        calc = sp.getBoolean("calc", false);

        lastAuthDatestamp = sp.getLong("lastAuthDatestamp", 0L);
        //rndType1 = sp.getInt("rndType1", 0);
        //wo_int1 = sp.getInt("wo_int1", 0);
        //rndx1 = sp.getInt("rndx1", 0);
        int sp_skills = sp.getInt("sp_skills", 0);

        if (sp_skills != 0){
            skills = sp_skills;
        }
        int sp_days = sp.getInt("sp_days", 0);

        if (sp_days != 0){
            days = sp_days;
        }
        int sp_current_days = sp.getInt("sp_current_days", 0);
 /*       if (sp_current_days == 0){
            current_days = days;
        }*/
        current_days = sp_current_days;

        int sp_hardness = sp.getInt("sp_hardness", 0);

        if (sp_hardness != 0){
            hardness = sp_hardness;
        }
        int sp_fokus = sp.getInt("sp_fokus", 3);

        if (sp_fokus != 3){
            fokus = sp_fokus;
        }
        spWorkoutList1 = sp.getString("spWorkoutList1", "");
        spWorkoutList2 = sp.getString("spWorkoutList2", "");
        spWorkoutList3 = sp.getString("spWorkoutList3", "");
        spWorkoutList4 = sp.getString("spWorkoutList4", "");
        spWorkoutList5 = sp.getString("spWorkoutList5", "");
        spWorkoutList6 = sp.getString("spWorkoutList6", "");
        spWorkoutList7 = sp.getString("spWorkoutList7", "");
        //this.setTitle(spWorkoutList7);
        checked[0] = sp.getInt("checked0",0);
        checked[1] = sp.getInt("checked1",0);
        checked[2] = sp.getInt("checked2",0);
        checked[3] = sp.getInt("checked3",0);
        checked[4] = sp.getInt("checked4",0);
        checked[5] = sp.getInt("checked5",0);
        checked[6] = sp.getInt("checked6",0);

        sp_changeprefs = Integer.valueOf(sp.getInt("sp_changeprefs", 2));

        if (sp_changeprefs == 0){
                    changeprefs = false;
        }
        if (sp_changeprefs == 1){
                    changeprefs = true;
        }
    }
    private void saveChecked() {
        SharedPreferences sp = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor e = sp.edit();
        //for(int idx=0;idx<7;idx++){e.putInt(String.valueOf("checked"+idx), checked[idx]);}
        e.putInt("checked0", checked[0]);
        e.putInt("checked1", checked[1]);
        e.putInt("checked2", checked[2]);
        e.putInt("checked3", checked[3]);
        e.putInt("checked4", checked[4]);
        e.putInt("checked5", checked[5]);
        e.putInt("checked6", checked[6]);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        e.putLong("lastUpdate",timestamp.getTime());
        e.commit();
    }
    private void savePrefs() {
        SharedPreferences sp = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor e = sp.edit();
        e.putInt("sp_skills", skills);
        e.putInt("sp_days", days);
        e.putInt("sp_hardness", hardness);
        e.putInt("sp_fokus", fokus);
        e.putInt("sp_changeprefs", 1);
        e.commit();
        changeprefs = true;
        // Enable Neutral button
        neutralButton.setEnabled(true);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        e.putLong("lastUpdate",timestamp.getTime());
        generalList();
    }
    private void saveDate() {
        datestamp=Integer.parseInt(getDateAsString());
        SharedPreferences sp = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor e = sp.edit();
        e.putInt("datestamp", datestamp);
        e.commit();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        e.putLong("lastUpdate",timestamp.getTime());
    }
   public String getDateAsString() {
        DateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        return formatter.format(new Date());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    public void onRadioButtonClicked(View v) {
        //require to import the RadioButton class
        RadioButton rb1 = (RadioButton) findViewById(R.id.radioEndurance);
        RadioButton rb2 = (RadioButton) findViewById(R.id.radioStandard);
        RadioButton rb3 = (RadioButton) findViewById(R.id.radioStrength);

        //is the current radio button now checked?
        boolean  checked = ((RadioButton) v).isChecked();
        switch(v.getId()) {

            case R.id.radioEndurance: {
                if (checked) {
                    fokus = 0;
                    savePrefs();
                    //Toast.makeText(MainActivity.this, "RB = Endurance", Toast.LENGTH_LONG).show();
                }
                break;
            }
            case R.id.radioStandard: {
                if (checked) {
                    fokus = 1;
                    savePrefs();
                    //Toast.makeText(MainActivity.this, "RB = Standard", Toast.LENGTH_LONG).show();
                }
                break;
            }
            case R.id.radioStrength: {
                if (checked) {
                    fokus = 2;
                    savePrefs();
                    //Toast.makeText(MainActivity.this, "RB = Strength", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }
    @Override //Menü
    public boolean onOptionsItemSelected(MenuItem item) {
        // Wir prüfen, ob Menü-Element mit der ID "action_settings"
        // ausgewählt wurde und geben eine Meldung aus
        int id = item.getItemId();
        /*if (id == R.id.action_profile) {
            dialog_profile();
            return true;
        }*/
        if (id == R.id.action_settings) {
            dialog_settings();
            return true;
        }
        if (id == R.id.action_history) {
            Intent in = new Intent(MainActivity.this,HistoryActivity.class);
            startActivity(in);
            return true;
        }
        if (id == R.id.action_matrix) {
            Intent in = new Intent(MainActivity.this,MatrixActivity.class);
            startActivity(in);
            return true;
        }
        if (id == R.id.action_matrix2018) {
            Intent in = new Intent(MainActivity.this,Matrix2018Activity.class);
            startActivity(in);
            return true;
        }
        if (id == R.id.action_workouts) {
            Intent in = new Intent(MainActivity.this,WorkoutsActivity.class);
            startActivity(in);
            return true;
        }
        if (id == R.id.action_workouts_2018) {
            Intent in = new Intent(MainActivity.this,Workouts2018Activity.class);
            startActivity(in);
            return true;
        }
        if (id == R.id.action_exercises) {
            Intent in = new Intent(MainActivity.this,ExercisesActivity.class);
            startActivity(in);
            return true;
        }
        if (id == R.id.action_logout) {
            delCompletedWorkouts();
            SharedPreferences sp1 = getPreferences(MODE_PRIVATE);
            SharedPreferences.Editor e = sp1.edit();
            e.remove("customID");
            e.remove("authCode");
            e.remove("spWorkoutList1");
            e.remove("spWorkoutList2");
            e.remove("spWorkoutList3");
            e.remove("spWorkoutList4");
            e.remove("spWorkoutList5");
            e.remove("spWorkoutList6");
            e.remove("spWorkoutList7");
            e.remove("sp_changeprefs");
            e.remove("lastUpdate");
            e.remove("datestamp");
            e.remove("coachuser");
            e.remove("lastAuthDatestamp");
            e.remove("sp_skills");
            e.remove("sp_days");
            e.remove("sp_hardness");
            e.remove("sp_current_days");
            e.remove("sp_fokus");
            e.remove("calc");
            e.commit();
            //current_days=3;
            dataSource.delete();
            authCode="";
            customID=0;
            datestamp=0;
            lastUpdate=0;
            changeprefs=false;
            fokus = 1;
            skills = 9;
            days = 3;
            hardness = 2;
            calc=false;
            loadDate();
            checkForLogin();


            return true;
        }
        return super.onOptionsItemSelected(item);
    }
   public void dialog_login() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(
                new ContextThemeWrapper(this, android.R.style.Theme_DeviceDefault_Light_Dialog));

        LayoutInflater inflater = getLayoutInflater();

        View dialogsViewNL = inflater.inflate(R.layout.dialog_login, null);
        builder.setView(dialogsViewNL);
        final EditText email = (EditText) dialogsViewNL.findViewById(R.id.email);
        final EditText password = (EditText) dialogsViewNL.findViewById(R.id.password);
        builder.setTitle(R.string.dialog_title_login)
                .setPositiveButton(R.string.dialog_button_sign_in, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        editTextEmail=email.getText().toString().toLowerCase();
                        editTextPassword=password.getText().toString();
                        //Toast.makeText(MainActivity.this, "Done = "+ email.getText().toString()+" "+ password.getText().toString(), Toast.LENGTH_LONG).show();

                        if(!editTextEmail.equals("") && !editTextPassword.equals("")){
                            //updateData(editTextEmail,editTextPassword);
                            Log.i(LOG_TAG,"Login: "+editTextEmail);
                            /*SharedPreferences sp = getPreferences(MODE_PRIVATE);
                            SharedPreferences.Editor e = sp.edit();
                            e.putString("email", editTextEmail);

                            e.commit();*/
                            serverCheckLogin(editTextEmail,editTextPassword);

                             //Toast.makeText(MainActivity.this, "Done = "+ edt.getText().toString(), Toast.LENGTH_LONG).show();
                        }
                        else {
                            Toast.makeText(MainActivity.this, getString(R.string.error_sign_in), Toast.LENGTH_LONG).show();
                            dialog_login();
                        }
                        //dialog.dismiss();
                        //serverCheckLogin();
                       // dialog.cancel();
                    }
                })

                .setNeutralButton(R.string.dialog_button_register, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        editTextEmail=email.getText().toString().toLowerCase();
                        editTextPassword=password.getText().toString();


                      if(!isValidEmail(editTextEmail)) {
                            Toast.makeText(MainActivity.this,editTextEmail+" is not a valid email!", Toast.LENGTH_LONG).show();
                            dialog_login();
                        }
                        else if(!editTextEmail.equals("") && !editTextPassword.equals("")){
                            //updateData(editTextEmail,editTextPassword);
                            Log.i(LOG_TAG,"Register: "+editTextEmail);
                            serverCheckRegister(editTextEmail,editTextPassword);
                            //Toast.makeText(MainActivity.this, "Done = "+ edt.getText().toString(), Toast.LENGTH_LONG).show();
                      }
                        else {
                            Toast.makeText(MainActivity.this, getString(R.string.error_sign_in), Toast.LENGTH_LONG).show();
                            dialog_login();
                        }
                        /*generalList();
                        WorkoutCalc();
                        saveDate();
                        loadDate();
                        printWorkout();
                        dialog.cancel();*/
                    }
                });



        // create alert dialog
        final AlertDialog alertDialog = builder.create();
        final TextView textView = (TextView) dialogsViewNL.findViewById(R.id.forgot_pw);
        textView.setPaintFlags(textView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);



       dialogsViewNL.findViewById(R.id.forgot_pw).setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               //Toast.makeText(MainActivity.this, getString(R.string.send_password), Toast.LENGTH_LONG).show();
               editTextEmail=email.getText().toString().toLowerCase();
               serverForgotPassword(editTextEmail);
               //alertDialog.dismiss();
           }
       });
       // show it
       alertDialog.show();
   }
    public void dialog_profile() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(
                new ContextThemeWrapper(this, android.R.style.Theme_DeviceDefault_Light_Dialog));

        LayoutInflater inflater = getLayoutInflater();

        View dialogsViewNL = inflater.inflate(R.layout.dialog_profile, null);
        builder.setView(dialogsViewNL);
        final EditText email = (EditText) dialogsViewNL.findViewById(R.id.profile_email);
        final EditText password = (EditText) dialogsViewNL.findViewById(R.id.profile_password);
        builder.setTitle(R.string.dialog_title_profile)
                .setPositiveButton(R.string.dialog_button_save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        editTextEmail=email.getText().toString().toLowerCase();
                        editTextPassword=password.getText().toString();
                        //Toast.makeText(MainActivity.this, "Done = "+ email.getText().toString()+" "+ password.getText().toString(), Toast.LENGTH_LONG).show();

                        if(!editTextEmail.equals("") && !editTextPassword.equals("")){
                            //updateData(editTextEmail,editTextPassword);
                            Log.i(LOG_TAG,"Profil: "+editTextEmail);
                            /*SharedPreferences sp = getPreferences(MODE_PRIVATE);
                            SharedPreferences.Editor e = sp.edit();
                            e.putString("email", editTextEmail);

                            e.commit();*/
                            //serverCheckLogin(editTextEmail,editTextPassword);

                            //Toast.makeText(MainActivity.this, "Done = "+ edt.getText().toString(), Toast.LENGTH_LONG).show();
                        }
                        else {
                            //Toast.makeText(MainActivity.this, getString(R.string.error_sign_in), Toast.LENGTH_LONG).show();
                            //dialog_login();
                        }
                        dialog.dismiss();
                        //serverCheckLogin();
                        // dialog.cancel();
                    }
                })

                .setNeutralButton(R.string.dialog_button_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        editTextEmail=email.getText().toString().toLowerCase();
                        editTextPassword=password.getText().toString();


                        /*if(!isValidEmail(editTextEmail)) {
                            Toast.makeText(MainActivity.this,editTextEmail+" is not a valid email!", Toast.LENGTH_LONG).show();
                            dialog_login();
                        }
                        else if(!editTextEmail.equals("") && !editTextPassword.equals("")){
                            //updateData(editTextEmail,editTextPassword);
                            Log.i(LOG_TAG,"Register: "+editTextEmail);
                            serverCheckRegister(editTextEmail,editTextPassword);
                            //Toast.makeText(MainActivity.this, "Done = "+ edt.getText().toString(), Toast.LENGTH_LONG).show();
                        }
                        else {
                            Toast.makeText(MainActivity.this, getString(R.string.error_sign_in), Toast.LENGTH_LONG).show();
                            dialog_login();
                        }*/
                        /*generalList();
                        WorkoutCalc();
                        saveDate();
                        loadDate();
                        printWorkout();
                        */
                        dialog.cancel();
                    }
                });



        // create alert dialog
        final AlertDialog alertDialog = builder.create();
//        final TextView textView = (TextView) dialogsViewNL.findViewById(R.id.forgot_pw);
//        textView.setPaintFlags(textView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);



/*        dialogsViewNL.findViewById(R.id.forgot_pw).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(MainActivity.this, getString(R.string.send_password), Toast.LENGTH_LONG).show();
                editTextEmail=email.getText().toString().toLowerCase();
                serverForgotPassword(editTextEmail);
                //alertDialog.dismiss();
            }
        });*/
        // show it
        alertDialog.show();



    }
    public void dialog_settings() {
        android.app.AlertDialog.Builder builder =  new android.app.AlertDialog.Builder(
                new ContextThemeWrapper(this, android.R.style.Theme_DeviceDefault_Light_Dialog));

        LayoutInflater inflater = getLayoutInflater();

        View dialogsViewNL = inflater.inflate(R.layout.dialog_settings, null);

        final Spinner spSpinnerType;
        String spinnerDaysListType[] = { "2", "3", "4","5","6","7" };
        ArrayAdapter<String> adapterSpinnerType;
        spSpinnerType = (Spinner)dialogsViewNL.findViewById(R.id.edit_spinner_days);
        adapterSpinnerType = new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_spinner_item, spinnerDaysListType);
        adapterSpinnerType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spSpinnerType.setAdapter(adapterSpinnerType);


        //int selectionPosition=adapterSpinnerType.getPosition(shoppingMemo.getUnit());
        spSpinnerType.setSelection(days-2);

        spSpinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapter, View v,
                                       int position, long id) {
                // On selecting a spinner item


                if(days != Integer.valueOf(adapter.getItemAtPosition(position).toString())){
                    days = Integer.valueOf(adapter.getItemAtPosition(position).toString());
                    //Toast.makeText(MainActivity.this, "days = "+ String.valueOf(days), Toast.LENGTH_LONG).show();
                    savePrefs();
                }

            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        final Spinner SpinnerType;
        String spinnerHardnessListType[] = { "1","2","3","4","5","6" };
        ArrayAdapter<String> adapterSpinnerHardnessType;
        SpinnerType = (Spinner)dialogsViewNL.findViewById(R.id.edit_spinner_hardness);
        adapterSpinnerHardnessType = new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_spinner_item, spinnerHardnessListType);
        adapterSpinnerHardnessType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        SpinnerType.setAdapter(adapterSpinnerHardnessType);


        //int selectionPosition=adapterSpinnerType.getPosition(shoppingMemo.getUnit());
        SpinnerType.setSelection(hardness-1);

        SpinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapter, View v,
                                       int position, long id) {
                // On selecting a spinner item

                if(hardness != Integer.valueOf(adapter.getItemAtPosition(position).toString())){
                    hardness = Integer.valueOf(adapter.getItemAtPosition(position).toString());
                    //Toast.makeText(MainActivity.this, "days = "+ String.valueOf(hardness), Toast.LENGTH_LONG).show();
                    savePrefs();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
//
            }
        });
        //Toast.makeText(MainActivity.this, "changeprefs = "+ String.valueOf(changeprefs), Toast.LENGTH_LONG).show();

        builder.setView(dialogsViewNL)
                //.setTitle(R.string.menu_name)
                .setPositiveButton(R.string.dialog_button_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                })

                .setNeutralButton(R.string.dialog_button_calc, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        generalList();
                        checkForCalc();
                        //WorkoutCalc();
                        saveDate();
                        loadDate();
                        printWorkout();
                        dialog.cancel();
                    }
                });




        // create alert dialog
        AlertDialog alertDialog = builder.create();



        // show it
        alertDialog.show();
        neutralButton = alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL);

        RadioGroup rg = (RadioGroup)alertDialog.findViewById(R.id.radioType);
        if(fokus == 0){
            rg.check(R.id.radioEndurance);
        }
        if(fokus == 1){
            rg.check(R.id.radioStandard);
        }
        if(fokus == 2){
            rg.check(R.id.radioStrength);
        }

        //loadDate();
        if (!changeprefs) {
            // Disable neutral button
            ((AlertDialog)alertDialog).getButton(AlertDialog.BUTTON_NEUTRAL).setEnabled(false);

            //neutralButton.setEnabled(false);
        }
        // int in binär umwandeln
        String binary = Integer.toBinaryString(skills);
        // für Auswertung binär in int umwandeln
        Integer i = Integer.valueOf(binary);
        // führende Nullen wenn nötig
        String formatted = String.format("%06d",i);
        //Toast.makeText(this, "Prefs: "+formatted, Toast.LENGTH_LONG).show();

        if(String.valueOf(formatted.charAt(formatted.length()-1)).equals("1")){
            // Pushups
            final CheckBox checkBox = (CheckBox)alertDialog.findViewById(R.id.checkbox_pullups);
            checkBox.setChecked(true);
        }
        if(String.valueOf(formatted.charAt(formatted.length()-2)).equals("1")){
            // HS Pushups
            final CheckBox checkBox = (CheckBox)alertDialog.findViewById(R.id.checkbox_hs_pushups);
            checkBox.setChecked(true);
        }
        if(String.valueOf(formatted.charAt(formatted.length()-3)).equals("1")){
            // OH Pushups
            final CheckBox checkBox = (CheckBox)alertDialog.findViewById(R.id.checkbox_oh_pushups);
            checkBox.setChecked(true);
        }
        if(String.valueOf(formatted.charAt(formatted.length()-4)).equals("1")){
            // Toes to Bar
            final CheckBox checkBox = (CheckBox)alertDialog.findViewById(R.id.checkbox_ttb);
            checkBox.setChecked(true);
        }
        if(String.valueOf(formatted.charAt(formatted.length()-5)).equals("1")){
            // Pistol Squats
            final CheckBox checkBox = (CheckBox)alertDialog.findViewById(R.id.checkbox_pistols);
            checkBox.setChecked(true);
        }
        if(String.valueOf(formatted.charAt(formatted.length()-6)).equals("1")){
            // Muscleups
            final CheckBox checkBox = (CheckBox)alertDialog.findViewById(R.id.checkbox_muscleups);
            checkBox.setChecked(true);
        }


    }
    public void onCheckboxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();

        // Check which checkbox was clicked
        switch(view.getId()) {
            case R.id.checkbox_pullups:
                if (checked) {
                    skills=skills+1;
                    savePrefs();
                }
                else {
                    skills=skills-1;
                    savePrefs();
                }
                break;
            case R.id.checkbox_hs_pushups:
                if (checked) {
                    skills=skills+2;
                    savePrefs();
                }
                else {
                    skills=skills-2;
                    savePrefs();
                }
                break;
            case R.id.checkbox_oh_pushups:
                if (checked) {
                    skills=skills+4;
                    savePrefs();
                }
                else {
                    skills=skills-4;
                    savePrefs();
                }
                break;
            case R.id.checkbox_ttb:
                if (checked) {
                    skills=skills+8;
                    savePrefs();
                }
                else {
                    skills=skills-8;
                    savePrefs();
                }
                break;
            case R.id.checkbox_pistols:
                if (checked) {
                    skills=skills+16;
                    savePrefs();
                }
                else {
                    skills=skills-16;
                    savePrefs();
                }
                break;
            case R.id.checkbox_muscleups:
                if (checked) {
                    skills=skills+32;
                    savePrefs();
                }
                else {
                    skills=skills-32;
                    savePrefs();
                }
                break;


        }
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

    public static boolean isValidEmail(String email){
        boolean stricterFilter = true;
        String stricterFilterString = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}";
        String laxString = ".+@.+\\.[A-Za-z]{2}[A-Za-z]*";
        String emailRegex = stricterFilter ? stricterFilterString : laxString;
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(emailRegex);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }
}
