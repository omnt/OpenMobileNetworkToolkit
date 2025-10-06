/*
 * SPDX-FileCopyrightText:  2025 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Parameter;

import android.os.Parcel;
import android.os.Parcelable;

public class Parameter {
    private ParameterType parameterType;
    private String rootPath;
    private String rawDirPath;
    private String lineProtocolDirPath;
    private String lineProtocolFilePath;
    private String rawLogFilePath;
    protected Parameter(Parcel in) {
        lineProtocolFilePath = in.readString();
        rawLogFilePath = in.readString();
    }

    public Parameter(ParameterType type,
                     String rootPath,
                     String testUUID) {
        this.parameterType = type;
        this.rootPath = rootPath;
        this.rawDirPath = this.rootPath + "/" + type.toString() + "/raw";
        this.lineProtocolDirPath = this.rootPath + "/" + type.toString() + "/line_protocol";
        this.rawLogFilePath = this.rawDirPath + "/" + testUUID + ".log";
        this.lineProtocolFilePath = this.lineProtocolDirPath + "/" + testUUID + ".lp";
    }

    public ParameterType getParameterType() {
        return parameterType;
    }

    public String getRootPath() {
        return rootPath;
    }

    public String getRawDirPath() {
        return rawDirPath;
    }

    public void setRawDirPath(String rawDirPath) {
        this.rawDirPath = rawDirPath;
    }

    public String getLineProtocolDirPath() {
        return lineProtocolDirPath;
    }

    public void setLineProtocolDirPath(String lineProtocolDirPath) {
        this.lineProtocolDirPath = lineProtocolDirPath;
    }

    public String getLineProtocolFilePath() {
        return lineProtocolFilePath;
    }

    public void setLineProtocolFilePath(String lineProtocolFilePath) {
        this.lineProtocolFilePath = lineProtocolFilePath;
    }

    public String getRawLogFilePath() {
        return rawLogFilePath;
    }

    public void setRawLogFilePath(String rawLogFilePath) {
        this.rawLogFilePath = rawLogFilePath;
    }
}
