package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import android.util.Log;

import org.json.JSONObject;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Error;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Interval;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.start.Start;


public class Iperf3Parser {

    private final String pathToFile;
    private final File file;
    private BufferedReader br = null;
    private PropertyChangeSupport support;
    private Start start;
    private final Intervals intervals = new Intervals();
    private final String TAG = "Iperf3Parser";
    private boolean isStopped = false;



    public Iperf3Parser(String pathToFile) {
        this.pathToFile = pathToFile;
        this.file = new File(this.pathToFile);
        try {
            br = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException ex) {
            System.out.println("File not found");
            return;
        }
        this.support = new PropertyChangeSupport(this);
    }
    public void close() {
        isStopped = true;
    }


    public Intervals getIntervals() {
        return intervals;
    }
    public Start getStart() {
        return start;
    }
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        support.addPropertyChangeListener(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        support.removePropertyChangeListener(pcl);
    }
}
