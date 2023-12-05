/*
 * SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 * SPDX-License-Identifier: apache2
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import android.content.Context;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.telephony.CarrierConfigManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Objects;

public class CarrierSettingsFragment extends Fragment {
    String TAG = "CarrierSettingsFragment";
    Context context;
    TelephonyManager tm;

    public CarrierSettingsFragment() {
        super(R.layout.fragment_carrier_settings);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_carrier_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        GlobalVars gv = GlobalVars.getInstance();
        tm = gv.getTm();
        context = requireContext();
        super.onViewCreated(view, savedInstanceState);

        Button btn_apply = requireView().findViewById(R.id.button_apply_carrier_settings);
        if (gv.isCarrier_permissions()) {
            btn_apply.setOnClickListener(this::apply_settings);
        } else {
            btn_apply.setEnabled(false);
        }

        Button btn_read = requireView().findViewById(R.id.button_read_carrier_settings);
        btn_read.setOnClickListener(this::read_settings);

        CardView cv = requireView().findViewById(R.id.carrier_settings_card_view);
        cv.setRadius(15);
        cv.setContentPadding(20, 10, 10, 0);
        cv.setUseCompatPadding(true);
        TextView tv = new TextView(context);
        tv.setText(R.string.press_the_read_button);
        cv.addView(tv);
    }

    private void read_settings(View view) {
        CardView cv = requireView().findViewById(R.id.carrier_settings_card_view);
        cv.removeAllViews();
        TableLayout tl = new TableLayout(context);
        tl.setColumnShrinkable(1, true);
        PersistableBundle cf =  tm.getCarrierConfig();
        for (String key:  cf.keySet()) {
            TextView key_column = new TextView(context);
            TextView value_column = new TextView(context);
            TableRow tr = new TableRow(context);
            key_column.setText(key.toUpperCase());
            key_column.setWidth(700);
            key_column.setTextIsSelectable(true);
            key_column.setPadding(0,0,0,20);
            Object obj = cf.get(key);
            String ret = "";
            if (obj instanceof int[] || obj instanceof long[] || obj instanceof double[] || obj instanceof boolean[]) {
                //ArrayList list = (ArrayList) obj;
                //for (Object item: list){
                //    ret = ret + " " + obj.toString() + "\n";
                //}

            } else if (obj instanceof Integer || obj instanceof Long || obj instanceof Double) {
                ret = String.valueOf(obj);
            } else if (obj instanceof String[]) {
                String[] tmp = (String[]) obj;
                for (String str: tmp) {
                    ret = ret + str.replace(",","\n") + "\n";
                }

            } else if (obj instanceof String) {
                ret = obj.toString().replace(",","\n");
            } else if (obj instanceof Boolean) {
                ret = obj.toString();
            } else if (obj instanceof PersistableBundle) {
                //PersistableBundle psb = (PersistableBundle) obj;
                //for (String key2: psb.keySet()) {
                //    ret = String.valueOf(psb.get(key2));
                //}
                ret = "persitablebundle";

            } else {
                try {
                    obj.toString();
                } catch (Exception e) {
                    //obj.toString();
                }

            }
            value_column.setText(ret);
            value_column.setPadding(50,0,0,0);
            value_column.setTextIsSelectable(true);
            tr.addView(key_column);
            tr.addView(value_column);
            tl.addView(tr);
        }
        cv.addView(tl);
    }

    private void apply_settings(View view) {
        CarrierConfigManager cs = (CarrierConfigManager) context.getSystemService(Context.CARRIER_CONFIG_SERVICE);
        cs.notifyConfigChangedForSubId(tm.getSubscriptionId());
    }
}
