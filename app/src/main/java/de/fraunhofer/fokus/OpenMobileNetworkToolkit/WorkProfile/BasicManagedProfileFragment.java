package de.fraunhofer.fokus.OpenMobileNetworkToolkit.WorkProfile;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.View;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class BasicManagedProfile extends Fragment implements
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
}
