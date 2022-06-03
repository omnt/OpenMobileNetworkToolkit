/*
 * SPDX-FileCopyrightText: 2022 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2022 Fraunhofer FOKUS
 *
 * SPDX-License-Identifier: apache2
 */
package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import java.io.DataInputStream;
import java.io.IOException;

public class Iperf3Adapter extends Fragment {

    static{
        System.loadLibrary("iperf3.10.1");
    }
    private static final String TAG = "iperf3Adapter";

    public Iperf3Adapter(){
        super(R.layout.iperf3_fragment);
    }
    private DataInputStream inputStream;

    private String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        return inflater.inflate(R.layout.iperf3_fragment, parent, false);
    }

    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Button button = (Button) view.findViewById(R.id.iperf3commandButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText inputIperf = (EditText) view.findViewById(R.id.iperf3command);
                Log.i(TAG, "onClick: button clicked");
                String inputText = inputIperf.getText().toString();
                Log.d(TAG, "onClick: command exec: "+startProcess(inputText));
                Log.d(TAG, "onClick: Output: "+getOutputString());
            }
        });
    }


    public String getOutputString(){
        return convertStreamToString(this.inputStream);
    }
    public int startProcess(String command){
        try {
            Process p = Runtime.getRuntime().exec(command);
            this.inputStream = new DataInputStream(p.getErrorStream());
            return p.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return 1;
        }
    }
}
