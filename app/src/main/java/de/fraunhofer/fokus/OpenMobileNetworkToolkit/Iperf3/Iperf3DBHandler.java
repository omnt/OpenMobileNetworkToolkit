package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

public class Iperf3DBHandler extends SQLiteOpenHelper {

    private static final String TAG = "Iperf3DBHandler";
    private static final String DB_NAME = "Iperf3Worker";
    private static final int DB_VERSION = 1;
    private static final String TABLE_NAME = "Iperf3Worker";
    private static final String ID_COL = "id";
    private static final String IPERF3_RUNNER_B = "Iperf3AsBLOB";
    private static Iperf3DBHandler sInstance;
    public static synchronized Iperf3DBHandler getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new Iperf3DBHandler(context);
        }
        return sInstance;
    }

    private Iperf3DBHandler(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME + " ("
                + ID_COL + " VARCHAR(255) PRIMARY KEY, "
                + IPERF3_RUNNER_B + " BLOB)";

        // at last we are calling a exec sql
        // method to execute above sql query
        db.execSQL(query);
    }

    public void addNewRunner(String ID, byte[] bytes){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(ID_COL, ID);
        values.put(IPERF3_RUNNER_B, bytes);

        db.execSQL("DELETE FROM " + TABLE_NAME+ " WHERE "+ID_COL+"='"+ID+"'");
        db.insert(TABLE_NAME, null, values);
    }


    public byte[] getRunnerByID(String ID){
        SQLiteDatabase db = this.getWritableDatabase();

        String query = "SELECT " + "*" + " "
                        +"FROM " + TABLE_NAME + " "
                        +"WHERE "+ ID_COL+"='"+ID+"'";

        byte[] bytes = null;
        Cursor cursor = db.rawQuery(query, null);
        try {
            if (cursor.moveToFirst()){
                bytes = cursor.getBlob(cursor.getColumnIndexOrThrow(IPERF3_RUNNER_B));
            }
        } catch (Exception e){
            Log.d(TAG, "getRunnerByID: Error while trying to get Runner from DB");
            e.printStackTrace();
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return bytes;
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_NAME);
        onCreate(db);
    }
}
