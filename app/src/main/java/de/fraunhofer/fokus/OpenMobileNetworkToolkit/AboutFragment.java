/*
 *  SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import android.os.Bundle;
import android.text.util.Linkify;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class AboutFragment extends Fragment {
    public AboutFragment() {
        super(R.layout.fragment_about);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView about_text = requireView().findViewById(R.id.about_text_box);
        about_text.setAutoLinkMask(Linkify.WEB_URLS);
        about_text.append(getString(R.string.appinfo));
        about_text.append("\n\n");
        about_text.append("https://github.com/omnt/OpenMobileNetworkToolkit");
        about_text.append("\n\nThird party software used in this app: \n \n");
        about_text.append(
            "The InfluxDB 2.x JVM Based Client is released under the MIT License. \nhttps://github.com/influxdata/influxdb-client-java");
        about_text.append("\n\n");
        about_text.append(
            "iPerf3 is licensed under a BSD style license. \nhttps://github.com/esnet/iperf");
    }
}
