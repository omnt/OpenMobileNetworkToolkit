package de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider;

import android.util.Log;
import org.json.JSONObject;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.BuildConfig;

public class BuildInformation {
    private final String TAG = "BuildInformation";

    public String getBuildType() {
        return BuildConfig.BUILD_TYPE;
    }

    public int getVersionCode() {
        return BuildConfig.VERSION_CODE;
    }

    public String getVersionName() {
        return BuildConfig.VERSION_NAME;
    }

    public String getApplicationId() {
        return BuildConfig.APPLICATION_ID;
    }

    public boolean isDebug() {
        return BuildConfig.DEBUG;
    }

    public JSONObject toJSON(){

        JSONObject json = new JSONObject();
        try {
            json.put("BuildType", getBuildType());
            json.put("VersionCode", getVersionCode());
            json.put("VersionName", getVersionName());
            json.put("ApplicationId", getApplicationId());
            json.put("Debug", isDebug());
        } catch (Exception e) {
            Log.d(TAG,e.toString());
        }
        return json;
    }
}
