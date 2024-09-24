package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Database.Converter.IntervalsConverter;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Database.Iperf3RunResult;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Error;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Interval;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.start.Start;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Database.Iperf3ResultsDataBase;


public class Iperf3Parser {
    private static final String TAG = "Iperf3Parser";
    private final String pathToFile;
    private final File file;
    private BufferedReader br = null;
    private PropertyChangeSupport support;
    private Start start;
    private final Intervals intervals = new Intervals();
    private Iperf3ResultsDataBase db;
    private Context context;
    private Iperf3Input iperf3Input;
    public Iperf3Parser(Context context, String pathToFile, Iperf3Input iperf3Input) {
        this.context = context;
        this.pathToFile = pathToFile;
        this.file = new File(this.pathToFile);
        this.support = new PropertyChangeSupport(this);
        this.db = Iperf3ResultsDataBase.getDatabase(this.context);
        this.iperf3Input = iperf3Input;
    }

    public void parse(){
        Log.i(TAG, "Parsing file");
                int i = 0;
                while(br == null && i < 500) {
                    try {
                        br = new BufferedReader(new FileReader(pathToFile));
                        Log.i(TAG, "parse: file found "+pathToFile);
                    } catch (FileNotFoundException ex) {
                        Log.d(TAG, "parse: "+ex);
                    }
                    i++;
                }
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


                                Iperf3RunResult iperf3RunResult = db.iperf3RunResultDao().getRunResult(iperf3Input.getUuid());
                                iperf3RunResult.intervals = intervals.getIntervalArrayList();

                                db.iperf3RunResultDao().update(iperf3RunResult);


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
                            default:;
                                System.out.println("Unknown event");
                                break;
                        }
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
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
