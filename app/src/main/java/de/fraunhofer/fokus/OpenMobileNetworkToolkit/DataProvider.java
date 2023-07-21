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
import android.os.Build;
import android.telephony.CarrierConfigManager;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityNr;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoNr;
import android.telephony.CellSignalStrength;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthNr;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import com.google.common.base.Splitter;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Model.CellInformation;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Model.NetworkInformation;

public class DataProvider {
    private CarrierConfigManager ccm;
    private ConnectivityManager cm;
    public LocationManager lm;
    private TelephonyManager tm;
    private PackageManager pm;
    private static final String TAG = "DataProvider";
    private boolean feature_telephony;
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
            cm = (ConnectivityManager) ct.getSystemService(Context.CONNECTIVITY_SERVICE);
            tm = (TelephonyManager) ct.getSystemService(Context.TELEPHONY_SERVICE);
        }
    }
    // Filter values before adding them as we dont need to log not available information
    public void addOnlyAvailablePoint(Point point, String key, int value) {
        if (value != CellInfo.UNAVAILABLE) {
            point.addField(key, value);
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
        } else {
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
        return new NetworkInformation(
                tm.getNetworkOperatorName(),
                tm.getSimOperatorName(),
                tm.getNetworkSpecifier(),
                tm.getDataState(),
                tm.getDataNetworkType(),
                tm.getPhoneType(),
                tm.getPreferredOpportunisticDataSubscription()
        );
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

    public String getIMEI(){
        if (tm.hasCarrierPrivileges()) {
            return tm.getImei();
        } else {
            return "";
        }
    }

    public String getIMSI(){
        if (tm.hasCarrierPrivileges()) {
            return tm.getSubscriberId();
        } else {
            return "";
        }
    }
    public Map<String, String> getTagsMap() {
        String tags = sp.getString("tags", "").strip().replace(" ", "");
        Map<String, String> tags_map = Collections.emptyMap();
        if (!tags.isEmpty()) {
            try {
                tags_map = Splitter.on(',').withKeyValueSeparator('=').split(tags);
            } catch (IllegalArgumentException e) {
                Log.d(TAG, "cant parse tags, ignoring");
            }
        }
        Map<String, String> tags_map_modifiable = new HashMap<>(tags_map);
        tags_map_modifiable.put("manufacturer", Build.MANUFACTURER);
        tags_map_modifiable.put("measurement_name", sp.getString("measurement_name", "OMNT"));
        tags_map_modifiable.put("model", Build.MODEL);
        tags_map_modifiable.put("sdk_version", String.valueOf(Build.VERSION.SDK_INT));
        tags_map_modifiable.put("android_version", Build.VERSION.RELEASE);
        tags_map_modifiable.put("secruity_patch", Build.VERSION.SECURITY_PATCH);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            tags_map_modifiable.put("soc_model", Build.SOC_MODEL);
        }
        if (tm.hasCarrierPrivileges()) {
            tags_map_modifiable.put("imei", getIMEI());
            tags_map_modifiable.put("imsi", getIMSI());
        }
        tags_map_modifiable.put("radio_version", Build.getRadioVersion());
        return  tags_map_modifiable;
    }

    public List<Point> getCellInfoPoint() {
        List<Point> points = new ArrayList<>();
        long ts = System.currentTimeMillis();
        boolean nc = sp.getBoolean("log_neighbour_cells", false);

        List<CellInfo> cil = getCellInfo();
        for (CellInfo ci : cil) {
            // check if want to log neighbour cells and skip non registered cells
            if (!nc) {
                if (!ci.isRegistered()) {
                    continue;
                }
            }
            Point point = new Point("CellInformation");
            point.time(ts, WritePrecision.MS);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                point.addField("OperatorAlphaLong", (String) ci.getCellIdentity().getOperatorAlphaLong());
            }
            point.addField("CellConnectionStatus", ci.getCellConnectionStatus());
            point.addField("IsRegistered", ci.isRegistered());
            if (ci instanceof CellInfoNr) {
                point.addField("CellType", "NR");
                CellInfoNr ciNR = (CellInfoNr) ci;
                CellIdentityNr ciNRId = (CellIdentityNr) ciNR.getCellIdentity();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    point.addField("Bands", Arrays.toString(ciNRId.getBands()));
                }
                point.addField("CI", ciNRId.getNci());
                point.addTag("CI", String.valueOf(ciNRId.getNci()));
                point.addField("NRARFCN", ciNRId.getNrarfcn());
                point.addField("MNC", ciNRId.getMncString());
                point.addField("MCC", ciNRId.getMccString());
                point.addField("PCI", ciNRId.getPci());
                point.addField("TAC", ciNRId.getTac());
            }
            if (ci instanceof CellInfoLte) {
                CellInfoLte ciLTE = (CellInfoLte) ci;
                CellIdentityLte ciLTEId = ciLTE.getCellIdentity();
                point.addField("CellType", "LTE");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    point.addField("Bands", Arrays.toString(ciLTEId.getBands()));
                }
                point.addField("Bandwidth", ciLTEId.getBandwidth());
                point.addField("CI", ciLTEId.getCi());
                point.addTag("CI", String.valueOf(ciLTEId.getCi()));
                point.addField("EARFCN", ciLTEId.getEarfcn());
                point.addField("MNC", ciLTEId.getMncString());
                point.addField("MCC", ciLTEId.getMccString());
                point.addField("PCI", ciLTEId.getPci());
                point.addField("TAC", ciLTEId.getTac());
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
            }
            points.add(point);
        }
        return points;
    }

    public List<CellInformation> getCellInformation() {
        List<CellInformation> ciml = new ArrayList<>();
        List<CellInfo> cil = getCellInfo();
        for (CellInfo ci : cil) {
            CellInformation cim = new CellInformation();
            cim.setCellConnectionStatus(ci.getCellConnectionStatus());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                cim.setAlphaLong((String) ci.getCellIdentity().getOperatorAlphaLong());
            }
            if (ci instanceof CellInfoNr) {
                cim.setCellType("NR");
                CellInfoNr ciNR = (CellInfoNr) ci;
                CellIdentityNr ciNRId = (CellIdentityNr) ciNR.getCellIdentity();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    cim.setBands(Arrays.toString(ciNRId.getBands()));
                }
                cim.setCi(ciNRId.getNci());
                cim.setNrarfcn(ciNRId.getNrarfcn());
                cim.setMnc(ciNRId.getMncString());
                cim.setPci(ciNRId.getPci());
                cim.setTac(ciNRId.getTac());
                CellSignalStrengthNr ssNR = (CellSignalStrengthNr) ciNR.getCellSignalStrength();
                cim.setLevel(ssNR.getLevel());
                cim.setCsirsrp(ssNR.getCsiRsrp());
                cim.setCsirsrq(ssNR.getCsiRsrq());
                cim.setCsisinr(ssNR.getCsiSinr());
                cim.setSsrsrp(ssNR.getSsRsrp());
                cim.setSsrsrq(ssNR.getSsRsrq());
                cim.setSssinr(ssNR.getSsSinr());
            }
            if (ci instanceof CellInfoLte) {
                CellInfoLte ciLTE = (CellInfoLte) ci;
                CellIdentityLte ciLTEId = ciLTE.getCellIdentity();
                cim.setCellType("LTE");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    cim.setBands(Arrays.toString(ciLTEId.getBands()));
                }
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

    public Point getNetworkCapabilitiesPoint() {
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
        List<android.telephony.CellSignalStrength> css = tm.getSignalStrength().getCellSignalStrengths();
        for (CellSignalStrength ss:css) {
            if (ss instanceof CellSignalStrengthNr) {
                CellSignalStrengthNr ssnr =  (CellSignalStrengthNr) ss;
                addOnlyAvailablePoint(point, "Level", ssnr.getLevel());
                addOnlyAvailablePoint(point, "CsiRSRP", ssnr.getCsiRsrp());
                addOnlyAvailablePoint(point, "CsiRSRQ", ssnr.getCsiRsrq());
                addOnlyAvailablePoint(point, "CsiSINR", ssnr.getSsSinr());
                addOnlyAvailablePoint(point, "SSRSRP", ssnr.getSsRsrp());
                addOnlyAvailablePoint(point,"SSRSRQ", ssnr.getSsRsrq());
                addOnlyAvailablePoint(point, "SSSINR", ssnr.getSsSinr());
            }
            if (ss instanceof CellSignalStrengthLte) {
                CellSignalStrengthLte ssLTE =  (CellSignalStrengthLte) ss;
                addOnlyAvailablePoint(point,"Level", ssLTE.getLevel());
                addOnlyAvailablePoint(point,"CQI", ssLTE.getCqi());
                addOnlyAvailablePoint(point,"RSRP", ssLTE.getRsrp());
                addOnlyAvailablePoint(point,"RSRQ", ssLTE.getRsrq());
                addOnlyAvailablePoint(point,"RSSI", ssLTE.getRssi());
                addOnlyAvailablePoint(point,"RSSNR", ssLTE.getRssnr());
            }
            if (ss instanceof CellSignalStrengthCdma) {
                CellSignalStrengthCdma ssCdma =  (CellSignalStrengthCdma) ss;
                addOnlyAvailablePoint(point, "Level", ssCdma.getLevel());
                addOnlyAvailablePoint(point, "EvoDbm", ssCdma.getEvdoDbm());
            }
            if (ss instanceof CellSignalStrengthGsm) {
                CellSignalStrengthGsm ssGSM = (CellSignalStrengthGsm) ss;
                addOnlyAvailablePoint(point,"Level",ssGSM.getLevel());
                addOnlyAvailablePoint(point,"AsuLevel", ssGSM.getAsuLevel());
                addOnlyAvailablePoint(point,"Dbm", ssGSM.getDbm());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    addOnlyAvailablePoint(point,"RSSI", ssGSM.getRssi());
                }
            }
        }
        return point;
    }

    public ArrayList<String> GetSliceInformation(){
        return null;
    }

    public ArrayList<String> GetWifiInformation(){
        return null;
    }
}
