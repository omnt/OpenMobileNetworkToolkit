/*
 *  SPDX-FileCopyrightText: 2024 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2024 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2024 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.TrafficStats;
import android.net.wifi.WifiInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Looper;
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
import android.telephony.PhysicalChannelConfig;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyCallback;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
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

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.GlobalVars;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SPType;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SharedPreferencesGrouper;

/**
 * OMNT Data Provider class. Collects and provides all information the app can access
 */
public class DataProvider extends TelephonyCallback implements LocationListener, TelephonyCallback.CellInfoListener, TelephonyCallback.PhysicalChannelConfigListener, TelephonyCallback.SignalStrengthsListener {
    private static final String TAG = "DataProvider";
    private final Context ct;
    private final boolean permission_phone_state;
    private final DeviceInformation di = new DeviceInformation();
    private final BatteryInformation bi = new BatteryInformation();
    private final LocationCallback locationCallback;
    private final SharedPreferencesGrouper spg;
    private ConnectivityManager cm;
    private TelephonyManager tm;
    private SubscriptionManager sm;
    // internal data caches
    private List<CellInformation> ci = new ArrayList<>();
    private LocationInformation li = new LocationInformation();
    private NetworkInformation ni;// = new NetworkInformation();
    private List<NetworkInterfaceInformation> nii = new ArrayList<>();
    private ArrayList<SignalStrengthInformation> ssi = new ArrayList<>();
    private WifiInfo wi = null;
    private LocationManager lm;
    // Time stamp, should be updated on each update of internal data caches
    private long ts = System.currentTimeMillis();

