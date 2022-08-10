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


    private EditText iperf3LogFileName;
    private EditText iperf3IP;
    private EditText iperf3Port;
    private EditText iperf3Bandwidth;
    private EditText iperf3Duration;
    private EditText iperf3Interval;
    private EditText iperf3Bytes;


    private LinkedList<EditText> editTexts;

    private static final String TAG = "iperf3Activity";

    private ThreadGroup iperf3TG;
    private Iperf3OverView iperf3OverView;
    private Iperf3DBHandler iperf3DBHandler;
    private String logFilePath;
    private String logFileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iperf3);
        View v = findViewById(android.R.id.content).getRootView();

        iperf3LogFileName = v.findViewById(R.id.iperf3_logfile);
        iperf3IP = v.findViewById(R.id.iperf3_ip);
        iperf3Port = v.findViewById(R.id.iperf3_port);
        iperf3Bandwidth = v.findViewById(R.id.iperf3_bandwith);
        iperf3Duration = v.findViewById(R.id.iperf3_duration);
        iperf3Interval = v.findViewById(R.id.iperf3_interval);
        iperf3Bytes = v.findViewById(R.id.iperf3_bytes);

        editTexts = new LinkedList<>();
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

        if(savedInstanceState != null){
            iperf3LogFileName.setText(savedInstanceState.getString("iperf3LogFileName"));
            iperf3IP.setText(savedInstanceState.getString("iperf3IP"));
            iperf3Port.setText(savedInstanceState.getString("iperf3Port"));
            iperf3Bandwidth.setText(savedInstanceState.getString("iperf3Bandwidth"));
            iperf3Duration.setText(savedInstanceState.getString("iperf3Duration"));
            iperf3Interval.setText(savedInstanceState.getString("iperf3Interval"));
            iperf3Bytes.setText(savedInstanceState.getString("iperf3Bytes"));

            iperf3BiDir.setChecked(savedInstanceState.getBoolean("iperf3BiDir"));

            iperf3Reverse.setChecked(savedInstanceState.getBoolean("iperf3BiDir"));
            iperf3BiDir.setChecked(savedInstanceState.getBoolean("iperf3Reverse"));
            iperf3Json.setChecked(savedInstanceState.getBoolean("iperf3Json"));
            iperf3OneOff.setChecked(savedInstanceState.getBoolean("iperf3OneOff"));

            iperf3Client.setChecked(savedInstanceState.getBoolean("iperf3Client"));
        }

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

        Iperf3Runner iperf3R = new Iperf3Runner(command, getApplicationContext(), this.iperf3TG, this.logFilePath, this.logFileName);
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
                    this.logFileName = value+iperfTS.toString().replace(" ", "_")+".log";
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

        outState.putString("iperf3LogFileName", iperf3LogFileName.getText().toString());
        outState.putString("iperf3IP", iperf3IP.getText().toString());
        outState.putString("iperf3Port", iperf3Port.getText().toString());
        outState.putString("iperf3Bandwidth", iperf3Bandwidth.getText().toString());
        outState.putString("iperf3Duration", iperf3Duration.getText().toString());
        outState.putString("iperf3Interval", iperf3Interval.getText().toString());
        outState.putString("iperf3Bytes", iperf3Bytes.getText().toString());

        outState.putBoolean("iperf3BiDir", iperf3BiDir.isChecked());

        outState.putBoolean("iperf3BiDir", iperf3BiDir.isChecked());
        outState.putBoolean("iperf3Reverse", iperf3Reverse.isChecked());
        outState.putBoolean("iperf3Json", iperf3Json.isChecked());
        outState.putBoolean("iperf3OneOff", iperf3OneOff.isChecked());
        outState.putBoolean("iperf3Client", iperf3Client.isChecked());
        super.onSaveInstanceState(outState);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        for (Iperf3Runner iperf3R:this.iperf3OverView.getIperf3Runners()) {
          //  requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE,READ_EXTERNAL_STORAGE}, 1);
        }
        //todo copy all log files to external storage where user can see all logs

    }
}