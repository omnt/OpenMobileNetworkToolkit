/*
 * SPDX-FileCopyrightText: 2021 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2021 Fraunhofer FOKUS
 *
 * SPDX-License-Identifier: apache2
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.telephony.CarrierConfigManager;
import android.telephony.CellInfo;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Model.NetworkInformation;

public class DataProvider {
    private CarrierConfigManager ccm;
    private ConnectivityManager cm;
    private ConnectivityManager connectivityManager;
    public LocationManager lm;
    private TelephonyManager tm;
    private PackageManager pm;
    private boolean cp;
    private static final String TAG = "DataCollector";
    private boolean feature_telephony = false;
    private Context ct;


    public DataProvider(Context context) {
        ct = context;
        pm = ct.getPackageManager();
        lm = (LocationManager) ct.getSystemService(Context.LOCATION_SERVICE);
        feature_telephony = pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
        if (feature_telephony) {
            ccm = (CarrierConfigManager) ct.getSystemService(Context.CARRIER_CONFIG_SERVICE);
            tm = (TelephonyManager) ct.getSystemService(Context.TELEPHONY_SERVICE);
            cp = tm.hasCarrierPrivileges();
        }
    }

    public Location getLocation() {
        if (ActivityCompat.checkSelfPermission(ct, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ct, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        Location lastLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        return lastLocation;
    }

    public NetworkInformation GetNetworkInformation() {
        if (ActivityCompat.checkSelfPermission(ct, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        NetworkInformation netInfo = new NetworkInformation(
                tm.getNetworkOperatorName(),
                tm.getSimOperatorName(),
                tm.getNetworkSpecifier(),
                tm.getDataState(),
                tm.getDataNetworkType(),
                tm.getPhoneType(),
                tm.getPreferredOpportunisticDataSubscription()
        );
        return netInfo;
    }

    public List<CellInfo> getCellInfo() {
        List<CellInfo> cellInfo;
        if (ActivityCompat.checkSelfPermission(ct, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        cellInfo = tm.getAllCellInfo();
        return cellInfo;
    }

    public SignalStrength getSignalStrength() {
        if (tm != null) {
            SignalStrength signalStrength;
            signalStrength = tm.getSignalStrength();
            return signalStrength;
        } else {
            return null;
        }
    }

    public ArrayList<String> GetSliceInformation(){
        return null;
    }

    public ArrayList<String> GetWifiInformation(){
        return null;
    }
}
