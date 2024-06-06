package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Interval.Interval;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.start.Start;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import org.json.JSONObject;


public class Iperf3Parser {

    private String pathToFile;
    private File file;
    private BufferedReader br = null;

    private Start start;
    private Intervals intervals = new Intervals();
    Iperf3Parser(String pathToFile) {
        this.pathToFile = pathToFile;
        this.file = new File(pathToFile);
        try {
            br = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException ex) {
            System.out.println("File not found");
            return;
        }

    }
    public void parse(){
        String line;
        try {
            while ((line = br.readLine()) != null) {
                JSONObject obj = new JSONObject(line);
                String event = obj.getString("event");
                JSONObject data = obj.getJSONObject("data");
                switch (event) {
                    case "start":
                        start = new Start();
                        start.parseStart(data);
                        break;
                    case "interval":
                        Interval interval = new Interval();
                        interval.parse(data);
                        intervals.addInterval(interval);
                        break;
                    case "end":
                        System.out.println("End");
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




}
