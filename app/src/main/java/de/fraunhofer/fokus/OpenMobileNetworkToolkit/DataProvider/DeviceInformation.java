/*
 *  SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider;

import android.content.Context;
import android.widget.TableLayout;

public class DeviceInformation extends Information {
    private String Model;
    private String Manufacturer;
    private String SOCManufacturer;
    private String SOCModel;
    private String RadioVersion;
    private String SupportedModemCount;
    private String AndroidSDK;
    private String AndroidRelease;
    private String DeviceSoftwareVersion;
    private String SecurityPatchLevel;
    private String IMEI;
    private String IMSI;
    private String MEID;
    private String SimSerial;
    private String SubscriberId;
    private String NetworkAccessIdentifier;
    private String SubscriptionId;

    public DeviceInformation(String model, String manufacturer, String SOCManufacturer, String SOCModel, String radioVersion, String supportedModemCount, String androidSDK, String androidRelease, String deviceSoftwareVersion, String IMEI, String MEID, String IMSI, String simSerial, String subscriberId, String networkAccessIdentifier, String subscriptionId, long timestamp) {
        super(timestamp);
        Model = model;
        Manufacturer = manufacturer;
        this.SOCManufacturer = SOCManufacturer;
        this.SOCModel = SOCModel;
        RadioVersion = radioVersion;
        SupportedModemCount = supportedModemCount;
        AndroidSDK = androidSDK;
        AndroidRelease = androidRelease;
        DeviceSoftwareVersion = deviceSoftwareVersion;
        this.IMEI = IMEI;
        this.MEID = MEID;
        this.IMSI = IMSI;
        SimSerial = simSerial;
        SubscriberId = subscriberId;
        NetworkAccessIdentifier = networkAccessIdentifier;
        SubscriptionId = subscriptionId;
    }

    public DeviceInformation() {
    }

    public String getIMSI() {
        return IMSI;
    }

    public void setIMSI(String IMSI) {
        this.IMSI = IMSI;
    }

    public String getSecurityPatchLevel() {
        return SecurityPatchLevel;
    }

    public void setSecurityPatchLevel(String securityPatchLevel) {
        SecurityPatchLevel = securityPatchLevel;
    }

    public String getModel() {
        return Model;
    }

    public void setModel(String model) {
        Model = model;
    }

    public String getManufacturer() {
        return Manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        Manufacturer = manufacturer;
    }

    public String getSOCManufacturer() {
        return SOCManufacturer;
    }

    public void setSOCManufacturer(String SOCManufacturer) {
        this.SOCManufacturer = SOCManufacturer;
    }

    public String getSOCModel() {
        return SOCModel;
    }

    public void setSOCModel(String SOCModel) {
        this.SOCModel = SOCModel;
    }

    public String getRadioVersion() {
        return RadioVersion;
    }

    public void setRadioVersion(String radioVersion) {
        RadioVersion = radioVersion;
    }

    public String getSupportedModemCount() {
        return SupportedModemCount;
    }

    public void setSupportedModemCount(String supportedModemCount) {
        SupportedModemCount = supportedModemCount;
    }

    public String getAndroidSDK() {
        return AndroidSDK;
    }

    public void setAndroidSDK(String androidSDK) {
        AndroidSDK = androidSDK;
    }

    public String getAndroidRelease() {
        return AndroidRelease;
    }

    public void setAndroidRelease(String androidRelease) {
        AndroidRelease = androidRelease;
    }

    public String getDeviceSoftwareVersion() {
        return DeviceSoftwareVersion;
    }

    public void setDeviceSoftwareVersion(String deviceSoftwareVersion) {
        DeviceSoftwareVersion = deviceSoftwareVersion;
    }

    public String getIMEI() {
        return IMEI;
    }

    public void setIMEI(String IMEI) {
        this.IMEI = IMEI;
    }

    public String getMEID() {
        return MEID;
    }

    public void setMEID(String MEID) {
        this.MEID = MEID;
    }

    public String getSimSerial() {
        return SimSerial;
    }

    public void setSimSerial(String simSerial) {
        SimSerial = simSerial;
    }

    public String getSubscriberId() {
        return SubscriberId;
    }

    public void setSubscriberId(String subscriberId) {
        SubscriberId = subscriberId;
    }

    public String getNetworkAccessIdentifier() {
        return NetworkAccessIdentifier;
    }

    public void setNetworkAccessIdentifier(String networkAccessIdentifier) {
        NetworkAccessIdentifier = networkAccessIdentifier;
    }

    public String getSubscriptionId() {
        return SubscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        SubscriptionId = subscriptionId;
    }

    @Override
    public TableLayout getTable(TableLayout tl, Context context, boolean displayNull) {
        addRows(tl, context, new String[][]{
                {PrettyPrintMap.deviceInformation.MODEL.toString(), String.valueOf(this.getModel())},
                {PrettyPrintMap.deviceInformation.Manufacturer.toString(), String.valueOf(this.getManufacturer())},
                {PrettyPrintMap.deviceInformation.SOCManufacturer.toString(), String.valueOf(this.getSOCManufacturer())},
                {PrettyPrintMap.deviceInformation.SOCModel.toString(), String.valueOf(this.getSOCModel())},
                {PrettyPrintMap.deviceInformation.RadioVersion.toString(), String.valueOf(this.getRadioVersion())},
                {PrettyPrintMap.deviceInformation.SupportedModemCount.toString(), String.valueOf(this.getSupportedModemCount())},
                {PrettyPrintMap.deviceInformation.AndroidSDK.toString(), String.valueOf(this.getAndroidSDK())},
                {PrettyPrintMap.deviceInformation.AndroidRelease.toString(), String.valueOf(this.getAndroidRelease())},
                {PrettyPrintMap.deviceInformation.DeviceSoftwareVersion.toString(), String.valueOf(this.getDeviceSoftwareVersion())},
                {PrettyPrintMap.deviceInformation.SecurityPatchLevel.toString(), String.valueOf(this.getSecurityPatchLevel())},
                {PrettyPrintMap.deviceInformation.IMEI.toString(), String.valueOf(this.getIMEI())},
                {PrettyPrintMap.deviceInformation.MEID.toString(), String.valueOf(this.getMEID())},
                {PrettyPrintMap.deviceInformation.IMSI.toString(), String.valueOf(this.getIMSI())},
                {PrettyPrintMap.deviceInformation.SimSerial.toString(), String.valueOf(this.getSimSerial())},
                {PrettyPrintMap.deviceInformation.SubscriberId.toString(), String.valueOf(this.getSubscriberId())},
                {PrettyPrintMap.deviceInformation.NetworkAccessIdentifier.toString(), String.valueOf(this.getNetworkAccessIdentifier())},
                {PrettyPrintMap.deviceInformation.SubscriptionId.toString(), String.valueOf(this.getSubscriptionId())},
        }, displayNull);
        return tl;
    }

}
