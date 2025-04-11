package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Metric;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class MetricView extends LinearLayout {

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
        setOrientation(VERTICAL);
        // You may want to customize view via XML attributes if needed.
    }

    public void setup(String title) {

        removeAllViews();

        directionName = new TextView(getContext());
        directionName.setText(title);
        addView(directionName);

        LinearLayout cardViewResult = new LinearLayout(getContext());
        cardViewResult.setOrientation(HORIZONTAL);
        cardViewResult.setLayoutParams(new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        ));

        mean = createTile("mean");
        median = createTile("median");
        max = createTile("max");
        min = createTile("min");
        last = createTile("last");

        cardViewResult.addView(mean);
        cardViewResult.addView(median);
        cardViewResult.addView(max);
        cardViewResult.addView(min);
        cardViewResult.addView(last);

        addView(cardViewResult);
    }

    private LinearLayout createTile(String key) {
        Context ct = getContext();
        LinearLayout ll = new LinearLayout(ct);
        ll.setOrientation(VERTICAL);
        ll.setGravity(Gravity.CENTER);

        GradientDrawable gd = new GradientDrawable();
        gd.setColor(ct.getColor(R.color.cardview_dark_background));
        gd.setCornerRadius(10);
        gd.setStroke(2, 0xFF000000);
        ll.setBackground(gd);

        LinearLayout.LayoutParams tileParams = new LinearLayout.LayoutParams(200, 150);
        tileParams.weight = 1;
        tileParams.setMargins(10, 10, 10, 10);
        ll.setLayoutParams(tileParams);

        TextView keyView = new TextView(ct);
        keyView.setGravity(Gravity.CENTER);
        keyView.setTypeface(null, Typeface.BOLD);
        keyView.setText(key);
        LinearLayout.LayoutParams keyViewParams = new LinearLayout.LayoutParams(200, 50);
        keyViewParams.setMargins(0, 0, 0, 10);
        keyView.setLayoutParams(keyViewParams);

        TextView valueView = new TextView(ct);
        valueView.setGravity(Gravity.CENTER);
        valueView.setLayoutParams(new LinearLayout.LayoutParams(200, 50));

        ll.addView(keyView);
        ll.addView(valueView);
        return ll;
    }

    public void update(Double value) {
        if (metricCalculator == null) return;

        metricCalculator.update(value);

        ((TextView) mean.getChildAt(1)).setText(metricCalculator.getFormattedString(metricCalculator.getMean()));
        ((TextView) median.getChildAt(1)).setText(metricCalculator.getFormattedString(metricCalculator.getMedian()));
        ((TextView) max.getChildAt(1)).setText(metricCalculator.getFormattedString(metricCalculator.getMax()));
        ((TextView) min.getChildAt(1)).setText(metricCalculator.getFormattedString(metricCalculator.getMin()));
        ((TextView) last.getChildAt(1)).setText(metricCalculator.getFormattedString(value));
    }

    public void setMetricCalculator(MetricCalculator metricCalculator) {
        this.metricCalculator = metricCalculator;
    }

    public MetricCalculator getMetricCalculator() {
        return metricCalculator;
    }
}
