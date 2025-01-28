
/*
 *  SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Network;
import android.os.Build;
import android.os.Bundle;
import android.telephony.CellInfo;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.divider.MaterialDivider;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.CellInformations.CDMAInformation;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.CellInformations.CellInformation;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.CellInformations.GSMInformation;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.CellInformations.LTEInformation;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.CellInformations.NRInformation;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.DataProvider;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.DeviceInformation;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.LocationInformation;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.NetworkCallback;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.NetworkInformation;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.NetworkInterfaceInformation;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.WifiInformation;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SPType;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SharedPreferencesGrouper;


public class DetailFragment extends Fragment {
    public TelephonyManager tm;
    public PackageManager pm;
    DataProvider dp;
    Context context;
    private boolean cp;
    private GlobalVars gv;
    private SwipeRefreshLayout swipeRefreshLayout;
    private SharedPreferencesGrouper spg;

    public DetailFragment() {
        super(R.layout.fragment_home);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = requireContext();
        spg = SharedPreferencesGrouper.getInstance(context);
        Thread.setDefaultUncaughtExceptionHandler(new GlobalExceptionHandler());
    }

    @SuppressLint("MissingPermission")
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup parent,
            Bundle savedInstanceState
    ) {
        gv = GlobalVars.getInstance();
        pm = gv.getPm();
        dp = gv.get_dp();
        if (gv.isFeature_telephony()) {
            cp = gv.isCarrier_permissions();
            tm = gv.getTm();
        }

        View view = inflater.inflate(R.layout.fragment_detail, parent, false);

        swipeRefreshLayout = view.findViewById(R.id.detail_swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            LinearLayout ll = requireView().findViewById(R.id.detail_layout);
            ll.removeAllViews();
            dp.refreshAll();
            ll.addView(get_cell_card_view(), 0);
            ll.addView(get_signal_strength_card_view(), 1);
            ll.addView(get_wifi_card_view(), 2);
            ll.addView(get_network_card_view(), 3);
            ll.addView(get_device_card_view(), 4);
            ll.addView(get_features_card_view(), 5);
            ll.addView(get_permissions_card_view(), 6);
            ll.addView(get_interfaces_card_view(), 7);
            ll.addView(get_location_card_view(), 8);
            swipeRefreshLayout.setRefreshing(false);
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dp.refreshAll();
        LinearLayout ll = requireView().findViewById(R.id.detail_layout);
        ll.addView(get_cell_card_view(), 0);
        ll.addView(get_signal_strength_card_view(), 1);
        ll.addView(get_wifi_card_view(), 2);
        ll.addView(get_network_card_view(), 3);
        ll.addView(get_device_card_view(), 4);
        ll.addView(get_features_card_view(), 5);
        ll.addView(get_permissions_card_view(), 6);
        ll.addView(get_interfaces_card_view(), 7);
        ll.addView(get_location_card_view(), 8);
    }


    /**
     * Build a card view with a table layout
     *
     * @param title title of the card
     * @param tl    table layout
     * @return card view
     */
    private CardView cardView_from_table_builder(String title, TableLayout tl) {
        // setup card view
        CardView cv = new CardView(context);
        //CardView cv = findViewById(R.id.base_cardview);
        cv.setRadius(15);
        cv.setContentPadding(20, 20, 20, 20);
        cv.setUseCompatPadding(true);

        // setup button
        int id = View.generateViewId();
        ImageButton btn = getImageButton(id);

        // setup header
        TextView title_text = new TextView(context);
        title_text.setTypeface(null, Typeface.BOLD);
        title_text.setText(title);
        title_text.setPadding(0, 0, 0, 20);
        title_text.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f
        ));
        LinearLayout header = new LinearLayout(context);
        header.setOrientation(LinearLayout.HORIZONTAL);


        header.addView(title_text);
        header.addView(btn);

        // setup body
        tl.setId(id);
        tl.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams
                .MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        tl.setStretchAllColumns(true);

        // setup card content
        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.addView(header);
        ll.addView(tl);
        cv.addView(ll);
        return cv;
    }

    /**
     * Build the collapse buttons for the home screen
     *
     * @param id id of the button
     * @return button
     */
    private @NonNull ImageButton getImageButton(int id) {
        ImageButton btn = new ImageButton(context);
        btn.setImageResource(R.drawable.baseline_expand_less_24);
        btn.setBackground(null);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.MATCH_PARENT);

        btn.setLayoutParams(lp);
        btn.setOnClickListener(v -> {
            TableLayout tl = requireView().findViewById(id);
            if (tl.getVisibility() == View.VISIBLE) {
                tl.setVisibility(View.GONE);
                btn.setImageResource(R.drawable.baseline_expand_more_24);
            } else {
                tl.setVisibility(View.VISIBLE);
                btn.setImageResource(R.drawable.baseline_expand_less_24);
            }

        });
        return btn;
    }

    /**
     * Build a row for the table
     *
     * @param column1 first column
     * @param column2 second column
     * @return TableRow
     */
    private TableRow rowBuilder(String column1, String column2) {
        if (Objects.equals(column2, String.valueOf(CellInfo.UNAVAILABLE))) {
            column2 = "N/A";
        }
        TableRow tr = new TableRow(context);
        tr.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT,
                1.0f));
        tr.setPadding(2, 2, 2, 2);

        MaterialTextView tv1 = new MaterialTextView(context);
        TableRow.LayoutParams tv1_params = new TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.WRAP_CONTENT, 0.4f);
        tv1.setLayoutParams(tv1_params);
        tv1.setPadding(20, 0, 20, 0);
        tv1.append(column1);
        tv1.setTextIsSelectable(true);
        tr.addView(tv1);

        MaterialTextView tv2 = new MaterialTextView(context);
        TableRow.LayoutParams tv2_params = new TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.WRAP_CONTENT, 0.6f);
        tv2.setLayoutParams(tv2_params);
        tv2.setPadding(0, 0, 0, 0);
        tv2.append(Objects.requireNonNullElse(column2, "N/A"));
        tv2.setTextIsSelectable(true);
        tr.addView(tv2);
        return tr;
    }

    @SuppressLint({"MissingPermission", "HardwareIds"})
    private CardView get_device_card_view() {
        DeviceInformation di = dp.getDeviceInformation();
        TableLayout tl = new TableLayout(context);
        addRows(tl, new String[][]{
                {getString(R.string.model), String.valueOf(di.getModel())},
                {getString(R.string.manufacturer), String.valueOf(di.getManufacturer())},
                {getString(R.string.socManufacturer), String.valueOf(di.getSOCManufacturer())},
                {getString(R.string.socModel), String.valueOf(di.getSOCModel())},
                {getString(R.string.radioVersion), String.valueOf(di.getRadioVersion())},
                {getString(R.string.supportedModemCount), String.valueOf(di.getSupportedModemCount())},
                {getString(R.string.androidSDK), String.valueOf(di.getAndroidSDK())},
                {getString(R.string.androidRelease), String.valueOf(di.getAndroidRelease())},
                {getString(R.string.deviceSoftwareVersion), String.valueOf(di.getDeviceSoftwareVersion())},
                {getString(R.string.securityPatchLevel), String.valueOf(di.getSecurityPatchLevel())},
                {getString(R.string.IMEI), String.valueOf(di.getIMEI())},
                {getString(R.string.MEID), String.valueOf(di.getMEID())},
                {getString(R.string.IMSI), String.valueOf(di.getIMSI())},
                {getString(R.string.simSerial), String.valueOf(di.getSimSerial())},
                {getString(R.string.subscriptionId), String.valueOf(di.getSubscriberId())},
                {getString(R.string.networkAccessIdentifier), String.valueOf(di.getNetworkAccessIdentifier())},
                {getString(R.string.subscriptionId), String.valueOf(di.getSubscriptionId())},
        }, true);



        return cardView_from_table_builder("Device Information", tl);
    }
    private void addRows(TableLayout tl, String[][] rows, boolean displayNull) {
        for (String[] row : rows) {
            if(!displayNull && (row[1].equals("N/A")
                    || row[1].equals("null")
                    || row[1].equals("0")
                    || row[1].equals("false"))
            ) continue;
            tl.addView(rowBuilder(row[0], row[1]));
        }
    }

    /**
     * Add a divider to the table
     *
     * @param tl table layout
     */
    public void addDivider(TableLayout tl) {
        MaterialDivider divider = new MaterialDivider(context);
        divider.setDividerThickness(3);
        divider.setDividerInsetStart(20);
        divider.setDividerInsetEnd(20);
        tl.addView(divider);
    }

    /**
     * Populate the table with the cell information
     * also used for the signal strength information
     *
     * @param signalStrength signal strength information
     * @param tl             table layout
     * @param displayNull    display null values
     */
    private void populateCellTable(CellInformation signalStrength, TableLayout tl, boolean displayNull){
        switch (signalStrength.getCellType()){
            case NR:
                NRInformation nr = (NRInformation) signalStrength;
                addRows(tl, new String[][]{
                        {getString(R.string.alphaLong), String.valueOf(nr.getAlphaLong())},
                        {getString(R.string.mcc), String.valueOf(nr.getMcc())},
                        {getString(R.string.mnc), String.valueOf(nr.getMnc())},
                        {getString(R.string.cellType), String.valueOf(nr.getCellType())},
                        {getString(R.string.pci), String.valueOf(nr.getPci())},
                        {getString(R.string.tac), String.valueOf(nr.getTac())},
                        {getString(R.string.ci), String.valueOf(nr.getCi())},
                        {getString(R.string.isRegistered), String.valueOf(nr.isRegistered())},
                        {getString(R.string.cellConnectionStatus), String.valueOf(nr.getCellConnectionStatus())},
                }, displayNull);
                addDivider(tl);
                addRows(tl, new String[][]{
                        {getString(R.string.bands), String.valueOf(nr.getBands())},
                        {getString(R.string.nrarfcn), String.valueOf(nr.getNrarfcn())},
                        {getString(R.string.lac), String.valueOf(nr.getTac())},
                        {getString(R.string.timingAdvance), String.valueOf(nr.getTimingAdvance())},
                }, displayNull);
                addDivider(tl);
                addRows(tl, new String[][]{
                        {getString(R.string.dbm), String.valueOf(nr.getDbm())},
                        {getString(R.string.level), String.valueOf(nr.getLevel())},
                        {getString(R.string.asuLevel), String.valueOf(nr.getAsuLevel())},
                        {getString(R.string.csirsrp), String.valueOf(nr.getCsirsrp())},
                        {getString(R.string.csirsrq), String.valueOf(nr.getCsirsrq())},
                        {getString(R.string.csisinr), String.valueOf(nr.getCsisinr())},
                        {getString(R.string.cqi), String.valueOf(nr.getCqis())}
                }, displayNull);
                addDivider(tl);
                addRows(tl, new String[][]{
                        {getString(R.string.ssrsrp), String.valueOf(nr.getSsrsrp())},
                        {getString(R.string.ssrsrq), String.valueOf(nr.getSsrsrq())},
                        {getString(R.string.sssinr), String.valueOf(nr.getSssinr())},
                }, displayNull);
                break;
            case GSM:
                GSMInformation gsm = (GSMInformation) signalStrength;
                addRows(tl, new String[][]{
                        {getString(R.string.alphaLong), String.valueOf(gsm.getAlphaLong())},
                        {getString(R.string.mcc), String.valueOf(gsm.getMcc())},
                        {getString(R.string.mnc), String.valueOf(gsm.getMnc())},
                        {getString(R.string.cellType), String.valueOf(gsm.getCellType())},
                        {getString(R.string.ci), String.valueOf(gsm.getCi())},
                        {getString(R.string.isRegistered), String.valueOf(gsm.isRegistered())},
                        {getString(R.string.cellConnectionStatus), String.valueOf(gsm.getCellConnectionStatus())},
                }, displayNull);
                addDivider(tl);
                addRows(tl, new String[][]{
                        {getString(R.string.bands), String.valueOf(gsm.getBands())},
                        {getString(R.string.lac), String.valueOf(gsm.getLac())},
                        {getString(R.string.timingAdvance), String.valueOf(gsm.getTimingAdvance())},
                }, displayNull);

                addDivider(tl);
                addRows(tl, new String[][]{
                        {getString(R.string.dbm), String.valueOf(gsm.getDbm())},
                        {getString(R.string.level), String.valueOf(gsm.getLevel())},
                        {getString(R.string.asuLevel), String.valueOf(gsm.getAsuLevel())},
                        {getString(R.string.bitErrorRate), String.valueOf(gsm.getBitErrorRate())},
                        {getString(R.string.rssi), String.valueOf(gsm.getRssi())},
                }, displayNull);
                break;
            case LTE:
                LTEInformation lte = (LTEInformation) signalStrength;
                addRows(tl, new String[][]{
                        {getString(R.string.alphaLong), String.valueOf(lte.getAlphaLong())},
                        {getString(R.string.mcc), String.valueOf(lte.getMcc())},
                        {getString(R.string.mnc), String.valueOf(lte.getMnc())},
                        {getString(R.string.cellType), String.valueOf(lte.getCellType())},
                        {getString(R.string.pci), String.valueOf(lte.getPci())},
                        {getString(R.string.tac), String.valueOf(lte.getTac())},
                        {getString(R.string.ci), String.valueOf(lte.getCi())},
                        {getString(R.string.isRegistered), String.valueOf(lte.isRegistered())},
                        {getString(R.string.cellConnectionStatus), String.valueOf(lte.getCellConnectionStatus())},
                }, displayNull);

                addDivider(tl);

                addRows(tl, new String[][]{
                        {getString(R.string.bands), String.valueOf(lte.getBands())},
                        {getString(R.string.earfcn), String.valueOf(lte.getEarfcn())},
                        {getString(R.string.bandwidth), String.valueOf(lte.getBandwidth())},
                        {getString(R.string.timingAdvance), String.valueOf(lte.getTimingAdvance())},
                }, displayNull);

                addDivider(tl);

                addRows(tl, new String[][]{
                        {getString(R.string.level), String.valueOf(lte.getLevel())},
                        {getString(R.string.asuLevel), String.valueOf(lte.getAsuLevel())},
                        {getString(R.string.rsrp), String.valueOf(lte.getRsrp())},
                        {getString(R.string.rsrq), String.valueOf(lte.getRsrq())},
                        {getString(R.string.cqi), String.valueOf(lte.getCqi())}
                }, displayNull);

                addDivider(tl);

                addRows(tl, new String[][]{
                        {getString(R.string.rssi), String.valueOf(lte.getRssi())},
                        {getString(R.string.rssnr), String.valueOf(lte.getRssnr())},
                }, displayNull);

                break;
            case CDMA:
                CDMAInformation cdma = (CDMAInformation) signalStrength;
                addRows(tl, new String[][]{
                        {getString(R.string.alphaLong), String.valueOf(cdma.getAlphaLong())},
                        {getString(R.string.cellType), String.valueOf(cdma.getCellType())},
                        {getString(R.string.isRegistered), String.valueOf(cdma.isRegistered())},
                        {getString(R.string.cellConnectionStatus), String.valueOf(cdma.getCellConnectionStatus())},
                }, displayNull);

                addDivider(tl);

                addRows(tl, new String[][]{
                        {getString(R.string.cmdaDbm), String.valueOf(cdma.getCmdaDbm())},
                        {getString(R.string.cmdaEcio), String.valueOf(cdma.getCmdaEcio())},
                        {getString(R.string.evdoDbm), String.valueOf(cdma.getEvdoDbm())},
                        {getString(R.string.evdoEcio), String.valueOf(cdma.getEvdoEcio())},
                        {getString(R.string.evdoSnr), String.valueOf(cdma.getEvdoSnr())},
                }, displayNull);

                break;
            case UNKNOWN:
            default:
                addRows(tl, new String[][]{
                        {getString(R.string.alphaLong), String.valueOf(signalStrength.getAlphaLong())},
                        {getString(R.string.cellType), String.valueOf(signalStrength.getCellType())},
                        {getString(R.string.isRegistered), String.valueOf(signalStrength.isRegistered())},
                        {getString(R.string.cellConnectionStatus), String.valueOf(signalStrength.getCellConnectionStatus())},
                }, displayNull);
                break;
        }
    }


    @SuppressLint({"MissingPermission", "HardwareIds", "ObsoleteSdkInt"})
    private CardView get_signal_strength_card_view() {
        ArrayList<CellInformation> signalStrengthInformations = dp.getSignalStrengthInformation();
        TableLayout tl = new TableLayout(context);
        if (signalStrengthInformations.isEmpty()) {
            tl.addView(rowBuilder("No Signal Strength available", ""));
        } else {
            int cell = 1;
            for (CellInformation signalStrengthInformation : signalStrengthInformations) {
                if (signalStrengthInformation.getCellType() == null) {
                    continue;
                }
                TableRow title = rowBuilder("Cell " + cell, "");
                if (cell > 1) {
                    title.setPadding(0, 20, 0, 0);
                }
                ++cell;
                TextView tv = (TextView) title.getChildAt(0);
                tv.setTypeface(Typeface.DEFAULT_BOLD);
                tl.addView(title);
                populateCellTable(signalStrengthInformation, tl, false);

            }
        }
        return cardView_from_table_builder("Signal Strength Information", tl);
    }

    @SuppressLint("ObsoleteSdkInt")
    private CardView get_features_card_view() {
        TableLayout tl = new TableLayout(context);
        tl.addView(rowBuilder("Feature Telephony", String.valueOf(gv.isFeature_telephony())));
        tl.addView(rowBuilder("Work Profile", String.valueOf(gv.isFeature_work_profile())));
        tl.addView(rowBuilder("Feature Admin", String.valueOf(gv.isFeature_admin())));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            tl.addView(rowBuilder("Slicing Config supported", String.valueOf(
                    pm.hasSystemFeature(TelephonyManager.CAPABILITY_SLICING_CONFIG_SUPPORTED))));
        }
        return cardView_from_table_builder("Device Features", tl);
    }

    private CardView get_permissions_card_view() {
        TableLayout tl = new TableLayout(context);
        tl.addView(rowBuilder("Carrier Permissions", String.valueOf(cp)));
        tl.addView(rowBuilder("READ_PHONE_STATE", String.valueOf(gv.isPermission_phone_state())));
        tl.addView(rowBuilder("ACCESS_FINE_LOCATION", String.valueOf(ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)));
        tl.addView(rowBuilder("ACCESS_BACKGROUND_LOCATION", String.valueOf(ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED)));
        return cardView_from_table_builder("App Permission", tl);
    }

    private CardView get_location_card_view() {
        TableLayout tl = new TableLayout(context);
        LocationInformation loc = dp.getLocation();
        if (loc != null) {
            addRows(tl, new String[][]{
                    {getString(R.string.longitude), String.valueOf(loc.getLongitude())},
                    {getString(R.string.latitude), String.valueOf(loc.getLatitude())},
                    {getString(R.string.altitude), String.valueOf(loc.getAltitude())},
                    {getString(R.string.accuracy), String.valueOf(loc.getAccuracy())},
                    {getString(R.string.speed), String.valueOf(loc.getSpeed())},
                    {getString(R.string.provider), loc.getProvider()},
            }, true);
        } else {
            tl.addView(rowBuilder("Location not available", ""));
        }
        return cardView_from_table_builder("Location", tl);
    }

    private CardView get_interfaces_card_view() {
        TableLayout tl = new TableLayout(context);
        List<NetworkInterfaceInformation> niil = dp.getNetworkInterfaceInformation();
        for (NetworkInterfaceInformation nii : niil) {
            tl.addView(rowBuilder(nii.getInterfaceName(), nii.getAddress()));
        }
        return cardView_from_table_builder("Network Interfaces", tl);
    }

    @SuppressLint({"MissingPermission", "ObsoleteSdkInt"})
    private CardView get_network_card_view() {
        NetworkInformation ni = dp.getNetworkInformation();
        NetworkCallback nc = new NetworkCallback(context);
        TableLayout tl = new TableLayout(context);

        if (ni != null) {
            addRows(tl, new String[][]{
                    {getString(R.string.networkOperatorName), ni.getNetworkOperatorName()},
                    {getString(R.string.simOperatorName), ni.getSimOperatorName()},
                    {getString(R.string.networkSpecifier), ni.getNetworkSpecifier()},
                    {getString(R.string.dataState), ni.getDataStateString()},
                    {getString(R.string.dataNetworkType), ni.getDataNetworkTypeString()},
                    {getString(R.string.phoneType), ni.getPhoneTypeString()},
                    {getString(R.string.preferredOpportunisticDataSubscriptionId), String.valueOf(ni.getPreferredOpportunisticDataSubscriptionId())},
            }, true);
        } else {
            Toast.makeText(getContext(), "NetworkInformation was not ready!", Toast.LENGTH_SHORT).show();
        }

        if (GlobalVars.getInstance().isPermission_phone_state() && tm.getSimState() == TelephonyManager.SIM_STATE_READY) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                addRows(tl, new String[][]{
                        {"Equivalent Home PLMNs", tm.getEquivalentHomePlmns().toString().replace("[", "").replace("]", "").replace(", ", "\n")},
                        {"Forbidden PLMNs", Arrays.toString(tm.getForbiddenPlmns()).replace("[", "").replace("]", "").replace(", ", "\n")}
                }, true);
            }
        }

        Network network = nc.getCurrentNetwork();
        if (network != null) {
            tl.addView(rowBuilder("Default Network", network.toString()));
        } else {
            tl.addView(rowBuilder("Default Network", "N/A"));
        }

        tl.addView(rowBuilder("Interface Name", nc.getInterfaceName()));
        tl.addView(rowBuilder("Network counter", String.valueOf(GlobalVars.counter)));
        tl.addView(rowBuilder("Default DNS", nc.getDefaultDNS().toString().replace("[", "").replace("]", "").replace(", ", "\n")));
        tl.addView(rowBuilder("Enterprise Capability", String.valueOf(nc.getCapabilityEnterprise())));
        tl.addView(rowBuilder("Validated Capability", String.valueOf(nc.getCapabilityValidity())));
        tl.addView(rowBuilder("Internet Capability", String.valueOf(nc.getCapabilityInternet())));
        tl.addView(rowBuilder("IMS Capability", String.valueOf(nc.getCapabilityIMS())));
        // Network Slicing
        tl.addView(rowBuilder("Slice Capability", String.valueOf(nc.getCapabilitySlicing())));
        tl.addView(rowBuilder("Capabilities", nc.getNetworkCapabilityList()));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            int[] ids = nc.getEnterpriseIds();
            if (ids.length > 0) {
                tl.addView(rowBuilder("Enterprise IDs", Arrays.toString(ids)));
            } else {
                tl.addView(rowBuilder("Enterprise IDs", "N/A"));
            }
        }
        // The following functions do not return useful information jet
        //tl.addView(rowBuilder("TM Slice", String.valueOf(nc.getConfigurationTM())));
        //tl.addView(rowBuilder("Slice Info", String.valueOf(nc.getNetworkSlicingInfo())));
        //tl.addView(rowBuilder("Slice Config", String.valueOf(nc.getNetworkSlicingConfig())));
        // Routing and Traffic
        //tl.addView(rowBuilder("Route Descriptor", String.valueOf(nc.getRouteSelectionDescriptor())));
        //tl.addView(rowBuilder("Traffic Descriptor", String.valueOf(nc.getTrafficDescriptor())));
        return cardView_from_table_builder("Network Information", tl);
    }

    @SuppressLint("ObsoleteSdkInt")
    private CardView get_cell_card_view() {
        TableLayout tl = new TableLayout(context);
        List<CellInformation> cil = dp.getCellInformation();
        int cell = 1;
        for (CellInformation ci : cil) {
            if (!spg.getSharedPreference(SPType.default_sp).getBoolean("show_neighbour_cells", false) && ! ci.isRegistered()) {
                continue;
            }

            TableRow title = rowBuilder("Cell " + cell, "");
            if (cell > 1) {
                title.setPadding(0, 20, 0, 0);
            }
            ++cell;
            TextView tv = (TextView) title.getChildAt(0);
            tv.setTypeface(Typeface.DEFAULT_BOLD);
            tl.addView(title);
            populateCellTable(ci, tl, true);
        }
        if (tl.getChildCount() == 0) {
            tl.addView(rowBuilder("No cells available", ""));
        }
        return cardView_from_table_builder("Cell Information", tl);
    }

    private CardView get_wifi_card_view() {
        TableLayout tl = new TableLayout(context);
        WifiInformation wi = dp.getWifiInformation();
        if (wi != null) {
            addRows(tl, new String[][]{
                    {getString(R.string.ssid), wi.getSsid()},
                    {getString(R.string.bssid), wi.getBssid()},
                    {getString(R.string.rssi), Integer.toString(wi.getRssi())},
                    {getString(R.string.frequency), Integer.toString(wi.getFrequency())},
                    {getString(R.string.link_speed), Integer.toString(wi.getLink_speed())},
                    {getString(R.string.tx_link_speed), Integer.toString(wi.getTx_link_speed())},
                    {getString(R.string.max_tx_link_speed), Integer.toString(wi.getMax_tx_link_speed())},
                    {getString(R.string.rx_link_speed), Integer.toString(wi.getRx_link_speed())},
                    {getString(R.string.max_rx_link_speed), Integer.toString(wi.getMax_rx_link_speed())},
                    {getString(R.string.standard), wi.getStandardString()},
                    {getString(R.string.channel_bandwidth), wi.getChannelBandwithString()},
                    {getString(R.string.wifi_security_type), wi.getSecurityTypeString()}

            }, true);


        } else {
            tl.addView(rowBuilder("No WiFi information available", ""));
        }
        return cardView_from_table_builder("Wifi Information", tl);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
