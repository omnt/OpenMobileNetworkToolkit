package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.SettingPreferences.ClearPreferencesListener;

public class ClearPreferencesFragment extends DialogFragment {
    private ClearPreferencesListener listener;

    public void setClearPreferencesListener(ClearPreferencesListener listener) {
        this.listener = listener;
    }
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction.
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.clear_preferences_message)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SharedPreferencesGrouper.getInstance(getContext()).clearConfig();
                        Toast.makeText(getContext(), "All Config Cleared!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(getContext(), "Canceled", Toast.LENGTH_SHORT).show();
                    }
                });
        // Create the AlertDialog object and return it.
        return builder.create();
    }

}
