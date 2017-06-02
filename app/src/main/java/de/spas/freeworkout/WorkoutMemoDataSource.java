package de.spas.freeworkout;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import tools.BaseGameActivity;


public class WorkoutMemoDataSource extends BaseGameActivity {

    private static final String LOG_TAG = WorkoutMemoDataSource.class.getSimpleName();

    private SQLiteDatabase database;
    private WorkoutMemoDbHelper dbHelper;

    private String[] columns = {
            WorkoutMemoDbHelper.COLUMN_ID,
            WorkoutMemoDbHelper.COLUMN_WORE,
            WorkoutMemoDbHelper.COLUMN_NUMBER,
            WorkoutMemoDbHelper.COLUMN_NAME,
            WorkoutMemoDbHelper.COLUMN_TYPE,
            WorkoutMemoDbHelper.COLUMN_QUANTITY,
            WorkoutMemoDbHelper.COLUMN_STARTTIME,
            WorkoutMemoDbHelper.COLUMN_ENDTIME,
            WorkoutMemoDbHelper.COLUMN_DURATION,
            WorkoutMemoDbHelper.COLUMN_EXTIMES,
            WorkoutMemoDbHelper.COLUMN_STAR,
            WorkoutMemoDbHelper.COLUMN_CHECKED,
            WorkoutMemoDbHelper.COLUMN_UPLOAD
    };
    private String[] columns_duration_min = {
            WorkoutMemoDbHelper.COLUMN_ID,
            WorkoutMemoDbHelper.COLUMN_WORE,
            WorkoutMemoDbHelper.COLUMN_NUMBER,
            WorkoutMemoDbHelper.COLUMN_NAME,
            WorkoutMemoDbHelper.COLUMN_TYPE,
            WorkoutMemoDbHelper.COLUMN_QUANTITY,
            WorkoutMemoDbHelper.COLUMN_STARTTIME,
            WorkoutMemoDbHelper.COLUMN_ENDTIME,
            "min(" + WorkoutMemoDbHelper.COLUMN_DURATION + ")",
            WorkoutMemoDbHelper.COLUMN_EXTIMES,
            WorkoutMemoDbHelper.COLUMN_STAR,
            WorkoutMemoDbHelper.COLUMN_CHECKED,
            WorkoutMemoDbHelper.COLUMN_UPLOAD
    };

    public WorkoutMemoDataSource(Context context) {
        Log.d(LOG_TAG, "Unsere DataSource erzeugt jetzt den dbHelper.");
        dbHelper = new WorkoutMemoDbHelper(context);
    }

    public void open() {
        Log.d(LOG_TAG, "Eine Referenz auf die Datenbank wird jetzt angefragt.");
        database = dbHelper.getWritableDatabase();
        Log.d(LOG_TAG, "Datenbank-Referenz erhalten. Pfad zur Datenbank: " + database.getPath());
    }

    public void close() {
        dbHelper.close();
        Log.d(LOG_TAG, "Datenbank mit Hilfe des DbHelpers geschlossen.");
    }

    public WorkoutMemo createWorkoutMemo(int wore, int number, String name, int type, int quantity, long startTime, long endTime, long duration, String exTimes, boolean star, boolean checked, boolean upload) {
        ContentValues values = new ContentValues();
        values.put(WorkoutMemoDbHelper.COLUMN_WORE, wore);
        values.put(WorkoutMemoDbHelper.COLUMN_NUMBER, number);
        values.put(WorkoutMemoDbHelper.COLUMN_NAME, name);
        values.put(WorkoutMemoDbHelper.COLUMN_TYPE, type);
        values.put(WorkoutMemoDbHelper.COLUMN_QUANTITY, quantity);
        values.put(WorkoutMemoDbHelper.COLUMN_STARTTIME, startTime);
        values.put(WorkoutMemoDbHelper.COLUMN_ENDTIME, endTime);
        values.put(WorkoutMemoDbHelper.COLUMN_DURATION, duration);
        values.put(WorkoutMemoDbHelper.COLUMN_EXTIMES, exTimes);
        values.put(WorkoutMemoDbHelper.COLUMN_STAR, star);
        values.put(WorkoutMemoDbHelper.COLUMN_CHECKED, checked);
        values.put(WorkoutMemoDbHelper.COLUMN_UPLOAD, upload);

        long insertId = database.insert(WorkoutMemoDbHelper.TABLE_WORKOUT_LIST, null, values);

        Cursor cursor = database.query(WorkoutMemoDbHelper.TABLE_WORKOUT_LIST,
                columns, WorkoutMemoDbHelper.COLUMN_ID + "=" + insertId,
                null, null, null, null);

        cursor.moveToFirst();
        WorkoutMemo workoutMemo = cursorToWorkoutMemo(cursor);
        cursor.close();
        Log.d(LOG_TAG, "Eintrag angelegt! ID: "+cursor+" Inhalt: " + workoutMemo.toString());

        return workoutMemo;
    }

