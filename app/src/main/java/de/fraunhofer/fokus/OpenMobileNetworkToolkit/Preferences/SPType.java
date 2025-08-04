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
                return "Default_Settings";
            case logging_sp:
                return "Logging_Settings";
            case ping_sp:
                return "Ping_Settings";
            case carrier_sp:
                return "Carrier_Settings";
            case mqtt_sp:
                return "MQTT_Settings";
            case mobile_network_sp:
                return "Mobile_Network_Settings";
            case iperf3_sp:
                return "iPerf3_Settings";
            default:
                return "";
        }
    }
    public static SPType fromString(String text) {
        switch (text){
            case "Default_Settings":
                return default_sp;
            case "Logging_Settings":
                return logging_sp;
            case "Ping_Settings":
                return ping_sp;
            case "Carrier_Settings":
                return carrier_sp;
            case "MQTT_Settings":
                return mqtt_sp;
            case "Mobile_Network_Settings":
                return mobile_network_sp;
            case "iPerf3 Settings":
                return iperf3_sp;
        }
        return null;
    }


}
