package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.CellInformations.CellInformation;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.DataProvider;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SPType;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SharedPreferencesGrouper;

public class QuickFragment extends Fragment {
    public TelephonyManager tm;
    public PackageManager pm;
    private DataProvider dp;
    private Context context;
    private GlobalVars gv;
    private SharedPreferencesGrouper spg;
    private Handler updateUIHandler;
    private LinearLayout mainLL;

    public QuickFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = requireContext();
        tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        pm = context.getPackageManager();
        gv = GlobalVars.getInstance();
        dp = gv.get_dp();
        spg = SharedPreferencesGrouper.getInstance(context);
        updateUIHandler = new Handler(Objects.requireNonNull(Looper.myLooper()));
    }


    Runnable updateUI = new Runnable() {
        @Override
        public void run() {
            mainLL.removeAllViews();
            dp.refreshAll();
            List<CellInformation> cellInformations = dp.getRegisteredCells();
            List<CellInformation> neighborCells = dp.getNeighbourCellInformation();
            if(cellInformations.isEmpty()){
                LinearLayout error = new LinearLayout(context);
                error.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                ));
                error.setOrientation(LinearLayout.VERTICAL);
                TextView errorText = new TextView(context);
                errorText.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                ));
                errorText.setText("No Cell Information Available");
                error.addView(errorText);
                mainLL.addView(error);
            } else {
                cellInformations.forEach(cellInformation -> {
                    mainLL.addView(cellInformation.createQuickView(context));
                });
            }

            if (spg.getSharedPreference(SPType.default_sp).getBoolean("show_neighbour_cells", false)) {
                if(!neighborCells.isEmpty()){
                    neighborCells.forEach(cellInformation -> {
                        mainLL.addView(cellInformation.createQuickView(context));
                    });
                }
            }
            updateUIHandler.postDelayed(updateUI, 500);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quick, container, false);
        mainLL = view.findViewById(R.id.quick_fragment);
        updateUIHandler.postDelayed(updateUI, 500);
        return view;
    }



}
