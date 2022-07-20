package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import android.os.Bundle;
import android.system.ErrnoException;
import android.system.Os;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;

public class Iperf3Activity extends AppCompatActivity {
    private CheckBox iperf3BiDir;
    private CheckBox iperf3Reverse;
    private CheckBox iperf3Json;
    private CheckBox iperf3OneOff;

    private ToggleButton iperf3Client;

    private LinkedList<EditText> editTexts;

    private static final String TAG = "iperf3Activity";

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



    }




    public void executeIperfCommand(View view) {
        String command =  parseInput();

        try {
            Os.setenv("TMPDIR", String.valueOf(getCacheDir()), true);
        } catch (ErrnoException e) {
            e.printStackTrace();
        }

        Data.Builder iperf3Data = new Data.Builder();
        iperf3Data.putStringArray("commands", command.split(" "));
        OneTimeWorkRequest iperf3WR = new OneTimeWorkRequest.Builder(iperf3Worker.class).setInputData(iperf3Data.build()).build();
        WorkManager iperf3WM =  WorkManager.getInstance(getApplicationContext());
        ExistingWorkPolicy iperf3EWP = ExistingWorkPolicy.REPLACE;
        iperf3WM.enqueueUniqueWork("iperf3", iperf3EWP, iperf3WR);

    }

    private String getKeyFromId(String s){
        String key = "";
        switch (s){
            case "iperf3_logfile":
                key = "--logfile";
                break;
            case "iperf3_ip":
                key = "-c";
                if(iperf3Client.isChecked()){
                    key = "-s";
                }
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
                }
                stb.add(value);
            }
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


/*
    public void executeIperfCommand2(View view) {
        Log.i(TAG, "onClick: button clicked");
        //String inputText = iperf3CmdInput.getText().toString();
        //String[] split = inputText.split(" ");
        try {
            Os.setenv("TMPDIR", String.valueOf(getCacheDir()), true);
        } catch (ErrnoException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "executeIperfCommand: "+ getCacheDir());
        List<String> cmdList = Arrays.asList(split);
        if(cmdList.contains("--logfile")){
            int idx = cmdList.indexOf("--logfile")+1;
            String path = split[idx];
            Timestamp iperfTS = new Timestamp(System.currentTimeMillis());
            path = getFilesDir() +"/"+path+iperfTS.toString().replace(" ", "_")+".log";
            split[idx] = path;
        }


        Data.Builder iperf3Data = new Data.Builder();
        iperf3Data.putStringArray("commands", split);
        OneTimeWorkRequest iperf3WR = new OneTimeWorkRequest.Builder(iperf3Worker.class).setInputData(iperf3Data.build()).build();
        WorkManager iperf3WM =  WorkManager.getInstance(getApplicationContext());
        ExistingWorkPolicy iperf3EWP = ExistingWorkPolicy.REPLACE;
        iperf3WM.enqueueUniqueWork("iperf3", iperf3EWP, iperf3WR);

    }
*/
}