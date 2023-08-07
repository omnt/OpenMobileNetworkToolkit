/*
 * SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 * SPDX-License-Identifier: apache2
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.TrafficStats;
import android.os.Build;
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

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Model.CellInformation;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Model.DeviceInformation;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Model.NetworkInformation;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Model.NetworkInterfaceInformation;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Model.SignalStrengthInformation;

public class DataProvider {
    private static final String TAG = "DataProvider";
    private final Context ct;
    private final SharedPreferences sp;
    public LocationManager lm;
    boolean feature_phone_state;
    private ConnectivityManager cm;
    private boolean cp;
    private TelephonyManager tm;

    public DataProvider(Context context) {
        ct = context;
        PackageManager pm = ct.getPackageManager();
        lm = (LocationManager) ct.getSystemService(Context.LOCATION_SERVICE);
        sp = PreferenceManager.getDefaultSharedPreferences(ct);
        boolean feature_telephony = pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
        feature_phone_state = (ActivityCompat.checkSelfPermission(ct, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED);

        if (feature_telephony) {
            cm = (ConnectivityManager) ct.getSystemService(Context.CONNECTIVITY_SERVICE);
            tm = (TelephonyManager) ct.getSystemService(Context.TELEPHONY_SERVICE);
            cp = tm.hasCarrierPrivileges();
        }
    }

    // Filter values before adding them as we don't need to log not available information
    public void addOnlyAvailablePoint(Point point, String key, int value) {
        if (value != CellInfo.UNAVAILABLE) {
            point.addField(key, value);
        }
    }

    // return location object if available
    public Location getLocation() {
        if (ActivityCompat.checkSelfPermission(ct, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ct, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        Location lastLocation;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            lastLocation = lm.getLastKnownLocation(LocationManager.FUSED_PROVIDER);
        } else {
            lastLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        return lastLocation;
    }

    // return location as influx point
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

    // return a network Information Object
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

    // return network information as influx point
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

    // return all cell information as a list. This list also contains not available cells
    public List<CellInfo> getAllCellInfo() {
        List<CellInfo> cellInfo;
        if (ActivityCompat.checkSelfPermission(ct, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        } else {
            cellInfo = tm.getAllCellInfo();
        }
        return cellInfo;
    }

    // return the result of an getAllCellInfo query as influx point
    public List<Point> getAllCellInfoPoint() {
        List<Point> points = new ArrayList<>();
        long ts = System.currentTimeMillis();
        boolean nc = sp.getBoolean("log_neighbour_cells", false);

        List<CellInfo> cil = getAllCellInfo();
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

                CellSignalStrengthNr ssNR = (CellSignalStrengthNr) ciNR.getCellSignalStrength();
                point.addField("Level", ssNR.getLevel());
                point.addField(GlobalVars.CSIRSRP, ssNR.getCsiRsrp());
                point.addField(GlobalVars.CSIRSRQ, ssNR.getCsiRsrq());
                point.addField(GlobalVars.CSISINR, ssNR.getCsiSinr());
                point.addField(GlobalVars.SSRSRP, ssNR.getSsRsrp());
                point.addField(GlobalVars.SSRSRQ, ssNR.getSsRsrq());
                point.addField(GlobalVars.SSSINR, ssNR.getSsSinr());
                point.addField(GlobalVars.AsuLevel, ssNR.getAsuLevel());
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

    // return Cell information objects only for the registered cells
    public List<CellInfo> getRegisteredCells() {
        List<CellInfo> cil = getAllCellInfo();
        List<CellInfo> rcil = new ArrayList<>();
        for (CellInfo ci : cil) {
            if (ci.isRegistered()) { //we only care for the serving cell
                rcil.add(ci);
            }
        }
        return rcil;
    }

    // get the phone IMEI if accessible
    public String getIMEI() {
        if (tm.hasCarrierPrivileges()) {
            return tm.getImei();
        } else {
            return "N/A";
        }
    }

    // get the SIMs IMSI if accessible
    // We suppress the linter warning as we need IMSI even that not recommended for most apps.
    @SuppressLint("HardwareIds")
    public String getIMSI() {
        if (tm.hasCarrierPrivileges()) {
            return tm.getSubscriberId();
        } else {
            return "N/A";
        }
    }

    // return a Map of key values pairs to be used as tags in the influx points
    // List consist of device information and user defined tags
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
        DeviceInformation di = getDeviceInformation();

        Map<String, String> tags_map_modifiable = new HashMap<>(tags_map);
        tags_map_modifiable.put("measurement_name", sp.getString("measurement_name", "OMNT"));
        tags_map_modifiable.put("manufacturer", di.getManufacturer());
        tags_map_modifiable.put("model", di.getModel());
        tags_map_modifiable.put("sdk_version", String.valueOf(di.getAndroidSDK()));
        tags_map_modifiable.put("android_version", di.getAndroidRelease());
        tags_map_modifiable.put("secruity_patch", di.getSecurityPatchLevel());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            tags_map_modifiable.put("soc_model", di.getSOCModel());
        }
        if (tm.hasCarrierPrivileges()) {
            tags_map_modifiable.put("imei", di.getIMEI());
            tags_map_modifiable.put("imsi", getIMSI());
        }
        tags_map_modifiable.put("radio_version", Build.getRadioVersion());
        return tags_map_modifiable;
    }

    // return a deviceInformation object with device specific information
    @SuppressLint("MissingPermission")
    public DeviceInformation getDeviceInformation() {
        DeviceInformation di = new DeviceInformation();
        di.setModel(Build.MODEL);
        di.setManufacturer(Build.MANUFACTURER);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            di.setSOCManufacturer(Build.SOC_MANUFACTURER);
            di.setSOCModel(Build.SOC_MODEL);
        }
        di.setRadioVersion(Build.getRadioVersion());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            di.setSupportedModemCount(String.valueOf(tm.getSupportedModemCount()));
        }
        di.setAndroidSDK(String.valueOf(Build.VERSION.SDK_INT));
        di.setAndroidRelease(Build.VERSION.RELEASE);
        if (feature_phone_state) {
            di.setDeviceSoftwareVersion(String.valueOf(tm.getDeviceSoftwareVersion()));
        }
        if (cp) { // todo try root privileges or more fine granular permission
            di.setIMEI(tm.getImei());
            di.setMEID(tm.getMeid());
            di.setSimSerial(tm.getSimSerialNumber());
            di.setSubscriberId(tm.getSubscriberId());
            di.setNetworkAccessIdentifier(tm.getNai());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                di.setSubscriberId(String.valueOf(tm.getSubscriptionId()));
            }
        }
        di.setSecurityPatchLevel(Build.VERSION.SECURITY_PATCH);
        return di;
    }

    public List<NetworkInterfaceInformation> getNetworkInterfaceInformation() {
        List<NetworkInterfaceInformation> niil = new ArrayList<>();
        try {
            List<NetworkInterface> networkInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface networkInterface : networkInterfaces) {
                List<InetAddress> iNets = Collections.list(networkInterface.getInetAddresses());
                for (InetAddress iNet : iNets) {
                    NetworkInterfaceInformation nii = new NetworkInterfaceInformation();
                    nii.setInterfaceName(networkInterface.getDisplayName());
                    nii.setAddress(iNet.getHostAddress().split("%")[0]);
                    niil.add(nii);
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return niil;
    }

    // Currently not used in favor of getCellInfoPoint but capt for future use when refactor home fragment
    public List<CellInformation> getCellInformation() {
        List<CellInformation> ciml = new ArrayList<>();
        List<CellInfo> cil = getAllCellInfo();
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
                cim.setARFCN(ciNRId.getNrarfcn());
                cim.setMnc(ciNRId.getMncString());
                cim.setMcc(ciNRId.getMccString());
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
                cim.setAsuLevel(ssNR.getAsuLevel());
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
                cim.setARFCN(ciLTEId.getEarfcn());
                cim.setMnc(ciLTEId.getMncString());
                cim.setMcc(ciLTEId.getMccString());
                cim.setPci(ciLTEId.getPci());
                cim.setTac(ciLTEId.getTac());
                cim.setAlphaLong(String.valueOf(ciLTEId.getOperatorAlphaLong()));
                CellSignalStrengthLte ssLTE = ciLTE.getCellSignalStrength();
                cim.setLevel(ssLTE.getLevel());
                cim.setCqi(ssLTE.getCqi());
                cim.setRsrp(ssLTE.getRsrp());
                cim.setRsrp(ssLTE.getRsrq());
                cim.setRssi(ssLTE.getRssi());
                cim.setRssnr(ssLTE.getRssnr());
                cim.setAsuLevel(ssLTE.getAsuLevel());
            }
            if (ci instanceof CellInfoCdma) {
                cim.setCellType("CDMA");
            }
            if (ci instanceof CellInfoGsm) {
                CellInfoGsm ciGSM = (CellInfoGsm) ci;
                CellIdentityGsm ciGSMId = ciGSM.getCellIdentity();
                cim.setCellType("GSM");
                cim.setMnc(ciGSMId.getMncString());
                cim.setCi(ciGSMId.getCid());
                cim.setMcc(ciGSMId.getMccString());
                cim.setARFCN(ciGSMId.getArfcn());
                CellSignalStrengthGsm ssGSM = ciGSM.getCellSignalStrength();
                cim.setLevel(ssGSM.getLevel());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    cim.setRssi(ssGSM.getRssi());
                }
                cim.setDbm(ssGSM.getDbm());
                cim.setAsuLevel(ssGSM.getAsuLevel());
                cim.setLevel(ssGSM.getLevel());
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

    public ArrayList<SignalStrengthInformation> getSignalStrength() {
        List<android.telephony.CellSignalStrength> css = Objects.requireNonNull(tm.getSignalStrength()).getCellSignalStrengths();
        ArrayList<SignalStrengthInformation> signalStrengthInformations = new ArrayList<>();
        for (CellSignalStrength ss : css) {
            SignalStrengthInformation signalStrengthInformation = new SignalStrengthInformation(System.currentTimeMillis());
            if (ss instanceof CellSignalStrengthNr) {
                CellSignalStrengthNr ssnr = (CellSignalStrengthNr) ss;
                signalStrengthInformation.setLevel(ssnr.getLevel());
                signalStrengthInformation.setCsiRSRP(ssnr.getCsiRsrp());
                signalStrengthInformation.setCsiRSRQ(ssnr.getCsiRsrq());
                signalStrengthInformation.setCsiSINR(ssnr.getCsiSinr());
                signalStrengthInformation.setSSRSRP(ssnr.getSsRsrp());
                signalStrengthInformation.setSSRSRQ(ssnr.getSsRsrq());
                signalStrengthInformation.setSSSINR(ssnr.getSsSinr());
                signalStrengthInformation.setConnectionType(SignalStrengthInformation.connectionTypes.NR);
            }
            if (ss instanceof CellSignalStrengthLte) {
                CellSignalStrengthLte ssLTE = (CellSignalStrengthLte) ss;
                signalStrengthInformation.setLevel(ssLTE.getLevel());
                signalStrengthInformation.setCQI(ssLTE.getCqi());

                signalStrengthInformation.setRSRQ(ssLTE.getRsrq());
                signalStrengthInformation.setRSRQ(ssLTE.getRsrp());
                signalStrengthInformation.setRSSI(ssLTE.getRssi());
                signalStrengthInformation.setRSSNR(ssLTE.getRssnr());
                signalStrengthInformation.setConnectionType(SignalStrengthInformation.connectionTypes.LTE);
            }
            if (ss instanceof CellSignalStrengthCdma) {
                CellSignalStrengthCdma ssCdma = (CellSignalStrengthCdma) ss;
                signalStrengthInformation.setLevel(ssCdma.getLevel());
                signalStrengthInformation.setEvoDbm(ssCdma.getEvdoDbm());
                signalStrengthInformation.setConnectionType(SignalStrengthInformation.connectionTypes.CDMA);
            }
            if (ss instanceof CellSignalStrengthGsm) {
                CellSignalStrengthGsm ssGSM = (CellSignalStrengthGsm) ss;
                signalStrengthInformation.setLevel(ssGSM.getLevel());
                signalStrengthInformation.setAsuLevel(ssGSM.getAsuLevel());
                signalStrengthInformation.setDbm(ssGSM.getDbm());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    signalStrengthInformation.setRSSI(ssGSM.getRssi());
                }
                signalStrengthInformation.setConnectionType(SignalStrengthInformation.connectionTypes.GSM);
            }
            signalStrengthInformations.add(signalStrengthInformation);

        }
        return signalStrengthInformations;
    }

    public Point getSignalStrengthPoint() {
        Point point = new Point("SignalStrength");
        point.time(System.currentTimeMillis(), WritePrecision.MS);
        List<android.telephony.CellSignalStrength> css = Objects.requireNonNull(tm.getSignalStrength()).getCellSignalStrengths();
        for (CellSignalStrength ss : css) {
            if (ss instanceof CellSignalStrengthNr) {
                CellSignalStrengthNr ssnr = (CellSignalStrengthNr) ss;
                addOnlyAvailablePoint(point, GlobalVars.Level, ssnr.getLevel());
                addOnlyAvailablePoint(point, GlobalVars.CSIRSRP, ssnr.getCsiRsrp());
                addOnlyAvailablePoint(point, GlobalVars.CSIRSRQ, ssnr.getCsiRsrq());
                addOnlyAvailablePoint(point, GlobalVars.CSISINR, ssnr.getSsSinr());
                addOnlyAvailablePoint(point, GlobalVars.SSRSRP, ssnr.getSsRsrp());
                addOnlyAvailablePoint(point, GlobalVars.SSRSRQ, ssnr.getSsRsrq());
                addOnlyAvailablePoint(point, GlobalVars.SSSINR, ssnr.getSsSinr());
            }
            if (ss instanceof CellSignalStrengthLte) {
                CellSignalStrengthLte ssLTE = (CellSignalStrengthLte) ss;
                addOnlyAvailablePoint(point, GlobalVars.Level, ssLTE.getLevel());
                addOnlyAvailablePoint(point, GlobalVars.CQI, ssLTE.getCqi());
                addOnlyAvailablePoint(point, GlobalVars.RSRP, ssLTE.getRsrp());
                addOnlyAvailablePoint(point, GlobalVars.RSRQ, ssLTE.getRsrq());
                addOnlyAvailablePoint(point, GlobalVars.RSSI, ssLTE.getRssi());
                addOnlyAvailablePoint(point, GlobalVars.RSSNR, ssLTE.getRssnr());
            }
            if (ss instanceof CellSignalStrengthCdma) {
                CellSignalStrengthCdma ssCdma = (CellSignalStrengthCdma) ss;
                addOnlyAvailablePoint(point, GlobalVars.Level, ssCdma.getLevel());
                addOnlyAvailablePoint(point, GlobalVars.EvoDbm, ssCdma.getEvdoDbm());
            }
            if (ss instanceof CellSignalStrengthGsm) {
                CellSignalStrengthGsm ssGSM = (CellSignalStrengthGsm) ss;
                addOnlyAvailablePoint(point, GlobalVars.Level, ssGSM.getLevel());
                addOnlyAvailablePoint(point, GlobalVars.AsuLevel, ssGSM.getAsuLevel());
                addOnlyAvailablePoint(point, GlobalVars.Dbm, ssGSM.getDbm());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    addOnlyAvailablePoint(point, GlobalVars.RSSI, ssGSM.getRssi());
                }
            }
        }
        return point;
    }
}
