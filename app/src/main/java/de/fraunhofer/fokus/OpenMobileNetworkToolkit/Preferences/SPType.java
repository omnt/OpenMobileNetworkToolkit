/*
 * SPDX-FileCopyrightText:  2025 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences;

public enum SPType {
    LOGGING,
    IPERF3,
    PING,
    CARRIER,
    MOBILE_NETWORK,
    MAIN,
    MQTT;


    public String toString() {
        switch(this){
            case LOGGING:
                return "logging";
            case MQTT:
                return "mqtt";
            case MOBILE_NETWORK:
                return "mobile_network";
            case PING:
                return "ping";
            case IPERF3:
                return "iperf3";
            case CARRIER:
                return "carrier";
            case MAIN:
            default:
                return "main";
        }
    }



}
