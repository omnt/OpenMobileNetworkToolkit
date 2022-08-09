package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import android.content.Intent;
import android.os.Bundle;
import android.system.ErrnoException;
import android.system.Os;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class Iperf3Activity extends AppCompatActivity {
    private CheckBox iperf3BiDir;
    private CheckBox iperf3Reverse;
    private CheckBox iperf3Json;
    private CheckBox iperf3OneOff;

    private ToggleButton iperf3Client;

    private LinkedList<EditText> editTexts;

    private static final String TAG = "iperf3Activity";

    private ThreadGroup iperf3TG;
    private Iperf3OverView iperf3OverView;
    private Iperf3DBHandler iperf3DBHandler;
    private String logFilePath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iperf3);
        View v = findViewById(android.R.id.content).getRootView();

        editTexts = new LinkedList<>();

        EditText iperf3LogFileName = v.findViewById(R.id.iperf3_logfile);
        EditText iperf3IP = v.findViewById(R.id.iperf3_ip);
        EditText iperf3Port = v.findViewById(R.id.iperf3_port);
        EditText iperf3Bandwidth = v.findViewById(R.id.iperf3_bandwith);
        EditText iperf3Duration = v.findViewById(R.id.iperf3_duration);
        EditText iperf3Interval = v.findViewById(R.id.iperf3_interval);
        EditText iperf3Bytes = v.findViewById(R.id.iperf3_bytes);

        editTexts.add(iperf3LogFileName);
        editTexts.add(iperf3IP);
        editTexts.add(iperf3Port);
        editTexts.add(iperf3Bandwidth);
        editTexts.add(iperf3Duration);
        editTexts.add(iperf3Interval);
        editTexts.add(iperf3Bytes);


        iperf3BiDir = v.findViewById(R.id.iperf_bidir);
        iperf3Reverse = v.findViewById(R.id.iperf3_reverse);
        iperf3Json = v.findViewById(R.id.iperf3_json);
        iperf3OneOff = v.findViewById(R.id.iperf3_one_off);

        iperf3Client = v.findViewById(R.id.iperf3_client);
        try {
            Os.setenv("TMPDIR", String.valueOf(getCacheDir()), true);
        } catch (ErrnoException e) {
            e.printStackTrace();
        }

        this.iperf3TG = new ThreadGroup("iperf3ThreadGroup");
        this.iperf3DBHandler = Iperf3DBHandler.getInstance(getApplicationContext());
        this.iperf3OverView = new Iperf3OverView(this.iperf3TG, this.iperf3DBHandler);

    }


    public void showInstances(View view){
        Intent intent = new Intent(Iperf3Activity.this, Iperf3ListActivity.class);
        intent.putExtra("json", this.iperf3OverView.updateRunners());
        startActivity(intent);
    }

    public void executeIperfCommand(View view) {
        String[] command =  parseInput().split(" ");

        Iperf3Runner iperf3R = new Iperf3Runner(command, getApplicationContext(), this.iperf3TG, this.logFilePath);
        this.iperf3OverView.addRunner(iperf3R);
        iperf3R.start();
    }

    private String getKeyFromId(String s){
        String key = "";
        switch (s){
            case "iperf3_logfile":
                key = "--logfile";
                break;
            case "iperf3_ip":
                key = "-c";
                break;
            case "iperf3_port":
                key = "-p";
                break;
            case "iperf3_bandwidth":
                key = "-b";
                break;
            case "iperf3_duration":
                key = "-t";
                break;
            case "iperf3_interval":
                key = "-i";
                break;
            case "iperf3_bytes":
                key = "-n";
                break;

        }
        return key;
    }

    private String parseInput(){
        List<String> stb = new LinkedList<>();
        for (EditText et: editTexts) {
            String value = et.getText().toString();
            if(!value.equals("")){
                String s = getResources().getResourceEntryName(et.getId());
                String key = getKeyFromId(s);
                stb.add(key);
                if(key.equals("--logfile")){
                    Timestamp iperfTS = new Timestamp(System.currentTimeMillis());
                    value = getFilesDir() +"/"+value+iperfTS.toString().replace(" ", "_")+".log";
                    this.logFilePath = value;
                }
                stb.add(value);
            }
        }
        if(iperf3Client.isChecked()){
            stb.add("-s");
        }
        if(iperf3BiDir.isChecked()){
            stb.add("--bidir");
        }
        if(iperf3Reverse.isChecked()){
            stb.add("--reverse");
        }
        if(iperf3OneOff.isChecked()){
            stb.add("--one-off");
        }
        if(iperf3Json.isChecked()){
            stb.add("--json");
        }

        String joined = String.join(" ", stb);
        Log.d(TAG, "parseInput: joined command "+ joined);
        return joined;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}