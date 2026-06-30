
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
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
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
        this.metricCalculator = metricCalculator;
        init(context, null);
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
        setRadius(dpToPx(16));
        setCardElevation(0);
        setPreventCornerOverlap(true);

        int bgColor = getThemeColor(R.attr.colorSurfaceVariant, 0xFFF0F0F0);
        setCardBackgroundColor(bgColor);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        );
        int m8 = dpToPx(8);
        params.setMargins(0, m8, 0, m8);
        this.setLayoutParams(params);
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private int getThemeColor(int attr, int fallback) {
        TypedValue typedValue = new TypedValue();
        if (getContext().getTheme().resolveAttribute(attr, typedValue, true)) {
            return typedValue.data;
        }
        return fallback;
    }

    public void setup(String title) {
        removeAllViews();

        LinearLayout mainLayout = new LinearLayout(getContext());
        mainLayout.setOrientation(VERTICAL);
        int p16 = dpToPx(16);
        mainLayout.setPadding(p16, p16, p16, p16);
        mainLayout.setLayoutParams(new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        ));

        directionName = new TextView(getContext());
        directionName.setText(title.toUpperCase());
        directionName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        directionName.setLetterSpacing(0.05f);
        directionName.setTypeface(Typeface.create("sans-serif-black", Typeface.NORMAL));
        directionName.setTextColor(getThemeColor(R.attr.colorPrimary, Color.BLUE));
        directionName.setPadding(0, 0, 0, dpToPx(12));
        mainLayout.addView(directionName);

        LinearLayout statsLayout = new LinearLayout(getContext());
        statsLayout.setOrientation(HORIZONTAL);
        statsLayout.setGravity(Gravity.CENTER_VERTICAL);
        statsLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        ));

        mean = createTile("AVG");
        median = createTile("MED");
        max = createTile("MAX");
        min = createTile("MIN");
        last = createTile("CUR");

        statsLayout.addView(mean);
        statsLayout.addView(median);
        statsLayout.addView(max);
        statsLayout.addView(min);
        statsLayout.addView(last);

        mainLayout.addView(statsLayout);
        addView(mainLayout);
    }

    private LinearLayout createTile(String label) {
        Context ct = getContext();
        LinearLayout ll = new LinearLayout(ct);
        ll.setOrientation(VERTICAL);
        ll.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams tileParams = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f);
        ll.setLayoutParams(tileParams);

        TextView labelView = new TextView(ct);
        labelView.setText(label);
        labelView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        labelView.setAlpha(0.7f);
        labelView.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        labelView.setTextColor(getThemeColor(R.attr.colorOnSurfaceVariant, Color.GRAY));
        labelView.setGravity(Gravity.CENTER);

        TextView valueView = new TextView(ct);
        valueView.setText("0.0");
        valueView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        valueView.setTypeface(Typeface.DEFAULT_BOLD);
        valueView.setTextColor(getThemeColor(R.attr.colorOnSurface, Color.BLACK));
        valueView.setGravity(Gravity.CENTER);
        valueView.setPadding(0, dpToPx(4), 0, 0);

        ll.addView(labelView);
        ll.addView(valueView);
        return ll;
    }

    public void update(Double value) {
        if (metricCalculator == null) return;
        metricCalculator.update(value);
        update();
    }

    public void update() {
        if (metricCalculator == null || mean == null) return;
        metricCalculator.calcAll();
        updateTextView(mean, metricCalculator.getMean());
        updateTextView(median, metricCalculator.getMedian());
        updateTextView(max, metricCalculator.getMax());
        updateTextView(min, metricCalculator.getMin());
        updateTextView(last, metricCalculator.getLast());
    }

    private void updateTextView(LinearLayout container, double value) {
        if (container != null && container.getChildCount() > 1) {
            View child = container.getChildAt(1);
            if (child instanceof TextView) {
                TextView tv = (TextView) child;
                tv.setText(metricCalculator.getFormattedString(value));
            }
        }
    }

    public void setMetricCalculator(MetricCalculator metricCalculator) {
        this.metricCalculator = metricCalculator;
    }

    public MetricCalculator getMetricCalculator() {
        return metricCalculator;
    }
}
