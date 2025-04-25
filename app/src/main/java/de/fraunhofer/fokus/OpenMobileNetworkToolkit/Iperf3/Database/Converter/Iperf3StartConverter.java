/*
 *  SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Database.Converter;

import android.util.Log;

import androidx.room.ProvidedTypeConverter;
import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Inputs.Iperf3Input;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.start.Start;


@ProvidedTypeConverter
public class Iperf3StartConverter {
    private final String TAG = "Iperf3StartConverter";
    @TypeConverter
    public Start StringToIperf3Start(String string) {
        Start start = new Start();
        try {
            start = new Gson().fromJson(string, Start.class);
        } catch (JsonSyntaxException e){
            Log.d(TAG, "StringToIperf3Start: "+e);
        }
        return start;
    }

    @TypeConverter
    public String Iperf3StartToString(Start example) {
        return new Gson().toJson(example);
    }
}