    public void deleteWorkoutMemo(WorkoutMemo workoutMemo) {
        long id = workoutMemo.getId();

        database.delete(WorkoutMemoDbHelper.TABLE_WORKOUT_LIST,
                WorkoutMemoDbHelper.COLUMN_ID + "=" + id,
                null);

        Log.d(LOG_TAG, "Eintrag gelöscht! ID: " + id + " Inhalt: " + workoutMemo.toString());
    }

    public WorkoutMemo updateWorkoutMemo(long id, int newWore, int newNumber, String newName, int newType, int newQuantity, long newStartTime, long newEndTime, long newDuration, String newExTimes, boolean newStar, boolean newChecked, boolean newUpload) {
        int intValueChecked = (newChecked)? 1 : 0;

        ContentValues values = new ContentValues();
        values.put(WorkoutMemoDbHelper.COLUMN_WORE, newWore);
        values.put(WorkoutMemoDbHelper.COLUMN_NUMBER, newNumber);
        values.put(WorkoutMemoDbHelper.COLUMN_NAME, newName);
        values.put(WorkoutMemoDbHelper.COLUMN_TYPE, newType);
        values.put(WorkoutMemoDbHelper.COLUMN_QUANTITY, newQuantity);
        values.put(WorkoutMemoDbHelper.COLUMN_STARTTIME, newStartTime);
        values.put(WorkoutMemoDbHelper.COLUMN_ENDTIME, newEndTime);
        values.put(WorkoutMemoDbHelper.COLUMN_DURATION, newDuration);
        values.put(WorkoutMemoDbHelper.COLUMN_EXTIMES, newExTimes);
        values.put(WorkoutMemoDbHelper.COLUMN_STAR, newStar);
        values.put(WorkoutMemoDbHelper.COLUMN_CHECKED, intValueChecked);
        values.put(WorkoutMemoDbHelper.COLUMN_UPLOAD, newUpload);

        database.update(WorkoutMemoDbHelper.TABLE_WORKOUT_LIST,
                values,
                WorkoutMemoDbHelper.COLUMN_ID + "=" + id,
                null);

        Cursor cursor = database.query(WorkoutMemoDbHelper.TABLE_WORKOUT_LIST,
                columns, WorkoutMemoDbHelper.COLUMN_ID + "=" + id,
                null, null, null, null);

        cursor.moveToFirst();
        WorkoutMemo workoutMemo = cursorToWorkoutMemo(cursor);
        cursor.close();

        return workoutMemo;
    }

    private WorkoutMemo cursorToWorkoutMemo(Cursor cursor) {
        int idIndex = cursor.getColumnIndex(WorkoutMemoDbHelper.COLUMN_ID);
        int idWore = cursor.getColumnIndex(WorkoutMemoDbHelper.COLUMN_WORE);
        int idNumber = cursor.getColumnIndex(WorkoutMemoDbHelper.COLUMN_NUMBER);
        int idName = cursor.getColumnIndex(WorkoutMemoDbHelper.COLUMN_NAME);
        int idType = cursor.getColumnIndex(WorkoutMemoDbHelper.COLUMN_TYPE);
        int idQuantity = cursor.getColumnIndex(WorkoutMemoDbHelper.COLUMN_QUANTITY);
        int idStartTime = cursor.getColumnIndex(WorkoutMemoDbHelper.COLUMN_STARTTIME);
        int idEndTime = cursor.getColumnIndex(WorkoutMemoDbHelper.COLUMN_ENDTIME);
        int idDuration = cursor.getColumnIndex(WorkoutMemoDbHelper.COLUMN_DURATION);
        int idExTimes = cursor.getColumnIndex(WorkoutMemoDbHelper.COLUMN_EXTIMES);
        int idStar = cursor.getColumnIndex(WorkoutMemoDbHelper.COLUMN_STAR);
        int idChecked = cursor.getColumnIndex(WorkoutMemoDbHelper.COLUMN_CHECKED);
        int idUpload = cursor.getColumnIndex(WorkoutMemoDbHelper.COLUMN_UPLOAD);


        long id = cursor.getLong(idIndex);
        int wore = cursor.getInt(idWore);
        int number = cursor.getInt(idNumber);
        String name = cursor.getString(idName);
        int type = cursor.getInt(idType);
        int quantity = cursor.getInt(idQuantity);
        long startTime = cursor.getLong(idStartTime);
        long endTime = cursor.getLong(idEndTime);
        long duration = cursor.getInt(idDuration);
        String exTimes = cursor.getString(idExTimes);
        int intValueStar =  cursor.getInt(idStar);
        int intValueChecked = cursor.getInt(idChecked);
        int intValueUploaded =  cursor.getInt(idUpload);

        boolean isChecked = (intValueChecked != 0);
        boolean isStar = (intValueStar != 0);
        boolean isUploaded = (intValueUploaded != 0);

        WorkoutMemo workoutMemo = new WorkoutMemo(wore, number, name, type, id, isChecked, quantity, startTime, endTime, duration, exTimes, isStar, isUploaded);

        return workoutMemo;
    }

