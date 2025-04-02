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
    public void parse(){
        String line;
        try {
            while (!isStopped) {
                line = br.readLine();
                if(line == null) {
                    continue;
                }
                JSONObject obj = new JSONObject(line);
                String event = obj.getString("event");
                switch (event) {
                    case "start":
                        start = new Start();
                        JSONObject startData = obj.getJSONObject("data");
                        start.parseStart(startData);
                        support.firePropertyChange("start", null, start);
                        Log.d(TAG, "parse: Start");
                        break;
                    case "interval":
                        Interval interval = new Interval();
                        JSONObject intervalData = obj.getJSONObject("data");
                        interval.parse(intervalData);
                        support.firePropertyChange("interval", null, interval);
                        intervals.addInterval(interval);
                        Log.d(TAG, "parse: Interval");
                        break;
                    case "end":
                        //todo
                        //End end = new End();
                        //JSONObject endData = obj.getJSONObject("data");
                        //end.parseEnd(endData);
                        //support.firePropertyChange("interval", null, end);
                        Log.d(TAG, "parse: End");
                        break;
                    case "error":
                        Error error = new Error();
                        String errorString = obj.getString("data");
                        error.parse(errorString);
                        support.firePropertyChange("error", null, error);
                        Log.d(TAG, "parse: Error");
                        break;
                    default:
                        Log.d(TAG, "parse: Unknown event");
                        break;
                }
            }
            Log.d(TAG, "parse: Done reading file");
        } catch (Exception e) {
            Log.d(TAG, "parse: Error reading file");
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
