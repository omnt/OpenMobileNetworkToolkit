/*
 *  SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider;

import java.util.List;

public class LocationInformation extends Information {
    private double longitude;
    private double latitude;
    private double altitude;
    private String provider;
    private float accuracy;
    private float speed;
    private List<String> pl;

    public LocationInformation(double longitude, double latitude, double altitude, String provider, float accuracy, float speed, long timestamp) {
        super(timestamp);
        this.longitude = longitude;
        this.latitude = latitude;
        this.altitude = altitude;
        this.provider = provider;
        this.accuracy = accuracy;
        this.speed = speed;
    }

    public LocationInformation() {
        this.longitude = -1;
        this.latitude = -1;
        this.altitude = -1;
        this.provider = "N/A";
        this.accuracy = -1;
        this.speed = -1;
    }

    public List<String> getProviderList() {
        return pl;
    }

    public void setProviderList(List<String> pl) {
        this.pl = pl;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

}
