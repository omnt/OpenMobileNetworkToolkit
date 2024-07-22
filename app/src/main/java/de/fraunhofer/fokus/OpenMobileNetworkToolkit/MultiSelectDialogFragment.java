package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiSelectDialogFragment extends DialogFragment {

    private Map<String, Boolean> itemsMap;
    private OnMultiSelectListener listener;
    private String title;
    private Button selectAllButton;
    private boolean allSelected = false;

    public MultiSelectDialogFragment(List<String> keys, OnMultiSelectListener listener, String title) {
        this.itemsMap = new HashMap<>();
        for (String key : keys) {
            itemsMap.put(key, false);
        }
        this.listener = listener;
        this.title = title;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        selectAllButton = new Button(requireContext());
        selectAllButton.setText("Select All");
        selectAllButton.setOnClickListener(v -> toggleSelection());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(selectAllButton);
        builder.setTitle(title)
                .setMultiChoiceItems(itemsMap.keySet().toArray(new CharSequence[0]),
                        getCheckedItems(), this::onMultiChoiceClick)
                .setPositiveButton("OK", this::onPositiveClick)
                .setNegativeButton("Cancel", null);
        return builder.create();
    }

    private void toggleSelection() {
        allSelected = !allSelected;
        selectAllButton.setText(allSelected ? "Deselect All" : "Select All");
        AlertDialog dialog = (AlertDialog) getDialog();
        int i = 0;
        for (Map.Entry<String, Boolean> entry : itemsMap.entrySet()) {
            entry.setValue(allSelected);
            dialog.getListView().setItemChecked(i++, allSelected);
        }
    }

    private void onMultiChoiceClick(DialogInterface dialog, int which, boolean isChecked) {
        String item = (String) ((AlertDialog) dialog).getListView().getItemAtPosition(which);
        itemsMap.put(item, isChecked);
    }

    private void onPositiveClick(DialogInterface dialog, int id) {
        List<String> selectedItems = new ArrayList<>();
        for (Map.Entry<String, Boolean> entry : itemsMap.entrySet()) {
            if (entry.getValue()) {
                selectedItems.add(entry.getKey());
            }
        }
        listener.onItemsSelected(selectedItems);
    }

    private boolean[] getCheckedItems() {
        boolean[] checkedItems = new boolean[itemsMap.size()];
        int i = 0;
        for (Boolean isChecked : itemsMap.values()) {
            checkedItems[i++] = isChecked;
        }
        return checkedItems;
    }

    public interface OnMultiSelectListener {
        void onItemsSelected(List<String> selectedItems);
    }
}