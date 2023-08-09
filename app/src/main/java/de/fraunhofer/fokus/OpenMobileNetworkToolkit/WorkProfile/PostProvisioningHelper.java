package de.fraunhofer.fokus.OpenMobileNetworkToolkit.WorkProfile;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class PostProvisioningHelper {

    private static final String PREFS = "post-provisioning";
    private static final String PREF_DONE = "done";

    private final Context mContext;
    private final DevicePolicyManager mDevicePolicyManager;
    private final SharedPreferences mSharedPrefs;

    PostProvisioningHelper(@NonNull Context context) {
        mContext = context;
        mDevicePolicyManager =
            (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        mSharedPrefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void completeProvisioning() {
        if (isDone()) {
            return;
        }
        ComponentName componentName = BasicDeviceAdminReceiver.getComponentName(mContext);
        // This is the name for the newly created managed profile.
        mDevicePolicyManager.setProfileName(
            componentName,
            mContext.getString(R.string.profile_name)
        );
        // We enable the profile here.
        mDevicePolicyManager.setProfileEnabled(componentName);
    }

    public boolean isDone() {
        return mSharedPrefs.getBoolean(PREF_DONE, false);
    }

}
