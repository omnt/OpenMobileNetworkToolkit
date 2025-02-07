package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Parameter;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public class PingParameter extends Parameter {
    private static final String TAG = "PingParameter";
    private String command;

    public PingParameter(JSONObject command) {
        super(null);
        try {
            this.command = command.getString("command");
        } catch (JSONException e) {
            Log.d(TAG, e.toString());
            Log.w(TAG, "could not create PingParameter!");
        }
    }

    protected PingParameter(Parcel in) {
        super(in);
        command = in.readString();
    }

    public static final Creator<PingParameter> CREATOR = new Creator<PingParameter>() {
        @Override
        public PingParameter createFromParcel(Parcel in) {
            return new PingParameter(in);
        }

        @Override
        public PingParameter[] newArray(int size) {
            return new PingParameter[size];
        }
    };

    @Override
    public int describeContents() {
        return super.describeContents();
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(command);
    }
}
