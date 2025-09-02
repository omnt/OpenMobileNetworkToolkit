/*
 * SPDX-FileCopyrightText:  2025 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.BuildInformation;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class SharedPreferencesIO {

    private static final String TAG = "SharedPreferencesIO";

    public static String exportPreferences(Context context) {
        JSONArray jsonArray = null;
        try (InputStream is = context.getResources().openRawResource(R.raw.config);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String json = reader.lines().collect(Collectors.joining());
            jsonArray = new JSONArray(json);

        } catch (Exception e) {
            Log.e(TAG, "Failed to read or parse JSON array from config file", e);
            return "";
        }

        SharedPreferencesGrouper spg = SharedPreferencesGrouper.getInstance(context);
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject settingObject = jsonArray.getJSONObject(i);
                if(settingObject.has("metadata")) continue;
                String settingName = settingObject.getString("setting");
                JSONArray categoriesArray = settingObject.getJSONArray("categories");
                SPType spType = SPType.valueOf(settingName.toUpperCase());
                if(spType == null) {
                    Log.w(TAG, "Unknown SPType: " + settingName);
                    continue;
                }
                for (int j = 0; j < categoriesArray.length(); j++) {
                    JSONObject categorieObject = categoriesArray.getJSONObject(j);
                    JSONArray preferenceArray = categorieObject.getJSONArray("preferences");
                    for (int k = 0; k < preferenceArray.length(); k++) {
                        JSONObject preferenceObject = preferenceArray.getJSONObject(k);
                        getValue(preferenceObject, spg.getSharedPreference(spType));
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to import preferences", e);
            Toast.makeText(context, "Failed to import preferences", Toast.LENGTH_SHORT).show();
        }

        return jsonArray.toString();

    }

    private static void getValue(JSONObject jsonObject, SharedPreferences sp){
        try{
            String type = jsonObject.getString("type");
            String key = jsonObject.getString("key");
            switch (type){
                case "string":
                    jsonObject.put("value", sp.getString(key, ""));
                    break;
                case "boolean":
                    jsonObject.put("value", sp.getBoolean(key, false));
                    break;
                case "set":
                    JSONArray jsonArray = new JSONArray();
                    Set<String> stringSet = sp.getStringSet(key, new HashSet<>());
                    for (String string: stringSet) {
                        jsonArray.put(string);
                    }
                    jsonObject.put("value", jsonArray);
                default:
                    Log.e(TAG, "getValue: could not get value of type: "+type);
                    break;
            }
        } catch (JSONException e) {
            Log.e(TAG, "getValue: could not get data from JSON object");;
        }
    }
    private static void putValue(JSONObject jsonObject, SharedPreferences sp){
        try{
            String type = jsonObject.getString("type");
            String key = jsonObject.getString("key");
            switch (type){
                case "string":
                    sp.edit().putString(key, jsonObject.getString("value")).apply();
                    break;
                case "boolean":
                    sp.edit().putBoolean(key, jsonObject.getBoolean("value")).apply();
                    break;
                case "set":
                    Set<String> stringSet = new HashSet<>();
                    JSONArray jsonArray = jsonObject.getJSONArray("value");
                    if(jsonArray == null || jsonArray.length() == 0){
                        break;
                    }
                    for (int i = 0; i < jsonArray.length(); i++) {
                        stringSet.add(jsonArray.getString(i));
                    }
                    sp.edit().putStringSet(key, stringSet);
                default:
                    Log.e(TAG, "putValue: could not put value of type: "+type);
                    break;
            }
        } catch (JSONException e) {
            Log.e(TAG, "putValue: could not get data from JSON object");;
        }
    }
    public static void importPreferences(Context context, String jsonString) {
        SharedPreferencesGrouper spg = SharedPreferencesGrouper.getInstance(context);
        try {
            JSONArray spJSON = new JSONArray(jsonString);
            for (int i = 0; i < spJSON.length(); i++) {
                JSONObject settingObject = spJSON.getJSONObject(i);
                if(settingObject.has("metadata")) continue;
                String settingName = settingObject.getString("setting");
                JSONArray categoriesArray = settingObject.getJSONArray("categories");
                SPType spType = SPType.valueOf(settingName.toUpperCase());
                if(spType == null) {
                    Log.w(TAG, "Unknown SPType: " + settingName);
                    continue;
                }
                for (int j = 0; j < categoriesArray.length(); j++) {
                    JSONObject categorieObject = categoriesArray.getJSONObject(j);
                    JSONArray preferenceArray = categorieObject.getJSONArray("preferences");
                    for (int k = 0; k < preferenceArray.length(); k++) {
                        JSONObject preferenceObject = preferenceArray.getJSONObject(k);
                        putValue(preferenceObject, spg.getSharedPreference(spType));
                    }

                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to import preferences", e);
            Toast.makeText(context, "Failed to import preferences", Toast.LENGTH_SHORT).show();
        }
    }


}
