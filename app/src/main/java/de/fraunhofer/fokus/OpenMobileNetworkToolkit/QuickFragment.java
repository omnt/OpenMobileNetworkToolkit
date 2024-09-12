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

import com.google.android.material.textview.MaterialTextView;

import java.util.List;
import java.util.Objects;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.CellInformations.CDMAInformation;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.CellInformations.CellInformation;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.CellInformations.GSMInformation;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.CellInformations.NRInformation;
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

    private void addCellInformationToView(CellInformation cellInformation){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        switch (cellInformation.getCellType()) {
            case LTE:
                //mainLL.addView(cellInformation.createQuickView(context));
                break;
            case CDMA:
                CDMAInformation cdma = (CDMAInformation) cellInformation;
                View cdmaView = inflater.inflate(R.layout.quickview_cdma, null, false);
                LinearLayout cdmaLL = cdmaView.findViewById(R.id.quickview_cdma);
                ((MaterialTextView) cdmaLL.findViewById(R.id.quickview_cdma_cellType)).setText(cdma.getCellType().toString());
                ((MaterialTextView) cdmaLL.findViewById(R.id.quickview_cdma_mnc)).setText(cdma.getMnc());
                ((MaterialTextView) cdmaLL.findViewById(R.id.quickview_cdma_ci)).setText(cdma.getCiString());
                ((MaterialTextView) cdmaLL.findViewById(R.id.quickview_cdma_pci)).setText(cdma.getPciString());
                ((MaterialTextView) cdmaLL.findViewById(R.id.quickview_cdma_tac)).setText(cdma.getTacString());

                ((MaterialTextView) cdmaLL.findViewById(R.id.quickview_cdma_dbm)).setText(cdma.getCmdaDbm());
                ((MaterialTextView) cdmaLL.findViewById(R.id.quickview_cdma_ecio)).setText(cdma.getCmdaEcioString());
                ((MaterialTextView) cdmaLL.findViewById(R.id.quickview_cdma_evdo_dbm)).setText(cdma.getEvdoDbmString());
                ((MaterialTextView) cdmaLL.findViewById(R.id.quickview_cdma_evdo_ecio)).setText(cdma.getEvdoEcioString());
                ((MaterialTextView) cdmaLL.findViewById(R.id.quickview_cdma_evdo_snr)).setText(cdma.getEvdoSnr());
                break;
            case UMTS:
                //mainLL.addView(cellInformation.createQuickView(context));
                break;
            case GSM:
                GSMInformation gsm = (GSMInformation) cellInformation;
                View gsmView = inflater.inflate(R.layout.quickview_gsm, null, false);
                LinearLayout gsmLL = gsmView.findViewById(R.id.quickview_gsm);
                ((MaterialTextView) gsmLL.findViewById(R.id.quickview_gsm_cellType)).setText(gsm.getCellType().toString());
                ((MaterialTextView) gsmLL.findViewById(R.id.quickview_gsm_plmn)).setText(gsm.getMcc()+gsm.getMnc());
                ((MaterialTextView) gsmLL.findViewById(R.id.quickview_gsm_ci)).setText(gsm.getCiString());
                ((MaterialTextView) gsmLL.findViewById(R.id.quickview_gsm_lac)).setText(gsm.getLacString());
                ((MaterialTextView) gsmLL.findViewById(R.id.quickview_gsm_bsic)).setText(gsm.getBsicString());
                ((MaterialTextView) gsmLL.findViewById(R.id.quickview_gsm_rssi)).setText(gsm.getRssiString());
                ((MaterialTextView) gsmLL.findViewById(R.id.quickview_gsm_ber)).setText(gsm.getBitErrorRateString());
                mainLL.addView(gsmView);
                break;
            case NR:
                NRInformation nr = (NRInformation) cellInformation;
                View nrView = inflater.inflate(R.layout.quickview_nr, null, false);
                LinearLayout nrLL = nrView.findViewById(R.id.quickview_nr);
                ((MaterialTextView) nrLL.findViewById(R.id.quickview_nr_cellType)).setText(nr.getCellType().toString());
                ((MaterialTextView) nrLL.findViewById(R.id.quickview_nr_plmn)).setText(nr.getPlmn());
                ((MaterialTextView) nrLL.findViewById(R.id.quickview_nr_ci)).setText(nr.getCiString());
                ((MaterialTextView) nrLL.findViewById(R.id.quickview_nr_pci)).setText(nr.getPciString());
                ((MaterialTextView) nrLL.findViewById(R.id.quickview_nr_tac)).setText(nr.getTacString());
                ((MaterialTextView) nrLL.findViewById(R.id.quickview_nr_ssrsrp)).setText(nr.getSsrsrpString());
                ((MaterialTextView) nrLL.findViewById(R.id.quickview_nr_ssrsrq)).setText(nr.getSsrsrqString());
                ((MaterialTextView) nrLL.findViewById(R.id.quickview_nr_sssinr)).setText(nr.getSssinrString());
                mainLL.addView(nrView);
                break;
            default:
                break;
        }
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
                    addCellInformationToView(cellInformation);
                });
            }

            if (spg.getSharedPreference(SPType.default_sp).getBoolean("show_neighbour_cells", false)) {
                if(!neighborCells.isEmpty()){
                    neighborCells.forEach(cellInformation -> {
                        addCellInformationToView(cellInformation);
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
