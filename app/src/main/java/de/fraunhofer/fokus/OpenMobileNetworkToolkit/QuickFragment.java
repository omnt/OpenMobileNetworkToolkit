package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.StringRes;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.android.material.textview.MaterialTextView;

import java.util.List;
import java.util.Objects;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.CellInformations.CDMAInformation;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.CellInformations.CellInformation;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.CellInformations.GSMInformation;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.CellInformations.LTEInformation;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.CellInformations.NRInformation;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.DataProvider;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SPType;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SharedPreferencesGrouper;


public class QuickFragment extends Fragment {
    private DataProvider dp;
    private Context context;
    private SharedPreferencesGrouper spg;
    private Handler updateUIHandler;
    private LinearLayout mainLL;
    private static final int TEXT_SIZE_LARGE = 20;
    private static final int TEXT_SIZE_MEDIUM = 16;
    private static final int TEXT_SIZE_SMALL = 14;
    private static final int TEXT_SIZE_XSMALL = 10;

    public QuickFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = requireContext();
        GlobalVars gv = GlobalVars.getInstance();
        dp = gv.get_dp();
        spg = SharedPreferencesGrouper.getInstance(context);
        updateUIHandler = new Handler(Objects.requireNonNull(Looper.myLooper()));
    }


    /**
     * Determine the text size of the textview based on the length of the value
     * @param value The value to be displayed
     * @return The text size
     */
    private int determineTextSize(String value) {
        if(value.length() < 3) return TEXT_SIZE_LARGE;
        if(value.length() < 7) return TEXT_SIZE_MEDIUM;
        if(value.length() < 9) return TEXT_SIZE_SMALL;

        return TEXT_SIZE_XSMALL;
    }

    /**
     * Get the color based on the value
     * @param min The minimum value
     * @param max The maximum value
     * @param value The value
     * @param minHex The minimum color
     * @param maxHex The maximum color
     * @return Color
     */
    public int getColor(int min, int max, float value, String minHex, String maxHex) {
        if (value < min) value = min;
        if (value > max) value = max;

        float normalizedValue = (value - min) / (max - min);

        int minColor = Color.parseColor("#"+minHex);
        int maxColor = Color.parseColor("#"+maxHex);

        int red = (int) ((1 - normalizedValue) * Color.red(minColor) + normalizedValue * Color.red(maxColor));
        int green = (int) ((1 - normalizedValue) * Color.green(minColor) + normalizedValue * Color.green(maxColor));
        int blue = (int) ((1 - normalizedValue) * Color.blue(minColor) + normalizedValue * Color.blue(maxColor));

        return Color.rgb(red, green, blue);
    }

    /**
     * Modify the textview
     * @param textView The textview to be modified
     * @param value The value to be displayed
     * @param min The minimum value
     * @param minColor The minimum color
     * @param max The maximum value
     * @param maxColor The maximum color
     */
    private void modifyTextView(MaterialTextView textView, String value,
                                int min, int minColor,
                                int max, int maxColor) {
        textView.setText(value);
        if (value.equals(String.valueOf(Integer.MAX_VALUE)) || value.equals("nullnull")) {
            textView.setText("N/A");
            return;
        }
        textView.setTextSize(determineTextSize(value));
        if(min == Integer.MAX_VALUE || max == Integer.MAX_VALUE) return;
        CardView parent = (CardView) textView.getParent().getParent();
        parent.setCardBackgroundColor(getColor(min, max, Float.parseFloat(value),
                Integer.toHexString(context.getColor(minColor)).substring(2),
                Integer.toHexString(context.getColor(maxColor)).substring(2)));
    }


    /**
     * Add the cell information to the view
     * @param cellInformation The cell information
     */
    private void addCellInformationToView(CellInformation cellInformation){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        switch (cellInformation.getCellType()) {
            case LTE:
                //mainLL.addView(cellInformation.createQuickView(context));
                LTEInformation lte = (LTEInformation) cellInformation;
                View lteView = inflater.inflate(R.layout.quickview_lte, null, false);
                LinearLayout lteLL = lteView.findViewById(R.id.quickview_lte);
                modifyTextView( lteLL.findViewById(R.id.quickview_lte_cellType),
                        lte.getCellType().toString(),
                        Integer.MAX_VALUE, -1,
                        Integer.MAX_VALUE, -1);
                modifyTextView( lteLL.findViewById(R.id.quickview_lte_plmn),
                        lte.getMcc() + lte.getMnc(),
                        Integer.MAX_VALUE, -1,
                        Integer.MAX_VALUE, -1);
                modifyTextView( lteLL.findViewById(R.id.quickview_lte_ci),
                        lte.getCiString(),
                        Integer.MAX_VALUE, -1,
                        Integer.MAX_VALUE, -1);
                modifyTextView( lteLL.findViewById(R.id.quickview_lte_pci),
                        lte.getPciString(),
                        Integer.MAX_VALUE, -1,
                        Integer.MAX_VALUE, -1);
                modifyTextView( lteLL.findViewById(R.id.quickview_lte_tac),
                        lte.getTacString(),
                        Integer.MAX_VALUE, -1,
                        Integer.MAX_VALUE, -1);

                modifyTextView( lteLL.findViewById(R.id.quickview_lte_rsrp),
                        lte.getRsrpString(),
                        -140, R.color.radio_red,
                        -43, R.color.radio_green);
                modifyTextView( lteLL.findViewById(R.id.quickview_lte_rsrq),
                        lte.getRsrqString(),
                        -34, R.color.radio_red,
                        -3, R.color.radio_green);
                modifyTextView( lteLL.findViewById(R.id.quickview_lte_rssi),
                        lte.getRssiString(),
                        -113, R.color.radio_red,
                        -51, R.color.radio_green);
                modifyTextView( lteLL.findViewById(R.id.quickview_lte_rssnr),
                        lte.getRssnrString(),
                        -20, R.color.radio_red,
                        30, R.color.radio_green);
                modifyTextView( lteLL.findViewById(R.id.quickview_lte_cqi),
                        lte.getCqiString(),
                        0, R.color.radio_red,
                        15, R.color.radio_green);
                mainLL.addView(lteView);
                break;
            case CDMA:
                CDMAInformation cdma = (CDMAInformation) cellInformation;
                View cdmaView = inflater.inflate(R.layout.quickview_cdma, null, false);
                LinearLayout cdmaLL = cdmaView.findViewById(R.id.quickview_cdma);

                modifyTextView( cdmaLL.findViewById(R.id.quickview_cdma_pci),
                        cdma.getPciString(),
                        Integer.MAX_VALUE, -1,
                        Integer.MAX_VALUE, -1);
                modifyTextView( cdmaLL.findViewById(R.id.quickview_cdma_tac),
                        cdma.getTacString(),
                        Integer.MAX_VALUE, -1,
                        Integer.MAX_VALUE, -1);
                modifyTextView( cdmaLL.findViewById(R.id.quickview_cdma_mnc),
                        cdma.getMnc(),
                        Integer.MAX_VALUE, -1,
                        Integer.MAX_VALUE, -1);
                modifyTextView( cdmaLL.findViewById(R.id.quickview_cdma_cellType),
                        cdma.getCellType().toString(),
                        Integer.MAX_VALUE, -1,
                        Integer.MAX_VALUE, -1);
                modifyTextView( cdmaLL.findViewById(R.id.quickview_cdma_ci),
                        cdma.getCiString(),
                        Integer.MAX_VALUE, -1,
                        Integer.MAX_VALUE, -1);

                modifyTextView( cdmaLL.findViewById(R.id.quickview_cdma_dbm),
                        cdma.getCmdaDbmString(),
                        -120, R.color.radio_red,
                        -70, R.color.radio_green);
                modifyTextView( cdmaLL.findViewById(R.id.quickview_cdma_ecio),
                        cdma.getEvdoEcioString(),
                        0, R.color.radio_green,
                        20, R.color.radio_red);
                modifyTextView( cdmaLL.findViewById(R.id.quickview_cdma_evdo_dbm),
                        cdma.getEvdoDbmString(),
                        -120, R.color.radio_red,
                        -70, R.color.radio_green);
                modifyTextView( cdmaLL.findViewById(R.id.quickview_cdma_evdo_snr),
                        cdma.getEvdoSnrString(),
                        0, R.color.radio_red,
                        20, R.color.radio_green);
                mainLL.addView(cdmaView);
                break;
            case UMTS:
                //mainLL.addView(cellInformation.createQuickView(context));
                break;
            case GSM:
                GSMInformation gsm = (GSMInformation) cellInformation;
                View gsmView = inflater.inflate(R.layout.quickview_gsm, null, false);
                LinearLayout gsmLL = gsmView.findViewById(R.id.quickview_gsm);
                modifyTextView( gsmLL.findViewById(R.id.quickview_gsm_cellType),
                        gsm.getCellType().toString(),
                        Integer.MAX_VALUE, -1,
                        Integer.MAX_VALUE, -1);
                modifyTextView( gsmLL.findViewById(R.id.quickview_gsm_plmn),
                        gsm.getMcc() + gsm.getMnc(),
                        Integer.MAX_VALUE, -1,
                        Integer.MAX_VALUE, -1);
                modifyTextView( gsmLL.findViewById(R.id.quickview_gsm_ci),
                        gsm.getCiString(),
                        Integer.MAX_VALUE, -1,
                        Integer.MAX_VALUE, -1);
                modifyTextView( gsmLL.findViewById(R.id.quickview_gsm_lac),
                        gsm.getLacString(),
                        Integer.MAX_VALUE, -1,
                        Integer.MAX_VALUE, -1);
                modifyTextView( gsmLL.findViewById(R.id.quickview_gsm_bsic),
                        gsm.getBsicString(),
                        Integer.MAX_VALUE, -1,
                        Integer.MAX_VALUE, -1);

                modifyTextView( gsmLL.findViewById(R.id.quickview_gsm_rssi),
                        gsm.getRssiString(),
                        -40, R.color.radio_green,
                        -110, R.color.radio_red);
                modifyTextView( gsmLL.findViewById(R.id.quickview_gsm_ber),
                        gsm.getBitErrorRateString(),
                        0, R.color.radio_green,
                        7, R.color.radio_red);
                mainLL.addView(gsmView);
                break;
            case NR:
                NRInformation nr = (NRInformation) cellInformation;
                View nrView = inflater.inflate(R.layout.quickview_nr, null, false);
                LinearLayout nrLL = nrView.findViewById(R.id.quickview_nr);
                modifyTextView( nrLL.findViewById(R.id.quickview_nr_cellType),
                        nr.getCellType().toString(),
                        Integer.MAX_VALUE, -1,
                        Integer.MAX_VALUE, -1);
                modifyTextView( nrLL.findViewById(R.id.quickview_nr_plmn),
                        nr.getPlmn(),
                        Integer.MAX_VALUE, -1,
                        Integer.MAX_VALUE, -1);
                modifyTextView( nrLL.findViewById(R.id.quickview_nr_ci),
                        nr.getCiString(),
                        Integer.MAX_VALUE, -1,
                        Integer.MAX_VALUE, -1);
                modifyTextView( nrLL.findViewById(R.id.quickview_nr_pci),
                        nr.getPciString(),
                        Integer.MAX_VALUE, -1,
                        Integer.MAX_VALUE, -1);
                modifyTextView( nrLL.findViewById(R.id.quickview_nr_tac),
                        nr.getTacString(),
                        Integer.MAX_VALUE, -1,
                        Integer.MAX_VALUE, -1);

                modifyTextView( nrLL.findViewById(R.id.quickview_nr_ssrsrp),
                        nr.getSsrsrpString(),
                        -140, R.color.radio_red,
                        -51, R.color.radio_green);
                modifyTextView( nrLL.findViewById(R.id.quickview_nr_ssrsrq),
                        nr.getSsrsrqString(),
                        -43, R.color.radio_red,
                        20, R.color.radio_green);
                modifyTextView( nrLL.findViewById(R.id.quickview_nr_sssinr),
                        nr.getSssinrString(),
                        -23, R.color.radio_red,
                        40, R.color.radio_green);
                mainLL.addView(nrView);
                break;
            default:
                break;
        }
    }


     final Runnable updateUI = new Runnable() {
        @Override
        public void run() {
            mainLL.removeAllViews();
            List<CellInformation> cellInformationList = dp.getRegisteredCells();
            List<CellInformation> neighborCells = dp.getNeighbourCellInformation();
            if(cellInformationList.isEmpty()){
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
                errorText.setText(getSafeString(R.string.cell_na));
                error.addView(errorText);
                mainLL.addView(error);
            } else {
                cellInformationList.forEach(cellInformation -> addCellInformationToView(cellInformation));
            }
            if (spg.getSharedPreference(SPType.default_sp).getBoolean("show_neighbour_cells", false)) {
                if(!neighborCells.isEmpty()){
                    neighborCells.forEach(cellInformation -> addCellInformationToView(cellInformation));
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

    public String getSafeString(@StringRes int resId) {
        if (context == null) {
            return "N/A";
        } else {
            return context.getResources().getString(resId);
        }
    }
}