    @SuppressLint("ObsoleteSdkInt")
    public DataProvider(Context context) {
        GlobalVars gv = GlobalVars.getInstance();
        ct = context;
        spg = SharedPreferencesGrouper.getInstance(ct);
        permission_phone_state = gv.isPermission_phone_state();

        // we can only relay on some APIs if this is a phone.
        if (gv.isFeature_telephony()) {
            cm = (ConnectivityManager) ct.getSystemService(Context.CONNECTIVITY_SERVICE);
            tm = gv.getTm();
            sm = (SubscriptionManager) ct.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
        }

        // We need location permission otherwise logging is useless
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            lm = (LocationManager) ct.getSystemService(Context.LOCATION_SERVICE);
            if (lm.isLocationEnabled()) {
                Log.d(TAG, "Location Provider " + lm.getProviders(true));
                li = new LocationInformation(); // empty LocationInformation to be filled by callback
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    lm.requestLocationUpdates(LocationManager.FUSED_PROVIDER, 0, 0, this);
                    Location loc = lm.getLastKnownLocation(LocationManager.FUSED_PROVIDER);
                    if (loc != null) {
                        onLocationChanged(Objects.requireNonNull(lm.getLastKnownLocation(LocationManager.FUSED_PROVIDER)));
                    }
                } else {
                    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                    onLocationChanged(Objects.requireNonNull(lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)));
                }
            } else {
                Log.d(TAG, "GPS is disabled");
                // todo use same popup as in main activity
            }
        } else {
            Log.d(TAG, "No Location Permissions");
            // todo we need to handle this in more details as we can't do logging without it
        }

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                onLocationChanged(locationResult.getLocations().get(0));
            }
        };

        startLocationUpdates();
        registerWiFiCallback();

        // initialize internal state
        refreshAll();
    }

    /**
     * Refresh Network Information
     */
    @SuppressLint("MissingPermission")
    public void refreshNetworkInformation() {
        if (permission_phone_state) {
            updateTimestamp();
            ni = new NetworkInformation(
                    tm.getNetworkOperatorName(),
                    tm.getSimOperatorName(),
                    tm.getNetworkSpecifier(),
                    tm.getDataState(),
                    tm.getDataNetworkType(),
                    tm.getPhoneType(),
                    tm.getPreferredOpportunisticDataSubscription()
            );
            ni.setTimeStamp(ts);
        } else {
            Log.d(TAG, "refreshNetworkInformation called but permission phone state is missing");
        }
    }

    /**
     * Refresh and get network information
     *
     * @return network information
     */
    public NetworkInformation getNetworkInformation() {
        refreshNetworkInformation();
        return ni;
    }

    /**
     * Get network information as influx point
     *
     * @return influx point
     */
    public Point getNetworkInformationPoint() {
        NetworkInformation ni = getNetworkInformation();
        Point point = new Point("NetworkInformation");
        point.time(ni.getTimeStamp(), WritePrecision.MS);
        point.addField("NetworkOperatorName", ni.getNetworkOperatorName());
        point.addField("NetworkSpecifier", ni.getNetworkSpecifier());
        point.addField("SimOperatorName", ni.getSimOperatorName());
        point.addField("DataState", ni.getDataState());
        point.addField("PhoneType", ni.getPhoneType());
        point.addField("PreferredOpportunisticDataSubscriptionId", ni.getPreferredOpportunisticDataSubscriptionId());
        return point;
    }

    /**
     * Refresh Device Information cache
     */
    @SuppressLint({"MissingPermission", "HardwareIds", "ObsoleteSdkInt"})
    public void refreshDeviceInformation() {
        updateTimestamp();
        di.setTimeStamp(ts);
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
        if (permission_phone_state) {
            di.setDeviceSoftwareVersion(String.valueOf(tm.getDeviceSoftwareVersion()));
        }
        if (tm.hasCarrierPrivileges()) {
            try {
                di.setIMEI(tm.getImei());
                di.setMEID(tm.getMeid());
                di.setSimSerial(tm.getSimSerialNumber());
                di.setSubscriberId(tm.getSubscriberId());
                di.setNetworkAccessIdentifier(tm.getNai());
            } catch (SecurityException e) {
                Log.d(TAG, "Can't get IMEI, MEID, SimSerial or SubscriberId");
            }
            di.setIMSI(getIMSI());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                di.setSubscriberId(String.valueOf(tm.getSubscriptionId()));
            }
        }
        di.setSecurityPatchLevel(Build.VERSION.SECURITY_PATCH);
    }

    /**
     * Get a DeviceInformation object with device specific information
     *
     * @return Device Information
     */
    public DeviceInformation getDeviceInformation() {
        return di;
    }

    /**
     * Refresh Network Interface Information
     */
    public void refreshNetworkInterfaceInformation() {
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
        nii = niil;
    }

    public List<NetworkInterfaceInformation> getNetworkInterfaceInformation() {
        return nii;
    }

    /**
     * Get the network interface information as influx points
     *
     * @return influx points
     */
    public List<Point> getNetworkInterfaceInformationPoints() {
        List<Point> points = new ArrayList<>();
        try {
            List<NetworkInterface> networkInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface networkInterface : networkInterfaces) {
                List<InetAddress> iNets = Collections.list(networkInterface.getInetAddresses());
                int i = 0;
                for (InetAddress iNet : iNets) {
                    String ifname = networkInterface.getDisplayName();
                    if (!Objects.equals(ifname, "lo") && !Objects.equals(ifname, "dummy0")) {
                        Point point = new Point("IPAddressInformation");
                        point.time(System.currentTimeMillis(), WritePrecision.MS);
                        point.addTag("interface_name", ifname);
                        point.addTag("address_index", String.valueOf(i));
                        i++;
                        point.addField("address", iNet.getHostAddress());
                        points.add(point);
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return points;
    }

    /**
     * Callback to receive current cell information
     *
     * @param list is the list of currently visible cells.
     */
    @SuppressLint("ObsoleteSdkInt")
    @Override
    public void onCellInfoChanged(@NonNull List<CellInfo> list) {
        updateTimestamp();
        long ts_ = ts;
        List<CellInformation> ciml = new ArrayList<>();
        for (CellInfo ci : list) {
            CellInformation cim = new CellInformation();
            cim.setTimeStamp(ts_);
            cim.setCellConnectionStatus(ci.getCellConnectionStatus());
            cim.setRegistered(ci.isRegistered());
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    cim.setTimingAdvance(ssNR.getTimingAdvanceMicros());
                }
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
                cim.setRsrq(ssLTE.getRsrq());
                cim.setRssi(ssLTE.getRssi());
                cim.setRssnr(ssLTE.getRssnr());
                cim.setAsuLevel(ssLTE.getAsuLevel());
                cim.setTimingAdvance(ssLTE.getTimingAdvance());
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
                cim.setLac(ciGSM.getCellIdentity().getLac());
                CellSignalStrengthGsm ssGSM = ciGSM.getCellSignalStrength();
                cim.setLevel(ssGSM.getLevel());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    cim.setRssi(ssGSM.getRssi());
                }
                cim.setDbm(ssGSM.getDbm());
                cim.setAsuLevel(ssGSM.getAsuLevel());
                cim.setTimingAdvance(ssGSM.getTimingAdvance());
            }
            ciml.add(cim);
        }
        ci = ciml;
    }

    /**
     * Get CellInformation object
     *
     * @return CellInformation
     */
    public List<CellInformation> getCellInformation() {
        return ci;
    }

    /**
     * Get CellInformation as Influx point
     *
     * @return List of InfluxPoints
     */
    @SuppressLint("ObsoleteSdkInt")
    public List<Point> getCellInformationPoint() {
        List<Point> points = new ArrayList<>();
        boolean nc = spg.getSharedPreference(SPType.logging_sp).getBoolean("log_neighbour_cells", false);
        for (CellInformation ci_ : ci) {
            // check if want to log neighbour cells and skip non registered cells
            if (!nc) {
                if (!ci_.isRegistered()) {
                    continue;
                }
            }
            Point point = new Point("CellInformation");
            point.time(ts, WritePrecision.MS);
            point.addField("OperatorAlphaLong", ci_.getAlphaLong());
            point.addField("CellConnectionStatus", ci_.getCellConnectionStatus());
            point.addField("IsRegistered", ci_.isRegistered());
            if (Objects.equals(ci_.getCellType(), "NR")) {
                point.addField("CellType", "NR");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    point.addField("Bands", ci_.getBands());
                }
                long ci = ci_.getCi();
                if (ci != CellInfo.UNAVAILABLE) {
                    point.addTag("CI", String.valueOf(ci));
                }
                point.addField("NRARFCN", ci_.getARFCN());
                point.addField("MNC", ci_.getMnc());
                point.addField("MCC", ci_.getMcc());
                point.addField("PCI", ci_.getPci());
                point.addField("TAC", ci_.getTac());
                point.addField("Level", ci_.getLevel());
                point.addField(GlobalVars.CSIRSRP, ci_.getRsrp());
                point.addField(GlobalVars.CSIRSRQ, ci_.getRsrq());
                point.addField(GlobalVars.CSISINR, ci_.getCsisinr());
                point.addField(GlobalVars.SSRSRP, ci_.getSsrsrp());
                point.addField(GlobalVars.SSRSRQ, ci_.getSsrsrq());
                point.addField(GlobalVars.SSSINR, ci_.getSssinr());
                point.addField("TimingAdvance", ci_.getTimingAdvance());
            }
            if (Objects.equals(ci_.getCellType(), "LTE")) {
                point.addField("CellType", "LTE");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    point.addField("Bands", ci_.getBands());
                }
                point.addField("Bandwidth", ci_.getBandwidth());
                long ci = ci_.getCi();
                if (ci != CellInfo.UNAVAILABLE) {
                    point.addTag("CI", String.valueOf(ci_.getCi()));
                }
                point.addField("ARFCN", ci_.getARFCN());
                point.addField("MNC", ci_.getMnc());
                point.addField("MCC", ci_.getMcc());
                point.addField("PCI", ci_.getPci());
                point.addField("TAC", ci_.getTac());
                point.addField("TAC", ci_.getTac());
                point.addField("Level", ci_.getLevel());
                point.addField("AsuLevel", ci_.getAsuLevel());
                point.addField("Level", ci_.getLevel());
                point.addField(GlobalVars.CQI, ci_.getCqi());
                point.addField("RSRP", ci_.getRsrp());
                point.addField("RSRQ", ci_.getRsrq());
                point.addField("RSSI", ci_.getRssi());
                point.addField("TimingAdvance", ci_.getTimingAdvance());
            }
            if (Objects.equals(ci_.getCellType(), "CDMA")) {
                point.addField("CellType", "CDMA");
            }
            if (Objects.equals(ci_.getCellType(), "GSM")) {
                point.addField("CellType", "GSM");
                long ci = ci_.getCi();
                if (ci != CellInfo.UNAVAILABLE) {
                    point.addTag("CI", String.valueOf(ci_.getCi()));
                }
                point.addField("ARFCN", ci_.getARFCN());
                point.addField("MNC", ci_.getMnc());
                point.addField("MCC", ci_.getMcc());
                point.addField("Level", ci_.getLevel());
                point.addField("AsuLevel", ci_.getAsuLevel());
                point.addField("Dbm", ci_.getDbm());
                point.addField("RSSI", ci_.getRssi());
                point.addField("LAC", ci_.getLac());
                point.addField("TimingAdvance", ci_.getTimingAdvance());
            }
            points.add(point);
        }
        return points;
    }

    /**
     * return a list of CellInfo. This list also contains not available cells
     *
     * @return CellInfo list
     */
    public List<CellInfo> getAllCellInfo() {
        List<CellInfo> cellInfo;
        if (ActivityCompat.checkSelfPermission(ct, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Access Fine Location permission missing");
            cellInfo = new ArrayList<>();
        } else {
            cellInfo = tm.getAllCellInfo();
        }
        return cellInfo;
    }

    /**
     * Filter CellInfo objects for the registered cells
     *
     * @return List of registered cells
     */
    public List<CellInformation> getRegisteredCells() {
        List<CellInformation> rcil = new ArrayList<>();
        for (CellInformation ci_ : ci) {
            if (ci_.isRegistered()) { //we only care for the serving cell
                rcil.add(ci_);
            }
        }
        return rcil;
    }

    /**
     * get network capabilities as influx point
     *
     * @return influx point
     */
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

    /**
     * Signal Strength Information chane callback
     *
     * @param signalStrength Signal Strength
     */
    @SuppressLint("ObsoleteSdkInt")
    @Override
    public void onSignalStrengthsChanged(@NonNull SignalStrength signalStrength) {
        updateTimestamp();
        long ts_ = ts;
        List<CellSignalStrength> css = signalStrength.getCellSignalStrengths();
        ArrayList<SignalStrengthInformation> signalStrengthInformationList = new ArrayList<>();
        for (CellSignalStrength ss : css) {
            SignalStrengthInformation signalStrengthInformation = new SignalStrengthInformation(ts_);
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
            signalStrengthInformationList.add(signalStrengthInformation);

        }
        ssi = signalStrengthInformationList;
    }

    public ArrayList<SignalStrengthInformation> getSignalStrengthInformation() {
        return ssi;
    }

    /**
     * Get the last signal strength information as influx point
     *
     * @return signal strength point
     */
    @SuppressLint("ObsoleteSdkInt")
    public Point getSignalStrengthPoint() {
        Point point = new Point("SignalStrength");
        point.time(System.currentTimeMillis(), WritePrecision.MS);

        List<CellSignalStrength> css;
        // On some devices we get here a null object if no SIM card is inserted in the phone.
        try {
            css = Objects.requireNonNull(tm.getSignalStrength()).getCellSignalStrengths();
        } catch (Exception e) {
            return point;
        }
        for (CellSignalStrength ss : css) {
            if (ss instanceof CellSignalStrengthNr) {
                CellSignalStrengthNr ssnr = (CellSignalStrengthNr) ss;
                addOnlyAvailablePoint(point, "Level", ssnr.getLevel());
                addOnlyAvailablePoint(point, "CsiRSRP", ssnr.getCsiRsrp());
                addOnlyAvailablePoint(point, "CsiRSRQ", ssnr.getCsiRsrq());
                addOnlyAvailablePoint(point, "CsiSINR", ssnr.getCsiSinr());
                addOnlyAvailablePoint(point, "SSRSRP", ssnr.getSsRsrp());
                addOnlyAvailablePoint(point, "SSRSRQ", ssnr.getSsRsrq());
                addOnlyAvailablePoint(point, "SSSINR", ssnr.getSsSinr());
            }
            if (ss instanceof CellSignalStrengthLte) {
                CellSignalStrengthLte ssLTE = (CellSignalStrengthLte) ss;
                addOnlyAvailablePoint(point, "Level", ssLTE.getLevel());
                addOnlyAvailablePoint(point, "CQI", ssLTE.getCqi());
                addOnlyAvailablePoint(point, "RSRP", ssLTE.getRsrp());
                addOnlyAvailablePoint(point, "RSRQ", ssLTE.getRsrq());
                addOnlyAvailablePoint(point, "RSSI", ssLTE.getRssi());
                addOnlyAvailablePoint(point, "RSSNR", ssLTE.getRssnr());
            }
            if (ss instanceof CellSignalStrengthCdma) {
                CellSignalStrengthCdma ssCdma = (CellSignalStrengthCdma) ss;
                addOnlyAvailablePoint(point, "Level", ssCdma.getLevel());
                addOnlyAvailablePoint(point, "EvoDbm", ssCdma.getEvdoDbm());
            }
            if (ss instanceof CellSignalStrengthGsm) {
                CellSignalStrengthGsm ssGSM = (CellSignalStrengthGsm) ss;
                addOnlyAvailablePoint(point, "Level", ssGSM.getLevel());
                addOnlyAvailablePoint(point, "AsuLevel", ssGSM.getAsuLevel());
                addOnlyAvailablePoint(point, "Dbm", ssGSM.getDbm());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    addOnlyAvailablePoint(point, "RSSI", ssGSM.getRssi());
                }
            }
        }
        return point;
    }

    /**
     * get location object if available
     *
     * @return LocationInformation object
     */
    public LocationInformation getLocation() {
        return li;
    }

    /**
     * return the location as influx point
     *
     * @return influx point of current location
     */
    public Point getLocationPoint() {
        Point point = new Point("Location");
        point.time(System.currentTimeMillis(), WritePrecision.MS);
        // falling back to fake if no location is available is not the best solution.
        // We should ask the user / add configuration what to do
        if (spg.getSharedPreference(SPType.logging_sp).getBoolean("fake_location", false) || li == null) {
            point.addField("longitude", 13.3143266);
            point.addField("latitude", 52.5259678);
            point.addField("altitude", 34.0);
            point.addField("speed", 0.0);
            point.addField("provider", "fake location");
            point.addField("accuracy", 100.0);

        } else {
            point.addField("longitude", li.getLongitude());
            point.addField("latitude", li.getLatitude());
            point.addField("altitude", li.getAltitude());
            point.addField("speed", li.getSpeed());
            point.addField("provider", li.getProvider());
            point.addField("accuracy", li.getAccuracy());
        }
        return point;
    }

    /**
     * Location provider on change callback
     *
     * @param location location
     */
    @Override
    public void onLocationChanged(@NonNull Location location) {
        li.setLatitude(location.getLatitude());
        li.setLongitude(location.getLongitude());
        li.setAltitude(location.getAltitude());
        li.setProvider(location.getProvider());
        li.setAccuracy(location.getAccuracy());
        li.setSpeed(location.getSpeed());
        if (lm != null) {
            li.setProviderList(lm.getProviders(true));
        }
    }

    /**
     * Location provider disabled callback
     *
     * @param provider location provider
     */
    @Override
    public void onProviderDisabled(@NonNull String provider) {
        Log.d(TAG, String.format("%s is disabled", provider));
    }

    /**
     * Location provider enabled callback
     *
     * @param provider location provider
     */
    @Override
    public void onProviderEnabled(@NonNull String provider) {
        Log.d(TAG, String.format("%s is enabled", provider));
    }

    /**
     * Start a location update request loop
     */
    private void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest.Builder(200)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build();
        FusedLocationProviderClient flpc = LocationServices.getFusedLocationProviderClient(ct);
        if (ActivityCompat.checkSelfPermission(ct, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ct, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            flpc.requestLocationUpdates(locationRequest,
                    locationCallback,
                    Looper.getMainLooper());
        }
    }

    /**
     * Refresh the internal BatteryInformation Object
     */
    public void refreshBatteryInfo() {
        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = ct.registerReceiver(null, iFilter);
        bi.setLevel(batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1);
        bi.setScale(batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1);
        bi.setCharge_type(batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1) : -1);
    }

    /**
     * Build an influx point from the BatteryInformation object
     *
     * @return Influx Point with current battery information
     */
    public Point getBatteryInformationPoint() {
        refreshBatteryInfo();
        Point point = new Point("BatteryInformation");
        point.time(System.currentTimeMillis(), WritePrecision.MS);
        point.addField("Level", bi.getLevel());
        point.addField("Scale", bi.getScale());
        point.addField("Percent", bi.getPercent());
        point.addField("Charge Type", bi.getCharge_type());
        return point;
    }

    /**
     * return a Map of key values pairs to be used as tags in the influx points
     * List consist of device information and user defined tags
     *
     * @return Map of k,v strings
     */
    @SuppressLint("ObsoleteSdkInt")
    public Map<String, String> getTagsMap() {
        String tags = spg.getSharedPreference(SPType.logging_sp).getString("tags", "").strip().replace(" ", "");
        Map<String, String> tags_map = Collections.emptyMap();
        if (!tags.isEmpty()) {
            try {
                tags_map = Splitter.on(',').withKeyValueSeparator('=').split(tags);
            } catch (IllegalArgumentException e) {
                Log.d(TAG, "can't parse tags, ignoring");
            }
        }
        DeviceInformation di = getDeviceInformation();

        Map<String, String> tags_map_modifiable = new HashMap<>(tags_map);
        tags_map_modifiable.put("measurement_name", spg.getSharedPreference(SPType.logging_sp).getString("measurement_name", "OMNT"));
        tags_map_modifiable.put("manufacturer", di.getManufacturer());
        tags_map_modifiable.put("model", di.getModel());
        tags_map_modifiable.put("sdk_version", String.valueOf(di.getAndroidSDK()));
        tags_map_modifiable.put("android_version", di.getAndroidRelease());
        tags_map_modifiable.put("security_patch", di.getSecurityPatchLevel());
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

    /**
     * WIP Physical Channel Config Listener
     *
     * @param list List of the current {@link PhysicalChannelConfig}s
     */
    @Override
    public void onPhysicalChannelConfigChanged(@NonNull List<PhysicalChannelConfig> list) {
        Log.d(TAG, list.toString());
    }

    /**
     * get the SIMs IMSI if accessible
     * We suppress the linter warning as we need IMSI even that not recommended for most apps.
     *
     * @return String of IMSI is available
     */
    @SuppressLint("HardwareIds")
    public String getIMSI() {
        if (tm.hasCarrierPrivileges()) {
            return tm.getSubscriberId();
        } else {
            return "N/A";
        }
    }

    /**
     * Get a list of subscription information
     *
     * @return List of SubscriptionIno
     */
    @SuppressLint("ObsoleteSdkInt")
    public List<SubscriptionInfo> getSubscriptions() {
        List<SubscriptionInfo> subscriptions = new ArrayList<>();
        ArrayList<SubscriptionInfo> activeSubscriptions = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= 30) {
            subscriptions.addAll(sm.getCompleteActiveSubscriptionInfoList());
        } else {
            if (ActivityCompat.checkSelfPermission(ct, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                List<SubscriptionInfo> subscriptions_ = sm.getActiveSubscriptionInfoList();
                if (subscriptions_ != null) {
                    subscriptions.addAll(subscriptions_);
                }
            }
        }
        for (SubscriptionInfo info : Objects.requireNonNull(subscriptions)) {
            if (tm.getSimState(info.getSimSlotIndex()) == TelephonyManager.SIM_STATE_READY) {
                activeSubscriptions.add(info);
            }
        }

        return activeSubscriptions;
    }

    /**
     * trigger a refresh of all internal data caches
     */
    @SuppressLint("MissingPermission") // we check this in the method to call
    public void refreshAll() {
        refreshDeviceInformation();
        refreshNetworkInformation();
        refreshBatteryInfo();
        refreshNetworkInterfaceInformation();
        onCellInfoChanged(getAllCellInfo());

        SignalStrength ss = tm.getSignalStrength();
        // if the phone is not connected and we missed the update on tis we clear our internal cache
        if (ss != null) {
            onSignalStrengthsChanged(ss);
        } else {
            ssi = new ArrayList<>();
        }
    }

    /**
     * Update the DataProvider cached timestamp
     */
    private void updateTimestamp() {
        ts = System.currentTimeMillis();
    }

    /**
     * Return a influx point representation of the wifi information
     *
     * @return Influx Point
     */
    public Point getWifiInformationPoint() {
        Point point = new Point("WifiInformation");
        point.time(System.currentTimeMillis(), WritePrecision.MS);
        WifiInfo wi_ = wi;
        point.addField("SSID", wi_.getSSID());
        point.addField("BSSID", wi_.getBSSID());
        point.addField("RSSI", wi_.getRssi());
        point.addField("Frequency", wi_.getFrequency());
        point.addField("Link Speed", wi_.getLinkSpeed());
        point.addField("TXLink Speed", wi_.getTxLinkSpeedMbps());
        point.addField("Max Supported RX Speed", wi_.getMaxSupportedRxLinkSpeedMbps());
        point.addField("RX Link Speed", wi_.getRxLinkSpeedMbps());
        point.addField("Max Supported TX Speed", wi_.getMaxSupportedTxLinkSpeedMbps());
        point.addField("TX Link Speed", wi_.getTxLinkSpeedMbps());
        return point;
    }

    /**
     * Return wifi info if available
     *
     * @return Wifi info or null
     */
    public WifiInfo getWifiInfo() {
        if (wi != null) {
            return wi;
        } else {
            return null;
        }
    }

    /**
     * Register the wifi callback
     */
    @SuppressLint("ObsoleteSdkInt")
    public void registerWiFiCallback() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                final NetworkRequest request = new NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build();
                final ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback(ConnectivityManager.NetworkCallback.FLAG_INCLUDE_LOCATION_INFO) {
                    @Override
                    public void onAvailable(@NonNull Network network) {
                        super.onAvailable(network);
                    }

                    @Override
                    public void onLost(@NonNull Network network) {
                        super.onLost(network);
                        wi = null;
                    }

                    @Override
                    public void onBlockedStatusChanged(@NonNull Network network, boolean blocked) {
                        super.onBlockedStatusChanged(network, blocked);
                    }

                    @Override
                    public void onCapabilitiesChanged(@NonNull Network network, @NonNull
                    NetworkCapabilities networkCapabilities) {
                        super.onCapabilitiesChanged(network, networkCapabilities);
                        wi = (WifiInfo) networkCapabilities.getTransportInfo();
                    }

                    @Override
                    public void onLinkPropertiesChanged(@NonNull Network network, @NonNull LinkProperties linkProperties) {
                        super.onLinkPropertiesChanged(network, linkProperties);
                    }

                    @Override
                    public void onLosing(@NonNull Network network, int maxMsToLive) {
                        super.onLosing(network, maxMsToLive);
                        wi = null;
                    }

                    @Override
                    public void onUnavailable() {
                        super.onUnavailable();
                        wi = null;
                    }
                };
                cm.registerNetworkCallback(request, networkCallback);
                cm.requestNetwork(request, networkCallback);
            }
        } catch (Exception e) {
            Log.d("Network Callback: Exception in registerNetworkCallback", "Catch exception");
        }
    }

    /**
     * Filter values before adding them as we don't need to log not available information
     */
    public void addOnlyAvailablePoint(Point point, String key, int value) {
        if (value != CellInfo.UNAVAILABLE) {
            point.addField(key, value);
        }
    }
}
