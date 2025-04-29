/*
 * SPDX-FileCopyrightText:  2025 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.start;

import org.json.JSONException;
import org.json.JSONObject;

public class Timestamp{
    private String time;
    private int timesecs;

    public void parse(JSONObject data) throws JSONException {
        this.time = data.getString("time");
        this.timesecs = data.getInt("timesecs");
    }
    public String getTime() {
        return time;
    }
    public int getTimesecs() {
        return timesecs;
    }
}
