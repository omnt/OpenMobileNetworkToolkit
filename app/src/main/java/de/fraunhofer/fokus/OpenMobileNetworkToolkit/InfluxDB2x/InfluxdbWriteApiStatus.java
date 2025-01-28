package de.fraunhofer.fokus.OpenMobileNetworkToolkit.InfluxDB2x;

import androidx.annotation.NonNull;

public enum InfluxdbWriteApiStatus {
    Backpressure,
    WriteSuccess,
    WriteErrorEvent,
    WriteRetriableErrorEvent,
    Unknown;
    @NonNull
    public String toString() {
        switch(this) {
            case Backpressure: return "Backpressure";
            case WriteSuccess: return "WriteSuccess";
            case WriteErrorEvent: return "WriteErrorEvent";
            case WriteRetriableErrorEvent: return "WriteRetriableErrorEvent";
            case Unknown:;
            default: return "Unknown";
        }
    }
    public static InfluxdbWriteApiStatus fromString(String str) {
        str = str.toLowerCase();
        switch(str) {
            case "backpressure": return Backpressure;
            case "writesuccess": return WriteSuccess;
            case "writeerrorevent": return WriteErrorEvent;
            case "writeeetriableerrorevent": return WriteRetriableErrorEvent;
            case "unknown": return Unknown;
            default: return Unknown;
        }
    }

    public boolean isEquals(InfluxdbWriteApiStatus status) {
        return this==status;
    }
}
