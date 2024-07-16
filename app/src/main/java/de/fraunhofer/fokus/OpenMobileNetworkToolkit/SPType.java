package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

public enum SPType {
    logging_sp,
    iperf3_sp,
    ping_sp,
    carrier_sp,
    mobile_network_sp,
    default_sp;

    public static SPType fromString(String text) {
        for (SPType b : SPType.values()) {
            if (b.toString().equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }
}
