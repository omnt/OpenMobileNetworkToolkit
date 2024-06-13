package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Ping;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

public class PingParser {
    private static PingParser instance = null;
    private BufferedReader br;
    private ArrayList<PingInformation> lines;

    private PropertyChangeSupport support;
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

    public void parse(){
        String line;
        try {
            while((line = this.br.readLine()) != null){
                PingInformation pi = new PingInformation(line);
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

