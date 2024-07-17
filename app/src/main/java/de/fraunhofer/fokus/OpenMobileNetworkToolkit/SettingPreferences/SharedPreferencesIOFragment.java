package de.fraunhofer.fokus.OpenMobileNetworkToolkit.SettingPreferences;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Map;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.ClearPreferencesFragment;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.SPType;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.SharedPreferencesGrouper;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.SharedPreferencesIO;


public class SharedPreferencesIOFragment extends Fragment implements ClearPreferencesListener {

    private static final String TAG = "SharedPreferencesIOFragment";
    private Context context;
    private String configDir;
    private Uri uri;
    private LinearLayout mainLayout;
    private LinearLayout buttonLayout;
    private ScrollView scrollView;

    private final ActivityResultLauncher<Intent> exportPreferencesLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    exportPreferencesToFile(uri);
                } else {
                    showToast("Failed to export preferences");
                }
            });

    private final ActivityResultLauncher<Intent> importPreferencesLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    importPreferencesFromFile(uri);
                } else {
                    showToast("Failed to import preferences");
                }
            });

    public SharedPreferencesIOFragment() {
        super(R.layout.fragment_shared_preferences_io);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shared_preferences_io, container, false);
        context = requireContext();

        setupUI(view);

        configDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                .getAbsolutePath() + "/omnt/configs/";
        File configFolder = new File(configDir);
        if (!configFolder.exists()) {
            configFolder.mkdir();
        }
        uri = Uri.parse(configDir);

        addSharedPreferencesViews();

        return view;
    }

    private void clearConfig() {
        ClearPreferencesFragment fragment = new ClearPreferencesFragment();
        fragment.setClearPreferencesListener(this);
        fragment.show(getParentFragmentManager(), "clear_preferences");
    }


    private void setupUI(View view) {
        mainLayout = view.findViewById(R.id.fragment_shared_preferences_io);

        Button exportButton = createButton("Export Config", v -> createFile());
        Button importButton = createButton("Import Config", v -> pickFile());
        Button clearConfigButton = createButton("Clear Config", v -> clearConfig());

        buttonLayout = new LinearLayout(context);
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonLayout.addView(exportButton);
        buttonLayout.addView(importButton);

        LinearLayout buttonContainer = new LinearLayout(context);
        buttonContainer.setOrientation(LinearLayout.VERTICAL);
        buttonContainer.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        buttonContainer.addView(buttonLayout);
        buttonContainer.addView(clearConfigButton);
        buttonContainer.setBackgroundColor(context.getColor(R.color.debug_darkDebugBg));

        mainLayout.addView(buttonContainer);
    }

    private Button createButton(String text, View.OnClickListener onClickListener) {
        Button button = new Button(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.weight = 0.5f;
        button.setLayoutParams(params);
        button.setText(text);
        button.setOnClickListener(onClickListener);
        return button;
    }

    private void addSharedPreferencesViews() {
        LinearLayout preferencesLayout = new LinearLayout(context);
        preferencesLayout.setOrientation(LinearLayout.VERTICAL);
        for (Map.Entry<SPType, SharedPreferences> spEntry : SharedPreferencesGrouper.getInstance(context).getAllSharedPreferences().entrySet()) {
            preferencesLayout.addView(generateSharedPreferencesView(spEntry.getKey(), spEntry.getValue()));
        }

        scrollView = new ScrollView(context);
        scrollView.addView(preferencesLayout);
        mainLayout.addView(scrollView);
    }

    private void createFile() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, "omnt_config.json");
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri);
        exportPreferencesLauncher.launch(intent);
    }

    private void pickFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("application/json");
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri);
        importPreferencesLauncher.launch(intent);
    }

    private void exportPreferencesToFile(Uri uri) {
        try (OutputStreamWriter writer = new OutputStreamWriter(context.getContentResolver().openOutputStream(uri))) {
            String jsonString = SharedPreferencesIO.exportPreferences(context);
            writer.write(jsonString);
            showToast("Preferences exported");
        } catch (Exception e) {
            Log.e(TAG, "Failed to export preferences", e);
            showToast("Failed to export preferences");
        }
    }

    private void importPreferencesFromFile(Uri uri) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(context.getContentResolver().openInputStream(uri)))) {
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            SharedPreferencesIO.importPreferences(context, stringBuilder.toString());
            showToast("Preferences imported");
        } catch (Exception e) {
            Log.e(TAG, "Failed to import preferences", e);
            showToast("Failed to import preferences");
        }
    }

    private LinearLayout generateSharedPreferencesView(SPType type, SharedPreferences sharedPreferences) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        // Container layout for the TextView and ImageView
        LinearLayout headerLayout = new LinearLayout(context);
        headerLayout.setOrientation(LinearLayout.HORIZONTAL);
        headerLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        headerLayout.setPadding(16, 16, 16, 16); // Add padding

        TextView typeTextView = new TextView(context);
        typeTextView.setText(type.toReadable());
        typeTextView.setTextSize(18);
        typeTextView.setTypeface(typeTextView.getTypeface(), Typeface.BOLD);
        typeTextView.setTextColor(context.getColor(R.color.design_default_color_primary));
        typeTextView.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f)); // Weight 1 to take remaining space

        ImageView expandIcon = new ImageView(context);
        expandIcon.setImageResource(R.drawable.baseline_expand_more_24); // Set your expand/collapse icon
        expandIcon.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        expandIcon.setPadding(16, 0, 16, 0); // Padding for the icon

        // Add TextView and ImageView to the header layout
        headerLayout.addView(typeTextView);
        headerLayout.addView(expandIcon);

        layout.addView(headerLayout);

        // Add a divider below the header layout
        View divider = new View(context);
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                2);
        dividerParams.setMargins(0, 0, 0, 16);
        divider.setLayoutParams(dividerParams);
        divider.setBackgroundColor(context.getColor(R.color.design_default_color_on_primary));
        layout.addView(divider);

        // Container for shared preferences entries
        LinearLayout entriesLayout = new LinearLayout(context);
        entriesLayout.setOrientation(LinearLayout.VERTICAL);
        entriesLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        layout.addView(entriesLayout);

        // Initially hide the entries layout
        entriesLayout.setVisibility(View.GONE);

        // Add shared preferences entries to the entriesLayout
        for (Map.Entry<String, ?> entry : sharedPreferences.getAll().entrySet()) {
            LinearLayout entryLayout = new LinearLayout(context);
            entryLayout.setOrientation(LinearLayout.HORIZONTAL);
            entryLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));

            // Create and style the keyTextView
            TextView keyTextView = new TextView(context);
            keyTextView.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    0.6f)); // Adjusted weight for better appearance
            keyTextView.setText(entry.getKey());
            keyTextView.setTextSize(10);
            keyTextView.setPadding(8, 8, 8, 8); // Add padding

            // Create and style the valueEditText
            EditText valueEditText = new EditText(context);
            valueEditText.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    0.4f)); // Adjusted weight for better appearance
            valueEditText.setText(entry.getValue().toString());
            valueEditText.setTextSize(10);
            valueEditText.setPadding(8, 8, 8, 8); // Add padding

            entryLayout.addView(keyTextView);
            entryLayout.addView(valueEditText);
            entriesLayout.addView(entryLayout);
        }

        // Toggle the visibility of the entries layout on icon click
        expandIcon.setOnClickListener(new View.OnClickListener() {
            private boolean isExpanded = false;

            @Override
            public void onClick(View v) {
                if (isExpanded) {
                    entriesLayout.setVisibility(View.GONE);
                    expandIcon.setImageResource(R.drawable.baseline_expand_more_24);
                } else {
                    entriesLayout.setVisibility(View.VISIBLE);
                    expandIcon.setImageResource(R.drawable.baseline_expand_less_24);
                }
                isExpanded = !isExpanded;
            }
        });

        return layout;
    }



    private void showToast(String message) {
        if (context != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClearPreferencesAccepted() {
        mainLayout.removeView(scrollView);
        addSharedPreferencesViews();
    }
}
