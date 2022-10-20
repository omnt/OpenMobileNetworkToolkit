package de.fraunhofer.fokus.OpenMobileNetworkToolkit.WorkProfile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import static android.app.admin.DevicePolicyManager.FLAG_MANAGED_CAN_ACCESS_PARENT;
import static android.app.admin.DevicePolicyManager.FLAG_PARENT_CAN_ACCESS_MANAGED;

import java.net.FileNameMap;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.SRLog;


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
     * Package name of calculator
     */
    private static final String PACKAGE_NAME_CALCULATOR = "com.android.calculator2";

    /**
     * Package name of Chrome
     */
    private static final String PACKAGE_NAME_CHROME = "com.android.chrome";

    /**
     * {@link Button} to remove this managed profile.
     */
    private Button mButtonRemoveProfile;

    /**
     * Whether the calculator app is enabled in this profile
     */
    private boolean mCalculatorEnabled;

    /**
     * Whether Chrome is enabled in this profile
     */
    private boolean mChromeEnabled;

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

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Retrieves whether the calculator app is enabled in this profile
        mCalculatorEnabled = isApplicationEnabled(PACKAGE_NAME_CALCULATOR);
        // Retrieves whether Chrome is enabled in this profile
        mChromeEnabled = isApplicationEnabled(PACKAGE_NAME_CHROME);

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
        view.findViewById(R.id.set_chrome_restrictions).setOnClickListener(this);
        view.findViewById(R.id.clear_chrome_restrictions).setOnClickListener(this);
        view.findViewById(R.id.enable_forwarding).setOnClickListener(this);
        view.findViewById(R.id.disable_forwarding).setOnClickListener(this);
        view.findViewById(R.id.send_intent).setOnClickListener(this);


        mButtonRemoveProfile = (Button) view.findViewById(R.id.remove_profile);
        mButtonRemoveProfile.setOnClickListener(this);


        Switch toggleCalculator = (Switch) view.findViewById(R.id.toggle_calculator);
        toggleCalculator.setChecked(mCalculatorEnabled);
        toggleCalculator.setOnClickListener(this);

        Switch toggleChrome = (Switch) view.findViewById(R.id.toggle_chrome);
        toggleChrome.setChecked(mChromeEnabled);
        toggleChrome.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.set_chrome_restrictions: {
                setChromeRestrictions();
                break;
            }
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
            case R.id.toggle_calculator: {
                setAppEnabled(PACKAGE_NAME_CALCULATOR, isChecked);
                mCalculatorEnabled = isChecked;
                break;
            }
            case R.id.toggle_chrome: {
                setAppEnabled(PACKAGE_NAME_CHROME, isChecked);
                mChromeEnabled = isChecked;
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
                    SRLog.e(TAG, "Cannot disable this app: " + packageName);
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
            SRLog.e(TAG, "The app cannot be found: " + packageName, exception);
        }
    }

    /**
     * Sets restrictions to Chrome
     */
    private void setChromeRestrictions(){
        final Activity activity = getActivity();
        if(null == activity) {
            return;
        }
        final DevicePolicyManager devicePolicyManager =
                (DevicePolicyManager) activity.getSystemService(Context.DEVICE_POLICY_SERVICE);
        final Bundle settings = new Bundle();

        settings.putString("EditBookmarksEnabled", "false");
        settings.putString("IncognitoModeAvailability", "1");
        settings.putString("ManagedBookmarks",
                "[{\"name\": \"Chromium\", \"url\": \"http://chromium.org\"}, " +
                        "{\"name\": \"Google\", \"url\": \"https://www.google.com\"}]");
        settings.putString("DefaultSearchProviderEnabled", "true");
        settings.putString("DefaultSearchProviderName", "\"LMGTFY\"");
        settings.putString("DefaultSearchProviderSearchURL",
                "\"http://lmgtfy.com/?q={searchTerms}\"");
        settings.putString("URLBlacklist", "[\"example.com\", \"example.org\"]");

        StringBuilder message = new StringBuilder("Setting Chrome restrictions");
        for(String key: settings.keySet()) {
            message.append("\n");
            message.append(key);
            message.append(": ");
            message.append(settings.getString(key));
        }
        ScrollView view = new ScrollView(activity);
        TextView text = new TextView(activity);
        text.setText(message);
        int size = (int) activity.getResources().getDimension(R.dimen.activity_horizontal_margin);
        view.setPadding(size, size, size, size);
        view.addView(text);

        new AlertDialog.Builder(activity)
                .setView(view)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // This is how you can set restrictions to an app.
                        // The format for settings in Bundle differs from app to app.
                        devicePolicyManager.setApplicationRestrictions(
                                BasicDeviceAdminReceiver.getComponentName(activity),
                                PACKAGE_NAME_CHROME, settings);
                        Toast.makeText(activity, R.string.restrictions_set,
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    /**
     * Clears restrictions to Chrome
     */

    private void clearChromeRestrictions() {
        final Activity activity = getActivity();
        if (null == activity) {
            return;
        }
        final DevicePolicyManager manager =
                (DevicePolicyManager) activity.getSystemService(Context.DEVICE_POLICY_SERVICE);
        // In order to clear restrictions, pass null as the restriction Bundle for
        // setApplicationRestrictions
        manager.setApplicationRestrictions
                (BasicDeviceAdminReceiver.getComponentName(activity),
                        PACKAGE_NAME_CHROME, null);
        Toast.makeText(activity, R.string.cleared, Toast.LENGTH_SHORT).show();
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
            SRLog.d(TAG, "A sample intent was sent.");
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
}
