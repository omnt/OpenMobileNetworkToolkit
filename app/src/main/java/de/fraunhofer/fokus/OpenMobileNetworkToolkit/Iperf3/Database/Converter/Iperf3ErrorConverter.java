/*
 *  SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Database.Converter;

import androidx.room.ProvidedTypeConverter;
import androidx.room.TypeConverter;

import com.google.gson.Gson;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Error;


@ProvidedTypeConverter
public class Iperf3ErrorConverter {
    @TypeConverter
    public Error fromJSONString(String string) {
        return new Gson().fromJson(string, Error.class);
    }

    @TypeConverter
    public String toJSONString(Error example) {
        return new Gson().toJson(example);
    }
}
