/*
 * SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 * SPDX-License-Identifier: apache2
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.TrafficStats;
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
import android.telephony.TelephonyCallback;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

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
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Model.BatteryInformation;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Model.CellInformation;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Model.DeviceInformation;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Model.FeatureInformation;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Model.LocationInformation;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Model.NetworkInformation;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Model.NetworkInterfaceInformation;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Model.SignalStrengthInformation;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Model.SliceInformation;

@RequiresApi(api = Build.VERSION_CODES.S)
public class DataProvider implements LocationListener, TelephonyCallback.CellInfoListener, TelephonyCallback.PhysicalChannelConfigListener {
    private static final String TAG = "DataProvider";
    private final Context ct;
    private final SharedPreferences sp;
    SliceInformation si = new SliceInformation();
    private LocationManager lm;
    private boolean permission_phone_state;
    private ConnectivityManager cm;
    private boolean cp;
    private TelephonyManager tm;
    private List<CellInformation> ci = new ArrayList<>();
    private DeviceInformation di = new DeviceInformation();
    private FeatureInformation fi = new FeatureInformation();
    private LocationInformation li;
    private NetworkInformation ni = new NetworkInformation();
    private NetworkInterfaceInformation nii = new NetworkInterfaceInformation();
    private LocationCallback locationCallback;
    private BatteryInformation bi = new BatteryInformation();

    public DataProvider(Context context) {
        GlobalVars gv = GlobalVars.getInstance();
        ct = context;
        lm = (LocationManager) ct.getSystemService(Context.LOCATION_SERVICE);
        sp = PreferenceManager.getDefaultSharedPreferences(ct);
        permission_phone_state = gv.isPermission_phone_state();

        // we can only relay on some APIs if this is a phone.
        if (gv.isFeature_telephony()) {
            cm = (ConnectivityManager) ct.getSystemService(Context.CONNECTIVITY_SERVICE);
            tm = gv.getTm();
            cp = gv.isCarrier_permissions();
        }

        //BatteryManager bm = (BatteryManager) context.getSystemService(BATTERY_SERVICE);
        refreshBatteryInfo();

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
            }
        } else {
            Log.d(TAG, "DataProvider: No Location Permissions");
            // todo we need to handle this in more details as we can't run without it
        }

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                onLocationChanged(locationResult.getLocations().get(0));
            }
        };
        startLocationUpdates();

        // initialize internal state
        refreshNetworkInformation();
        refreshDeviceInformation();
        onCellInfoChanged(getAllCellInfo());
    }

    // Filter values before adding them as we don't need to log not available information
    public void addOnlyAvailablePoint(Point point, String key, int value) {
        if (value != CellInfo.UNAVAILABLE) {
            point.addField(key, value);
        }
    }

    // return location object if available
    public LocationInformation getLocation() {
        return li;
    }

    // return location as influx point
    public Point getLocationPoint() {
        Point point = new Point("Location");
        point.time(System.currentTimeMillis(), WritePrecision.MS);
        // falling back to fake if no location is available is not the best solution.
        // We should ask the user / add configuration what to do
        if (sp.getBoolean("fake_location", false) || li == null) {
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

    // return a network Information Object
    public void refreshNetworkInformation() {
        if (permission_phone_state) {
            ni = new NetworkInformation(
                    tm.getNetworkOperatorName(),
                    tm.getSimOperatorName(),
                    tm.getNetworkSpecifier(),
                    tm.getDataState(),
                    tm.getDataNetworkType(),
                    tm.getPhoneType(),
                    tm.getPreferredOpportunisticDataSubscription()
            );
        }
    }

    public NetworkInformation getNetworkInformation() {
        return ni;
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
        tags_map_modifiable.put("security_patch", di.getSecurityPatchLevel());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            tags_map_modifiable.put("soc_model", di.getSOCModel());
        }
        if (cp) {
            tags_map_modifiable.put("imei", di.getIMEI());
            tags_map_modifiable.put("imsi", getIMSI());
        }
        tags_map_modifiable.put("radio_version", Build.getRadioVersion());
        return tags_map_modifiable;
    }

    // return a deviceInformation object with device specific information
    public DeviceInformation getDeviceInformation() {
        return di;
    }

    @SuppressLint({"MissingPermission", "HardwareIds"})
    public void refreshDeviceInformation() {
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
        if (cp) { // todo try root privileges or more fine granular permission
            di.setIMEI(tm.getImei());
            di.setMEID(tm.getMeid());
            di.setIMSI(getIMSI());
            di.setSimSerial(tm.getSimSerialNumber());
            di.setSubscriberId(tm.getSubscriberId());
            di.setNetworkAccessIdentifier(tm.getNai());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                di.setSubscriberId(String.valueOf(tm.getSubscriptionId()));
            }
        }
        di.setSecurityPatchLevel(Build.VERSION.SECURITY_PATCH);
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

    public List<CellInformation> getCellInformation() {
        return ci;
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

        List<CellSignalStrength> css = null;
        // On some devices we get here a null object if no SIM card is inserted in the phone.
        try {
            css = tm.getSignalStrength().getCellSignalStrengths();
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

    // Callbacks for location
    public void onLocationChanged(@NonNull Location location) {
        li.setLatitude(location.getLatitude());
        li.setLongitude(location.getLongitude());
        li.setAltitude(location.getAltitude());
        li.setProvider(location.getProvider());
        li.setAccuracy(location.getAccuracy());
        li.setSpeed(location.getSpeed());
    }

    public void onProviderDisabled(@NonNull String provider) {
        Log.d(TAG, String.format("%s is disabled", provider));
    }

    public void onProviderEnabled(@NonNull String provider) {
        Log.d(TAG, String.format("%s is enabled", provider));
    }

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

    @Override
    public void onCellInfoChanged(@NonNull List<CellInfo> list) {
        if(list == null) return;
        List<CellInformation> ciml = new ArrayList<>();
        for (CellInfo ci : list) {
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
        ci = ciml;
    }

    public ArrayList<SignalStrengthInformation> getSignalStrength() {
        SignalStrength signalStrength = tm.getSignalStrength();
        if (signalStrength != null) {
            List<android.telephony.CellSignalStrength> css = signalStrength.getCellSignalStrengths();
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
        } else {
            ArrayList<SignalStrengthInformation> signalStrengthInformations = new ArrayList<>();
            return signalStrengthInformations;
        }
    }

    public void refreshBatteryInfo() {
            IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = ct.registerReceiver(null, iFilter);
            bi.setLevel(batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1);
            bi.setScale(batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1);
            bi.setCharge_type(batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1) : -1);
    }

    public BatteryInformation getBatteryInformation() {
        refreshBatteryInfo();
        return bi;
    }

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

    @Override
    public void onPhysicalChannelConfigChanged(@NonNull List<PhysicalChannelConfig> list) {
        Log.d(TAG, list.toString());
    }

    public void refreshAll() {
        refreshDeviceInformation();
        refreshNetworkInformation();
        refreshBatteryInfo();
    }
}
