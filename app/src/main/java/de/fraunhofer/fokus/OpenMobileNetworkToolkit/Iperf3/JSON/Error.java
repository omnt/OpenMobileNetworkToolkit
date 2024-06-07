package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON;

import org.json.JSONException;
import org.json.JSONObject;

public class Error {
    private String error;

    public Error() {
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public void parse(JSONObject data) throws JSONException {
        this.error = data.getString("error");
    }

}
