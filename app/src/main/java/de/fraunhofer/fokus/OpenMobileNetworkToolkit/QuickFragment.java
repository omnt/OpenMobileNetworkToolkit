package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import java.util.List;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.CellInformation;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.DataProvider;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences.SharedPreferencesGrouper;

public class QuickFragment extends Fragment {
    public TelephonyManager tm;
    public PackageManager pm;
    private DataProvider dp;
    private Context context;
    private GlobalVars gv;
    private SharedPreferencesGrouper spg;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quick, container, false);
        mainLL = view.findViewById(R.id.quick_fragment);
        List<CellInformation> cil = dp.getRegisteredCells();

        // Add CardViews
        addCardView("FOOBAR", "FOOFOO");
        addCardView("FOOBAR", "FOOFOO");

        return view;
    }

    private void addCardView(String title, String text) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View cardLayoutView = layoutInflater.inflate(R.layout.stats_viewer, mainLL, false);
        CardView cardView = cardLayoutView.findViewById(R.id.stats_viewer_card);
        GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
        layoutParams.width = GridLayout.LayoutParams.WRAP_CONTENT;
        layoutParams.height = GridLayout.LayoutParams.WRAP_CONTENT;
        layoutParams.setMargins(0, 0, 0, 20);
        layoutParams.columnSpec = GridLayout.spec(0, 1f);
        layoutParams.rowSpec = GridLayout.spec(0, 1f);
        cardView.setLayoutParams(layoutParams);
        LinearLayout cardLayout = (LinearLayout) cardView.getChildAt(0);
        ((TextView) cardLayout.getChildAt(0)).setText(title);
        ((TextView) cardLayout.getChildAt(1)).setText(text);

        mainLL.addView(cardLayoutView);
    }
}
