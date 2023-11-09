/*
 * SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 * SPDX-License-Identifier: apache2
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.InfluxDB2x;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;
import com.influxdb.client.domain.OnboardingRequest;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;

import java.util.List;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class InfluxDBFragment extends Fragment {
    LinearLayout result_layout;
    private View view;
    private final String TAG = "InfluxDBFragment";

    public InfluxDBFragment() {
        super(R.layout.fragment_influxdb);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.fragment_influxdb, parent, false);

        Button setupBtn = view.findViewById(R.id.button_setup_local_db);
        //TextView localDbStatus = view.findViewById(R.id.local_db_status);
        result_layout = view.findViewById(R.id.result_layout);
        //localDbStatus.setText("lol");
        TextView warning = view.findViewById(R.id.influx_view_warning);
        warning.setText(
            "This feature is still under development \n and requires a InfluxDB running on the phone");

        setupBtn.setOnClickListener(this::setupInfluxDB);
        //showLastEntries(view);
        return view;
    }

    private void showLastEntries(View view) {
        try {
            InfluxDBClient influxDBClient =
                InfluxDBClientFactory.create("http://127.0.0.1:8086", "1234567890".toCharArray(),
                    "OMNT", "omnt");
            QueryApi qa = influxDBClient.getQueryApi();
            List<FluxTable> res = qa.query("from(bucket: \"omnt\")\n" +
                "  |> range(start: -1m)\n" +
                "  |> filter(fn: (r) => r[\"_measurement\"] == \"CellInformation\")\n" +
                "  |> filter(fn: (r) => r[\"_field\"] == \"CI\" or r[\"_field\"] == \"RSRP\" or r[\"_field\"] == \"RSRQ\")");


            for (FluxTable fluxTable : res) {
                List<FluxRecord> records = fluxTable.getRecords();
                LinearLayout col = new LinearLayout(getContext());
                col.setOrientation(LinearLayout.VERTICAL);
                TextView header = new TextView(getContext());
                TextView content = new TextView(getContext());
                content.setPadding(0, 0, 15, 0);
                Boolean first = true;
                for (FluxRecord fluxRecord : records) {
                    //content.append(fluxRecord.getTime().toString());
                    if (first) {
                        header.setText(fluxRecord.getField());
                        first = false;
                    }
                    content.append(fluxRecord.getValueByKey("_value").toString() + "\n");
                    //logView.append(fluxRecord.getTime() + ": " + fluxRecord.getValueByKey("_value") + "\n");
                }
                col.addView(header);
                col.addView(content);
                result_layout.addView(col);
            }
        } catch (com.influxdb.exceptions.InfluxException e) {
            Log.d(TAG, e.toString());
            Toast.makeText(getContext(), e.toString(), Toast.LENGTH_LONG).show();
        }

        //logView.setText(res.listIterator().next().getRecords().listIterator().next().getTable().toString());
    }

    private void setupInfluxDB(View view) {
        try {
            InfluxDBClient influxDBClient =
                InfluxDBClientFactory.create("http://127.0.0.1:8086", "blank".toCharArray(), "OMNT",
                    "omnt");
            if (influxDBClient.isOnboardingAllowed()) {
                OnboardingRequest or = new OnboardingRequest();
                or.bucket("omnt");
                or.org("OMNT");
                or.password("omnt2022"); //todo THIS SHOULD NOT BE HARDCODED
                or.username("omnt");
                or.token("1234567890"); //todo generate a token
                influxDBClient.onBoarding(or);
                Log.d(TAG, "Database onboarding successfully");
                Toast.makeText(getContext(), "Database onboarding successfully", Toast.LENGTH_LONG)
                    .show();
            } else {
                Log.d(TAG, "Database was already onboarded");
                Toast.makeText(getContext(), "Database was already onboarded", Toast.LENGTH_LONG)
                    .show();
            }
            influxDBClient.close();

        } catch (com.influxdb.exceptions.InfluxException e) {
            Log.d(TAG, e.toString());
            Toast.makeText(getContext(), "Something bad happened", Toast.LENGTH_LONG).show();
        }
    }

}
