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
    private static PingParser instance = null;
    private BufferedReader br;
    private final ArrayList<PingInformation> lines;

    private final PropertyChangeSupport support;
    private PropertyChangeListener listener;
    private PingParser(BufferedReader br) {
        this.br = br;
        this.lines = new ArrayList<>();
        support = new PropertyChangeSupport(this);
    }

    public static PingParser getInstance(BufferedReader br){
        if (instance == null) instance = new PingParser(br);
        if(br != null); instance.setBr(br);
        return instance;
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
    public void parse(){
        String line;
        try {
            while((line = this.br.readLine()) != null){
                PingInformation pi = null;
                switch (getLineType(line)){
                    case RTT:
                        pi = new RTTLine(line);
                    case UNREACHABLE:
                        //TDODO
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
                if(pi == null) continue;
                pi.parse();
                this.lines.add(pi);
                support.firePropertyChange("ping", null, pi);
            }
        } catch (IOException e){

        }
    }

    public void addPropertyChangeListener(PropertyChangeListener listener){
        support.addPropertyChangeListener(listener);
    }
    public void setListener(PropertyChangeListener listener){
        this.listener = listener;
    }
    public PropertyChangeListener getListener(){
        return this.listener;
    }
    public void removePropertyChangeListener(PropertyChangeListener listener){
        support.removePropertyChangeListener(listener);
    }

    public void setBr(BufferedReader br){
        this.br = br;
    }


}

