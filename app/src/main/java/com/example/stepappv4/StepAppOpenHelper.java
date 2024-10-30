package com.example.stepappv4;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;


public class StepAppOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "stepapp";
    public static final String TABLE_NAME = "num_steps";
    public static final String KEY_ID = "id";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_DAY = "day";
    public static final String KEY_HOUR = "hour";
    public static final String CREATE_TABLE_SQL = "CREATE TABLE  " + TABLE_NAME + " (" + KEY_ID + " INTEGER PRIMARY KEY, " +
            KEY_DAY + " TEXT, " + KEY_HOUR + "  TEXT, " + KEY_TIMESTAMP + "  TEXT);";


    public StepAppOpenHelper (Context context)
    {
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

        // Load all records in the database
    public static void loadRecords(Context context){
        List<String> dates = new LinkedList<String>();
        StepAppOpenHelper databaseHelper = new StepAppOpenHelper(context);
        SQLiteDatabase database = databaseHelper.getReadableDatabase();

        String [] columns = new String [] {StepAppOpenHelper.KEY_TIMESTAMP};
        Cursor cursor = database.query(StepAppOpenHelper.TABLE_NAME, columns, null, null, StepAppOpenHelper.KEY_TIMESTAMP,
                null, null );

        // iterate over returned elements
        cursor.moveToFirst();
        for (int index=0; index < cursor.getCount(); index++){
            dates.add(cursor.getString(0));
            cursor.moveToNext();
        }
        database.close();

        Log.d("STORED TIMESTAMPS: ", String.valueOf(dates));
    }

    // load records from a single day
    public static Integer loadSingleRecord(Context context, String date){
        List<String> steps = new LinkedList<String>();
        // Get the readable database
        StepAppOpenHelper databaseHelper = new StepAppOpenHelper(context);
        SQLiteDatabase database = databaseHelper.getReadableDatabase();

        String where = StepAppOpenHelper.KEY_DAY + " = ?";
        String [] whereArgs = { date };

        Cursor cursor = database.query(StepAppOpenHelper.TABLE_NAME, null, where, whereArgs, null,
                null, null );

        // iterate over returned elements
        cursor.moveToFirst();
        for (int index=0; index < cursor.getCount(); index++){
            steps.add(cursor.getString(0));
            cursor.moveToNext();
        }
        database.close();

        Integer numSteps = steps.size();
        Log.d("STORED STEPS TODAY: ", String.valueOf(numSteps));
        return numSteps;
    }

    public static void deleteRecords (Context context) {
        StepAppOpenHelper databaseHelper = new StepAppOpenHelper(context);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();

        int numberDeletedRecords = 0;

        numberDeletedRecords = database.delete(StepAppOpenHelper.TABLE_NAME, null, null);
        database.close();

        Toast.makeText(context, "Deleted + "+ String.valueOf(numberDeletedRecords) + " steps", Toast.LENGTH_LONG).show();

    }



    public static Map<Integer, Integer> loadStepsByHour(Context context, String date){
        Map<Integer, Integer>  map = new HashMap<>();

        StepAppOpenHelper databaseHelper = new StepAppOpenHelper(context);
        SQLiteDatabase database = databaseHelper.getReadableDatabase();

        Cursor cursor = database.rawQuery("SELECT hour, COUNT(*)  FROM num_steps " +
                "WHERE day = ? GROUP BY hour ORDER BY  hour ASC ", new String [] {date});

        cursor.moveToFirst();
        for (int index=0; index < cursor.getCount(); index++){
            Integer tmpKey = Integer.parseInt(cursor.getString(0));
            Integer tmpValue = Integer.parseInt(cursor.getString(1));

            map.put(tmpKey, tmpValue);


            cursor.moveToNext();
        }

        cursor.close();
        database.close();

        return map;
    }

    public static Map<String, Integer> loadStepsByDateForLastWeek(Context context) {
        Map<String, Integer> stepsByDateMap = new TreeMap<>();

        StepAppOpenHelper databaseHelper = new StepAppOpenHelper(context);
        SQLiteDatabase database = databaseHelper.getReadableDatabase();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (int i = 0; i < 7; i++) {
            String date = sdf.format(calendar.getTime());

            String query = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE " + KEY_DAY + " = ?";
            Cursor cursor = database.rawQuery(query, new String[]{date});

            if (cursor.moveToFirst()) {
                int stepsCount = cursor.getInt(0);
                stepsByDateMap.put(date, stepsCount);
            }

            calendar.add(Calendar.DAY_OF_MONTH, -1);
            cursor.close();
        }

        database.close();
        return stepsByDateMap;
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        sqLiteDatabase.execSQL(CREATE_TABLE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}