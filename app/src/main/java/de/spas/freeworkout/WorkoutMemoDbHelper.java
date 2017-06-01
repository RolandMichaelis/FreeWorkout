package de.spas.freeworkout;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class WorkoutMemoDbHelper extends SQLiteOpenHelper{

    private static final String LOG_TAG = WorkoutMemoDbHelper.class.getSimpleName();

    public static final String DB_NAME = "workout_list.db";
    public static final int DB_VERSION = 6;
    public static final String TABLE_WORKOUT_LIST = "workout_list";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_WORE = "wore";
    public static final String COLUMN_NUMBER = "number";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_QUANTITY = "quantity";
    public static final String COLUMN_STARTTIME = "startTime";
    public static final String COLUMN_ENDTIME = "endTime";
    public static final String COLUMN_DURATION = "duration";
    public static final String COLUMN_EXTIMES = "exTimes";
    public static final String COLUMN_STAR = "star";
    public static final String COLUMN_CHECKED = "checked";
    public static final String COLUMN_UPLOAD = "upload";


    public static final String SQL_CREATE =
            "CREATE TABLE " + TABLE_WORKOUT_LIST +
                    "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_WORE + " INTEGER NOT NULL, " +
                    COLUMN_NUMBER + " INTEGER NOT NULL, " +
                    COLUMN_NAME + " TEXT NOT NULL, " +
                    COLUMN_TYPE + " INTEGER NOT NULL, " +
                    COLUMN_QUANTITY + " INTEGER NOT NULL, " +
                    COLUMN_STARTTIME + " DATE NOT NULL, " +
                    COLUMN_ENDTIME + " DATE NOT NULL, " +
                    COLUMN_DURATION + " INTEGER NOT NULL, " +
                    COLUMN_EXTIMES + " TEXT NOT NULL, " +
                    COLUMN_STAR + " BOOLEAN NOT NULL DEFAULT 0, " +
                    COLUMN_CHECKED + " BOOLEAN NOT NULL DEFAULT 0, " +
                    COLUMN_UPLOAD + " BOOLEAN NOT NULL DEFAULT 0);";

    public static final String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_WORKOUT_LIST;


    public WorkoutMemoDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        Log.d(LOG_TAG, "DbHelper hat die Datenbank: " + getDatabaseName() + " erzeugt.");
    }

    // Die onCreate-Methode wird nur aufgerufen, falls die Datenbank noch nicht existiert
    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            Log.d(LOG_TAG, "Die Tabelle wird mit SQL-Befehl: " + SQL_CREATE + " angelegt.");
            db.execSQL(SQL_CREATE);
        }
        catch (Exception ex) {
            Log.e(LOG_TAG, "Fehler beim Anlegen der Tabelle: " + ex.getMessage());
        }
    }

    // Die onUpgrade-Methode wird aufgerufen, sobald die neue Versionsnummer höher
    // als die alte Versionsnummer ist und somit ein Upgrade notwendig wird
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(LOG_TAG, "Die Tabelle mit Versionsnummer " + oldVersion + " wird entfernt.");
        db.execSQL(SQL_DROP);
        onCreate(db);
    }
}