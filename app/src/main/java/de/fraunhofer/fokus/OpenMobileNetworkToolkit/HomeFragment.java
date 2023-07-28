
/*
 * SPDX-FileCopyrightText: 2021 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2021 Fraunhofer FOKUS
 *
 * SPDX-License-Identifier: apache2
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import static android.telephony.TelephonyManager.CAPABILITY_SLICING_CONFIG_SUPPORTED;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.CarrierConfigManager;
import android.telephony.CellIdentity;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;


public class HomeFragment extends Fragment implements LocationListener {
    private static final String TAG = "HomeFragment";
    public CarrierConfigManager ccm;
    public ConnectivityManager connectivityManager;
    public TelephonyManager tm;
    public PackageManager pm;
    public LocationManager lm;
    SharedPreferences sharedPreferences;
    boolean feature_telephony;
    TextView txtLat;
    private boolean cp;
    private MainActivity ma;
    private SwipeRefreshLayout swipeRefreshLayout;

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    public static ArrayList<String> getIPs(ArrayList<String> props) {
        try {
            List<NetworkInterface> networkInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface networkInterface : networkInterfaces) {
                List<InetAddress> iNets = Collections.list(networkInterface.getInetAddresses());
                for (InetAddress iNet : iNets) {
                    props.add(networkInterface.getDisplayName() + "\t\t" + iNet.getHostAddress().split("%")[0]);
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return props;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Sets the default uncaught exception handler. This handler is invoked
        // in case any Thread dies due to an unhandled exception.
        Thread.setDefaultUncaughtExceptionHandler(new GlobalExceptionHandler());
    }

    @SuppressLint("MissingPermission")
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup parent,
            Bundle savedInstanceState
    ) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        ma = (MainActivity) getActivity();
        pm = Objects.requireNonNull(ma).pm;
        feature_telephony = ma.feature_telephony;
        if (feature_telephony) {
            ccm = (CarrierConfigManager) ma.getSystemService(Context.CARRIER_CONFIG_SERVICE);
            cp = ma.HasCarrierPermissions();
            tm = (TelephonyManager) ma.getSystemService(Context.TELEPHONY_SERVICE);
        }

        View view = inflater.inflate(R.layout.fragment_home, parent, false);

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            lm = (LocationManager) ma.getSystemService(Context.LOCATION_SERVICE);
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        } else {
            Log.d(TAG, "onCreateView: No Location Permissions");
        }
        swipeRefreshLayout = view.findViewById(R.id.home_fragment);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //todo load fragment new
                //getActivity().recreate();
                //System.out.println("HELLO WORLD!");
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        return view;
    }

    @SuppressLint({"MissingPermission", "HardwareIds"})
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        boolean feature_admin = pm.hasSystemFeature(PackageManager.FEATURE_DEVICE_ADMIN);
        boolean feature_phone_state = (ActivityCompat.checkSelfPermission(ma, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED);
        boolean work_profile = pm.hasSystemFeature(PackageManager.FEATURE_MANAGED_USERS);
        Context context = requireContext();
        ArrayList<String> props = new ArrayList<String>();
        props.add("\n \n ## Device ##");
        props.add("Model: " + Build.MODEL);
        props.add("Manufacturer: " + Build.MANUFACTURER);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            props.add("SOC Manufacturer: " + Build.SOC_MANUFACTURER);
            props.add("SOC Model: " + Build.SOC_MODEL);
        }
        props.add("Radio Version: " + Build.getRadioVersion());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            props.add("Supported Modem Count: " + tm.getSupportedModemCount());
        }
        props.add("Android SDK: " + Build.VERSION.SDK_INT);
        props.add("Android Release: " + Build.VERSION.RELEASE);
        if (feature_phone_state) {
            props.add("Device Software version: " + tm.getDeviceSoftwareVersion());
        }
        props.add("\n \n ## Features ##");
        props.add("Feature Telephony: " + feature_telephony);
        props.add("Work Profile: " + work_profile);
        props.add("Feature Admin: " + feature_admin);
        props.add("Network Connection Available: " + GlobalVars.isNetworkConnected);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            boolean sc = pm.hasSystemFeature(TelephonyManager.CAPABILITY_SLICING_CONFIG_SUPPORTED);
            props.add("Slicing Config supported: " + sc);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            props.add("Radio Interface Capability Slicing Config: " + tm.isRadioInterfaceCapabilitySupported(CAPABILITY_SLICING_CONFIG_SUPPORTED));
        }

        props.add("\n \n ## Permissions ##");
        props.add("Carrier Permissions: " + cp);
        props.add("READ_PHONE_STATE: " + feature_phone_state);
        props.add("ACCESS_FINE_LOCATION: " + (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED));
        props.add("ACCESS_BACKGROUND_LOCATION: " + (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED));


        props.add("\n \n ## Interfaces ##");
        props = getIPs(props);

        props.add("\n \n ## Network ##");
        props.add("Network Operator: " + tm.getNetworkOperatorName());
        props.add("Sim Operator Name: " + tm.getSimOperatorName());
        props.add("Network Specifier: " + tm.getNetworkSpecifier());
        props.add("DataState: " + tm.getDataState());
        if (feature_phone_state) {
            props.add("DataNetworkType: " + tm.getDataNetworkType()); // todo print useful  strings
        }
        props.add("SignalStrength: " + tm.getSignalStrength());
        int phone_type = tm.getPhoneType();
        if (phone_type == 0)
            props.add("Phone Type: None");
        else if (phone_type == 1)
            props.add("Phone Type: GSM");
        else if (phone_type == 2)
            props.add("Phone Type: CDMA");
        else if (phone_type == 3)
            props.add("Phone Type: SIP");
        if (feature_phone_state) {
            props.add("Registered PLMN: " + NetworkCallback.getPLMN(context));
        }
        if (feature_phone_state && tm.getSimState() == TelephonyManager.SIM_STATE_READY) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                props.add("Equivalent Home PLMNs: " + tm.getEquivalentHomePlmns());

                StringBuilder tmp = new StringBuilder();
                for (String plnm : tm.getForbiddenPlmns()) {
                    tmp.append(plnm).append(" ");
                }
                props.add("Forbidden PLMNs: " + tmp);
            }
        }
        if (feature_phone_state) {
            props.add("Preferred Opportunistic Data Subscription ID: " + tm.getPreferredOpportunisticDataSubscription());
        }

        props.add("Default Network: " + NetworkCallback.getCurrentNetwork(context));
        props.add("Interface Name: " + NetworkCallback.getInterfaceName(context));
        props.add("Network counter: " + GlobalVars.counter);
        props.add("Default DNS: " + NetworkCallback.getDefaultDNS(context));
        props.add("Enterprise Capability: " + NetworkCallback.getEnterpriseCapability(context));
        props.add("Validated Capability: " + NetworkCallback.getValidity(context));
        props.add("Internet Capability: " + NetworkCallback.getInternet(context));
        props.add("IMS Capability: " + NetworkCallback.getIMS(context));
        props.add("Capabilities: \n" + NetworkCallback.getNetworkCapabilitylist(context));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            props.add("Enterprise ID: " + NetworkCallback.getEnterpriseIds(context));
        }
        props.add("Cell Information: " + cellInfo());

        // Network Slicing
        props.add("TM Slice: " + NetworkCallback.getConfigurationTM(context));
        props.add("Slice Info: " + NetworkCallback.getNetworkSlicingInfo(context));
        props.add("Slice Config: " + NetworkCallback.getNetworkSlicingConfig(context));
        // Routing and Traffic
        props.add("Route Descriptor: " + NetworkCallback.getRouteSelectionDescriptor(context));
        props.add("Traffic Descriptor: " + NetworkCallback.getTrafficDescriptor(context));
        if (cp) { // todo try root privileges or more fine granular permission
            props.add("\n \n ## Device Identification Information ##");
            props.add("IMEI: " + tm.getImei());
            props.add("MEID: " + tm.getMeid());
            props.add("SimSerial: " + tm.getSimSerialNumber());
            props.add("SubscriberId: " + tm.getSubscriberId());
            props.add("Network Access Identifier: " + tm.getNai());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                props.add("SubscriptionId: " + tm.getSubscriptionId());
            }
        }
        TextView main_infos = getView().findViewById(R.id.main_infos);
        for (String prop : props) {
            main_infos.append(prop + "\n");
        }
    }

    // todo rework this to use the dataprovider / move code there
    private String cellInfo() {
        StringBuilder cellInfoBuilder = new StringBuilder();
        String allCellInfo = "";
        Set<CellIdentity> seenCellTowers = new HashSet<>();
        if (ActivityCompat.checkSelfPermission(ma, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            List<CellInfo> cellInfoList = tm.getAllCellInfo();
            for (CellInfo cellInfo : cellInfoList) {
                if (cellInfo instanceof CellInfoGsm) {
                    CellIdentityGsm cellIdentity = ((CellInfoGsm) cellInfo).getCellIdentity();
                    if (!seenCellTowers.contains(cellIdentity)) {
                        seenCellTowers.add(cellIdentity);
                        cellInfoBuilder.append("Cell Identity (GSM): ");
                        cellInfoBuilder.append(cellIdentity.getCid());
                        cellInfoBuilder.append("\n");
                        cellInfoBuilder.append("MCC: ");
                        cellInfoBuilder.append(cellIdentity.getMcc());
                        cellInfoBuilder.append("\n");
                        cellInfoBuilder.append("MNC: ");
                        cellInfoBuilder.append(cellIdentity.getMnc());
                        cellInfoBuilder.append("\n");
                    }
                    // Display cell identity information for GSM network
                    // e.g. cellIdentity.getCid(), cellIdentity.getMcc(), cellIdentity.getMnc(), etc.
                    // Add more cell identity information as needed
                } else if (cellInfo instanceof CellInfoCdma) {
                    CellIdentityCdma cellIdentity = ((CellInfoCdma) cellInfo).getCellIdentity();
                    if (!seenCellTowers.contains(cellIdentity)) {
                        seenCellTowers.add(cellIdentity);
                        cellInfoBuilder.append("Cell Identity (CDMA): ");
                        cellInfoBuilder.append(cellIdentity.getBasestationId());
                        cellInfoBuilder.append("\n");
                        cellInfoBuilder.append("System ID: ");
                        cellInfoBuilder.append(cellIdentity.getSystemId());
                        cellInfoBuilder.append("\n");
                        cellInfoBuilder.append("Network ID: ");
                        cellInfoBuilder.append(cellIdentity.getNetworkId());
                        cellInfoBuilder.append("\n");
                    }
                    // Add more cell identity information as needed
                    // Display cell identity information for CDMA network
                    // e.g. cellIdentity.getBasestationId(), cellIdentity.getSystemId(), cellIdentity.getNetworkId(), etc.
                } else if (cellInfo instanceof CellInfoLte) {
                    CellIdentityLte cellIdentity = ((CellInfoLte) cellInfo).getCellIdentity();
                    if (!seenCellTowers.contains(cellIdentity)) {
                        seenCellTowers.add(cellIdentity);
                        cellInfoBuilder.append("Cell Identity (LTE): ");
                        cellInfoBuilder.append(cellIdentity.getCi());
                        cellInfoBuilder.append("\n");
                        cellInfoBuilder.append("MCC: ");
                        cellInfoBuilder.append(cellIdentity.getMcc());
                        cellInfoBuilder.append("\n");
                        cellInfoBuilder.append("MNC: ");
                        cellInfoBuilder.append(cellIdentity.getMnc());
                        cellInfoBuilder.append("\n");
                    }
                    // Add more cell identity information as needed
                    // Display cell identity information for LTE network
                    // e.g. cellIdentity.getCi(), cellIdentity.getMcc(), cellIdentity.getMnc(), etc.
                } else if (cellInfo instanceof CellInfoWcdma) {
                    CellIdentityWcdma cellIdentity = ((CellInfoWcdma) cellInfo).getCellIdentity();
                    if (!seenCellTowers.contains(cellIdentity)) {
                        seenCellTowers.add(cellIdentity);
                        cellInfoBuilder.append("Cell Identity (WCDMA): ");
                        cellInfoBuilder.append(cellIdentity.getCid());
                        cellInfoBuilder.append("\n");
                        cellInfoBuilder.append("MCC: ");
                        cellInfoBuilder.append(cellIdentity.getMcc());
                        cellInfoBuilder.append("\n");
                        cellInfoBuilder.append("MNC: ");
                        cellInfoBuilder.append(cellIdentity.getMnc());
                        cellInfoBuilder.append("\n");
                    }
                    // Add more cell identity information as needed
                    // Display cell identity information for WCDMA network
                    // e.g. cellIdentity.getCid(), cellIdentity.getMcc(), cellIdentity.getMnc(), etc.
                }
            }
            allCellInfo = cellInfoBuilder.toString();
        }
        return allCellInfo;
    }

    // todo use data provider here
    @Override
    public void onLocationChanged(@NonNull Location location) {
        txtLat = ma.findViewById(R.id.location_view);
        if (txtLat != null)
            txtLat.setText(String.format("Latitude:%s, Longitude:%s", location.getLatitude(), location.getLongitude()));
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        Log.d(TAG, String.format("%s is disabled", provider));
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        Log.d(TAG, String.format("%s is enabled", provider));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
