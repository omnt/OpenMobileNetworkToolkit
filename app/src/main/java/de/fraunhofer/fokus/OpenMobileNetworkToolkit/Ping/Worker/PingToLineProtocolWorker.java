/*
 *  SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Ping.Worker;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.gson.Gson;
import com.influxdb.client.write.Point;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.DeviceInformation;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.GlobalVars;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.InfluxDB2x.InfluxdbConnection;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Inputs.PingInput;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Parameter.PingParameter;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Ping.PingInformations.LINEType;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Ping.PingInformations.PacketLossLine;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Ping.PingInformations.PingInformation;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Ping.PingInformations.RTTLine;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SharedPreferencesGrouper;

public class PingToLineProtocolWorker extends Worker {
    public static final String TAG = "PingToLineProtocolWorker";
    InfluxdbConnection influx;
    private SharedPreferencesGrouper spg;

    private final DeviceInformation di = GlobalVars.getInstance().get_dp().getDeviceInformation();
    private PingInput pingInput;
    public PingToLineProtocolWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        Gson gson = new Gson();
        String iperf3InputString = getInputData().getString(PingInput.INPUT);
        pingInput = gson.fromJson(iperf3InputString, PingInput.class);
        spg = SharedPreferencesGrouper.getInstance(getApplicationContext());

        File lineProtocolDirPath = new File(PingParameter.lineProtocolDirPath);
        if(!lineProtocolDirPath.exists()){
            if(!lineProtocolDirPath.mkdirs()){
                Log.e(TAG, "Error creating lineProtocolDirPath directory: " + PingParameter.lineProtocolDirPath);
            }
        }
    }


    private LINEType getLineType(String line){
        if (line.contains("bytes from")) {
            return LINEType.RTT;
        } else if (line.contains("Unreachable")) {
            return LINEType.UNREACHABLE;
        } else if (line.contains("Request timeout")) {
            return LINEType.TIMEOUT;
        } else if (line.contains("packets transmitted")){
            return LINEType.PACKET_LOSS;
        } else {
            return LINEType.UNKNOWN;
        }
    }

    @NonNull
    @Override
    public Result doWork() {
        Data.Builder output = new Data.Builder().putBoolean("pingUpload", false);
        File myObj = new File(pingInput.getPingParameter().getLogfile());
        Scanner scanner = null;
        try {
            scanner = new Scanner(myObj);
        } catch (FileNotFoundException e) {
            return Result.failure(output.putString("error", "File not found").build());
        }
        ArrayList<PingInformation> pingInformations = new ArrayList<>();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            PingInformation pi = null;
            switch (getLineType(line)) {
                case RTT:
                    pi = new RTTLine(line);
                case UNREACHABLE:
                    //TDODO
                    break;
                case TIMEOUT:
                    //TODO
                    break;
                case PACKET_LOSS:
                    pi = new PacketLossLine(line);
                    break;
                case UNKNOWN:
                    break;
            }
            if (pi == null) continue;
            pi.parse();
            pingInformations.add(pi);
        }
        scanner.close();
        File lineprotocolfile = new File(pingInput.getPingParameter().getLineProtocolFile());
        if(lineprotocolfile.exists()){
            lineprotocolfile.delete();
            try {
                lineprotocolfile.createNewFile();
            } catch (IOException e) {
                Log.e(TAG, "doWork: could not create LP File!", e);
                return Result.failure(output.putString("error", "LP-File not created").build());
            }
        }
        FileOutputStream pingStream = null;
        try {
            pingStream = new FileOutputStream(pingInput.getPingParameter().getLineProtocolFile(), true);
        } catch (FileNotFoundException e) {
            Log.d(TAG, "doWork: " + e.toString());
            Log.e(TAG, "doWork: Could not create FileOutputStream");
        }

        for (PingInformation pi : pingInformations) {
            try {
                Point point = pi.getPoint();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    point.addTags(GlobalVars.getInstance().get_dp().getTagsMap());
                }
                pingStream.write((point.toLineProtocol() + "\n").getBytes());

            } catch (IOException e) {
                Log.d(TAG, "doWork: "+e.toString());
                return Result.failure(output.putString("error", "File not written").build());
            }
        }
        return Result.success(output.build());
    }

}
