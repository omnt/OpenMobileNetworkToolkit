/*
 * SPDX-FileCopyrightText: 2021 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2021 Fraunhofer FOKUS
 *
 * SPDX-License-Identifier: apache2
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.TrafficStats;
import android.telephony.CarrierConfigManager;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityNr;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoNr;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthNr;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Model.CellInformation;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Model.NetworkInformation;

public class DataProvider {
    private CarrierConfigManager ccm;
    private ConnectivityManager cm;
    public LocationManager lm;
    private TelephonyManager tm;
    private PackageManager pm;
    private boolean cp;
    private static final String TAG = "DataCollector";
    private boolean feature_telephony = false;
    private Context ct;
    private SharedPreferences sp;


    public DataProvider(Context context) {
        ct = context;
        pm = ct.getPackageManager();
        lm = (LocationManager) ct.getSystemService(Context.LOCATION_SERVICE);
        sp = PreferenceManager.getDefaultSharedPreferences(context);
        feature_telephony = pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
        if (feature_telephony) {
            ccm = (CarrierConfigManager) ct.getSystemService(Context.CARRIER_CONFIG_SERVICE);
            cm = (ConnectivityManager)ct.getSystemService(Context.CONNECTIVITY_SERVICE);
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
        if (sp.getBoolean("fake_location", false)) {
            point.addField("longitude", 13.3143266);
            point.addField("latitude", 52.5259678);
            point.addField("altitude", 34.0);
            point.addField("speed", 0.0);

        } else  {
            Location loc = getLocation();
            if (loc != null) {
                point.addField("longitude", loc.getLongitude());
                point.addField("latitude", loc.getLatitude());
                point.addField("altitude", loc.getAltitude());
                point.addField("speed", loc.getSpeed());
            } else {
                point.addField("longitude", 0.0);
                point.addField("latitude", 0.0);
                point.addField("altitude", 0.0);
                point.addField("speed", 0.0);
            }
        }
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

    public ConnectivityManager getCm() {
        return cm;
    }

    public List<Point> getCellInfoPoint() {
        List<Point> points = new ArrayList<>();
        long ts = System.currentTimeMillis();
        List<CellInfo> cil = getCellInfo();
        for (CellInfo ci:cil) {
            Point point = new Point("CellInformation");
            point.time(ts, WritePrecision.MS);
            point.addField("OperatorAlphaLong", (String) ci.getCellIdentity().getOperatorAlphaLong());
            point.addField("CellConnectionStatus", ci.getCellConnectionStatus());
            point.addField("IsRegistered", ci.isRegistered());
            if (ci instanceof CellInfoNr) {
                point.addField("CellType", "NR");
                CellInfoNr ciNR = (CellInfoNr) ci;
                CellIdentityNr ciNRId = (CellIdentityNr) ciNR.getCellIdentity();
                point.addField("Bands", Arrays.toString(ciNRId.getBands()));
                point.addField("CI", ciNRId.getNci());
                point.addTag("CI", String.valueOf(ciNRId.getNci()));
                point.addField("NRARFCN", ciNRId.getNrarfcn());
                point.addField("MNC", ciNRId.getMncString());
                point.addField("MCC", ciNRId.getMccString());
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
                point.addField("Bands", Arrays.toString(ciLTEId.getBands()));
                point.addField("Bandwidth", ciLTEId.getBandwidth());
                point.addField("CI", ciLTEId.getCi());
                point.addTag("CI", String.valueOf(ciLTEId.getCi()));
                point.addField("EARFCN", ciLTEId.getEarfcn());
                point.addField("MNC", ciLTEId.getMncString());
                point.addField("MCC", ciLTEId.getMccString());
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
                CellInfoGsm ciGSM = (CellInfoGsm) ci;
                point.addField("CellType", "GSM");
                CellIdentityGsm ciGSMId = ciGSM.getCellIdentity();
                point.addField("CI", ciGSMId.getCid());
                point.addTag("CI", String.valueOf(ciGSMId.getCid()));
                point.addField("ARFCN", ciGSMId.getArfcn());
                point.addField("MNC", ciGSMId.getMncString());
                point.addField("MCC", ciGSMId.getMccString());
                CellSignalStrengthGsm ssGSM = (CellSignalStrengthGsm) ciGSM.getCellSignalStrength();
                point.addField("Level",ssGSM.getLevel());
                point.addField("AsuLevel", ssGSM.getAsuLevel());
                point.addField("Dbm", ssGSM.getDbm());
                point.addField("RSSI", ssGSM.getRssi());
            }
            points.add(point);
        }
        return  points;
    }

    public List<CellInformation> getCellInformation(){
        List<CellInformation> ciml = new ArrayList<>();
        List<CellInfo> cil = getCellInfo();
        for (CellInfo ci:cil) {
            CellInformation cim = new CellInformation();
            cim.setCellConnectionStatus(ci.getCellConnectionStatus());
            cim.setAlphaLong((String) ci.getCellIdentity().getOperatorAlphaLong());
            if (ci instanceof CellInfoNr) {
                cim.setCellType("NR");
                CellInfoNr ciNR = (CellInfoNr) ci;
                CellIdentityNr ciNRId = (CellIdentityNr) ciNR.getCellIdentity();
                cim.setBands(Arrays.toString(ciNRId.getBands()));
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
                cim.setBands(Arrays.toString(ciLTEId.getBands()));
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
            ciml.add(cim);
        }
        return ciml;
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

    public Point getNetworkCapabilitiesPoint(){
        NetworkCapabilities nc = cm.getNetworkCapabilities(cm.getActiveNetwork());
        Point point = new Point("InterfaceThroughput");
        if (nc != null) {
            int downSpeed = nc.getLinkDownstreamBandwidthKbps();
            int upSpeed = nc.getLinkUpstreamBandwidthKbps();
            point.addField("downSpeed_kbps", downSpeed);
            point.addField("upSpeed_kbps", upSpeed);
            point.time(System.currentTimeMillis(), WritePrecision.MS);
        } else {
            point.addField("downSpeed_kbps", -1);
            point.addField("upSpeed_kbps", -1);
            point.time(System.currentTimeMillis(), WritePrecision.MS);
        }
        point.addField("MobileTxBytes", TrafficStats.getMobileTxBytes());
        point.addField("MobileRxBytes", TrafficStats.getMobileRxBytes());
        return point;
    }


    public Point getSignalStrengthPoint() {
        Point point = new Point("SignalStrength");
        point.time(System.currentTimeMillis(), WritePrecision.MS);
        SignalStrength ss = getSignalStrength();
        if(ss == null){
            point.addField("Level", -1);
            return point;
        }
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
