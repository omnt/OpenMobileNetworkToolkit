package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Metric;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class MetricView extends View {
    private LinearLayout mean;
    private LinearLayout median;
    private LinearLayout max;
    private LinearLayout min;
    private LinearLayout last;
    private TextView directionName;
    private final MetricCalculator metricCalculator;
    private final Context ct;
    private LinearLayout mainLL;

    public MetricView(MetricCalculator metricCalculator, Context ct) {
        super(ct);
        this.metricCalculator = metricCalculator;
        this.ct = ct;
    }

    private LinearLayout createTile(String key) {
        LinearLayout ll = new LinearLayout(ct);
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(ct.getColor(R.color.cardview_dark_background));
        gd.setCornerRadius(10);
        gd.setStroke(2, 0xFF000000);
        ll.setBackground(gd);
        ll.setMinimumHeight(ll.getWidth());
        ll.setGravity(Gravity.CENTER);

        ll.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams foo = new LinearLayout.LayoutParams(200, 150);
        foo.weight = 1;
        foo.setMargins(10, 10, 10, 10);
        ll.setLayoutParams(foo);
        TextView keyView = new TextView(ct);
        keyView.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams keyViewLayoutParams = new LinearLayout.LayoutParams(200, 50);
        keyViewLayoutParams.setMargins(0, 0, 0, 10);
        keyView.setLayoutParams(keyViewLayoutParams);
        keyView.setTypeface(null, Typeface.BOLD);
        keyView.setText(key);
        ll.addView(keyView);
        TextView valueView = new TextView(ct);
        valueView.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams valueViewLayoutParams = new LinearLayout.LayoutParams(200, 50);
        valueViewLayoutParams.setMargins(0, 0, 0, 0);
        valueView.setLayoutParams(valueViewLayoutParams);
        ll.addView(valueView);
        return ll;
    }

    private LinearLayout createLL(String key) {
        LinearLayout ll = null;
        switch (key) {
            case "mean":
                mean = createTile(key);
                ll = mean;
                break;
            case "median":
                median = createTile(key);
                ll = median;
                break;
            case "max":
                max = createTile(key);
                ll = max;
                break;
            case "min":
                min = createTile(key);
                ll = min;
                break;
            case "last":
                last = createTile(key);
                ll = last;
                break;
        }
        return ll;
    }

    public LinearLayout createMainLL(String direction) {
        mainLL = new LinearLayout(ct);
        LinearLayout.LayoutParams foo1 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        mainLL.setOrientation(LinearLayout.VERTICAL);
        mainLL.setLayoutParams(foo1);

        directionName = new TextView(ct);
        directionName.setText(direction);
        mainLL.addView(directionName);

        LinearLayout cardViewResult = new LinearLayout(ct);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardViewResult.setOrientation(LinearLayout.HORIZONTAL);
        cardViewResult.setLayoutParams(cardParams);

        cardViewResult.addView(createLL("mean"));
        cardViewResult.addView(createLL("median"));
        cardViewResult.addView(createLL("max"));
        cardViewResult.addView(createLL("min"));
        cardViewResult.addView(createLL("last"));
        mainLL.addView(cardViewResult);
        return mainLL;
    }

    public void update(Double value) {
        metricCalculator.update(value);

        ((TextView) mean.getChildAt(1)).setText(String.format(" %s", metricCalculator.getFormattedString(metricCalculator.getMean())));
        ((TextView) median.getChildAt(1)).setText(String.format(" %s", metricCalculator.getFormattedString(metricCalculator.getMedian())));
        ((TextView) max.getChildAt(1)).setText(String.format(" %s", metricCalculator.getFormattedString(metricCalculator.getMax())));
        ((TextView) min.getChildAt(1)).setText(String.format(" %s", metricCalculator.getFormattedString(metricCalculator.getMin())));
        ((TextView) last.getChildAt(1)).setText(String.format(" %s", metricCalculator.getFormattedString(value)));
    }

    public void setVisibility(int visibility) {
        mainLL.setVisibility(visibility);
    }

    public MetricCalculator getMetricCalculator() {
        return metricCalculator;
    }
}
