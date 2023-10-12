/*
 * SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 * SPDX-License-Identifier: apache2
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.WorkProfile;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class WorkProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workprofile);

        if (savedInstanceState == null) {
            DevicePolicyManager devicePolicyManager =
                (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);

            if (devicePolicyManager.isProfileOwnerApp(getApplicationContext().getPackageName())) {
                // If the managed profile is already set up, we show the main screen.
                showMainFragment();
            } else {
                // If not, we show the set up screen.
                showSetupProfile();
            }
        }

    }

    private void showSetupProfile() {
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.container, SetupProfileFragment.newInstance())
            .commit();
    }

    private void showMainFragment() {
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.container, BasicManagedProfileFragment.newInstance())
            .commit();
    }
}
