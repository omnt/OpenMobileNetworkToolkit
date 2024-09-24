package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Streams.TCP;

import android.annotation.SuppressLint;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Streams.STREAM_TYPE;

import org.json.JSONException;
import org.json.JSONObject;

public class TCP_DL_STREAM extends TCP_STREAM {
    public TCP_DL_STREAM(){
        super();
    }

    public void parse(JSONObject data) throws JSONException {
        super.parse(data);
        this.setStreamType(STREAM_TYPE.TCP_DL);
    }
}
