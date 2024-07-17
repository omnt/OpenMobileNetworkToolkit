package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.List;

public class MultiSelectDialogFragment extends DialogFragment {

    public interface OnMultiSelectListener {
        void onItemsSelected(List<String> selectedItems);
    }

    private List<String> keys;
    private OnMultiSelectListener listener;

    public MultiSelectDialogFragment(List<String> keys, OnMultiSelectListener listener) {
        this.keys = keys;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final boolean[] checkedItems = new boolean[keys.size()];
        final List<String> selectedItems = new ArrayList<>();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select Config to import")
                .setMultiChoiceItems(keys.toArray(new CharSequence[0]), checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if (isChecked) {
                            selectedItems.add(keys.get(which));
                        } else {
                            selectedItems.remove(keys.get(which));
                        }
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onItemsSelected(selectedItems);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Do nothing
                    }
                });

        return builder.create();
    }
}