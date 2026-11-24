/*
 * SPDX-FileCopyrightText: 2025 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.color.MaterialColors;

import java.util.ArrayList;
import java.util.List;

/**
 * Minimal line chart of the throughput per interval of the running test: upload in the theme's
 * primary color, download in its tertiary color, both scaled to the largest value so far.
 */
public class Iperf3SparklineView extends View {
    private final List<Double> upload = new ArrayList<>();
    private final List<Double> download = new ArrayList<>();
    private final Paint uploadPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint downloadPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint baselinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();

    public Iperf3SparklineView(Context context) {
        this(context, null);
    }

    public Iperf3SparklineView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        float stroke = 2.5f * getResources().getDisplayMetrics().density;
        setup(uploadPaint, MaterialColors.getColor(this, androidx.appcompat.R.attr.colorPrimary), stroke);
        setup(downloadPaint, MaterialColors.getColor(this, com.google.android.material.R.attr.colorTertiary), stroke);
        setup(baselinePaint, MaterialColors.getColor(this, com.google.android.material.R.attr.colorOutlineVariant),
                getResources().getDisplayMetrics().density);
    }

    private static void setup(Paint paint, int color, float width) {
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(width);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
    }

    /** Removes all points. */
    public void clear() {
        upload.clear();
        download.clear();
        invalidate();
    }

    /** Adds one interval; pass null for a direction the test does not measure. */
    public void add(@Nullable Double uploadBps, @Nullable Double downloadBps) {
        if (uploadBps != null) upload.add(uploadBps);
        if (downloadBps != null) download.add(downloadBps);
        invalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth() - getPaddingLeft() - getPaddingRight();
        float h = getHeight() - getPaddingTop() - getPaddingBottom();
        float bottom = getPaddingTop() + h;
        canvas.drawLine(getPaddingLeft(), bottom, getPaddingLeft() + w, bottom, baselinePaint);

        double max = 0;
        for (double v : upload) max = Math.max(max, v);
        for (double v : download) max = Math.max(max, v);
        if (max <= 0) return;
        int points = Math.max(upload.size(), download.size());
        drawSeries(canvas, upload, uploadPaint, max, points, w, h);
        drawSeries(canvas, download, downloadPaint, max, points, w, h);
    }

    private void drawSeries(Canvas canvas, List<Double> values, Paint paint, double max,
                            int points, float w, float h) {
        if (values.isEmpty()) return;
        // Leave room for the stroke at the top.
        float top = getPaddingTop() + paint.getStrokeWidth();
        float usable = h - paint.getStrokeWidth();
        float step = points > 1 ? w / (points - 1) : 0;
        path.reset();
        for (int i = 0; i < values.size(); i++) {
            float x = getPaddingLeft() + i * step;
            float y = top + usable - (float) (values.get(i) / max) * usable;
            if (i == 0) path.moveTo(x, y);
            else path.lineTo(x, y);
        }
        if (values.size() == 1) path.lineTo(getPaddingLeft() + 1, top + usable - (float) (values.get(0) / max) * usable);
        canvas.drawPath(path, paint);
    }
}
