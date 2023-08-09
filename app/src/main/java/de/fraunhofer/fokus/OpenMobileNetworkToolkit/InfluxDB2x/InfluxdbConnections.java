package de.fraunhofer.fokus.OpenMobileNetworkToolkit.InfluxDB2x;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.preference.PreferenceManager;

public class InfluxdbConnections {
    private static final String TAG = "InfluxdbConnections";
    private static InfluxdbConnection ric;
    private static InfluxdbConnection lic;

    private InfluxdbConnections() {
    }


    public static InfluxdbConnection getRicInstance(Context context) {
        if (ric == null) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            String url = sp.getString("influx_URL", "");
            String org = sp.getString("influx_org", "");
            String bucket = sp.getString("influx_bucket", "");
            String token = sp.getString("influx_token", "");
            if (url.isEmpty() || org.isEmpty() || bucket.isEmpty() || token.isEmpty()) {
                Log.e(TAG, "Influx parameters incomplete, can't setup logging");
                return null;
            }

            ric = new InfluxdbConnection(url, token, org, bucket, context);
        }
        return ric;
    }

    //todo Remote setting are currently hardcoded and should be generated
    public static InfluxdbConnection getLicInstance(Context context) {
        if (lic == null) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            String url = "http://127.0.0.1:8086";
            String org = "omnt";
            String bucket = "omnt";
            String token = "1234567890";
            if (url.isEmpty() || org.isEmpty() || bucket.isEmpty() || token.isEmpty()) {
                Log.e(TAG, "Influx parameters incomplete, can't setup logging");
                return null;
            }
            lic = new InfluxdbConnection(url, token, org, bucket, context);
        }
        return lic;
    }
}
