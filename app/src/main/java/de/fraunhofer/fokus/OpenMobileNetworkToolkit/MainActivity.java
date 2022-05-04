/*
 * SPDX-FileCopyrightText: 2021 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2021 Fraunhofer FOKUS
 *
 * SPDX-License-Identifier: apache2
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.PersistableBundle;
import android.telephony.CarrierConfigManager;
import android.telephony.TelephonyManager;
import android.telephony.data.NetworkSliceInfo;
import android.util.Log;

import androidx.core.app.ActivityCompat;
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
    public boolean cp = false;
    private static final String TAG = "OpenMobileNetworkToolkit";


    //@SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //cc = ccm.getConfig();
        tm = (TelephonyManager) getSystemService(this.TELEPHONY_SERVICE);
        ccm = (CarrierConfigManager) getSystemService(this.CARRIER_CONFIG_SERVICE);

        cp = HasCarrierPermissions();
        // check permissions
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
//            Log.d(TAG, "Requesting READ_PHONE_STATE Permission");
//            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_PHONE_STATE}, 1);
//        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Requesting FINE_LOCATION Permission");
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 2);
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
           Toast.makeText(getApplicationContext(), "Add slicing interface here", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(this, "Could not get READ_PHONE_STATE permission ", Toast.LENGTH_LONG).show();

                } else {
                    Toast.makeText(this, "Got READ_PHONE_STATE_PERMISSIONS", Toast.LENGTH_LONG).show();
                }
            }
            case 2: {
                if (iArr.length <= 0 || iArr[0] != 0) {
                    Toast.makeText(this, "Could not get LOCATION permissions", Toast.LENGTH_LONG).show();
                } else {
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

}