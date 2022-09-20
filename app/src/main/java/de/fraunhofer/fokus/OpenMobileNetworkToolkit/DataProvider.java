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
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityNr;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoNr;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthNr;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

import androidx.core.app.ActivityCompat;

import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;

import java.util.ArrayList;
import java.util.List;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Model.CellInformation;
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

    public Point getLocationPoint() {
        Point point = new Point("Location");
        point.time(System.currentTimeMillis(), WritePrecision.MS);
        Location loc = getLocation();
        point.addField("longitude", loc.getLongitude());
        point.addField("latitude", loc.getLatitude());
        point.addField("altitude", loc.getAltitude());
        point.addField("speed", loc.getSpeed());
        return point;
    }


    public NetworkInformation getNetworkInformation() {
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

    public Point getNetworkInformationPoint() {
        NetworkInformation ni = getNetworkInformation();
        Point point = new Point("NetworkInformation");
        point.time(System.currentTimeMillis(), WritePrecision.MS);
        point.addField("NetworkOperatorName", ni.getNetworkOperatorName());
        point.addField("NetworkSpecifier", ni.getNetworkSpecifier());
        point.addField("SimOperatorName", ni.getSimOperatorName());
        point.addField("DataState", ni.getDataState());
        point.addField("PhoneType", ni.getPhoneType());
        point.addField("PreferredOpportunisticDataSubscriptionId", ni.getPreferredOpportunisticDataSubscriptionId());
        return point;
    }

    public List<CellInfo> getCellInfo() {
        List<CellInfo> cellInfo;
        if (ActivityCompat.checkSelfPermission(ct, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        cellInfo = tm.getAllCellInfo();
        return cellInfo;
    }

    public Point getCellInfoPoint() {
        Point point = new Point("CellInformation");
        point.time(System.currentTimeMillis(), WritePrecision.MS);
        List<CellInfo> cil = getCellInfo();
        for (CellInfo ci:cil) {
            if (ci.isRegistered()) { //we only care for the serving cell
                point.addField("OperatorAlphaLong", (String) ci.getCellIdentity().getOperatorAlphaLong());
                if (ci instanceof CellInfoNr) {
                    point.addField("CellType", "NR");
                    CellInfoNr ciNR = (CellInfoNr) ci;
                    CellIdentityNr ciNRId = (CellIdentityNr) ciNR.getCellIdentity();
                    point.addField("Bands", ciNRId.getBands().toString());
                    point.addField("CI", ciNRId.getNci());
                    point.addField("NRARFCN", ciNRId.getNrarfcn());
                    point.addField("MNC", ciNRId.getMncString());
                    point.addField("PCI", ciNRId.getPci());
                    point.addField("TAC", ciNRId.getTac());
                    CellSignalStrengthNr ssNR = (CellSignalStrengthNr) ciNR.getCellSignalStrength();
                    point.addField("Level", ssNR.getLevel());
                    //point.addField("CQI", ssNR.getCqi());
                    point.addField("CsiRSRP", ssNR.getCsiRsrp());
                    point.addField("CsiRSRQ", ssNR.getCsiRsrq());
                    point.addField("CsiSINR", ssNR.getCsiSinr());
                    point.addField("SSRSRP", ssNR.getSsRsrp());
                    point.addField("SSRSRQ", ssNR.getSsRsrq());
                    point.addField("SSSINR", ssNR.getSsSinr());
                }
                if (ci instanceof CellInfoLte) {
                    CellInfoLte ciLTE = (CellInfoLte) ci;
                    CellIdentityLte ciLTEId = ciLTE.getCellIdentity();
                    point.addField("CellType", "LTE");
                    point.addField("Bands", ciLTEId.getBands().toString());
                    point.addField("Bandwidth", ciLTEId.getBandwidth());
                    point.addField("CI", ciLTEId.getCi());
                    point.addField("EARFCN", ciLTEId.getEarfcn());
                    point.addField("MNC", ciLTEId.getMncString());
                    point.addField("PCI", ciLTEId.getPci());
                    point.addField("TAC", ciLTEId.getTac());
                    CellSignalStrengthLte ssLTE = ciLTE.getCellSignalStrength();
                    point.addField("Level", ssLTE.getLevel());
                    point.addField("CQI", ssLTE.getCqi());
                    point.addField("RSRP", ssLTE.getRsrp());
                    point.addField("RSRQ", ssLTE.getRsrq());
                    point.addField("RSSI", ssLTE.getRssi());
                    point.addField("RSSNR", ssLTE.getRssnr());
                }
                if (ci instanceof CellInfoCdma) {
                    point.addField("CellType", "CDMA");
                }
                if (ci instanceof CellInfoGsm) {
                    point.addField("CellType", "GSM");
                }
            }
        }
        return  point;
    }

    public CellInformation getCellInformation(){
        CellInformation cim = new CellInformation();
        List<CellInfo> cil = getCellInfo();
        for (CellInfo ci:cil) {
            cim.setAlphaLong((String) ci.getCellIdentity().getOperatorAlphaLong());
            if (ci instanceof CellInfoNr) {
                cim.setCellType("NR");
                CellInfoNr ciNR = (CellInfoNr) ci;
                CellIdentityNr ciNRId = (CellIdentityNr) ciNR.getCellIdentity();
                cim.setBands(ciNRId.getBands().toString());
                cim.setCi(ciNRId.getNci());
                cim.setNrarfcn(ciNRId.getNrarfcn());
                cim.setMnc(ciNRId.getMncString());
                cim.setPci(ciNRId.getPci());
                cim.setTac(ciNRId.getTac());
                CellSignalStrengthNr ssNR = (CellSignalStrengthNr) ciNR.getCellSignalStrength();
                cim.setLevel(ssNR.getLevel());
                //point.addField("CQI", ssNR.getCqi());
                cim.setCsirsrp(ssNR.getCsiRsrp());
                cim.setCsirsrq(ssNR.getCsiRsrq());
                cim.setCsisinr(ssNR.getCsiSinr());
                cim.setSsrsrp(ssNR.getSsRsrp());
                cim.setSsrsrq(ssNR.getSsRsrq());
                cim.setSssinr(ssNR.getSsSinr());
            }
            if (ci instanceof CellInfoLte) {
                CellInfoLte ciLTE = (CellInfoLte) ci;
                CellIdentityLte ciLTEId= ciLTE.getCellIdentity();
                cim.setCellType("LTE");
                cim.setBands(ciLTEId.getBands().toString());
                cim.setBandwidth(ciLTEId.getBandwidth());
                cim.setCi(ciLTEId.getCi());
                cim.setEarfcn(ciLTEId.getEarfcn());
                cim.setMnc(ciLTEId.getMncString());
                cim.setPci(ciLTEId.getPci());
                cim.setTac(ciLTEId.getTac());
                CellSignalStrengthLte ssLTE = ciLTE.getCellSignalStrength();
                cim.setLevel(ssLTE.getLevel());
                cim.setCqi(ssLTE.getCqi());
                cim.setRsrp(ssLTE.getRsrp());
                cim.setRsrp(ssLTE.getRsrq());
                cim.setRssi(ssLTE.getRssi());
                cim.setRssnr(ssLTE.getRssnr());
            }
            if (ci instanceof CellInfoCdma) {
                cim.setCellType("CDMA");
            }
            if (ci instanceof CellInfoGsm) {
                cim.setCellType("GSM");
            }
        }return cim;
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

    public Point getSignalStrengthPoint() {
        Point point = new Point("SignalStrength");
        point.time(System.currentTimeMillis(), WritePrecision.MS);
        SignalStrength ss = getSignalStrength();
        point.addField("Level", ss.getLevel());
        return point;
    }

    public ArrayList<String> GetSliceInformation(){
        return null;
    }

    public ArrayList<String> GetWifiInformation(){
        return null;
    }
}
