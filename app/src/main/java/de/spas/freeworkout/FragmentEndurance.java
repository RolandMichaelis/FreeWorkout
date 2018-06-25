package de.spas.freeworkout;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.InputStream;



import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import java.io.InputStream;

/**
 * Created by roland on 25.05.2017.
 */

public class FragmentEndurance extends Fragment{

    private de.spas.freeworkout.workoutPack workoutPack;
    View rootView;
    int points;
    int rounds;
    int quantity=1;
    int wo_choose;
    private Spinner spSpinnerType;
    private String[] spinnerRoundsListType;
    private int spinner_value;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_endurance, container, false);


        try {
            InputStream source = getActivity().getAssets().open("workouts.xml");
            Serializer serializer = new Persister();
            workoutPack = serializer.read(de.spas.freeworkout.workoutPack.class, source);
            //Toast.makeText(getActivity(), "Wow! Klappt!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            //Toast.makeText(getContext(), "Oh oh! workoutPack", Toast.LENGTH_LONG).show();
            Log.e(getClass().getSimpleName(), "loading levels threw exception", e);
        }
        SharedPreferences sp = getActivity().getPreferences(Context.MODE_PRIVATE);
        wo_choose = sp.getInt("wo_choose", -1);
        //Toast.makeText(getContext(), String.valueOf(wo_choose), Toast.LENGTH_LONG).show();

        if(wo_choose<0) {
            Toast.makeText(getContext(), "Kein Wert in wo_choose!", Toast.LENGTH_LONG).show();
        }
        else{
            Workout w = workoutPack.getWorkouts().get(wo_choose);
            points=w.getEndurance().getPoints();
            Endurance wo = workoutPack.getWorkouts().get(wo_choose).getEndurance();
            rounds = 0;
            for (Round r : wo.getRounds()) { // alle Round-Knoten durchlaufen
                rounds++;
            }
            spinner_value=rounds-1; // anfangs immer letzte Position im Runden-Spinner z.B. auf 5/5
            Toast.makeText(getContext(), "Runden: "+String.valueOf(rounds), Toast.LENGTH_LONG).show();

            TextView text = (TextView) rootView.findViewById(R.id.text_points);
            text.setText(getString(R.string.points, points));
            //Toast.makeText(getContext(), "Punkte: "+String.valueOf(points), Toast.LENGTH_LONG).show();
        }
        spinnerRoundsListType = new String[rounds];
        for(int i = 0; i < rounds; i++){
            spinnerRoundsListType[i]=String.valueOf(i+1)+"/"+String.valueOf(rounds);
        }

        final ArrayAdapter<String> adapterSpinnerType;
        spSpinnerType = (Spinner) this.rootView.findViewById(R.id.edit_spinner_rounds);
        adapterSpinnerType = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_item, spinnerRoundsListType);
        adapterSpinnerType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spSpinnerType.setAdapter(adapterSpinnerType);
        spSpinnerType.setSelection(rounds-1);

        spSpinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapter, View v,
                                       int position, long id) {
                // On selecting a spinner item
                int quantRounds=0;
                    Toast.makeText(getContext(), "Spinner geklickt! position="+String.valueOf(position), Toast.LENGTH_LONG).show();
                    spinner_value=position;
                    Workout w = workoutPack.getWorkouts().get(wo_choose);
                    points=w.getEndurance().getPoints();
                    int pointsPerRound =  points/rounds;
                    if(quantity==1)  quantRounds = position+1;
                    if(quantity==2)  quantRounds = rounds+position+1;
                    if(quantity==3)  quantRounds = (2*rounds)+position+1;
                    TextView text = (TextView) rootView.findViewById(R.id.text_points);
                    text.setText(getString(R.string.points, quantRounds*pointsPerRound));


            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
//
            }
        });



        RadioGroup radioGroup = (RadioGroup) rootView.findViewById(R.id.radioQuantity);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // checkedId is the RadioButton selected

                TextView text = (TextView) rootView.findViewById(R.id.text_points);
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
                        break;
                }
            }
        });
        Button button = (Button) rootView.findViewById(R.id.button_wo_display);
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                switch (v.getId()) {
                    case R.id.button_wo_display:
                        //Toast.makeText(getActivity(), "Wow! Klappt!", Toast.LENGTH_LONG).show();
                        Intent workoutFragmentIntent = new Intent(getActivity(), WorkoutActivity.class);
                        String ListTextShadow = "0,"+String.valueOf(wo_choose)+",0,"+String.valueOf(quantity)+",-1,-1"; //Workout/Special,Workoutnummer,Type(Strth,Std,End.),Anzahl,checked_day,checked_pos -1 da nicht über Coach gewählt

                        workoutFragmentIntent.putExtra(Intent.EXTRA_TEXT, ListTextShadow);
                        startActivity(workoutFragmentIntent);
                        break;
                    default:
                        //Toast.makeText(getActivity(), "Seltsam!", Toast.LENGTH_LONG).show();
                        break;

                }
            }
        });
        return rootView;

    }
}
