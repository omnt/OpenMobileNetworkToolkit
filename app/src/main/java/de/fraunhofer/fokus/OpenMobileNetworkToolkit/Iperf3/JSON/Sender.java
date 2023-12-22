/*
 *  SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Sender {

    @SerializedName("socket")
    @Expose
    public int socket;
    @SerializedName("start")
    @Expose
    public int start;
    @SerializedName("end")
    @Expose
    public float end;
    @SerializedName("seconds")
    @Expose
    public float seconds;
    @SerializedName("bytes")
    @Expose
    public long bytes;
    @SerializedName("bits_per_second")
    @Expose
    public float bitsPerSecond;
    @SerializedName("retransmits")
    @Expose
    public int retransmits;
    @SerializedName("max_snd_cwnd")
    @Expose
    public int maxSndCwnd;
    @SerializedName("max_snd_wnd")
    @Expose
    public int maxSndWnd;
    @SerializedName("max_rtt")
    @Expose
    public int maxRtt;
    @SerializedName("min_rtt")
    @Expose
    public int minRtt;
    @SerializedName("mean_rtt")
    @Expose
    public int meanRtt;
    @SerializedName("sender")
    @Expose
    public boolean sender;

}
