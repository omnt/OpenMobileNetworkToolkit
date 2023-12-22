/*
 *  SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class Iperf3LogFragment extends Fragment {

    private static final String TAG = "Iperf3LogFragment";
    private View v;
    private TextView logView;
    private Iperf3ResultsDataBase db;
    private Handler logHandler;
    private File file;
    private String uid;

    private TextView headliner;
    private ImageView leftFirst;
    private TextView leftSecond;
    private TextView leftThird;
    private TextView rightFirst;
    private TextView rightSecond;
    private TextView rightThird;
    private TextView wrapper;


    public Iperf3LogFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.db = Iperf3ResultsDataBase.getDatabase(getActivity().getApplicationContext());
    }

    private void setFields(Iperf3RunResult iperf3RunResult) {
        headliner.setText(iperf3RunResult.input.measurementName);

        leftFirst.setImageDrawable(Iperf3Utils.getDrawable(getContext(), iperf3RunResult.result));
        rightFirst.setText("Uploaded \n" + iperf3RunResult.uploaded);
        leftSecond.setText("Remote host \n" + iperf3RunResult.input.iperf3IP);
        rightSecond.setText("Run ID \n" + iperf3RunResult.uid);
        leftThird.setText("Time \n" + iperf3RunResult.input.timestamp);
        rightThird.setText("");

        wrapper.setText(iperf3RunResult.input.iperf3Command);
    }    private final Runnable logUpdate = new Runnable() {
        @Override
        public void run() {
            StringBuilder text = new StringBuilder();
            Iperf3RunResult iperf3RunResult = db.iperf3RunResultDao().getRunResult(uid);
            Log.d(TAG, "run: " + iperf3RunResult.result);

            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;
                while ((line = br.readLine()) != null) {
                    text.append(line);
                    text.append('\n');
                }
                br.close();
                logView.setText(text.toString());

            } catch (IOException e) {
                e.printStackTrace();
                logHandler.removeCallbacks(logUpdate);
                return;
            }
            setFields(iperf3RunResult);

            if (iperf3RunResult.result != -100) {
                logHandler.removeCallbacks(logUpdate);
                return;
            }

            logHandler.postDelayed(this, 1000);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_iperf3_log, container, false);
        logView = v.findViewById(R.id.stats);
        logView.setMovementMethod(new ScrollingMovementMethod());
        Iperf3RunResult iperf3RunResult =
            db.iperf3RunResultDao().getRunResult(this.getArguments().getString("uid"));

        headliner = v.findViewById(R.id.headliner);
        leftFirst = v.findViewById(R.id.leftFirst);
        leftSecond = v.findViewById(R.id.leftSecond);
        leftThird = v.findViewById(R.id.leftThird);
        rightFirst = v.findViewById(R.id.rightFirst);
        rightSecond = v.findViewById(R.id.rightSecond);
        rightThird = v.findViewById(R.id.rightThird);
        wrapper = v.findViewById(R.id.wrapper);

        setFields(iperf3RunResult);

        file = new File(iperf3RunResult.input.iperf3LogFilePath);
        uid = iperf3RunResult.uid;
        Log.d(TAG, "onCreateView: " + file.getAbsolutePath());

        logHandler = new Handler(Looper.myLooper());
        logHandler.post(logUpdate);
        return v;
    }

    public void onPause() {
        super.onPause();
        logHandler.removeCallbacks(logUpdate);
    }


}