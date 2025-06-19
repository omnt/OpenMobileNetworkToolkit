/*
 * SPDX-FileCopyrightText:  2025 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Parameter;

import android.net.Network;
import android.os.Parcel;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class PingParameter extends Parameter {
    public static final String rawDirPath = rootPath+"/omnt/ping/raw/";

    public static final String lineProtocolDirPath = rootPath+"/omnt/ping/lineprotocol/";
    private static final String TAG = "PingParameter";

    public static final String PING = "ping";
    public static final String DESTINATION = "destination";
    public static final String COUNT = "count";
    public static final String TIMEOUT = "timeout";   // in seconds
    public static final String PACKET_SIZE = "packetSize";
    public static final String INTERVAL = "interval"; // in seconds

    public String getDestination() {
        return destination;
    }

    public int getCount() {
        return count;
    }

    public int getTimeoutMillis() {
        return timeoutMillis;
    }

    public int getPacketSize() {
        return packetSize;
    }

    public long getIntervalMillis() {
        return intervalMillis;
    }


    public Network getNetwork() {
        return network;
    }

    private String destination;
    private int count;
    private int timeoutMillis;
    private int packetSize;
    private long intervalMillis;
    private Network network;
    private int deadline;
    private String testUUID;


    public String[] getInputAsCommand() {
        ArrayList<String> command = new ArrayList<>();
        command.add("/system/bin/ping");
        if (count > 0) {
            command.add("-c");
            command.add(String.valueOf(count));
        }
        if (timeoutMillis > 0) {
            command.add("-W");
            command.add(String.valueOf(timeoutMillis));
        }
        if (packetSize > 0) {
            command.add("-s");
            command.add(String.valueOf(packetSize));
        }
        if (intervalMillis > 0) {
            command.add("-i");
            command.add(String.valueOf(intervalMillis));
        }
        if (deadline > 0) {
            command.add("-w");
            command.add(String.valueOf(deadline));
        }
        command.add("-D");
        command.add(destination);

        return command.toArray(new String[0]);
    }
    private void setupDirs(){

        try {
            Files.createDirectories(Paths.get(rawDirPath));
            Files.createDirectories(Paths.get(lineProtocolDirPath));
        } catch (IOException e) {
            Log.d(TAG, "Could not create directories.");
        }

    }
    public PingParameter(String stringParameter, String  testUUID) {
        super(rawDirPath + testUUID + ".txt", lineProtocolDirPath + testUUID + ".txt");
        this.testUUID = testUUID;
        String[] parts = stringParameter.split(" ");
        for (int i = 0; i < parts.length; i++) {
            switch (parts[i]) {
                case "-c":
                    count = Integer.parseInt(parts[i + 1]);
                    break;
                case "-W":
                    timeoutMillis = Integer.parseInt(parts[i + 1]);
                    break;
                case "-s":
                    packetSize = Integer.parseInt(parts[i + 1]);
                    break;
                case "-i":
                    intervalMillis = Long.parseLong(parts[i + 1]);
                    break;
                case "-w":
                    deadline = Integer.parseInt(parts[i + 1]);
                    break;
                default:
                    destination = parts[i];
            }
        }
    }

    public PingParameter(JSONObject parameter, String  testUUID) {
        super(rawDirPath + testUUID + ".txt", lineProtocolDirPath + testUUID + ".txt");
        this.testUUID = testUUID;
        try {
            destination = parameter.getString(DESTINATION);
        } catch (JSONException e) {
            Log.d(TAG, e.toString());
            Log.w(TAG, "could not create PingParameter!");
            throw new IllegalArgumentException("Destination is missing");
        }
        try {
            count = parameter.getInt(COUNT);
        } catch (JSONException e) {
            Log.d(TAG, e.toString());
            Log.i(TAG, "No count set.");
        }
        try {
            timeoutMillis = parameter.getInt(TIMEOUT);
        } catch (JSONException e) {
            Log.d(TAG, e.toString());
            Log.i(TAG, "no timeout set.");
        }
        try {
            packetSize = parameter.getInt(PACKET_SIZE);
        } catch (JSONException e) {
            Log.d(TAG, e.toString());
            Log.i(TAG, "no packet size set.");
        }
        try {
            intervalMillis = parameter.getLong(INTERVAL);
        } catch (JSONException e) {
            Log.d(TAG, e.toString());
            Log.i(TAG, "no interval set.");
        }
        try {
            deadline = parameter.getInt("deadline");
        } catch (JSONException e) {
            Log.d(TAG, e.toString());
            Log.i(TAG, "no deadline set.");
        }
        setupDirs();
    }

    protected PingParameter(Parcel in) {
        super(in);
        destination = in.readString();
        count = in.readInt();
        timeoutMillis = in.readInt();
        packetSize = in.readInt();
        intervalMillis = in.readLong();
        network = in.readParcelable(Network.class.getClassLoader());
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
        super.writeToParcel(dest, flags);
        dest.writeString(destination);
        dest.writeInt(count);
        dest.writeInt(timeoutMillis);
        dest.writeInt(packetSize);
        dest.writeLong(intervalMillis);
        dest.writeParcelable(network, flags);
    }

}
