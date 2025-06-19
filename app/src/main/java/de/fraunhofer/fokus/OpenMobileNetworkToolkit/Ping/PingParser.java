/*
 * SPDX-FileCopyrightText:  2025 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Ping;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Ping.PingInformations.LINEType;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Ping.PingInformations.PacketLossLine;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Ping.PingInformations.PingInformation;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Ping.PingInformations.RTTLine;

public class PingParser {

    private final ArrayList<PingInformation> lines;

    public PingParser() {
        this.lines = new ArrayList<>();
    }


    private LINEType getLineType(String line){
        if (line.contains("bytes from")) {
            return LINEType.RTT;
        } else if (line.contains("Unreachable")) {
            return LINEType.UNREACHABLE;
        } else if (line.contains("Request timeout")) {
            return LINEType.TIMEOUT;
        } else if (line.contains("packets transmitted")){
            return LINEType.PACKET_LOSS;
        } else {
            return LINEType.UNKNOWN;
        }
    }

    public void addLine(String line) {
        PingInformation pi = null;
        LINEType lineType = getLineType(line);
        switch (lineType){
            case RTT:
                pi = new RTTLine(line);
                break;
            case UNREACHABLE:
                //TODO
                break;
            case TIMEOUT:
                //TODO
                break;
            case PACKET_LOSS:
                pi = new PacketLossLine(line);
                break;
            case UNKNOWN:
                break;
        }
        if(pi != null){
            pi.parse();
            pi.setLineType(lineType);
            lines.add(pi);
        }
    }

    public PingInformation getLastPingInformation() {
        if (lines.isEmpty()) {
            return null;
        }
        return lines.get(lines.size() - 1);
    }

}

