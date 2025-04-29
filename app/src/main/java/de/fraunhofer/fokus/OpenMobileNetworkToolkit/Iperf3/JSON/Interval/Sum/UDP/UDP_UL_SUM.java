/*
 * SPDX-FileCopyrightText:  2025 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Sum.UDP;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Sum.SUM_TYPE;
import org.json.JSONException;
import org.json.JSONObject;

public class UDP_UL_SUM extends UDP_SUM{
    private int packets;
    public UDP_UL_SUM() {
        super();
        this.setSumType(SUM_TYPE.UDP_UL);
    }
    public void parse(JSONObject data) throws JSONException {
        super.parse(data);
        this.packets = data.getInt("packets");
    }
}
