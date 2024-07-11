package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class SharedPreferencesIO {

    private static final String TAG = "SharedPreferencesIO";

    public static String exportPreferences(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        JSONObject preferencesJson = new JSONObject(prefs.getAll());
        try {
            return preferencesJson.toString(4);
        } catch (JSONException e) {
            return "{}";
        }
    }

    public static void importPreferences(Context context, String jsonString) {
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = prefs.edit();
            JSONObject spJSON = new JSONObject(jsonString);
            for (Iterator<String> it = spJSON.keys(); it.hasNext(); ) {
                String key = it.next();
                Object value = spJSON.get(key);
                if (value instanceof Boolean) {
                    editor.putBoolean(key, (Boolean) value);
                } else if (value instanceof Float) {
                    editor.putFloat(key, (Float) value);
                } else if (value instanceof Integer) {
                    editor.putInt(key, (Integer) value);
                } else if (value instanceof Long) {
                    editor.putLong(key, (Long) value);
                } else if (value instanceof String) {
                    editor.putString(key, (String) value);
                }
            }
            editor.apply();
            Log.d(TAG, "Imported: " + jsonString);
            Toast.makeText(context, "Preferences imported", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Failed to import preferences", e);
            Toast.makeText(context, "Failed to import preferences", Toast.LENGTH_SHORT).show();
        }
    }
}
