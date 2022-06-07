/*
 * SPDX-FileCopyrightText: 2021 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2021 Fraunhofer FOKUS
 *
 * SPDX-License-Identifier: apache2
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.OutcomeReceiver;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.telephony.CarrierConfigManager;
import android.telephony.TelephonyManager;
import android.telephony.data.NetworkSliceInfo;
import android.telephony.data.NetworkSlicingConfig;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    public CarrierConfigManager ccm;
    private PersistableBundle cc;
    private ConnectivityManager cm;
    public TelephonyManager tm;
    public PackageManager pm;

    public boolean cp = false;
    public boolean ts = false;
    private static final String TAG = "OpenMobileNetworkToolkit";
    public final static int Overlay_REQUEST_CODE = 251;
    private final int REQUEST_READ_PHONE_STATE=1;



    //@SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //cc = ccm.getConfig();
        tm = (TelephonyManager) getSystemService(this.TELEPHONY_SERVICE);
        ccm = (CarrierConfigManager) getSystemService(this.CARRIER_CONFIG_SERVICE);

        //FEATURE_TELEPHONY_SUBSCRIPTION
       /* tm = (TelephonyManager) getSystemService(this.TELEPHONY_SUBSCRIPTION_SERVICE);*/

        //

        cp = HasCarrierPermissions();
        ts = HasCarrierPermissions();
        // check permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Requesting READ_PHONE_STATE Permission");
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_PHONE_STATE}, 1);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Requesting FINE_LOCATION Permission");
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 2);
        }
        if(ActivityCompat.checkSelfPermission(this, "android.permission.READ_PRIVILEGED_PHONE_STATE") != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Requesting Privillaged Phone State");
                ActivityCompat.requestPermissions(this, new String[]{"android.permission.READ_PRIVILEGED_PHONE_STATE"},123);
        }



        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
//        binding.fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
        //FragmentManager fm = getSupportFragmentManager();
        //HomeFragment f = (HomeFragment) fm.findFragmentById(R.id.home_fragment);
        //f.setHasCarrierPrivilages(cp)


        // TODO Add getNetworkSlicingConfiguration, need to add Privillaged phone state


        //int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
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
            Log.d(TAG, "CARRIER PERMISSION UNAVAILABLE TO SLICING!");
            //Toast.makeText(getApplicationContext(),"CARRIER PERMISSION UNAVAILABLE TO SLICING!", Toast.LENGTH_SHORT).show();
        }

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
            tm.sendDialerSpecialCode("4636");
        } else if (id == R.id.SonyService) {
            tm.sendDialerSpecialCode("7378423");
        }else if (id == R.id.HuaweiProjektMenu) {
            tm.sendDialerSpecialCode("2846579");
        } else if (id == R.id.about) {
            NavController navController = Navigation.findNavController(this, R.id.about_fragment);
            navController.navigate(R.id.action_FirstFragment_to_SecondFragment);
        }
         else if (id == R.id.slicing){
           //Toast.makeText(getApplicationContext(), "Add slicing interface here", Toast.LENGTH_SHORT).show();
           //openFloatingWindow();
            checkDrawOverlayPermission(this);
         }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onRequestPermissionsResult(int i, @NonNull String[] strArr, @NonNull int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);
        switch (i) {
            case 1: {
                if (iArr.length <= 0 || iArr[0] != 0) {
                    SRLog.d(TAG,"Could not get READ_PHONE_STATE permission");
                    Toast.makeText(this, "Could not get READ_PHONE_STATE permission ", Toast.LENGTH_LONG).show();

                } else {
                    SRLog.d(TAG,"Got READ_PHONE_STATE_PERMISSIONS");
                    Toast.makeText(this, "Got READ_PHONE_STATE_PERMISSIONS", Toast.LENGTH_LONG).show();
                }
            }
            case 2: {
                if (iArr.length <= 0 || iArr[0] != 0) {
                    SRLog.d(TAG,"Could not get LOCATION permission");
                    Toast.makeText(this, "Could not get LOCATION permissions", Toast.LENGTH_LONG).show();
                } else {
                    SRLog.d(TAG,"Got LOCATION permission");
                    Toast.makeText(this, "Got LOCATION permissions", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public void NoCarrierToast() {
        Toast.makeText(this, "Carrier Permissions needed for this", Toast.LENGTH_LONG).show();
    }

    public boolean HasCarrierPermissions() {
        return tm.hasCarrierPrivileges();
    }

    /*public boolean HasTelephonySubscription() {
        return tm.hasSystemFeature(FEATURE_TELEPHONY_SUBSCRIPTION);
    }*/



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

    private void openFloatingWindow() {
        Intent intent = new Intent(getApplicationContext(), DebuggerService.class);
        getApplicationContext().stopService(intent);
        ContextCompat.startForegroundService(getApplicationContext(), intent);
    }

}