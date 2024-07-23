/*
 *  SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import androidx.room.ProvidedTypeConverter;
import androidx.room.TypeConverter;

import com.google.gson.Gson;

@ProvidedTypeConverter
public class Iperf3InputConverter {
    @TypeConverter
    public Iperf3Input StringToIperf3Input(String string) {
        return new Gson().fromJson(string, Iperf3Input.class);
    }

    @TypeConverter
    public String Iperf3InputToString(Iperf3Input example) {
        return new Gson().toJson(example);
    }
}
