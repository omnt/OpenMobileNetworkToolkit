package de.fraunhofer.fokus.OpenMobileNetworkToolkit.WorkProfile;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class EnableProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final PostProvisioningHelper helper = new PostProvisioningHelper(this);
        if (!helper.isDone()) {
            // Important: After the profile has been created, the MDM must enable it for corporate
            // apps to become visible in the launcher.
            helper.completeProvisioning();
        }

        // This is just a friendly shortcut to the main screen.
        setContentView(R.layout.enable_profile_activity);
        findViewById(R.id.icon).setOnClickListener((v) -> {
            // Opens up the main screen
            startActivity(new Intent(this, WorkProfileActivity.class));
            finish();
        });

    }
}
