/*
 * SPDX-FileCopyrightText: 2021 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2021 Fraunhofer FOKUS
 *
 * SPDX-License-Identifier: apache2
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.influxdb.client.write.Point;


@Entity(tableName = "influx_point")
public class InfluxPointEntry {
    @PrimaryKey
    public long timeStamp;
    public Point point;

    public InfluxPointEntry(long timeStamp, Point point) {
        this.timeStamp = timeStamp;
        this.point = point;
    }
}

