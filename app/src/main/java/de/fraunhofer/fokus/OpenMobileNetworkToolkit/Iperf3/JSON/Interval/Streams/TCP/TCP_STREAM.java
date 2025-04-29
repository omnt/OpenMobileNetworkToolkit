/*
 * SPDX-FileCopyrightText:  2025 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Streams.TCP;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Streams.Stream;
import org.json.JSONException;
import org.json.JSONObject;

public class TCP_STREAM extends Stream {
    public TCP_STREAM(){
        super();
    }
    public void parse(JSONObject data) throws JSONException {
        super.parse(data);
    }
}
