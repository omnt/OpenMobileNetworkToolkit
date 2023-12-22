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

public class TestStart {

    @SerializedName("protocol")
    @Expose
    public String protocol;
    @SerializedName("num_streams")
    @Expose
    public int numStreams;
    @SerializedName("blksize")
    @Expose
    public int blksize;
    @SerializedName("omit")
    @Expose
    public int omit;
    @SerializedName("duration")
    @Expose
    public int duration;
    @SerializedName("bytes")
    @Expose
    public int bytes;
    @SerializedName("blocks")
    @Expose
    public int blocks;
    @SerializedName("reverse")
    @Expose
    public int reverse;
    @SerializedName("tos")
    @Expose
    public int tos;
    @SerializedName("target_bitrate")
    @Expose
    public int targetBitrate;

}
