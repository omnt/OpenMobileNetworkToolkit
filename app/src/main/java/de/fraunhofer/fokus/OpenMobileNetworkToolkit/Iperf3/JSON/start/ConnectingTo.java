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

public class ConnectingTo{
    private String host;
    private int port;

    public void parse(JSONObject data) throws JSONException {
        this.host = data.getString("host");
        this.port = data.getInt("port");
    }

    public String getHost() {
        return host;
    }
    public int getPort() {
        return port;
    }
}
