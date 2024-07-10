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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.provider.Settings;
import android.telephony.CarrierConfigManager;
import android.telephony.CellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

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
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.DataProvider;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.NetworkCallback;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.WorkProfile.WorkProfileActivity;

public class MainActivity extends AppCompatActivity implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
    public TelephonyManager tm;
    public PackageManager pm;
    public DataProvider dp;
    SharedPreferences sp;
    SharedPreferences.OnSharedPreferenceChangeListener listener;
    public boolean cp = false;
    public boolean feature_telephony = false;
    Intent loggingServiceIntent;
    private static final String TAG = "MainActivity";
    NavController navController;
    private Handler requestCellInfoUpdateHandler;
    private GlobalVars gv;

    @SuppressLint("ObsoleteSdkInt")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gv = GlobalVars.getInstance();
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        pm = getPackageManager();
        gv.setPm(pm);
        gv.setPermission_phone_state(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED);
        gv.setPermission_fine_location(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
        gv.setFeature_admin(pm.hasSystemFeature(PackageManager.FEATURE_DEVICE_ADMIN));
        gv.setFeature_work_profile(pm.hasSystemFeature(PackageManager.FEATURE_MANAGED_USERS));
        feature_telephony = pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
        gv.setFeature_telephony(feature_telephony);
        if (feature_telephony) {
            tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            gv.setTm(tm);
            dp = new DataProvider(this);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !dp.getSubscriptions().isEmpty()) {
                // make sure the subscription in the app settings exists in the current subscription list.
                // if it is not in the subscription list change it to the first one of the current list
                boolean valid_subscription = false;
                String pref_subscription_str = sp.getString("select_subscription","99999");
                //int pref_subscription = sp.getInt("select_subscription",9999999);
                for (SubscriptionInfo info : dp.getSubscriptions()) {
                    if (Integer.parseInt(pref_subscription_str) == info.getSubscriptionId()) {
                        valid_subscription = true;
                        Log.d(TAG, "pref sub: " + pref_subscription_str);
                    }
                }
                if (!valid_subscription) {
                    sp.edit().putString("select_subscription", String.valueOf(dp.getSubscriptions().iterator().next().getSubscriptionId())).apply();
                }
                // switch the telephony manager to a new one according to the app settings
                tm = tm.createForSubscriptionId(Integer.parseInt(sp.getString("select_subscription", "0")));
            }

            gv.setSm((SubscriptionManager) getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE));
            cp = HasCarrierPermissions();
            gv.setCarrier_permissions(cp);
            if (cp) {
                gv.setCcm((CarrierConfigManager) getSystemService(Context.CARRIER_CONFIG_SERVICE));
            }


        }
        gv.set_dp(dp);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        gv.setLog_status(findViewById(R.id.log_status_icon));

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainerView);
        navController = Objects.requireNonNull(navHostFragment).getNavController();

        // allow HTTP / insecure connections for the influxDB client
        // todo this should be a setting in the settings dialog
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // set up foreground service for logging of cell and location data

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


        loggingServiceIntent = new Intent(this, LoggingService.class);
        Context context = getApplicationContext();
        if (sp.getBoolean("enable_logging", false)) {
            Log.d(TAG, "Start logging service");
            context.startForegroundService(loggingServiceIntent);
        }

        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                if (Objects.equals(key, "enable_logging")) {
                    if (prefs.getBoolean(key, false)) {
                        Log.i(TAG, "Start logging service");
                        context.startForegroundService(loggingServiceIntent);
                    } else {
                        Log.i(TAG, "Stop logging service");
                        context.stopService(loggingServiceIntent);
                    }
                }
                if (Objects.equals(key, "carrier_Permission")) {
                    if(prefs.getBoolean(key, true)) {
                        Log.i(TAG, "Carrier Permission Approved");
                        cp = tm.hasCarrierPrivileges();
                        if(cp){
                            Toast.makeText(context, "Carrier Permission Approved!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context,"Carrier Permissions Rejected!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.i(TAG,"Carrier Permission Denied!");
                    }
                }
            }
        };

        sp.registerOnSharedPreferenceChangeListener(listener);

        // check permissions
        // todo handle waiting for permissions
        List<String> permissions = new ArrayList<String>();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Requesting READ_PHONE_STATE Permission");
            permissions.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Requesting FINE_LOCATION Permission");
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        // on android 13 an newer we need to ask for permission to show the notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Requesting POST_NOTIFICATIONS Permission");
                permissions.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
        if (!permissions.isEmpty()){
            String[] perms = permissions.toArray(new String[0]);
            ActivityCompat.requestPermissions(this, perms , 1337);
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (!lm.isLocationEnabled() && !sp.getBoolean("fake_location", false)) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.dialog_no_location_title)
                        .setMessage(R.string.dialog_no_location)
                        .setPositiveButton(R.string.dialog_no_location_enable, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                getApplicationContext().startActivity(intent);
                            }
                        })
                        .setNegativeButton(R.string.dialog_no_location_fake, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                sp.edit().putBoolean("fake_location", true).apply();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_map)
                        .show();
            } else {
                Log.i(TAG, "location API is disabled but fake location enabled");
            }
        }
        requestCellInfoUpdateHandler = new Handler(Objects.requireNonNull(Looper.myLooper()));
        requestCellInfoUpdateHandler.post(requestCellInfoUpdate);
    }


    private final Runnable requestCellInfoUpdate = new Runnable() {
        @SuppressLint("MissingPermission") // we check them already in the Mainactivity
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
            requestCellInfoUpdateHandler.postDelayed(this, Integer.parseInt(sp.getString("logging_interval", "1000")));
        }
    };


    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        // All other menus show icons, why leave the overflow menu out? It should match.
        if(menu instanceof MenuBuilder) {
            ((MenuBuilder) menu).setOptionalIconsVisible(true);
        }
        return true;
    }

     public ComponentName getComponentName(Context context) {
        return new ComponentName(context.getApplicationContext(), NetworkCallback.class);
    }

    public boolean getOrganization(Context context) {
        boolean flag = false;

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

        return flag;
    }

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

    @Override
    public boolean onSupportNavigateUp() {
        navController.navigate(R.id.HomeFragment);
        // NavController navController = Navigation.findNavController(this, R.id.home_fragment);
        //return NavigationUI.navigateUp(navController, appBarConfiguration)
        //        || super.onSupportNavigateUp();
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int i, @NonNull String[] strArr, @NonNull int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);
        switch (i) {
            case 1: {
                if (iArr.length == 0 || iArr[0] != 0) {
                    Log.d(TAG, "Could not get READ_PHONE_STATE permission");
                    Toast.makeText(this, "Could not get READ_PHONE_STATE permission ", Toast.LENGTH_LONG).show();

                } else {
                    Log.d(TAG, "Got READ_PHONE_STATE_PERMISSIONS");
                    Toast.makeText(this, "Got READ_PHONE_STATE_PERMISSIONS", Toast.LENGTH_LONG).show();
                }
                break;
            }
            case 2: {
                if (iArr.length <= 0 || iArr[0] != 0) {
                    Log.d(TAG, "Could not get LOCATION permission");
                    Toast.makeText(this, "Could not get LOCATION permissions", Toast.LENGTH_LONG).show();
                } else {
                    Log.d(TAG, "Got LOCATION permission");
                    Toast.makeText(this, "Got LOCATION permissions", Toast.LENGTH_LONG).show();
                }
                break;
            }

            case 3: {
                if (iArr.length == 0 || iArr[0] != 0) {
                    Log.d(TAG, "Could not get BACKGROUND_LOCATION permission");
                    Toast.makeText(this, "Could not get BACKGROUND_LOCATION permissions", Toast.LENGTH_LONG).show();
                } else {
                    Log.d(TAG, "Got BACKGROUND_LOCATION permission");
                    Toast.makeText(this, "Got BACKGROUND_LOCATION permissions", Toast.LENGTH_LONG).show();
                }
                break;
            }
            case 1337:
                // we need to request background location after we got foreground. todo add more checks here if the user said yes
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Requesting ACCESS_BACKGROUND_LOCATION Permission");
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 3);
                }
        }
    }

    public boolean HasCarrierPermissions() {
        Log.d(TAG,"Carrier Privileges: " + tm.hasCarrierPrivileges());
        return tm.hasCarrierPrivileges();
    }

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
        }
        return true;
    }
}