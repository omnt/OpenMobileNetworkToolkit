/*
 *  SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SubscriptionsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SubscriptionsFragment extends Fragment {

    String TAG = "Subscriptions";

    public SubscriptionsFragment() {
        // Required empty public constructor
    }

    public static SubscriptionsFragment newInstance() {
        SubscriptionsFragment fragment = new SubscriptionsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_subscriptions, container, false);
    }

    @SuppressLint("ObsoleteSdkInt")
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        Context context = requireContext();

        if (ActivityCompat.checkSelfPermission(context,
            android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            Activity activity = (Activity) context;
            SubscriptionManager sm = (SubscriptionManager) activity.getSystemService(
                Context.TELEPHONY_SUBSCRIPTION_SERVICE);
            List<SubscriptionInfo> list;
            if (android.os.Build.VERSION.SDK_INT >= 30) {
                list = sm.getCompleteActiveSubscriptionInfoList();
            } else {
                list = sm.getActiveSubscriptionInfoList();
            }
            LinearLayout ll = view.findViewById(R.id.subscriptions_linear_layout);
            if (list.isEmpty()){
                TextView msg = new TextView(context);
                msg.append("No UICC/eUICC found");
                ll.addView(msg);
            } else {
                for (SubscriptionInfo info : list) {
                    CardView cv = new CardView(context);
                    cv.setRadius(15);
                    cv.setContentPadding(20, 10, 10, 0);
                    cv.setUseCompatPadding(true);
                    TextView title = new TextView(context);
                    title.append("SIM Slot " + info.getSimSlotIndex());
                    title.setTypeface(Typeface.DEFAULT_BOLD);
                    TextView tv = new TextView(context);
                    tv.setTextIsSelectable(true);
                    tv.append(info.toString().replace(" ", "\n").replace("{", "").replace("}", ""));
                    LinearLayout ll_inner = new LinearLayout(context);
                    ll_inner.addView(title);
                    ll_inner.addView(tv);
                    ll_inner.setOrientation(LinearLayout.VERTICAL);
                    cv.addView(ll_inner);
                    ll.addView(cv);
                }
            }
        }
    }
}