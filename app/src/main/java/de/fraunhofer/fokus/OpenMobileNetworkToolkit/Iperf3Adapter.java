/*
 * SPDX-FileCopyrightText: 2022 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2022 Fraunhofer FOKUS
 *
 * SPDX-License-Identifier: apache2
 */
package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.fragment.app.Fragment;

import java.io.DataInputStream;
import java.io.IOException;

public class Iperf3Adapter extends Fragment {


    public Iperf3Adapter(){
        super(R.layout.iperf3_fragment);
    }
    private DataInputStream outputStream;

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
        // Setup any handles to view objects here
        // EditText etFoo = (EditText) view.findViewById(R.id.etFoo);
        Button button = (Button) view.findViewById(R.id.iperf3commandButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText inputIperf = (EditText) v.findViewById(R.id.iperf3command);
                String inputText = inputIperf.getText().toString();
                System.out.println(inputText);
            }
        });
    }


    public String getOutputString(){
        return convertStreamToString(this.outputStream);
    }
    public int startProcess(String command){
        try {
            Process p = Runtime.getRuntime().exec(command);
            this.outputStream = new DataInputStream(p.getInputStream());
            System.out.println(this.outputStream);
            return p.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return 1;
        }
    }
}
