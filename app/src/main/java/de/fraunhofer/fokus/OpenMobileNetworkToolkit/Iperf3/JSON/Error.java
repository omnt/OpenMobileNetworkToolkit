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

    public void parse(String data) throws JSONException {
        this.error = data;
    }

    public String toString() {
        return "Error: " + error;
    }

}