    public List<WorkoutMemo> getAllWorkoutMemos() {
        List<WorkoutMemo> workoutMemoList = new ArrayList<>();

        Cursor cursor = database.query(WorkoutMemoDbHelper.TABLE_WORKOUT_LIST,
                columns,
                null,
                null,
                null, null, null);
/*int quantity=1;
int type=2;
String name="Metis";
        Cursor cursor = database.query(WorkoutMemoDbHelper.TABLE_WORKOUT_LIST, null, WorkoutMemoDbHelper.COLUMN_QUANTITY + "=?" + " AND " + WorkoutMemoDbHelper.COLUMN_NAME + "=?" + " AND " + WorkoutMemoDbHelper.COLUMN_TYPE + "=?", new String[] {String.valueOf(quantity),name,String.valueOf(type)},
                null, null, null);*/
        cursor.moveToFirst();
        WorkoutMemo workoutMemo;

        while(!cursor.isAfterLast()) {
            workoutMemo = cursorToWorkoutMemo(cursor);
            workoutMemoList.add(workoutMemo);
            Log.d(LOG_TAG, "ID: " + workoutMemo.getId() + ", Inhalt: " + workoutMemo.toString());
            cursor.moveToNext();
        }

        cursor.close();

        return workoutMemoList;
    }
    public String getMinDuration(int quantity,String name,int type){

        Cursor c = database.query(WorkoutMemoDbHelper.TABLE_WORKOUT_LIST, new String[] { "min(" + WorkoutMemoDbHelper.COLUMN_DURATION + ")",WorkoutMemoDbHelper.COLUMN_ID }, WorkoutMemoDbHelper.COLUMN_QUANTITY + "=?" + " AND " + WorkoutMemoDbHelper.COLUMN_NAME + "=?" + " AND " + WorkoutMemoDbHelper.COLUMN_TYPE + "=?", new String[] {String.valueOf(quantity),name,String.valueOf(type)},
                null, null, null);
        c.moveToFirst();
        int rowID = c.getInt(1);
        c.close();
        if(rowID!=0) {
            Cursor cursor = database.query(WorkoutMemoDbHelper.TABLE_WORKOUT_LIST,
                    columns, WorkoutMemoDbHelper.COLUMN_ID + "=" +  String.valueOf(rowID),
                    null, null, null, null);
            cursor.moveToFirst();
            WorkoutMemo workoutMemo;
            workoutMemo = cursorToWorkoutMemo(cursor);
            cursor.close();
            return "PB: " + timeFormat((int) workoutMemo.getDuration() / 1000);
        }
        else {
            return "";
        }
    }
    public String getMinDurationGhost(int quantity,String name,int type){

        Cursor c = database.query(WorkoutMemoDbHelper.TABLE_WORKOUT_LIST, new String[] { "min(" + WorkoutMemoDbHelper.COLUMN_DURATION + ")",WorkoutMemoDbHelper.COLUMN_ID }, WorkoutMemoDbHelper.COLUMN_QUANTITY + "=?" + " AND " + WorkoutMemoDbHelper.COLUMN_NAME + "=?" + " AND " + WorkoutMemoDbHelper.COLUMN_TYPE + "=?", new String[] {String.valueOf(quantity),name,String.valueOf(type)},
                null, null, null);
        c.moveToFirst();
        int rowID = c.getInt(1);
        c.close();
        if(rowID!=0) {
            Cursor cursor = database.query(WorkoutMemoDbHelper.TABLE_WORKOUT_LIST,
                    columns, WorkoutMemoDbHelper.COLUMN_ID + "=" +  String.valueOf(rowID),
                    null, null, null, null);
            cursor.moveToFirst();
            WorkoutMemo workoutMemo;
            workoutMemo = cursorToWorkoutMemo(cursor);
            cursor.close();
            return workoutMemo.getExTimes();
        }
        else {
            return "";
        }
    }
    public String getMaxStartTime(int quantity,String name,int type){

        String out="";
        Cursor cc = database.query(WorkoutMemoDbHelper.TABLE_WORKOUT_LIST, new String[] { "min(" + WorkoutMemoDbHelper.COLUMN_DURATION + ")",WorkoutMemoDbHelper.COLUMN_ID }, WorkoutMemoDbHelper.COLUMN_QUANTITY + "=?" + " AND " + WorkoutMemoDbHelper.COLUMN_NAME + "=?" + " AND " + WorkoutMemoDbHelper.COLUMN_TYPE + "=?", new String[] {String.valueOf(quantity),name,String.valueOf(type)},
                null, null, null);
        cc.moveToFirst();
        int rowIDcc = cc.getInt(1);
        cc.close();

        Cursor c = database.query(WorkoutMemoDbHelper.TABLE_WORKOUT_LIST, new String[] { "max(" + WorkoutMemoDbHelper.COLUMN_STARTTIME + ")",WorkoutMemoDbHelper.COLUMN_ID }, WorkoutMemoDbHelper.COLUMN_QUANTITY + "=?" + " AND " + WorkoutMemoDbHelper.COLUMN_NAME + "=?" + " AND " + WorkoutMemoDbHelper.COLUMN_TYPE + "=?", new String[] {String.valueOf(quantity),name,String.valueOf(type)},
                null, null, null);
        c.moveToFirst();
        int rowID = c.getInt(1);
        c.close();

        if(rowID!=0 && rowIDcc!=rowID) {
            Cursor cursor = database.query(WorkoutMemoDbHelper.TABLE_WORKOUT_LIST,
                    columns, WorkoutMemoDbHelper.COLUMN_ID + "=" +  String.valueOf(rowID),
                    null, null, null, null);
            cursor.moveToFirst();
            WorkoutMemo workoutMemo;
            workoutMemo = cursorToWorkoutMemo(cursor);
            cursor.close();
            out="LT: " + timeFormat((int) workoutMemo.getDuration() / 1000);
        }
        return out;
    }
    public String getMaxStartTimeGhost(int quantity,String name,int type){

        String out="";
        Cursor cc = database.query(WorkoutMemoDbHelper.TABLE_WORKOUT_LIST, new String[] { "min(" + WorkoutMemoDbHelper.COLUMN_DURATION + ")",WorkoutMemoDbHelper.COLUMN_ID }, WorkoutMemoDbHelper.COLUMN_QUANTITY + "=?" + " AND " + WorkoutMemoDbHelper.COLUMN_NAME + "=?" + " AND " + WorkoutMemoDbHelper.COLUMN_TYPE + "=?", new String[] {String.valueOf(quantity),name,String.valueOf(type)},
                null, null, null);
        cc.moveToFirst();
        int rowIDcc = cc.getInt(1);
        cc.close();

        Cursor c = database.query(WorkoutMemoDbHelper.TABLE_WORKOUT_LIST, new String[] { "max(" + WorkoutMemoDbHelper.COLUMN_STARTTIME + ")",WorkoutMemoDbHelper.COLUMN_ID }, WorkoutMemoDbHelper.COLUMN_QUANTITY + "=?" + " AND " + WorkoutMemoDbHelper.COLUMN_NAME + "=?" + " AND " + WorkoutMemoDbHelper.COLUMN_TYPE + "=?", new String[] {String.valueOf(quantity),name,String.valueOf(type)},
                null, null, null);
        c.moveToFirst();
        int rowID = c.getInt(1);
        c.close();

        if(rowID!=0 && rowIDcc!=rowID) {
            Cursor cursor = database.query(WorkoutMemoDbHelper.TABLE_WORKOUT_LIST,
                    columns, WorkoutMemoDbHelper.COLUMN_ID + "=" + String.valueOf(rowID),
                    null, null, null, null);
            cursor.moveToFirst();
            WorkoutMemo workoutMemo;
            workoutMemo = cursorToWorkoutMemo(cursor);
            cursor.close();
            out=workoutMemo.getExTimes();
        }
        return out;
    }
}