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

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                String line;
                    while (!isStopped) {
                        line = br.readLine();
                        Log.d(TAG, "run: Reading line");
                        if(line == null) {
                            Thread.sleep(50);
                            continue;
                        }
                        JSONObject obj = new JSONObject(line);
                        String event = obj.getString("event");
                        switch (event) {
                            case "start":
                                Log.d(TAG, "parse: Start");
                                start = new Start();
                                JSONObject startData = obj.getJSONObject("data");
                                start.parseStart(startData);
                                support.firePropertyChange("start", null, start);
                                break;
                            case "interval":
                                Log.d(TAG, "parse: Interval");
                                Interval interval = new Interval();
                                JSONObject intervalData = obj.getJSONObject("data");
                                interval.parse(intervalData);
                                support.firePropertyChange("interval", null, interval);
                                intervals.addInterval(interval);
                                break;
                            case "end":
                                Log.d(TAG, "parse: End");
                                //todo
                                //End end = new End();
                                //JSONObject endData = obj.getJSONObject("data");
                                //end.parseEnd(endData);
                                //support.firePropertyChange("interval", null, end);
                                isStopped = true;
                                break;
                            case "error":
                                Log.d(TAG, "parse: Error");
                                Error error = new Error();
                                String errorString = obj.getString("data");
                                error.parse(errorString);
                                support.firePropertyChange("error", null, error);
                                isStopped = true;
                                break;
                            default:
                                Log.d(TAG, "parse: Unknown event");
                                break;
                        }
                    }
                    Log.d(TAG, "parse: Done reading file");
            } catch (Exception e) {
                Log.d(TAG, "run: Error parsing file "+e);
            }
        }
    };

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
        runnable.run();
    }

    public Runnable getRunnable(){
        return runnable;
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
