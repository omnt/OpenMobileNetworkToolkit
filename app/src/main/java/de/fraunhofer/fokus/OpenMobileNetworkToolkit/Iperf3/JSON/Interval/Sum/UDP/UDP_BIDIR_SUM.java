/*
 * SPDX-FileCopyrightText:  2025 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Sum.UDP;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Sum.SUM_TYPE;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Sum.Sum;
import org.json.JSONException;
import org.json.JSONObject;

public class UDP_BIDIR_SUM extends UDP_SUM{

    Sum bidirReverse;
    SUM_TYPE sumType;
    public UDP_BIDIR_SUM() {
        super();
        sumType = SUM_TYPE.UDP_BIDIR;
    }
    public void parse(JSONObject data) throws JSONException {
        super.parse(data.getJSONObject("sum"));
        this.bidirReverse.parse(data.getJSONObject("sum_bidir_reverse"));

    }

    public SUM_TYPE getSumType() {
        return sumType;
    }
}
