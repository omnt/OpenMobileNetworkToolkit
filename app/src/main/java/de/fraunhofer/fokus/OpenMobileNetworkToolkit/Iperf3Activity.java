package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import android.os.Bundle;
import android.system.ErrnoException;
import android.system.Os;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.regex.*;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class Iperf3Activity extends AppCompatActivity {


    private EditText iperf3CmdInput;
    private Button SendButton;

    private static final String TAG = "iperf3Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iperf3);
        View v = findViewById(android.R.id.content).getRootView();
        SendButton = (Button) v.findViewById(R.id.iperf3commandButton);
        //SendButton.setOnClickListener(this);

        iperf3CmdInput = (EditText) v.findViewById(R.id.iperf3command);
        //iperf3CmdInput.setOnClickListener(this);

    }

    public void sendMessage(View view) {
        Log.i(TAG, "onClick: button clicked");
        String inputText = iperf3CmdInput.getText().toString();
        String[] split = inputText.split(" ");
        try {
            Os.setenv("TMPDIR", String.valueOf(getCacheDir()), true);
        } catch (ErrnoException e) {
            e.printStackTrace();
        }

        Data.Builder iperf3Data = new Data.Builder();
        iperf3Data.putStringArray("commands", split);
        WorkRequest iperf3WR = new OneTimeWorkRequest.Builder(iperf3Worker.class).setInputData(iperf3Data.build()).build();
        WorkManager iperf3WM =  WorkManager.getInstance(getApplicationContext());

        iperf3WM.enqueue(iperf3WR);
    }
}