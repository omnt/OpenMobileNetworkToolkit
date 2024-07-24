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
import android.os.Looper;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.DataProvider;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.NetworkCallback;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Ping.PingFragment;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SPType;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SharedPreferencesGrouper;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.WorkProfile.WorkProfileActivity;

public class MainActivity extends AppCompatActivity implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
    private static final String TAG = "MainActivity";
    public TelephonyManager tm;
    public PackageManager pm;
    public DataProvider dp;
    public SharedPreferencesGrouper spg;
    public boolean cp = false;
    public boolean feature_telephony = false;
    Intent loggingServiceIntent;
    NavController navController;
    private Handler requestCellInfoUpdateHandler;
    private GlobalVars gv;
    /**
     * Runnable to handle Cell Info Updates
     */
    private final Runnable requestCellInfoUpdate = new Runnable() {
        @SuppressLint("MissingPermission") // we check them already in the Main activity
        @Override
        public void run() {
            if (gv.isPermission_fine_location()) {
                tm.requestCellInfoUpdate(Executors.newSingleThreadExecutor(), new TelephonyManager.CellInfoCallback() {
                    @Override
                    public void onCellInfo(@NonNull List<CellInfo> list) {
                        dp.onCellInfoChanged(list);
                    }
                });
            }
            requestCellInfoUpdateHandler.postDelayed(this, Integer.parseInt(spg.getSharedPreference(SPType.logging_sp).getString("logging_interval", "1000")));
        }
    };
    private Context context;

    @SuppressLint("ObsoleteSdkInt")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // check and request permissions from the user
        requestPermission();

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
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !dp.getSubscriptions().isEmpty()) {
                // make sure the subscription in the app settings exists in the current subscription list.
                // if it is not in the subscription list change it to the first one of the current list
                boolean valid_subscription = false;
                String pref_subscription_str = spg.getSharedPreference(SPType.mobile_network_sp).getString("select_subscription","99999");
                for (SubscriptionInfo info : dp.getSubscriptions()) {
                    if (Integer.parseInt(pref_subscription_str) == info.getSubscriptionId()) {
                        valid_subscription = true;
                    }
                }
                if (!valid_subscription) {
                    spg.getSharedPreference(SPType.mobile_network_sp).edit().putString("select_subscription", String.valueOf(dp.getSubscriptions().iterator().next().getSubscriptionId())).apply();
                }
                // switch the telephony manager to a new one according to the app settings
                tm = tm.createForSubscriptionId(Integer.parseInt(spg.getSharedPreference(SPType.mobile_network_sp).getString("select_subscription", "0")));
            }

            gv.setSm((SubscriptionManager) getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE));
            cp = tm.hasCarrierPrivileges();
            gv.setCarrier_permissions(cp);
            if (cp) {
                gv.setCcm((CarrierConfigManager) getSystemService(Context.CARRIER_CONFIG_SERVICE));
            }
        } //todo this will go very wrong on android devices without telephony api, maybe show warning and exit?

        gv.set_dp(dp);

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            // if the location API on android is disabled and we don't want a fake location make a popup
            if (!lm.isLocationEnabled() && !spg.getSharedPreference(SPType.logging_sp).getBoolean("fake_location", false)) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.dialog_no_location_title)
                        .setMessage(R.string.dialog_no_location)
                        .setPositiveButton(R.string.dialog_no_location_enable, (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            getApplicationContext().startActivity(intent);
                        })
                        .setNegativeButton(R.string.dialog_no_location_fake, (dialog, which) -> spg.getSharedPreference(SPType.logging_sp).edit().putBoolean("fake_location", true).apply())
                        .setIcon(android.R.drawable.ic_dialog_map)
                        .show();
            }
        }

        requestCellInfoUpdateHandler = new Handler(Objects.requireNonNull(Looper.myLooper()));
        requestCellInfoUpdateHandler.post(requestCellInfoUpdate);

        loggingServiceIntent = new Intent(this, LoggingService.class);
        if (spg.getSharedPreference(SPType.logging_sp).getBoolean("enable_logging", false)) {
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
        }, SPType.logging_sp);
        getAppSignature();
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
     * Check and request permission the app needs to access APIs and so on
     */
    private void requestPermission() {
        List<String> permissions = new ArrayList<>();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Requesting READ_PHONE_STATE Permission");
            permissions.add(Manifest.permission.READ_PHONE_STATE);
        } else {
            Log.d(TAG, "Got READ_PHONE_STATE Permission");
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Requesting COARSE_LOCATION Permission");
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        } else {
            Log.d(TAG, "Got COARSE_LOCATION_LOCATION Permission");
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Requesting FINE_LOCATION Permission");
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            Log.d(TAG, "Got FINE_LOCATION Permission");
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Requesting WIFI_STATE Permission");
            permissions.add(Manifest.permission.ACCESS_WIFI_STATE);
        } else {
            Log.d(TAG, "Got WIFI_STATE Permission");
        }

        // on android 13 an newer we need to ask for permission to show the notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Requesting POST_NOTIFICATIONS Permission");
                permissions.add(Manifest.permission.POST_NOTIFICATIONS);
            } else {
                Log.d(TAG, "Got POST_NOTIFICATIONS Permission");
            }
        }

        if (!permissions.isEmpty()) {
            String[] perms = permissions.toArray(new String[0]);
            ActivityCompat.requestPermissions(this, perms, 1337);
        }
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
            case R.id.influxDBMenu:
                navController.navigate(R.id.influxDBFragment);
                break;
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
            case "app_settings":
                navController.navigate(R.id.applicationSettingsFragment);
                break;
            case "log_settings":
                navController.navigate(R.id.loggingSettingsFragment);
                break;
            case "mobile_network_settings":
                navController.navigate(R.id.flagSettingFragment);
                break;
            case "shared_preferences_io":
                navController.navigate(R.id.fragment_shared_preferences_io);
                break;
        }
        return true;
    }
}