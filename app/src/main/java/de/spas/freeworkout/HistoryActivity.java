package de.spas.freeworkout;


import android.app.Activity;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
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
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


import tools.BaseGameActivity;


public class HistoryActivity extends Activity {

    public static final String LOG_TAG = HistoryActivity.class.getSimpleName();
    private WorkoutMemoDataSource dataSource;
    private ListView mWorkoutMemosListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_activity);
        dataSource = new WorkoutMemoDataSource(this);
        initializeWorkoutMemosListView();
        initializeContextualActionBar();
    }

    private void initializeWorkoutMemosListView() {
        List<WorkoutMemo> emptyListForInitialization = new ArrayList<>();

        mWorkoutMemosListView = (ListView) findViewById(R.id.listview_workout_memos);

        // Erstellen des ArrayAdapters für unseren ListView
        ArrayAdapter<WorkoutMemo> shoppingMemoArrayAdapter = new ArrayAdapter<WorkoutMemo> (
                this,
                android.R.layout.simple_list_item_multiple_choice,
                emptyListForInitialization) {

            // Wird immer dann aufgerufen, wenn der übergeordnete ListView die Zeile neu zeichnen muss
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                View view =  super.getView(position, convertView, parent);
                TextView textView = (TextView) view;

                WorkoutMemo memo = (WorkoutMemo) mWorkoutMemosListView.getItemAtPosition(position);

                // Hier prüfen, ob Eintrag abgehakt ist. Falls ja, Text durchstreichen
                if (memo.isChecked()) {
                    textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    textView.setTextColor(Color.rgb(175,175,175));
                }
                else {
                    textView.setPaintFlags( textView.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
                    textView.setTextColor(Color.DKGRAY);
                }

                return view;
            }
        };

        mWorkoutMemosListView.setAdapter(shoppingMemoArrayAdapter);

        mWorkoutMemosListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                WorkoutMemo memo = (WorkoutMemo) adapterView.getItemAtPosition(position);

                // Hier den checked-Wert des Memo-Objekts umkehren, bspw. von true auf false
                // Dann ListView neu zeichnen mit showAllListEntries()

                WorkoutMemo updatedWorkoutMemo = dataSource.updateWorkoutMemo(memo.getId(), memo.getWore(), memo.getNumber(),  memo.getName(), memo.getType(),  memo.getQuantity(),  memo.getStartTime(),  memo.getEndTime(),  memo.getDuration(),   memo.getExTimes(),  memo.getStar(),  memo.isUpload(), (!memo.isChecked()));
                Log.d(LOG_TAG, "Checked-Status von Eintrag: " + updatedWorkoutMemo.toString() + " ist: " + updatedWorkoutMemo.isChecked());
                showAllListEntries();
            }
        });

    }

    private void showAllListEntries () {
        List<WorkoutMemo> workoutMemoList = dataSource.getAllWorkoutMemos();

        ArrayAdapter<WorkoutMemo> adapter = (ArrayAdapter<WorkoutMemo>) mWorkoutMemosListView.getAdapter();

        adapter.clear();
        adapter.addAll(workoutMemoList);
        adapter.notifyDataSetChanged();
    }
    private void initializeContextualActionBar() {
        final ListView workoutMemosListView = (ListView) findViewById(R.id.listview_workout_memos);
        workoutMemosListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

        workoutMemosListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            int selCount = 0;

            // In dieser Callback-Methode zählen wir die ausgewählen Listeneinträge mit
            // und fordern ein Aktualisieren der Contextual Action Bar mit invalidate() an
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                if (checked) {
                    selCount++;
                } else {
                    selCount--;
                }
                String cabTitle = selCount + " " + getString(R.string.cab_checked_string);
                mode.setTitle(cabTitle);
                mode.invalidate();
            }

            // In dieser Callback-Methode legen wir die CAB-Menüeinträge an
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                getMenuInflater().inflate(R.menu.menu_contextual_action_bar, menu);
                return true;
            }
            // In dieser Callback-Methode reagieren wir auf den invalidate() Aufruf
            // Wir lassen das Edit-Symbol verschwinden, wenn mehr als 1 Eintrag ausgewählt ist
            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                MenuItem item = menu.findItem(R.id.cab_change);
                if (selCount == 1) {
                    item.setVisible(false);
                } else {
                    item.setVisible(false);
                }

                return true;
            }

            // In dieser Callback-Methode reagieren wir auf Action Item-Klicks
            // Je nachdem ob das Löschen- oder Ändern-Symbol angeklickt wurde
            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                boolean returnValue = true;
                SparseBooleanArray touchedWorkoutMemosPositions = workoutMemosListView.getCheckedItemPositions();

                switch (item.getItemId()) {
                    case R.id.cab_delete:
                        for (int i = 0; i < touchedWorkoutMemosPositions.size(); i++) {
                            boolean isChecked = touchedWorkoutMemosPositions.valueAt(i);
                            if (isChecked) {
                                int positionInListView = touchedWorkoutMemosPositions.keyAt(i);
                                WorkoutMemo workoutMemo = (WorkoutMemo) workoutMemosListView.getItemAtPosition(positionInListView);
                                //Log.d(LOG_TAG, "Position im ListView: " + positionInListView + " Inhalt: " + workoutMemo.toString() + ":" + unit);
                                dataSource.deleteWorkoutMemo(workoutMemo);
                            }
                        }
                        showAllListEntries();
                        mode.finish();
                        break;
                    case R.id.cab_change:
                        Log.d(LOG_TAG, "Eintrag ändern");
                        for (int i = 0; i < touchedWorkoutMemosPositions.size(); i++) {
                            boolean isChecked = touchedWorkoutMemosPositions.valueAt(i);
                            if (isChecked) {
                                int positionInListView = touchedWorkoutMemosPositions.keyAt(i);
                                WorkoutMemo workoutMemo = (WorkoutMemo) workoutMemosListView.getItemAtPosition(positionInListView);
                                //Log.d(LOG_TAG, "Position im ListView: " + positionInListView + " Inhalt: " + workoutMemo.toString() + ":" + unit);

                                //AlertDialog editShoppingMemoDialog = createEditShoppingMemoDialog(shoppingMemo);
                                //editShoppingMemoDialog.show();
                            }
                        }

                        mode.finish();
                        break;

                }
                return returnValue;
            }

            // In dieser Callback-Methode reagieren wir auf das Schließen der CAB
            // Wir setzen den Zähler auf 0 zurück
            @Override
            public void onDestroyActionMode(ActionMode mode) {
                selCount = 0;
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();

        Log.d(LOG_TAG, "Die Datenquelle wird geöffnet.");
        dataSource.open();

        Log.d(LOG_TAG, "Folgende Einträge sind in der Datenbank vorhanden:");
        showAllListEntries();
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.d(LOG_TAG, "Die Datenquelle wird geschlossen.");
        dataSource.close();
    }

}
