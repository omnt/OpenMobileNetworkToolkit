/*
 * SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 * SPDX-License-Identifier: apache2
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Sum {

    @SerializedName("start")
    @Expose
    public Float start;
    @SerializedName("end")
    @Expose
    public Float end;
    @SerializedName("seconds")
    @Expose
    public Float seconds;
    @SerializedName("bytes")
    @Expose
    public Double bytes;
    @SerializedName("bits_per_second")
    @Expose
    public Float bitsPerSecond;
    @SerializedName("retransmits")
    @Expose
    public Integer retransmits;
    @SerializedName("omitted")
    @Expose
    public Boolean omitted;
    @SerializedName("sender")
    @Expose
    public Boolean sender;

    @SerializedName("packets")
    @Expose
    public Integer packets;
    @SerializedName("jitter_ms")
    @Expose
    public Float jitterMs;
    @SerializedName("lost_packets")
    @Expose
    public Integer lostPackets;
    @SerializedName("lost_percent")
    @Expose
    public Float lostPercent;

}
