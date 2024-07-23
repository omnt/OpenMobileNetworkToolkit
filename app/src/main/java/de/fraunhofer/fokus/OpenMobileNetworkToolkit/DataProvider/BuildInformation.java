package de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider;

import org.json.JSONObject;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.BuildConfig;

public class BuildInformation {

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
            e.printStackTrace();
        }

        return json;
    }
}
