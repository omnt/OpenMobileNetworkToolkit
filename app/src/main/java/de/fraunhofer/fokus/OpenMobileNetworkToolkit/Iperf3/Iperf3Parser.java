package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Error;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Interval;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.start.Start;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Observable;
import java.util.Observer;
import org.json.JSONObject;


public class Iperf3Parser {

    private String pathToFile;
    private File file;
    private BufferedReader br = null;
    private PropertyChangeSupport support;
    private Start start;
    private Intervals intervals = new Intervals();
    Iperf3Parser(String pathToFile) {
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

    public void parse(){
        String line;
        try {
            while ((line = br.readLine()) != null) {
                JSONObject obj = new JSONObject(line);
                String event = obj.getString("event");
                switch (event) {
                    case "start":
                        start = new Start();
                        JSONObject startData = obj.getJSONObject("data");
                        start.parseStart(startData);
                        break;
                    case "interval":
                        Interval interval = new Interval();
                        JSONObject intervalData = obj.getJSONObject("data");
                        interval.parse(intervalData);
                        support.firePropertyChange("interval", null, interval);
                        intervals.addInterval(interval);
                        break;
                    case "end":
                        System.out.println("End");
                        break;
                    case "error":
                        Error error = new Error();
                        String errorString = obj.getString("data");
                        error.parse(errorString);
                        support.firePropertyChange("error", null, error);
                        break;
                    default:
                        System.out.println("Unknown event");
                        break;
                }
            }
        } catch (Exception e) {
            System.out.println("Error reading file");
        }
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
