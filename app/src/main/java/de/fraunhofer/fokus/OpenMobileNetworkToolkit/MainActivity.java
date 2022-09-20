/*
 * SPDX-FileCopyrightText: 2021 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2021 Fraunhofer FOKUS
 *
 * SPDX-License-Identifier: apache2
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    public TelephonyManager tm;
    public PackageManager pm;
    protected Context context;
    SharedPreferences sp;
    SharedPreferences.OnSharedPreferenceChangeListener listener;

    public boolean cp = false;
    public boolean feature_telephony = false;

    Intent loggingServiceIntent;


    private static final String TAG = "MainActivity";
    public final static int Overlay_REQUEST_CODE = 251;
    NavController navController;
    private AppBarConfiguration appBarConfiguration;



    //@SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        pm = getPackageManager();
        feature_telephony = pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
        if (feature_telephony) {
            tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            cp = HasCarrierPermissions();
        }
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainerView);
        navController = navHostFragment.getNavController();

        //allow HTTP / insecure connections for the influxDB client
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
                    if(prefs.getBoolean(key, false)) {
                        SRLog.d(TAG, "Carrier Permission Approved");
                        cp = tm.hasCarrierPrivileges();
                        if(cp){
                            Toast.makeText(context, "Carrier Permission Approved!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context,"Carrier Permissions Rejected!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        SRLog.d(TAG,"Carrier Permission Denied!");
                    }
                }
            }
        };

        sp.registerOnSharedPreferenceChangeListener(listener);

        // check permissions
        // todo handle waiting for permissions

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Requesting READ_PHONE_STATE Permission");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Requesting FINE_LOCATION Permission");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Requesting ACCESS_BACKGROUND_LOCATION Permission");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 3);
        }
        // on android 13 an newer we need to ask for permission to show the notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Requesting POST_NOTIFICATIONS Permission");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 4);
            }
        }

        /* TODO Clean this after slice config works */

       /* if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            SRLog.d(TAG,"Requesting permission for phone_state");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
            SRLog.d(TAG,"permission for phone_state acquired");

            if(tm.isRadioInterfaceCapabilitySupported(TelephonyManager.CAPABILITY_SLICING_CONFIG_SUPPORTED))
            {
                if(tm.hasCarrierPrivileges()) {

                    tm.getNetworkSlicingConfiguration(getApplicationContext().getMainExecutor(), new OutcomeReceiver<NetworkSlicingConfig, TelephonyManager.NetworkSlicingException>() {
                        @Override
                        public void onResult(@NonNull NetworkSlicingConfig networkSlicingConfig) {
                            SRLog.d(TAG, "SLICING CONFIG RESULT OK");
                            NetworkSlicingConfig networkSlicingConfig1 = networkSlicingConfig;
                            List<UrspRule> urspRuleList = networkSlicingConfig.getUrspRules();
                            List<NetworkSliceInfo> sliceInfoList = networkSlicingConfig.getSliceInfo();
                            SRLog.d(TAG, "Slice config works!!");
                            SRLog.d(TAG, "URSP List: " +urspRuleList);
                            SRLog.d(TAG, "sliceInfoList: " +sliceInfoList);
                            SRLog.d(TAG,"URSP received: " +urspRuleList.size());
                            for(int i = 0; i < urspRuleList.size(); i++){
                                UrspRule urspRule = networkSlicingConfig.getUrspRules().get(i);
                                List<TrafficDescriptor> trafficDescriptorList = urspRule.getTrafficDescriptors();
                                List<RouteSelectionDescriptor> routeSelectionDescriptorList = urspRule.getRouteSelectionDescriptor();
                                TrafficDescriptor trafficDescriptor = trafficDescriptorList.get(i);
                                RouteSelectionDescriptor routeSelectionDescriptor = routeSelectionDescriptorList.get(i);
                                List<NetworkSliceInfo> networkSliceInfoList = routeSelectionDescriptor.getSliceInfo();
                                NetworkSliceInfo networkSliceInfo = networkSliceInfoList.get(i);


                                SliceCreate sliceCreate = new SliceCreate();




                                SRLog.d(TAG, "URSP" + urspRule);
                                SRLog.d(TAG, "Traffic Descriptor" + trafficDescriptor);
                                SRLog.d(TAG, "Route Selection" + routeSelectionDescriptor);
                            }

                        }

                        @Override
                        public void onError(@NonNull TelephonyManager.NetworkSlicingException error) {
                            OutcomeReceiver.super.onError(error);
                            SRLog.d(TAG, "SLICING CONFIG ERROR!!");
                        }

                    });
                }
            }
        } else {
            SRLog.d(TAG, "READ_PHONE_STATE PERMISSION UNAVAILABLE TO SLICING!");
        }*/

        /*if(cp) {

            tm.getNetworkSlicingConfiguration(getApplicationContext().getMainExecutor(), new OutcomeReceiver<NetworkSlicingConfig, TelephonyManager.NetworkSlicingException>() {
                @Override
                public void onResult(@NonNull NetworkSlicingConfig networkSlicingConfig) {
                    Log.d(TAG, "SLICING CONFIG RESULT OK");
                }

                @Override
                public void onError(@NonNull TelephonyManager.NetworkSlicingException error) {
                    OutcomeReceiver.super.onError(error);
                    Log.d(TAG, "SLICING CONFIG ERROR!!");
                }

            });
        } else {
            Log.d(TAG, "CARRIER PERMISSION UNAVAIL7ABLE TO SLICING!");
            Toast.makeText(getApplicationContext(),"CARRIER PERMISSION UNAVAILABLE TO SLICING!", Toast.LENGTH_SHORT).show();
        }*/
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
                SRLog.d(TAG, "isProfileOwnerApp:" + dpm.isProfileOwnerApp(context.getPackageName()));
                SRLog.d(TAG, "isDeviceOwnerApp:" + dpm.isDeviceOwnerApp(context.getPackageName()));
                SRLog.d(TAG, "Component Name: " + componentName);
            }
        }

        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                getString(R.string.device_admin_description));
        startActivity(intent);

        SRLog.d(TAG, "Is admin active: " + dpm.isAdminActive(componentName));

        return flag;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.MediatekIMS) {
            if (cp) {
                tm.sendDialerSpecialCode("3646633");
            } else {
                NoCarrierToast();
            }
        } else if (id == R.id.SamsungIMS) {
            if (cp) {
                tm.sendDialerSpecialCode("467");
            } else {
                NoCarrierToast();
            }
        } else if (id == R.id.AndroidTesting) {
            if (cp) {
                tm.sendDialerSpecialCode("4636");
            } else
                NoCarrierToast();
        } else if (id == R.id.SonyService) {
            if (cp) {
                tm.sendDialerSpecialCode("7378423");
            } else {
                NoCarrierToast();
            }
        } else if (id == R.id.HuaweiProjektMenu) {
            if (cp) {
                tm.sendDialerSpecialCode("2846579");
            } else {
                NoCarrierToast();
            }
        } else if (id == R.id.NokiaTesting) {
            if (cp) {
                tm.sendDialerSpecialCode("55555");
            } else {
                NoCarrierToast();
            }
        } else if (id == R.id.about) {
            navController.navigate(R.id.about_fragment);
        } else if (id == R.id.apn) {
            Intent intent = new Intent(Settings.ACTION_APN_SETTINGS);
            startActivity(intent);
        } else if (id == R.id.slicingSetup) {
            navController.navigate(R.id.fragment_slicingsetup);
        } else if (id == R.id.iperf3) {
            navController.navigate(R.id.fragment_iperf3_input);
        }
        else if (id == R.id.settings){
            navController.navigate(R.id.settingsFragment);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        //NavController navController = Navigation.findNavController(this, R.id.home_fragment);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onRequestPermissionsResult(int i, @NonNull String[] strArr, @NonNull int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);
        switch (i) {
            case 1: {
                if (iArr.length <= 0 || iArr[0] != 0) {
                    SRLog.d(TAG, "Could not get READ_PHONE_STATE permission");
                    Toast.makeText(this, "Could not get READ_PHONE_STATE permission ", Toast.LENGTH_LONG).show();

                } else {
                    SRLog.d(TAG, "Got READ_PHONE_STATE_PERMISSIONS");
                    Toast.makeText(this, "Got READ_PHONE_STATE_PERMISSIONS", Toast.LENGTH_LONG).show();
                }
            }
            case 2: {
                if (iArr.length <= 0 || iArr[0] != 0) {
                    SRLog.d(TAG, "Could not get LOCATION permission");
                    Toast.makeText(this, "Could not get LOCATION permissions", Toast.LENGTH_LONG).show();
                } else {
                    SRLog.d(TAG, "Got LOCATION permission");
                    Toast.makeText(this, "Got LOCATION permissions", Toast.LENGTH_LONG).show();
                }
            }
            case 3: {
                if (iArr.length <= 0 || iArr[0] != 0) {
                    SRLog.d(TAG, "Could not get BACKGROUND_LOCATION permission");
                    Toast.makeText(this, "Could not get BACKGROUND_LOCATION permissions", Toast.LENGTH_LONG).show();
                } else {
                    SRLog.d(TAG, "Got BACKGROUND_LOCATION permission");
                    Toast.makeText(this, "Got BACKGROUND_LOCATION permissions", Toast.LENGTH_LONG).show();
                }
            }
        }
    }


    public void NoCarrierToast() {
        Toast.makeText(this, "Carrier Permissions needed for this", Toast.LENGTH_LONG).show();
    }

    public void checkDrawOverlayPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                if (null != activity) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                    activity.startActivityForResult(intent, Overlay_REQUEST_CODE);
                } else {
                    Toast.makeText(this, "Please grant \"Draw over other apps\" permission under application settings", Toast.LENGTH_LONG).show();
                }
            } else {
                openFloatingWindow();
            }
        } else {
            openFloatingWindow();
        }
    }

    public boolean HasCarrierPermissions() {
        return tm.hasCarrierPrivileges();
    }


    private void openFloatingWindow() {
        Intent intent = new Intent(getApplicationContext(), DebuggerService.class);
        getApplicationContext().stopService(intent);
        ContextCompat.startForegroundService(getApplicationContext(), intent);
    }
}