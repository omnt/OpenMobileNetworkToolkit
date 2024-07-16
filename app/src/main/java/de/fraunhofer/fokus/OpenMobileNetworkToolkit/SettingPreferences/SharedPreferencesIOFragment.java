package de.fraunhofer.fokus.OpenMobileNetworkToolkit.SettingPreferences;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.ClearPreferencesFragment;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.SharedPreferencesGrouper;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.SharedPreferencesIO;

public class SharedPreferencesIOFragment extends Fragment {

    private static final String TAG = "SharedPreferencesIOFragment";
    private Context ct;
    private String configDir;
    private Uri uri;

    private final ActivityResultLauncher<Intent> exportPreferences = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    exportPreferencesToFile(uri);
                } else {
                    if (this.ct != null) {
                        Toast.makeText(this.ct, "Failed to export preferences", Toast.LENGTH_SHORT).show();
                    }
                }
            });
    private final ActivityResultLauncher<Intent> importPreferences = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    importPreferencesFromFile(uri);
                } else {
                    if (this.ct != null) {
                        Toast.makeText(this.ct, "Failed to import preferences", Toast.LENGTH_SHORT).show();
                    }
                }
            });
    public SharedPreferencesIOFragment() {
        super(R.layout.fragment_shared_preferences_io);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shared_preferences_io, container, false);
        this.ct = requireContext();
        Button exportButton = view.findViewById(R.id.export_button);
        Button importButton = view.findViewById(R.id.import_button);
        Button clearConfig = view.findViewById(R.id.clear_button);

        exportButton.setOnClickListener(v -> createFile());
        importButton.setOnClickListener(v -> pickFile());
        clearConfig.setOnClickListener(v -> {
            new ClearPreferencesFragment().show(getParentFragmentManager(), "clear_preferences");
        });

        this.configDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                .getAbsolutePath() + "/omnt/configs/";
        File configFolder = new File(this.configDir);
        if (!configFolder.exists()) {
            configFolder.mkdir();
        }
        this.uri =  Uri.parse(this.configDir);

        return view;
    }

    private void createFile() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, "omnt_config.json");
        intent.putExtra("CREATE_FILE", true);
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, this.uri);
        exportPreferences.launch(intent);
    }

    private void pickFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("application/json");
        intent.putExtra("CREATE_FILE", false);
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, this.uri);
        importPreferences.launch(intent);
    }

    private void exportPreferencesToFile(Uri uri) {
        try {
            if (this.ct != null) {
                String jsonString = SharedPreferencesIO.exportPreferences(this.ct);
                OutputStreamWriter writer = new OutputStreamWriter(this.ct.getContentResolver().openOutputStream(uri));
                writer.write(jsonString);
                writer.close();
                Toast.makeText(this.ct, "Preferences exported", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to export preferences", e);
            if (this.ct != null) {
                Toast.makeText(this.ct, "Failed to export preferences", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void importPreferencesFromFile(Uri uri) {
        try {
            if (this.ct != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(this.ct.getContentResolver().openInputStream(uri)));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                reader.close();
                SharedPreferencesIO.importPreferences(this.ct, stringBuilder.toString());
                Toast.makeText(this.ct, "Preferences imported", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to import preferences", e);
            if (this.ct != null) {
                Toast.makeText(this.ct, "Failed to import preferences", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
