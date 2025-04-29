/*
 * SPDX-FileCopyrightText:  2025 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences;

public enum SPType {
    logging_sp,
    iperf3_sp,
    ping_sp,
    carrier_sp,
    mobile_network_sp,
    default_sp,
    mqtt_sp;


    public String toString() {
        switch(this){
            case default_sp:
                return "preferences";
            case logging_sp:
            case ping_sp:
            case carrier_sp:
            case mqtt_sp:
            case mobile_network_sp:
                return super.toString();
            default:
                return "";
        }
    }
    public static SPType fromString(String text) {
        for (SPType b : SPType.values()) {
            if (b.toString().equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }

    public String toReadable(){
        String name = this.name().split("_")[0];
        switch(this){
            case logging_sp:
            case ping_sp:
            case carrier_sp:

            case default_sp:
                return name.substring(0,1).toUpperCase() + name.substring(1);
            case mobile_network_sp:
                return "Mobile Network";
            case iperf3_sp:
                return "iPerf3";
            case mqtt_sp:
                return "MQTT";
            default:
                return null;
        }
    }
}
