/*
 * SPDX-FileCopyrightText:  2025 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Parameter;

import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;

public class Parameter implements Parcelable {
    public static final String rootPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath();
    private String lineProtocolFile;
    private String logfile;
    protected Parameter(Parcel in) {
        lineProtocolFile = in.readString();
        logfile = in.readString();
    }

    public Parameter(String logfile, String lineProtocolFile) {
        this.logfile = logfile;
        this.lineProtocolFile = lineProtocolFile;
    }

    public static final Creator<Parameter> CREATOR = new Creator<Parameter>() {
        @Override
        public Parameter createFromParcel(Parcel in) {
            return new Parameter(in);
        }

        @Override
        public Parameter[] newArray(int size) {
            return new Parameter[size];
        }
    };

    public String getLineProtocolFile() {
        return lineProtocolFile;
    }

    public String getLogfile() {
        return logfile;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(android.os.Parcel dest, int flags) {
        dest.writeString(lineProtocolFile);
        dest.writeString(logfile);
    }
    public void setLineProtocolFile(String lineProtocolFile) {
        this.lineProtocolFile = lineProtocolFile;
    }

    public void setLogfile(String logfile) {
        this.logfile = logfile;
    }
}
