package de.spas.freeworkout;


import android.app.Activity;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class MatrixActivity extends Activity {

    public static final String LOG_TAG = MatrixActivity.class.getSimpleName();
    private WorkoutMemoDataSource dataSource;
    private de.spas.freeworkout.workoutPack workoutPack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(LOG_TAG, "Die Datenbank wird geöffnet.");
        dataSource = new WorkoutMemoDataSource(this);
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

        ScrollView sv = new ScrollView(this);

        TableLayout ll=new TableLayout(this);
        ll.setColumnStretchable(0,true);
        HorizontalScrollView hsv = new HorizontalScrollView(this);

        TableRow tbrowf=new TableRow(this);
        TextView tvf=new TextView(this);
        tvf.setText("Difficulty");
        tvf.setTextColor(getResources().getColor(R.color.colorLightBlack));
        tvf.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        TableRow.LayoutParams params99 = new TableRow.LayoutParams();
        params99.setMargins(2, 0, 0, 0);
        /*tvf.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.FILL_PARENT, 1.0f));*/
        tbrowf.addView(tvf,params99);

        TextView tvf1=new TextView(this);
        tvf1.setText(" Endurance");
        tvf1.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        tvf1.setTextColor(getResources().getColor(R.color.colorLightBlack));

        TableRow.LayoutParams params1 = new TableRow.LayoutParams();
        params1.span = 3;
        params1.setMargins(2, 0, 0, 0);
        tbrowf.addView(tvf1,1,params1);

        TextView tvf2=new TextView(this);
        tvf2.setText(" Standard");
        tvf2.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        tvf2.setTextColor(getResources().getColor(R.color.colorLightBlack));
        tbrowf.addView(tvf2,2,params1);

        TextView tvf3=new TextView(this);
        tvf3.setText(" Strength");
        tvf3.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        tvf3.setTextColor(getResources().getColor(R.color.colorLightBlack));
        tbrowf.addView(tvf3,3,params1);

        ll.addView(tbrowf);



        TableRow tbrow0=new TableRow(this);
        TextView tv10=new TextView(this);
        tv10.setText("Workout");
        tv10.setBackgroundColor(getResources().getColor(R.color.colorLighterGray));
        tv10.setTextColor(getResources().getColor(R.color.colorLightBlack));
        TableRow.LayoutParams params0 = new TableRow.LayoutParams();
        params0.setMargins(2, 2, 0, 0);
        tbrow0.addView(tv10,params0);
        for(int i=0;i<3;i++) {
            for(int j=1;j<=3;j++) {
                TextView tvn=new TextView(this);
                tvn.setText("  "+j+"x ");
                tvn.setBackgroundColor(getResources().getColor(R.color.colorLighterGray));
                tvn.setTextColor(getResources().getColor(R.color.colorLightBlack));

                TableRow.LayoutParams params2 = new TableRow.LayoutParams();
                params2.setMargins(2, 2, 0, 0);

                tbrow0.addView(tvn,params2);
            }
        }


        ll.addView(tbrow0);

/*        View v1 = new View(this);
        LinearLayout.LayoutParams paramsV1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2);
        v1.setLayoutParams(paramsV1);
        v1.setBackgroundColor(getResources().getColor(android.R.color.white));
        ll.addView(v1);*/
        int workoutCount = workoutPack.getWorkouts().size();

        for(int i=0;i<workoutCount;i++) {
            Workout w = workoutPack.getWorkouts().get(i);
            String wName=w.getName();
            TableRow tbrow=new TableRow(this);

            for(int j=0;j<=9;j++) {
                TextView tv1=new TextView(this);
                int q;
                int d;

                if(j==0) {
                    tv1.setText(wName);
                    tv1.setBackgroundColor(getResources().getColor(R.color.colorLighterGray));
                    tv1.setTextColor(getResources().getColor(R.color.colorLightBlack));
                }
                else {
                    String text_pb="";
                    if(j<4){
                        q=j;
                        d=0;
                    }
                    else if (j>0 && j<7){
                        q=j-3;
                        d=1;
                    }
                    else {
                        q=j-6;
                        d=2;
                    }
                    text_pb=dataSource.getMinDuration(q,wName,d); //Abfrage DB kürzestes WO
                    tv1.setText(" ");
                    if(text_pb.equals("")) tv1.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
                    else  tv1.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                }
                TableRow.LayoutParams params3 = new TableRow.LayoutParams();
                params3.setMargins(2, 2, 0, 0);

                tbrow.addView(tv1,params3);
            }
            ll.addView(tbrow);

        }
        hsv.addView(ll);
        sv.addView(hsv);
        setContentView(sv);

    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "Die Datenbank wird geschlossen.");
        dataSource.close();
    }

}
