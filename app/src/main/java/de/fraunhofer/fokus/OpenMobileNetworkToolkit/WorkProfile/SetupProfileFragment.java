/*
 * SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 * SPDX-License-Identifier: apache2
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.WorkProfile;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class SetupProfileFragment extends Fragment {

    private static final int REQUEST_PROVISION_MANAGED_PROFILE = 1;

    public static SetupProfileFragment newInstance() {
        return new SetupProfileFragment();
    }

    @Nullable
    @Override
    public View onCreateView(
        @NonNull LayoutInflater inflater,
        @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.setup_profile_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        view.findViewById(R.id.set_up_profile).setOnClickListener((v) -> provisionManagedProfile());
    }

    /**
     * Initiates the managed profile provisioning. If we already have a managed profile set up on
     * this device, we will get an error dialog in the following provisioning phase.
     */
    private void provisionManagedProfile() {
        Activity activity = getActivity();
        if (null == activity) {
            return;
        }

        Intent intent = new Intent(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE);

        // Use a different intent extra below M to configure the admin component.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            intent.putExtra(
                DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_PACKAGE_NAME,
                activity.getApplicationContext().getPackageName()
            );
        } else {
            final ComponentName componentName = new ComponentName(activity,
                BasicDeviceAdminReceiver.class.getName());
            intent.putExtra(
                DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME,
                componentName
            );
        }

        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_PROVISION_MANAGED_PROFILE);
            activity.finish();
        } else {
            Toast.makeText(activity, "Device provisioning is not enabled. Stopping.",
                Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == REQUEST_PROVISION_MANAGED_PROFILE) {
            if (requestCode == Activity.RESULT_OK) {
                Toast.makeText(getActivity(), "Provisioning done.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Provisioning failed.", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
