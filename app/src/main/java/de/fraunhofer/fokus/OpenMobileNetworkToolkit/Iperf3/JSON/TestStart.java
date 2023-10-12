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

public class TestStart {

    @SerializedName("protocol")
    @Expose
    public String protocol;
    @SerializedName("num_streams")
    @Expose
    public Integer numStreams;
    @SerializedName("blksize")
    @Expose
    public Integer blksize;
    @SerializedName("omit")
    @Expose
    public Integer omit;
    @SerializedName("duration")
    @Expose
    public Integer duration;
    @SerializedName("bytes")
    @Expose
    public Integer bytes;
    @SerializedName("blocks")
    @Expose
    public Integer blocks;
    @SerializedName("reverse")
    @Expose
    public Integer reverse;
    @SerializedName("tos")
    @Expose
    public Integer tos;
    @SerializedName("target_bitrate")
    @Expose
    public Integer targetBitrate;

}
