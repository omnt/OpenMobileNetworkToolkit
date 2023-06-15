package de.fraunhofer.fokus.OpenMobileNetworkToolkit.WorkProfile;

import static android.app.admin.DevicePolicyManager.FLAG_MANAGED_CAN_ACCESS_PARENT;
import static android.app.admin.DevicePolicyManager.FLAG_PARENT_CAN_ACCESS_MANAGED;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;


/**
 * Provides several functions that are available in a managed profile. This includes
 * enabling/disabling other apps, setting app restrictions, enabling/disabling intent forwarding,
 * and wiping out all the data in the profile.
 */
public class BasicManagedProfileFragment extends Fragment implements
        View.OnClickListener,
        CompoundButton.OnCheckedChangeListener {

    /**
     * Tag for logging.
     */
    private static final String TAG = "ManagedProfileFragment";

    /**
     * {@link Button} to remove this managed profile.
     */
    private Button mButtonRemoveProfile;


    private boolean mPreferentialNetwork;

    public BasicManagedProfileFragment() {
    }

    public static BasicManagedProfileFragment newInstance() {
        return new BasicManagedProfileFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.basic_managed_profile_fragment, container, false);
    }


    /**
     * Checks if the application is available in this profile.
     *
     * @param packageName The package name
     * @return True if the application is available in this profile.
     */
    private boolean isApplicationEnabled(String packageName) {
        Activity activity = getActivity();
        PackageManager packageManager = activity.getPackageManager();
        try {
            int packageFlags;
            if (Build.VERSION.SDK_INT < 24) {
                //noinspection deprecation
                packageFlags = PackageManager.GET_UNINSTALLED_PACKAGES;
            } else {
                packageFlags = PackageManager.MATCH_UNINSTALLED_PACKAGES;
            }
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, packageFlags);
            // Return false if the app is not installed in this profile
            if (0 == (applicationInfo.flags & ApplicationInfo.FLAG_INSTALLED)) {
                return false;
            }
            // Check if the app is not hidden in this profile
            DevicePolicyManager devicePolicyManager =
                    (DevicePolicyManager) activity.getSystemService(Activity.DEVICE_POLICY_SERVICE);
            return !devicePolicyManager.isApplicationHidden(
                    BasicDeviceAdminReceiver.getComponentName(activity), packageName);
        } catch (PackageManager.NameNotFoundException exception) {
            return false;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // Bind event listeners and initial states

        view.findViewById(R.id.enable_forwarding).setOnClickListener(this);
        view.findViewById(R.id.disable_forwarding).setOnClickListener(this);
        view.findViewById(R.id.send_intent).setOnClickListener(this);
        view.findViewById(R.id.preferential_switch).setOnClickListener(this);


        mButtonRemoveProfile = (Button) view.findViewById(R.id.remove_profile);
        mButtonRemoveProfile.setOnClickListener(this);


        Switch preferentialNetwork = (Switch) view.findViewById(R.id.preferential_switch);
        preferentialNetwork.setChecked(setPreferentialEnabled());
        preferentialNetwork.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.enable_forwarding: {
                enableForwarding();
                break;
            }
            case R.id.disable_forwarding: {
                disableForwarding();
                break;
            }
            case R.id.send_intent: {
                sendIntent();
                break;
            }
            case R.id.remove_profile: {
                mButtonRemoveProfile.setEnabled(false);
                removeProfile();
                break;
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.preferential_switch: {
                setPreferentialEnabled();
                mPreferentialNetwork = isChecked;
                break;
            }
        }
    }

    /**
     * Enables or disables the specified app in this profile.
     *
     * @param packageName The package name of the target app.
     * @param enabled     Pass true to enable the app.
     */

    private void setAppEnabled(String packageName, boolean enabled){
        Activity activity = getActivity();
        if(null == activity) {
            return;
        }
        PackageManager packageManager = activity.getPackageManager();
        DevicePolicyManager devicePolicyManager =
                (DevicePolicyManager) activity.getSystemService(Context.DEVICE_POLICY_SERVICE);

        try {
            int packageFlags;
            if(Build.VERSION.SDK_INT < 24){
                //noinspection deprecation
                packageFlags = PackageManager.GET_UNINSTALLED_PACKAGES;
            } else {
                packageFlags = PackageManager.MATCH_UNINSTALLED_PACKAGES;
            }
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, packageFlags);
            // Here, we check the ApplicationInfo of the target app, and see if the flags have
            // ApplicationInfo.FLAG_INSTALLED turned on using bitwise operation.
            if (0 == (applicationInfo.flags & ApplicationInfo.FLAG_INSTALLED)) {
                // If the app is not installed in this profile, we can enable it by
                // DPM.enableSystemApp
                if(enabled) {
                    devicePolicyManager.enableSystemApp(
                            BasicDeviceAdminReceiver.getComponentName(activity), packageName);
                } else {
                    // But we cannot disable the app since it is already disabled
                    Log.e(TAG, "Cannot disable this app: " + packageName);
                    return;
                }
            } else {
                // If the app is already installed, we can enable or disable it by
                // DPM.setApplicationHidden
                devicePolicyManager.setApplicationHidden(
                        BasicDeviceAdminReceiver.getComponentName(activity), packageName, !enabled);
            }
            Toast.makeText(activity, enabled ? R.string.enabled : R.string.disabled,
                    Toast.LENGTH_SHORT).show();
        } catch (PackageManager.NameNotFoundException exception){
            Log.e(TAG, "The app cannot be found: " + packageName, exception);
        }
    }

    /**
     * Enables forwarding of share intent between private account and managed profile.
     */
    private void enableForwarding() {
        Activity activity = getActivity();
        if (null == activity || activity.isFinishing()) {
            return;
        }
        DevicePolicyManager manager =
                (DevicePolicyManager) activity.getSystemService(Context.DEVICE_POLICY_SERVICE);
        try {
            IntentFilter filter = new IntentFilter(Intent.ACTION_SEND);
            filter.addDataType("text/plain");
            filter.addDataType("image/jpeg");
            // This is how you can register an IntentFilter as allowed pattern of Intent forwarding
            manager.addCrossProfileIntentFilter(BasicDeviceAdminReceiver.getComponentName(activity),
                    filter, FLAG_MANAGED_CAN_ACCESS_PARENT | FLAG_PARENT_CAN_ACCESS_MANAGED);
        } catch (IntentFilter.MalformedMimeTypeException e) {
            e.printStackTrace();
        }
    }

    /**
     * Disables forwarding of all intents.
     */
    private void disableForwarding() {
        Activity activity = getActivity();
        if (null == activity || activity.isFinishing()) {
            return;
        }
        DevicePolicyManager manager =
                (DevicePolicyManager) activity.getSystemService(Context.DEVICE_POLICY_SERVICE);
        manager.clearCrossProfileIntentFilters(BasicDeviceAdminReceiver.getComponentName(activity));
    }

    /**
     * Sends a sample intent of a plain text message.  This is just a utility function to see how
     * the intent forwarding works.
     */
    private void sendIntent() {
        Activity activity = getActivity();
        if (null == activity || activity.isFinishing()) {
            return;
        }
        DevicePolicyManager manager =
                (DevicePolicyManager) activity.getSystemService(Context.DEVICE_POLICY_SERVICE);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT,
                manager.isProfileOwnerApp(activity.getApplicationContext().getPackageName())
                        ? "From the managed account" : "From the primary account");
        try {
            startActivity(intent);
            Log.d(TAG, "A sample intent was sent.");
        } catch (ActivityNotFoundException e) {
            Toast.makeText(activity, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Wipes out all the data related to this managed profile.
     */
    private void removeProfile() {
        Activity activity = getActivity();
        if (null == activity || activity.isFinishing()) {
            return;
        }
        DevicePolicyManager manager =
                (DevicePolicyManager) activity.getSystemService(Context.DEVICE_POLICY_SERVICE);
        manager.wipeData(0);
        // The screen turns off here
    }

    private boolean setPreferentialEnabled() {
        boolean flag = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Context context = getContext();
            if (context != null) {
                try {
                    DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
                    devicePolicyManager.setPreferentialNetworkServiceEnabled(true);
                    Log.d(TAG, "setPreferentialNetworkServiceEnabled");
                    flag = true;
                } catch (Exception exception) {
                    Log.e(TAG, "setPreferentialNetworkServiceEnabled failed!");
                    flag = false;
                }
            }
        }
        return flag;
    }
}
