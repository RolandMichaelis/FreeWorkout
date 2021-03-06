package de.spas.freeworkout;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by roland on 25.05.2017.
 */

public class WorkoutNewChooseActivity extends Activity {

    private de.spas.freeworkout.workoutNewPack workoutNewPack;
    View rootView;
    int points;
    int rounds;
    int type;
    String wName;
    String printRounds ;
    int quantity=1;
    int wo_choose;
    private Spinner spSpinnerType;
    private String[] spinnerRoundsListType;
    private int roundsValue;
    private WorkoutMemoDataSource dataSource;
    private ListView mWorkoutMemosListView;
    public static final String LOG_TAG = WorkoutNewChooseActivity.class.getSimpleName();
    Context c;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_standard);


        c=this;  // Context zum Übergeben an andere Klassen speichern (z.B) an workoutMemoDataSource

        //Log.i(LOG_TAG, "Die Datenbank wird geöffnet.");
        dataSource = new WorkoutMemoDataSource(this);
        //dataSource.open();

        try {
            InputStream source = this.getAssets().open("workouts_new.xml");
            Serializer serializer = new Persister();
            workoutNewPack = serializer.read(de.spas.freeworkout.workoutNewPack.class, source);
            //Toast.makeText(this, "Wow! Klappt! workoutNewPack loading successful", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            //Toast.makeText(this, "Oh oh! workoutNewPack loading failed!", Toast.LENGTH_LONG).show();
            Log.e(getClass().getSimpleName(), "loading levels threw exception: workoutNewPack loading failed", e);
        }
        Intent empfangenerIntent = this.getIntent();
        if (empfangenerIntent != null && empfangenerIntent.hasExtra(Intent.EXTRA_TEXT)) {
            //position
            wo_choose = Integer.valueOf(empfangenerIntent.getStringExtra(Intent.EXTRA_TEXT));
            WorkoutNew w = workoutNewPack.getWorkouts().get(wo_choose);
            this.setTitle(w.getName());
            type=w.getType(); // type ist bei den 2018ern in type gespeichert, -1 wenn keine alten Endurance oder Standard WO vorhanden sind
        }
        //Toast.makeText(this, String.valueOf(s), Toast.LENGTH_LONG).show();
        initializeWorkoutMemosListView();
        if(wo_choose<0) {
            Toast.makeText(this, "Kein Wert in wo_choose!", Toast.LENGTH_LONG).show();
        }
        else{
            WorkoutNew w = workoutNewPack.getWorkouts().get(wo_choose);
            wName = w.getName();
            points= w.getPoints();
            //Standard wo = workoutPack.getWorkouts().get(wo_choose).getStandard();
            rounds = 0;
            for (Round r : w.getRounds()) { // alle Round-Knoten durchlaufen
                rounds++;
            }
            //spinner_value=rounds-1; // anfangs immer letzte Position im Runden-Spinner z.B. auf 5/5
            //Toast.makeText(getContext(), "Runden: "+String.valueOf(rounds), Toast.LENGTH_LONG).show();

            //TextView text = (TextView) rootView.findViewById(R.id.text_points);
            //text.setText(getString(R.string.points, points));
            //Toast.makeText(getContext(), "Punkte: "+String.valueOf(points), Toast.LENGTH_LONG).show();
        }
        spinnerRoundsListType = new String[rounds];
        for(int i = 0; i < rounds; i++){
            spinnerRoundsListType[i]=String.valueOf(i+1)+"/"+String.valueOf(rounds);
        }
        final ArrayAdapter<String> adapterSpinnerType;
        spSpinnerType = (Spinner) this.findViewById(R.id.edit_spinner_rounds);
        adapterSpinnerType = new ArrayAdapter<String>(WorkoutNewChooseActivity.this, android.R.layout.simple_spinner_item, spinnerRoundsListType);
        adapterSpinnerType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spSpinnerType.setAdapter(adapterSpinnerType);
        spSpinnerType.setSelection(rounds-1);

        spSpinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapter, View v,
                                       int position, long id) {
                // On selecting a spinner item
                //float quantRounds=0;
                roundsValue=position+1;
                //Workout w = workoutPack.getWorkouts().get(wo_choose);
                //points=w.getStandard().getPoints();
                //Toast.makeText(getContext(), "Spinner geklickt! getPoints()="+String.valueOf(points), Toast.LENGTH_LONG).show();
                //double pointsPerRound =  points/rounds;
                float a = points; //In float umwandeln, sonst rechnet es INT aus
                float b = rounds; // dto
                float pointsPerRound =  a/b;
                //Toast.makeText(getContext(), "Spinner geklickt! pointsPerRound="+String.valueOf(pointsPerRound), Toast.LENGTH_LONG).show();
                    /*if(quantity==1)  quantRounds = position+1;
                    if(quantity==2)  quantRounds = rounds+position+1;
                    if(quantity==3)  quantRounds = (2*rounds)+position+1;*/
                float quantRounds =((quantity-1)*rounds)+position+1;
                TextView text = (TextView) WorkoutNewChooseActivity.this.findViewById(R.id.text_points);
                float p = quantRounds*pointsPerRound;
                text.setText(getString(R.string.points, (int) p));
                if(rounds==position+1){
                    printRounds="";
                }
                else {
                    printRounds=" ("+(int) quantRounds+"/"+rounds*quantity+")";
                }
                Log.i(LOG_TAG, "wName+printRounds: "+wName+printRounds+" quantity: "+quantity);

                printPBLT(quantity,wName+printRounds,type);
                showAllListEntries(quantity,wName+printRounds,type);
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        RadioGroup radioGroup = (RadioGroup) WorkoutNewChooseActivity.this.findViewById(R.id.radioQuantity);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // checkedId is the RadioButton selected

                TextView text = (TextView) WorkoutNewChooseActivity.this.findViewById(R.id.text_points);
                switch(checkedId) {
                    case R.id.radio1x:
                        // switch to fragment 1
                        quantity=1;
                        text.setText(getString(R.string.points, points));
                        for(int i = 0; i < rounds; i++){
                            spinnerRoundsListType[i]=String.valueOf(i+1)+"/"+String.valueOf(rounds);
                        }
                        adapterSpinnerType.notifyDataSetChanged();

                        spSpinnerType.setSelection(rounds-1);
                        printPBLT(quantity,wName,type);
                        showAllListEntries(quantity,wName,type);

                        break;
                    case R.id.radio2x:
                        // Fragment 2
                        quantity=2;
                        text.setText(getString(R.string.points, points*2));
                        for(int i = 0; i < rounds; i++){
                            spinnerRoundsListType[i]=String.valueOf(i+1+rounds)+"/"+String.valueOf(2*rounds);
                        }
                        adapterSpinnerType.notifyDataSetChanged();
                        spSpinnerType.setSelection(rounds-1);
                        printPBLT(quantity,wName,type);
                        showAllListEntries(quantity,wName,type);
                        break;
                    case R.id.radio3x:
                        // Fragment 3
                        // Toast.makeText(getContext(), "RB = 2x", Toast.LENGTH_LONG).show();
                        quantity=3;
                        text.setText(getString(R.string.points, points*3));
                        for(int i = 0; i < rounds; i++){
                            spinnerRoundsListType[i]=String.valueOf((i+1)+(2*rounds))+"/"+String.valueOf(3*rounds);
                        }
                        adapterSpinnerType.notifyDataSetChanged();
                        spSpinnerType.setSelection(rounds-1);
                        printPBLT(quantity,wName,type);
                        showAllListEntries(quantity,wName,type);
                        break;
                }
            }
        });
        Button button = (Button)  WorkoutNewChooseActivity.this.findViewById(R.id.button_wo_display);
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                switch (v.getId()) {
                    case R.id.button_wo_display:
                        //Toast.makeText(getActivity(), "Wow! Klappt!", Toast.LENGTH_LONG).show();
                        if(rounds==roundsValue){
                            roundsValue=0;
                        }
                        Intent workoutFragmentIntent = new Intent(WorkoutNewChooseActivity.this, WorkoutActivity.class);
                        String ListTextShadow = "2,"+String.valueOf(wo_choose)+","+type+","+String.valueOf(quantity)+",-1,-1,"+roundsValue; //Workout(0)/Special(1)/Workout2018(2),Workoutnummer,Type(Strth,Std,End.),Anzahl,checked_day,checked_pos (-1 da nicht über Coach gewählt),gewählte Rundenanzahl

                        workoutFragmentIntent.putExtra(Intent.EXTRA_TEXT, ListTextShadow);
                        startActivity(workoutFragmentIntent);
                        break;
                    default:
                        //Toast.makeText(getActivity(), "Seltsam!", Toast.LENGTH_LONG).show();
                        break;

                }
            }
        });

    }
    private void initializeWorkoutMemosListView() {
        List<WorkoutMemo> emptyListForInitialization = new ArrayList<>();

        mWorkoutMemosListView = (ListView) this.findViewById(R.id.listview_workout_memos);

        // Erstellen des ArrayAdapters für unseren ListView
        ArrayAdapter<WorkoutMemo> shoppingMemoArrayAdapter = new ArrayAdapter<WorkoutMemo> (
                this,
                android.R.layout.simple_list_item_1,
                emptyListForInitialization) {

            // Wird immer dann aufgerufen, wenn der übergeordnete ListView die Zeile neu zeichnen muss
/*            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                View view =  super.getView(position, convertView, parent);
                TextView textView = (TextView) view;

                WorkoutMemo memo = (WorkoutMemo) mWorkoutMemosListView.getItemAtPosition(position);

                if (memo.isChecked()) {
                    textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    textView.setTextColor(Color.rgb(175,175,175));
                }
                else {
                    textView.setPaintFlags( textView.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
                    textView.setTextColor(Color.DKGRAY);
                }

                return view;
            }*/
        };

        mWorkoutMemosListView.setAdapter(shoppingMemoArrayAdapter);
/*
        mWorkoutMemosListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                WorkoutMemo memo = (WorkoutMemo) adapterView.getItemAtPosition(position);

                WorkoutMemo updatedWorkoutMemo = dataSource.updateWorkoutMemo(memo.getId(), memo.getWore(), memo.getNumber(),  memo.getName(), memo.getType(),  memo.getQuantity(),  memo.getRounds(),  memo.getStartTime(),  memo.getEndTime(),  memo.getDuration(),   memo.getExTimes(),  memo.getStar(),  memo.isUpload(), (!memo.isChecked()));
                Log.d(LOG_TAG, "Checked-Status von Eintrag: " + updatedWorkoutMemo.toString() + " ist: " + updatedWorkoutMemo.isChecked());
                showAllListEntries();
            }
        });*/

    }
    private void showAllListEntries (int q,String n,int t) {
        List<WorkoutMemo> workoutMemoList = dataSource.getWorkoutMemos(q,n,t,c);  // Quantity,Name,type,Context

        ArrayAdapter<WorkoutMemo> adapter = (ArrayAdapter<WorkoutMemo>) mWorkoutMemosListView.getAdapter();
        adapter.clear();
        adapter.addAll(workoutMemoList);
        adapter.notifyDataSetChanged();
        Log.i(LOG_TAG, "workoutMemoList Eintrag: " + workoutMemoList);

    }

    private void printPBLT(int q, String r, int t) {
        // Input: q=quantity, r=Namenszusatz (z.B: 2/3), t=type
        // sucht MinDuration und MaxStartTime in der DB und gibt diese Werte aus

        final TextView textViewPB = (TextView) this.findViewById(R.id.fragment_dialog_pb);
        final TextView textViewLT = (TextView) this.findViewById(R.id.fragment_dialog_lt);
        String text_pb=dataSource.getMinDuration(q,r,t); //Abfrage DB kürzestes WO
        if(!text_pb.equals("")){
            int n = text_pb.indexOf(" ");
            text_pb = text_pb.substring(n+1,text_pb.length());
            textViewPB.setText(text_pb);
            showView(R.id.row_dialog_pb);
        }
        else {
            textViewPB.setText(text_pb);
            hideView(R.id.row_dialog_pb);
        }
        String text_lt=dataSource.getMaxStartTime(q,r,t); //Abfrage DB letztes WO
        if(!text_lt.equals("")){
            int n = text_lt.indexOf(" ");
            text_lt = text_lt.substring(n+1,text_lt.length());
            textViewLT.setText(text_lt);
            showView(R.id.row_dialog_lt);
        }
        else {
            textViewLT.setText(text_lt);
            hideView(R.id.row_dialog_lt);
        }

    }
    protected void hideView(int id) {
        this.findViewById(id).setVisibility(View.GONE);
    }
    protected void showView(int id) {
        this.findViewById(id).setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        //Log.d(LOG_TAG, "onResume: showAllListEntries: "+quantity+"/"+wName+printRounds+"/"+type);
        dataSource.open();
//        Toast.makeText(getContext(), "onResume: showAllListEntries: "+quantity+"/"+wName+printRounds+"/"+type, Toast.LENGTH_LONG).show();
//        Log.d(LOG_TAG, "Folgende Einträge sind in der Datenbank vorhanden:");
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(LOG_TAG, "Die Datenquelle wird geschlossen.");
        dataSource.close();
    }

}
