/*
 * SPDX-FileCopyrightText:  2025 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Metric;

import static android.widget.LinearLayout.HORIZONTAL;
import static android.widget.LinearLayout.VERTICAL;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class MetricView extends CardView {

    private LinearLayout mean;
    private LinearLayout median;
    private LinearLayout max;
    private LinearLayout min;
    private LinearLayout last;
    private TextView directionName;
    private MetricCalculator metricCalculator;

    public MetricView(Context context) {
        super(context);
        init(context, null);
    }
    public MetricView(MetricCalculator metricCalculator, Context context) {
        super(context);
        init(context, null);
        this.metricCalculator = metricCalculator;
    }
    public MetricView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MetricView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        // You may want to customize view via XML attributes if needed.
    }

    public void setup(String title) {

        removeAllViews();
        LinearLayout cardView = new LinearLayout(getContext());
        cardView.setOrientation(VERTICAL);

        directionName = new TextView(getContext());
        directionName.setText(title);
        directionName.setTextAppearance(R.style.Base_TextAppearance_AppCompat_Light_Widget_PopupMenu_Large);
        directionName.setTextColor(getContext().getColor(R.color.material_dynamic_secondary100));
        cardView.addView(directionName);

        LinearLayout cardViewResult = new LinearLayout(getContext());
        cardViewResult.setOrientation(HORIZONTAL);
        cardViewResult.setLayoutParams(new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        ));

        mean = createTile("mean", 0, 10);
        median = createTile("median", 10, 10);
        max = createTile("max", 10, 10);
        min = createTile("min", 10, 10);
        last = createTile("last", 10, 0);

        cardViewResult.addView(mean);
        cardViewResult.addView(median);
        cardViewResult.addView(max);
        cardViewResult.addView(min);
        cardViewResult.addView(last);

        cardView.addView(cardViewResult);
        addView(cardView);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        );
        cardView.setPadding(20, 20, 20 ,20);


        GradientDrawable gd = new GradientDrawable();
        gd.setColor(getContext().getColor( R.color.material_dynamic_secondary30));
        gd.setCornerRadius(10);
//        gd.setStroke(2, 0xFF000000);
        this.setBackground(gd);

        params.setMargins(20 , 10, 20, 10);
        this.setLayoutParams(params);
    }

    private LinearLayout createTile(String key, int marginLeft, int marginRight) {
        Context ct = getContext();
        LinearLayout ll = new LinearLayout(ct);
        ll.setOrientation(VERTICAL);
        ll.setGravity(Gravity.CENTER);

        GradientDrawable gd = new GradientDrawable();
        gd.setColor(ct.getColor(R.color.material_dynamic_primary100));
        gd.setCornerRadius(10);
        gd.setStroke(2, 0xFF000000);
        ll.setBackground(gd);

        LinearLayout.LayoutParams tileParams = new LinearLayout.LayoutParams(200, 150);
        tileParams.weight = 1;
        tileParams.setMargins(marginLeft, 10, marginRight, 10);
        ll.setLayoutParams(tileParams);

        TextView keyView = new TextView(ct);
        keyView.setGravity(Gravity.CENTER);
        keyView.setText(key);
        LinearLayout.LayoutParams keyViewParams = new LinearLayout.LayoutParams(200, 0);
        keyViewParams.weight = 1;
        keyViewParams.setMargins(0, 0, 0, 10);
        keyView.setLayoutParams(keyViewParams);
        keyView.setTypeface(null, Typeface.BOLD);
        keyView.setTextAppearance(R.style.Base_TextAppearance_AppCompat_Medium);
        keyView.setTextColor(ct.getColor(R.color.material_dynamic_primary10));
        keyView.setGravity(Gravity.CENTER);


        TextView valueView = new TextView(ct);
        valueView.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams valueViewParams = new LinearLayout.LayoutParams(200, 0);
        valueViewParams.weight = 1;
        valueView.setLayoutParams(valueViewParams);
        valueView.setTextAppearance(R.style.TextAppearance_AppCompat_Small);
        valueView.setTextColor(ct.getColor(R.color.material_dynamic_primary10));


        ll.addView(keyView);
        ll.addView(valueView);
        return ll;
    }

    public void update(Double value) {
        if (metricCalculator == null) return;

        metricCalculator.update(value);
        update();
    }

    public void update(){
        metricCalculator.calcAll();
        ((TextView) mean.getChildAt(1)).setText(metricCalculator.getFormattedString(metricCalculator.getMean()));
        ((TextView) median.getChildAt(1)).setText(metricCalculator.getFormattedString(metricCalculator.getMedian()));
        ((TextView) max.getChildAt(1)).setText(metricCalculator.getFormattedString(metricCalculator.getMax()));
        ((TextView) min.getChildAt(1)).setText(metricCalculator.getFormattedString(metricCalculator.getMin()));
        ((TextView) last.getChildAt(1)).setText(metricCalculator.getFormattedString(metricCalculator.getLast()));
    }

    public void setMetricCalculator(MetricCalculator metricCalculator) {
        this.metricCalculator = metricCalculator;
    }

    public MetricCalculator getMetricCalculator() {
        return metricCalculator;
    }
}
