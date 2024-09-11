package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.android.material.textview.MaterialTextView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.zip.Inflater;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.CellInformations.CellInformation;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.CellInformations.GSM;
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
    private View view;

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
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                cellInformations.forEach(cellInformation -> {
                    switch (cellInformation.getCellType()) {
                        case LTE:
                            mainLL.addView(cellInformation.createQuickView(context));
                            break;
                        case UMTS:
                            mainLL.addView(cellInformation.createQuickView(context));
                            break;
                        case GSM:
                            GSM gsm = (GSM) cellInformation;
                            View gsmView = inflater.inflate(R.layout.quickview_gsm, null, false);
                            LinearLayout gsm_ll = gsmView.findViewById(R.id.quickview_gsm);

                            ((MaterialTextView) gsm_ll.findViewById(R.id.quickview_gsm_cellType)).setText(gsm.getCellType().toString());
                            ((MaterialTextView) gsm_ll.findViewById(R.id.quickview_gsm_plmn)).setText(gsm.getMcc()+gsm.getMnc());
                            ((MaterialTextView) gsm_ll.findViewById(R.id.quickview_gsm_ci)).setText(gsm.getCiString());
                            ((MaterialTextView) gsm_ll.findViewById(R.id.quickview_gsm_lac)).setText(gsm.getLacString());
                            ((MaterialTextView) gsm_ll.findViewById(R.id.quickview_gsm_bsic)).setText(gsm.getBsicString());
                            ((MaterialTextView) gsm_ll.findViewById(R.id.quickview_gsm_rssi)).setText(gsm.getRssiString());
                            ((MaterialTextView) gsm_ll.findViewById(R.id.quickview_gsm_ber)).setText(gsm.getBitErrorRateString());
                            mainLL.addView(gsmView);
                            break;
                        case NR:
                            mainLL.addView(cellInformation.createQuickView(context));
                            break;
                        default:
                            break;
                    }
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
        view = inflater.inflate(R.layout.fragment_quick, container, false);
        mainLL = view.findViewById(R.id.quick_fragment);
        updateUIHandler.postDelayed(updateUI, 500);
        return view;
    }



}
