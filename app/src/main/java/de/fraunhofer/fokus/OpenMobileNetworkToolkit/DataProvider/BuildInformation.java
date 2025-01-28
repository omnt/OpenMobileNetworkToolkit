package de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider;

import android.util.Log;

import com.influxdb.client.write.Point;

import org.json.JSONObject;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.BuildConfig;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.GlobalVars;

public class BuildInformation extends Information {
    private final String TAG = "BuildInformation";

    public BuildInformation() {
        super();
    }

    public BuildInformation(long timeStamp) {
        super(timeStamp);
    }

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

    public String getGitHash() {
        return GlobalVars.getInstance().getGit_hash();
    }

    public JSONObject toJSON() {

        JSONObject json = new JSONObject();
        try {
            json.put("BuildType", getBuildType());
            json.put("VersionCode", getVersionCode());
            json.put("VersionName", getVersionName());
            json.put("ApplicationId", getApplicationId());
            json.put("Debug", isDebug());
            json.put("GitHash", getGitHash());
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
        return json;
    }

    public Point getPoint(Point point) {
        if (point == null) {
            Log.e(TAG, "getPoint: given point == null!");
            point = Point.measurement("BuildInformation");
        }
        point.addField("BuildType", getBuildType());
        point.addField("VersionCode", getVersionCode());
        point.addField("VersionName", getVersionName());
        point.addField("ApplicationID", getApplicationId());
        point.addField("Debug", isDebug());
        point.addField("GitHash", getGitHash());
        return point;
    }

}
