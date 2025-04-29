/*
 * SPDX-FileCopyrightText:  2025 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Sum.TCP;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Sum.SUM_TYPE;
import org.json.JSONException;
import org.json.JSONObject;

public class TCP_UL_SUM extends TCP_SUM{
    public int retransmits;
    public TCP_UL_SUM() {
        super();
        this.setSumType(SUM_TYPE.TCP_UL);
    }
    public void parse(JSONObject data) throws JSONException {
        super.parse(data);
        this.retransmits = data.getInt("retransmits");

    }
}
