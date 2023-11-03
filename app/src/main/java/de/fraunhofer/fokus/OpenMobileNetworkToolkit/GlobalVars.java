/*
 * SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 * SPDX-License-Identifier: apache2
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import android.content.pm.PackageManager;
import android.telephony.CarrierConfigManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.DataProvider;

public class GlobalVars {
    private GlobalVars(){}
    private static GlobalVars instance;
    public static synchronized GlobalVars getInstance(){
        if(instance==null){
            instance=new GlobalVars();
        }
        return instance;
    }
    private DataProvider dp;
    public void set_dp(DataProvider dataProvider) {
        dp = dataProvider;
    }
    public DataProvider get_dp() {
        return dp;
    }

    public boolean isFeature_telephony() {
        return feature_telephony;
    }

    public void setFeature_telephony(boolean feature_telephony) {
        this.feature_telephony = feature_telephony;
    }

    private boolean feature_telephony;

    public boolean isCarrier_permissions() {
        return carrier_permissions;
    }

    public void setCarrier_permissions(boolean carrier_permissions) {
        this.carrier_permissions = carrier_permissions;
    }

    private boolean carrier_permissions;

    public TelephonyManager getTm() {
        return tm;
    }

    public void setTm(TelephonyManager tm) {
        this.tm = tm;
    }

    public CarrierConfigManager getCcm() {
        return ccm;
    }

    public void setCcm(CarrierConfigManager ccm) {
        this.ccm = ccm;
    }

    private CarrierConfigManager ccm;
    private TelephonyManager tm;

    public PackageManager getPm() {
        return pm;
    }

    public SubscriptionManager getSm() {
        return sm;
    }

    public void setSm(SubscriptionManager sm) {
        this.sm = sm;
    }

    private SubscriptionManager sm;
    public void setPm(PackageManager pm) {
        this.pm = pm;
    }

    private PackageManager pm;

    public boolean isFeature_admin() {
        return feature_admin;
    }

    public void setFeature_admin(boolean feature_admin) {
        this.feature_admin = feature_admin;
    }

    private boolean feature_admin;

    public boolean isFeature_phone_state() {
        return feature_phone_state;
    }

    public boolean isFeature_work_profile() {
        return feature_work_profile;
    }

    public void setFeature_work_profile(boolean feature_work_profile) {
        this.feature_work_profile = feature_work_profile;
    }

    private boolean feature_work_profile;

    public void setFeature_phone_state(boolean feature_phone_state) {
        this.feature_phone_state = feature_phone_state;
    }

    private boolean feature_phone_state;

    public static int counter = 0;
    public static boolean isNetworkConnected = false;

    public static String Level = "Level";
    public static String CSIRSRP = "CsiRSRP";
    public static String CSIRSRQ = "CsiRSRQ";
    public static String CSISINR = "CsiSINR";
    public static String SSRSRP = "SSRSRP";
    public static String SSRSRQ = "SSRSRQ";
    public static String SSSINR = "SSSINR";
    public static String AsuLevel = "Asu Level";
    public static String Dbm = "Dbm";
    public static String RSSI = "RSSI";
    public static String RSRP = "RSRP";
    public static String RSRQ = "RSRQ";
    public static String RSSNR = "RSSNR";
    public static String CQI = "CQI";
    public static String EvoDbm = "EvoDbm";
}
