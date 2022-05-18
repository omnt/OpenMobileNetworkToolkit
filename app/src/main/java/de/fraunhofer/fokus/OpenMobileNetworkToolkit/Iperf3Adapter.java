/*
 * SPDX-FileCopyrightText: 2022 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2022 Fraunhofer FOKUS
 *
 * SPDX-License-Identifier: apache2
 */
package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Iperf3Adapter {
    private DataInputStream outputStream;


    private String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
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
