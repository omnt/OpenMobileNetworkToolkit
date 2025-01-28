/*
 *  SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import android.content.pm.PackageManager;
import android.telephony.CarrierConfigManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.widget.ImageView;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.DataProvider;

public class GlobalVars {
    public static int counter = 0;
    public static boolean isNetworkConnected = false;
    public static final String CSIRSRP = "CsiRSRP";
    public static final String CSIRSRQ = "CsiRSRQ";
    public static final String CSISINR = "CsiSINR";
    public static final String SSRSRP = "SSRSRP";
    public static final String SSRSRQ = "SSRSRQ";
    public static final String SSSINR = "SSSINR";
    public static final String INFLUX_WRITE_STATUS = "influxdb_write_status";
    private static GlobalVars instance;
    ImageView log_status;
    private DataProvider dp;
    private boolean feature_telephony;
    private boolean carrier_permissions;
    private CarrierConfigManager ccm;
    private TelephonyManager tm;
    private SubscriptionManager sm;
    private PackageManager pm;
    private boolean feature_admin;
    private boolean feature_work_profile;
    private boolean permission_phone_state;
    private String signing_hash;

    public String getGit_hash() {
        return git_hash;
    }

    public void setGit_hash(String git_hash) {
        this.git_hash = git_hash;
    }

    private String git_hash;
    private boolean permission_fine_location;

    private GlobalVars() {
    }

    public static synchronized GlobalVars getInstance() {
        if (instance == null) {
            instance = new GlobalVars();
        }
        return instance;
    }

    public String getSigning_hash() {
        return signing_hash;
    }

    public void setSigning_hash(String signing_hash) {
        this.signing_hash = signing_hash;
    }

    public ImageView getLog_status() {
        return log_status;
    }

    public void setLog_status(ImageView log_status) {
        this.log_status = log_status;
    }

    public DataProvider get_dp() {
        return dp;
    }

    public void set_dp(DataProvider dataProvider) {
        dp = dataProvider;
    }

    public boolean isFeature_telephony() {
        return feature_telephony;
    }

    public void setFeature_telephony(boolean feature_telephony) {
        this.feature_telephony = feature_telephony;
    }

    public boolean isCarrier_permissions() {
        return carrier_permissions;
    }

    public void setCarrier_permissions(boolean carrier_permissions) {
        this.carrier_permissions = carrier_permissions;
    }

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

    public PackageManager getPm() {
        return pm;
    }

    public void setPm(PackageManager pm) {
        this.pm = pm;
    }

    public SubscriptionManager getSm() {
        return sm;
    }

    public void setSm(SubscriptionManager sm) {
        this.sm = sm;
    }

    public boolean isFeature_admin() {
        return feature_admin;
    }

    public void setFeature_admin(boolean feature_admin) {
        this.feature_admin = feature_admin;
    }

    public boolean isPermission_phone_state() {
        return permission_phone_state;
    }

    public void setPermission_phone_state(boolean feature_phone_state) {
        this.permission_phone_state = feature_phone_state;
    }

    public boolean isFeature_work_profile() {
        return feature_work_profile;
    }

    public void setFeature_work_profile(boolean feature_work_profile) {
        this.feature_work_profile = feature_work_profile;
    }

    public boolean isPermission_fine_location() {
        return permission_fine_location;
    }

    public void setPermission_fine_location(boolean permission_fine_location) {
        this.permission_fine_location = permission_fine_location;
    }

}
