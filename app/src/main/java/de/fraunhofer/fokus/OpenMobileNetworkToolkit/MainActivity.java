/*
 *  SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import static de.fraunhofer.fokus.OpenMobileNetworkToolkit.GlobalVars.INVALID_SUBSCRIPTION_ID;
import static de.fraunhofer.fokus.OpenMobileNetworkToolkit.GlobalVars.MULTIPLE_SUBSCRIPTIONS_SELECTED_ID;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.StrictMode;
import android.provider.Settings;
import android.telephony.CarrierConfigManager;
import android.telephony.CellInfo;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.DataProvider;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.NetworkCallback;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.MQTT.MQTTService;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SPType;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SharedPreferencesGrouper;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.WorkProfile.WorkProfileActivity;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.util.DirectExecutor;

public class MainActivity extends AppCompatActivity implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
    private static final String TAG = "MainActivity";
    public TelephonyManager tm;
    private Map<Integer, TelephonyManager> telephonyManagers;
    public PackageManager pm;
    public DataProvider dp;
    public SharedPreferencesGrouper spg;
    public boolean cp = false;
    public boolean feature_telephony = false;
    Intent loggingServiceIntent;
    Intent mqttServiceIntent;
    Intent notificationServiceIntent;
    NavController navController;
    private Handler requestCellInfoUpdateHandler;
    private GlobalVars gv;

    private static final String[] BASE_PERMISSIONS = new String[]{
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
    };

    private String[] finalRequiredPermissions;

    private final ActivityResultLauncher<String[]> requestMultiplePermissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissionsMap -> {

                boolean allGranted = true;

                for (String permission : finalRequiredPermissions) {
                    Boolean isGranted = permissionsMap.get(permission);
                    if (isGranted == null || !isGranted) {
                        allGranted = false;
                    }
                }
                if (allGranted) {
                    init();
                }
            });


    private Context context;

    /**
     * Checks if all permissions in the provided array are currently granted.
     * @param permissions The array of permissions to check.
     * @return True if all permissions are granted, false otherwise.
     */
    private boolean hasAllPermissions(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void buildRequiredPermissionsList() {
        List<String> permissions = new ArrayList<>(Arrays.asList(BASE_PERMISSIONS));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS);
        }

        // Convert the List back to an array
        finalRequiredPermissions = permissions.toArray(new String[0]);
    }

    @SuppressLint("ObsoleteSdkInt")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // check and request permissions from the user
        buildRequiredPermissionsList();
        if (hasAllPermissions(finalRequiredPermissions)) {
            init();
        } else {
            requestMultiplePermissionsLauncher.launch(finalRequiredPermissions);
        }
    }

    void init() {
        // initialize variables we need later on
        context = getApplicationContext();
        gv = GlobalVars.getInstance();
        spg = SharedPreferencesGrouper.getInstance(getApplicationContext());
        pm = getPackageManager();
        feature_telephony = pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY);

        // populate global vars we use in other parts of the app.
        gv.setPm(pm);
        gv.setPermission_phone_state(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED);
        gv.setPermission_fine_location(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
        gv.setFeature_admin(pm.hasSystemFeature(PackageManager.FEATURE_DEVICE_ADMIN));
        gv.setFeature_work_profile(pm.hasSystemFeature(PackageManager.FEATURE_MANAGED_USERS));
        gv.setFeature_telephony(feature_telephony);

        // initialize android UX related thing the app needs
        WindowCompat.setDecorFitsSystemWindows(this.getWindow(), false);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        gv.setLog_status(findViewById(R.id.log_status_icon));

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainerView);
        navController = Objects.requireNonNull(navHostFragment).getNavController();

        // create notification channel
        CharSequence name = getString(R.string.channel_name);
        String description = getString(R.string.channel_description);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel("OMNT_notification_channel", name, importance);
        channel.setDescription(description);
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        // allow HTTP / insecure connections for the influxDB client
        // todo this should be a setting in the settings dialog
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        if (feature_telephony) {
            tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            gv.setTm(tm);
            dp = new DataProvider(this);
            updateTelephonyManagers();
        }
        //todo this will go very wrong on android devices without telephony api, maybe show warning and exit?

        gv.set_dp(dp);

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            // if the location API on android is disabled and we don't want a fake location make a popup
            if (!lm.isLocationEnabled() && !spg.getSharedPreference(SPType.LOGGING).getBoolean("fake_location", false)) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.dialog_no_location_title)
                        .setMessage(R.string.dialog_no_location)
                        .setPositiveButton(R.string.dialog_no_location_enable, (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            getApplicationContext().startActivity(intent);
                        })
                        .setNegativeButton(R.string.dialog_no_location_fake, (dialog, which) -> spg.getSharedPreference(SPType.LOGGING).edit().putBoolean("fake_location", true).apply())
                        .setIcon(android.R.drawable.ic_dialog_map)
                        .show();
            }
        }
        initHandlerAndHandlerThread();

        loggingServiceIntent = new Intent(this, LoggingService.class);
        if (spg.getSharedPreference(SPType.LOGGING).getBoolean("enable_logging", false)) {
            Log.d(TAG, "Start logging service");
            context.startForegroundService(loggingServiceIntent);
        }

        spg.setListener((prefs, key) -> {
            if (Objects.equals(key, "enable_logging")) {
                if (prefs.getBoolean(key, false)) {
                    Log.i(TAG, "Start logging service");
                    context.startForegroundService(loggingServiceIntent);
                } else {
                    Log.i(TAG, "Stop logging service");
                    context.stopService(loggingServiceIntent);
                }
            }
        }, SPType.LOGGING);

        notificationServiceIntent = new Intent(context, NotificationService.class);
        if(spg.getSharedPreference(SPType.MAIN).getBoolean("enable_radio_notification", false)){
            context.startService(notificationServiceIntent);
        }
        spg.setListener((prefs, key) -> {
            if(Objects.equals(key, "enable_radio_notification")){
                if(prefs.getBoolean(key, false)){
                    context.startService(notificationServiceIntent);
                } else {
                    context.stopService(notificationServiceIntent);
                }
            }
        }, SPType.MAIN);

        mqttServiceIntent = new Intent(this, MQTTService.class);
        if (spg.getSharedPreference(SPType.MQTT).getBoolean("enable_mqtt", false)) {
            Log.d(TAG, "Start MQTT service");
            context.startService(mqttServiceIntent);
        }

        spg.setListener((prefs, key) -> {
            if (Objects.equals(key, "enable_mqtt")) {
                if (prefs.getBoolean(key, false)) {
                    Log.d(TAG, "MQTT enabled");
                    context.startForegroundService(mqttServiceIntent);
                } else {
                    Log.d(TAG, "MQTT disabled");
                    context.stopService(mqttServiceIntent);
                }
            }
        }, SPType.MQTT);

        Intent intent = getIntent();
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            String mqtt_broker_address = intent.getStringExtra("mqtt_broker_address");
            String device_name = intent.getStringExtra("device_name");
            if(device_name != null){
                spg.getSharedPreference(SPType.MAIN).edit()
                        .putString("device_name", device_name)
                        .apply();
            }
            if(mqtt_broker_address != null){
                spg.getSharedPreference(SPType.MQTT).edit()
                        .putBoolean("enable_mqtt", true)
                        .putString("mqtt_host", mqtt_broker_address)
                        .apply();
                context.startForegroundService(mqttServiceIntent);
            }


        }
        String target = getIntent().getStringExtra("navigateToFragment");
        if (target != null && target.equals("PingFragment")) {
            navController.navigate(R.id.ping_fragment);
        }
        getAppSignature();
        gv.setGit_hash(getString(R.string.git_hash));
        dp.refreshAll();
    }

    private int getSelectedSubId() {
        return Integer.parseInt(spg.getSharedPreference(SPType.MAIN).getString(
                "select_subscription",
                String.valueOf(GlobalVars.MULTIPLE_SUBSCRIPTIONS_SELECTED_ID)
        ));
    }

    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    private void handleMultipleSubscription(List<SubscriptionInfo> subscriptions) {
        SubscriptionManager sm = (SubscriptionManager) getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
        for (SubscriptionInfo info : subscriptions) {
            int subId = info.getSubscriptionId();
            telephonyManagers.put(subId, tm.createForSubscriptionId(subId));

        }
        for (int i = 0; i <= 1; i++) {
            // tm still required for carrier screen, reboot modem, special numbers for dial.
            // Made it compatible to try and use first subscription in the list.
            SubscriptionInfo subInfo = sm.getActiveSubscriptionInfoForSimSlotIndex(i);
            if (subInfo == null) {
                continue;
            }
            int subId = subInfo.getSubscriptionId();
            if (subId != INVALID_SUBSCRIPTION_ID) {
                tm = tm.createForSubscriptionId(subId);
                break;
            }
        }
    }

    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    private void handleSingleSubscription(int selectedSubId, List<SubscriptionInfo> subscriptions) {
        boolean isValid = subscriptions.stream()
                .anyMatch(info -> info.getSubscriptionId() == selectedSubId);
        // backup mechanism to find first subscription from the list if selected subId is not valid
        if (!isValid) {
            subscriptions.stream()
                    .findFirst() // Get the first element as an Optional
                    .ifPresent(firstInfo -> {
                                int fallbackId = firstInfo.getSubscriptionId();
                                tm = tm.createForSubscriptionId(fallbackId);
                                telephonyManagers.put(fallbackId, tm.createForSubscriptionId(fallbackId));
                                spg.getSharedPreference(SPType.MAIN).edit()
                                        .putString("select_subscription", String.valueOf(fallbackId))
                                        .apply();
                            }
                    );
        } else {
            tm = tm.createForSubscriptionId(selectedSubId);
            telephonyManagers.put(selectedSubId, tm.createForSubscriptionId(selectedSubId));
        }
    }

    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    private void updateTelephonyManagers() {
        List<SubscriptionInfo> subscriptions = dp.getSubscriptions();
        telephonyManagers = new HashMap<>();

        if (subscriptions.isEmpty()) return;

        // make sure the subscription in the app settings exists in the current subscription list.
        // if it is not in the subscription list change it to the first one of the current list
        int selectedSubId = getSelectedSubId();
        if(selectedSubId == MULTIPLE_SUBSCRIPTIONS_SELECTED_ID) {
            handleMultipleSubscription(subscriptions);
        } else {
            handleSingleSubscription(selectedSubId, subscriptions);
        }
        // update reference to tm
        gv.setTm(tm);
        dp.syncTelephonyManager();
        SubscriptionManager sm = (SubscriptionManager) getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
        gv.setSm(sm);

        setCarrierConfig();
    }

    private void setCarrierConfig() {
        cp = tm.hasCarrierPrivileges();
        gv.setCarrier_permissions(cp);
        if (cp) {
            gv.setCcm((CarrierConfigManager) getSystemService(Context.CARRIER_CONFIG_SERVICE));
        }
    }

    /**
     * Get app signature and populate it in global vars
     */
    private void getAppSignature() {
        PackageInfo info;
        try {
            info = pm.getPackageInfo("de.fraunhofer.fokus.OpenMobileNetworkToolkit", PackageManager.GET_SIGNING_CERTIFICATES);
            assert info.signingInfo != null;
            Log.d(TAG, "Apk hash: " + info.signingInfo.getApkContentsSigners().length);
            for (Signature signature : info.signingInfo.getApkContentsSigners()) {
                MessageDigest md;
                md = MessageDigest.getInstance("SHA256");
                md.update(signature.toByteArray());
                String hash = new String(Base64.encode(md.digest(), 0));
                gv.setSigning_hash(hash);
                Log.d(TAG, "Signature: " + toHexString(md.digest()));
            }
        } catch (PackageManager.NameNotFoundException e1) {
            Log.e("name not found", e1.toString());
        } catch (Exception e) {
            Log.e("exception", e.toString());
        }
    }

    /**
     * Helper function to parse hex value to a string
     * @param bytes hex string
     * @return String of hex
     */
    public static String toHexString(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte aByte : bytes) {
            String hex = Integer.toHexString(0xFF & aByte);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }



    /**
     * @param i      The request code passed in by the callback
     * @param strArr The requested permissions. Never null.
     * @param iArr   The grant results for the corresponding permissions
     *               which is either {@link PackageManager#PERMISSION_GRANTED}
     *               or {@link PackageManager#PERMISSION_DENIED}. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int i, @NonNull String[] strArr, @NonNull int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);

        for (int j = 0; j < strArr.length; j = j + 1) {
            Log.d(TAG, "Permission Request Result with ID: " + i + " for " + strArr[j] + " is: " + iArr[j]);
            // we need to request background location after we got foreground.
            if (Objects.equals(strArr[j], "android.permission.ACCESS_FINE_LOCATION") && iArr[j] == 0) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Requesting ACCESS_BACKGROUND_LOCATION Permission");
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 3);
                } else {
                    Log.d(TAG, "Got ACCESS_BACKGROUND_LOCATION Permission");
                }
            }
        }
    }

    /**
     * Inflate the menu; this adds items to the action bar if it is present.
     *
     * @param menu reference to the menu we want to inflate
     * @return boolean as defined by super class
     */
    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        // All other menus show icons, why leave the overflow menu out? It should match.
        if (menu instanceof MenuBuilder) {
            ((MenuBuilder) menu).setOptionalIconsVisible(true);
        }
        return true;
    }

    /**
     * Get the component name
     *
     * @param context the current context
     * @return Component Name
     */
    public ComponentName getComponentName(Context context) {
        return new ComponentName(context.getApplicationContext(), NetworkCallback.class);
    }

    /**
     * Set device admin
     *
     * @param context the current context
     */
    public void getOrganization(Context context) {
        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        PackageManager pm = context.getPackageManager();
        ComponentName componentName = getComponentName(context);
        if (dpm != null) {
            if (pm != null) {
                Log.d(TAG, "isProfileOwnerApp:" + dpm.isProfileOwnerApp(context.getPackageName()));
                Log.d(TAG, "isDeviceOwnerApp:" + dpm.isDeviceOwnerApp(context.getPackageName()));
                Log.d(TAG, "Component Name: " + componentName);
            }
        }
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                getString(R.string.device_admin_description));
        startActivity(intent);
        Log.d(TAG, "Is admin active: " + Objects.requireNonNull(dpm).isAdminActive(componentName));
    }

    /**
     * Handle menu buttons
     *
     * @param item the selected menu item
     * @return weather the select was successful or not
     */
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                navController.navigate(R.id.about_fragment);
                break;
            case R.id.slicingSetup:
                navController.navigate(R.id.fragment_slicingsetup);
                break;
            case R.id.iperf3:
                navController.navigate(R.id.fragment_iperf3_input);
                break;
            case R.id.settings:
                navController.navigate(R.id.settingsFragment);
                break;
            case R.id.workprofilemanagement:
                Intent work_profile = new Intent(this, WorkProfileActivity.class);
                startActivity(work_profile);
            case R.id.special_codes:
                navController.navigate(R.id.specialCodesFragment);
                break;
            case R.id.subscriptions:
                navController.navigate(R.id.subscriptionsFragment);
                break;
            case R.id.ping:
                navController.navigate(R.id.ping_fragment);
                break;
            case R.id.carrier_settings_button:
                navController.navigate(R.id.carrierSettingsFragment);
                break;
            case R.id.btn_exit:
                this.finish();
                System.exit(0);
            case R.id.btn_home:
                navController.navigate(R.id.HomeFragment);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * tiny navigation helper
     *
     * @return always true
     */
    @Override
    public boolean onSupportNavigateUp() {
        navController.navigate(R.id.HomeFragment);
        return true;
    }

    /**
     * Handle settings navigation
     *
     * @param caller The fragment requesting navigation
     * @param pref   The preference requesting the fragment
     * @return always true
     */
    @Override
    public boolean onPreferenceStartFragment(@NonNull PreferenceFragmentCompat caller, Preference pref) {
        // Instantiate the new Fragment
        Log.d(TAG, "onPreferenceStartFragment: " + pref.getKey());
        switch (pref.getKey()) {
            case "log_settings":
                navController.navigate(R.id.loggingSettingsFragment);
                break;
            case "mobile_network_settings":
                navController.navigate(R.id.flagSettingFragment);
                break;
            case "shared_preferences_io":
                navController.navigate(R.id.fragment_shared_preferences_io);
                break;
            case "mqtt_settings":
                navController.navigate(R.id.mqttSettingsFragment);
                break;
        }
        return true;
    }

    @SuppressLint("MissingPermission") // we check them already in the Main activity
    private void syncTelephonyManagers() {
        int selectedSubId = getSelectedSubId();
        Set<Integer> currentSubIds = telephonyManagers.keySet();
        int totalManagers = currentSubIds.size();
        boolean needsUpdate = (selectedSubId != MULTIPLE_SUBSCRIPTIONS_SELECTED_ID && totalManagers > 1) ||
                (totalManagers == 1 && currentSubIds.stream().anyMatch(sub -> sub != selectedSubId));

        if (needsUpdate) {
            updateTelephonyManagers();
        }
    }

    @SuppressLint("MissingPermission") // we check them already in the Main activity
    private void sendCellInfo(Map<Integer, List<CellInfo>> cellInfoBySubId) {
        List<CellInfo> mergedList = cellInfoBySubId.keySet().stream()
                .flatMap(subId -> {
                    List<CellInfo> subList = cellInfoBySubId.get(subId);
                    return subList != null ? subList.stream() : Stream.empty();
                })
                .collect(Collectors.toList());
//        // Post the final list to the main thread using a lambda
        new Handler(context.getMainLooper()).post(() -> dp.onCellInfoChanged(mergedList));
    }

    @SuppressLint("MissingPermission") // we check them already in the Main activity
    private void requestCellInfoUpdateFromManagers() {
        final Set<Integer> subIds = telephonyManagers.keySet();
        final int totalManagers = subIds.size();

        // Concurrent map to safely collect results from multiple threads/callbacks
        final Map<Integer, List<CellInfo>> cellInfoBySubId = new ConcurrentHashMap<>();
        final AtomicInteger completedRequests = new AtomicInteger(0);

        for (int subId : subIds) {

            TelephonyManager telephonyManager = telephonyManagers.get(subId);
            if (telephonyManager == null) {
                throw new IllegalStateException("No TelephonyManager found for subId: " + subId);
            }

            telephonyManager.requestCellInfoUpdate(new DirectExecutor(), new TelephonyManager.CellInfoCallback() {
                @Override
                public void onCellInfo(@NonNull List<CellInfo> list) {
                    cellInfoBySubId.put(subId, list);

                    if (completedRequests.incrementAndGet() == totalManagers) {
                        sendCellInfo(cellInfoBySubId);
                    }
                }
            });
        }
    }

    /**
     * Runnable to handle Cell Info Updates
     */
    private final Runnable requestCellInfoUpdate = new Runnable() {
        @SuppressLint("MissingPermission") // we check them already in the Main activity
        @Override
        public void run() {
            if (!gv.isPermission_fine_location()) {
                scheduleUpdate();
                return;
            }
            syncTelephonyManagers();

            if (!telephonyManagers.isEmpty()) {
                requestCellInfoUpdateFromManagers();
            }

            scheduleUpdate();
        }


        private void scheduleUpdate() {
            requestCellInfoUpdateHandler.postDelayed(this, Integer.parseInt(spg.getSharedPreference(SPType.LOGGING).getString("logging_interval", "1000")));
        }

    };

    private void initHandlerAndHandlerThread() {
        HandlerThread requestCellInfoUpdateHandlerThread = new HandlerThread("RequestCellInfoUpdateHandlerThread");
        requestCellInfoUpdateHandlerThread.start();
        requestCellInfoUpdateHandler = new Handler(Objects.requireNonNull(requestCellInfoUpdateHandlerThread.getLooper()));
        requestCellInfoUpdateHandler.post(requestCellInfoUpdate);
    }
}