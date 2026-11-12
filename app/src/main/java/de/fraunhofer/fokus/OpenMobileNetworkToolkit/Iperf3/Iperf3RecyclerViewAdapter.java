/*
 *  SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.content.res.ColorStateList;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;

import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Database.RunResult.Iperf3RunResult;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Metric.METRIC_TYPE;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Metric.MetricCalculator;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Metric.MetricView;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Parameter.Iperf3Parameter;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

/**
 * History of iPerf3 tests. Each card shows a one-line summary (status, target, mean throughput or
 * error) and expands on tap to the parameters, the metrics and the actions.
 */
public class Iperf3RecyclerViewAdapter
    extends RecyclerView.Adapter<Iperf3RecyclerViewAdapter.ViewHolder> {
    private final String TAG = "Iperf3RecyclerViewAdapter";

    /** What the user can do with a test. */
    public interface Actions {
        void onRerun(@NonNull Iperf3RunResult run);

        void onDelete(@NonNull Iperf3RunResult run);

        void onCopyCommand(@NonNull Iperf3RunResult run);
    }

    /**
     * Payload for in-place updates. With a payload, RecyclerView rebinds the existing card;
     * without one, its ItemAnimator cross-fades to a new copy, so the card blinks.
     */
    private static final Object PAYLOAD_UPDATE = new Object();

    private final Actions actions;
    private Context context;

    /** History from Room (newest first), as delivered by Iperf3RunResultDao.getAll(). */
    private List<Iperf3RunResult> items = new ArrayList<>();
    /** Live progress of running tests (testUUID -> Iperf3LiveProgress data), streamed by the worker. */
    private final HashMap<String, Data> liveProgress = new HashMap<>();
    /** Cards the user expanded. */
    private final Set<String> expanded = new HashSet<>();

    public Iperf3RecyclerViewAdapter(@NonNull Actions actions) {
        this.actions = actions;
    }

    /** Replaces the list with the runs stored in Room. */
    public void setItems(@Nullable List<Iperf3RunResult> runs) {
        this.items = runs != null ? runs : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    public Iperf3RunResult getItem(int position) {
        return items.get(position);
    }

    /** Shows live progress for a running test; only that test's card is rebound. */
    public void setLiveProgress(@NonNull String testUUID, @NonNull Data progress) {
        liveProgress.put(testUUID, progress);
        int position = indexOf(testUUID);
        if (position >= 0) notifyItemChanged(position, PAYLOAD_UPDATE);
    }

    /** The test is finished; its card shows what is stored in Room again. */
    public void clearLiveProgress(@NonNull String testUUID) {
        if (liveProgress.remove(testUUID) != null) {
            int position = indexOf(testUUID);
            if (position >= 0) notifyItemChanged(position, PAYLOAD_UPDATE);
        }
    }

    private int indexOf(String testUUID) {
        for (int i = 0; i < items.size(); i++) {
            if (testUUID.equals(items.get(i).uid)) return i;
        }
        return -1;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View v = LayoutInflater.from(context).inflate(R.layout.fragment_iperf3_row_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Iperf3RunResult test = items.get(position);
        Iperf3Parameter p = test.input.getParameter();
        boolean running = test.result == -100;
        boolean success = test.result == 0;
        // Live data only while Room still says "running"; afterwards Room has the final values.
        Data live = running ? liveProgress.get(test.uid) : null;
        // A server's direction is chosen by the connecting client: show whatever was measured.
        boolean server = p.getMode() == Iperf3Parameter.Iperf3Mode.SERVER;
        boolean showUL = server || p.getDirection() != Iperf3Parameter.Iperf3Direction.DOWN;
        boolean showDL = server || p.getDirection() != Iperf3Parameter.Iperf3Direction.UP;

        MetricCalculator metricUL = test.metricUL;
        MetricCalculator metricDL = test.metricDL;
        if (Iperf3LiveProgress.isLive(live)) {
            metricUL = Iperf3LiveProgress.getUL(live);
            metricDL = Iperf3LiveProgress.getDL(live);
        }

        holder.itemView.setTag(test.uid);

        // ---- Summary line
        holder.title.setText(title(p));
        holder.subtitle.setText(String.format(Locale.getDefault(), "%s · %s · %s",
                server ? "Listening" : directionLabel(p), server ? "TCP/UDP" : p.getProtocol().name(),
                DateUtils.getRelativeTimeSpanString(test.timestamp, System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS)));

        int colorPrimary = MaterialColors.getColor(holder.itemView, androidx.appcompat.R.attr.colorPrimary);
        int colorError = MaterialColors.getColor(holder.itemView, androidx.appcompat.R.attr.colorError);
        int colorSuccess = context.getColor(R.color.iperf3_success);

        holder.progress.setVisibility(GONE);
        holder.errorLine.setVisibility(GONE);
        if (running) {
            boolean started = Iperf3LiveProgress.isRunning(live) || (live == null && test.start != null && !server);
            String badge = server ? (Iperf3LiveProgress.isRunning(live) ? "CLIENT" : "LISTENING")
                    : (started ? "RUNNING" : "CONNECTING");
            setStatus(holder, R.drawable.ic_iperf3_running, colorPrimary, badge);
            if (server && Iperf3LiveProgress.isRunning(live) && Iperf3LiveProgress.getClient(live) != null) {
                holder.subtitle.setText("Client " + Iperf3LiveProgress.getClient(live));
            }
            // While running, show the current values.
            holder.resultSummary.setText(throughputSummary(metricUL, metricDL, showUL, showDL, true));
            int expected = server ? 0 : expectedIntervals(p); // a client decides a server test's length
            // Switching between (in)determinate requires the indicator to be hidden.
            if (started && expected > 0) {
                holder.progress.setIndeterminate(false);
                holder.progress.setMax(expected);
                holder.progress.setProgressCompat(live != null ? Iperf3LiveProgress.getIntervals(live) : 0, false);
            } else {
                holder.progress.setIndeterminate(true);
            }
            holder.progress.setIndicatorColor(colorPrimary);
            holder.progress.setVisibility(VISIBLE);
        } else if (success) {
            setStatus(holder, R.drawable.ic_iperf3_success, colorSuccess, null);
            holder.resultSummary.setText(throughputSummary(metricUL, metricDL, showUL, showDL, false));
        } else {
            setStatus(holder, R.drawable.ic_iperf3_failed, colorError, "FAILED");
            holder.resultSummary.setText("");
            holder.errorLine.setText(test.error != null && test.error.getError() != null
                    ? test.error.getError() : "The test failed.");
            holder.errorLine.setVisibility(VISIBLE);
        }

        // ---- Details
        boolean isExpanded = expanded.contains(test.uid);
        holder.details.setVisibility(isExpanded ? VISIBLE : GONE);
        holder.chevron.setRotation(isExpanded ? 180 : 0);
        holder.errorLine.setMaxLines(isExpanded ? 20 : 2);
        if (isExpanded) {
            bindParameters(holder, p);
            holder.metricViewUL.setMetricCalculator(metricUL == null ? new MetricCalculator(METRIC_TYPE.THROUGHPUT) : metricUL);
            holder.metricViewDL.setMetricCalculator(metricDL == null ? new MetricCalculator(METRIC_TYPE.THROUGHPUT) : metricDL);
            boolean showMetrics = metricUL != null || metricDL != null;
            // As a server, only show the directions the client actually tested.
            boolean ulVisible = showMetrics && showUL && (!server || metricUL != null);
            boolean dlVisible = showMetrics && showDL && (!server || metricDL != null);
            holder.metricViewUL.setVisibility(ulVisible ? VISIBLE : GONE);
            holder.metricViewDL.setVisibility(dlVisible ? VISIBLE : GONE);
            if (ulVisible) holder.metricViewUL.update();
            if (dlVisible) holder.metricViewDL.update();
            // A running test can't be rerun or deleted (stop it first).
            holder.rerun.setVisibility(running ? GONE : VISIBLE);
            holder.delete.setVisibility(running ? GONE : VISIBLE);
        }

        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            String uid = items.get(pos).uid;
            if (!expanded.remove(uid)) expanded.add(uid);
            notifyItemChanged(pos, PAYLOAD_UPDATE);
        });
        holder.itemView.setOnLongClickListener(v -> {
            actions.onCopyCommand(test);
            return true;
        });
        holder.rerun.setOnClickListener(v -> actions.onRerun(test));
        holder.delete.setOnClickListener(v -> actions.onDelete(test));
        holder.copy.setOnClickListener(v -> actions.onCopyCommand(test));
    }

    private void setStatus(ViewHolder holder, int icon, int color, @Nullable String badge) {
        holder.statusIcon.setImageResource(icon);
        holder.statusIcon.setImageTintList(ColorStateList.valueOf(color));
        if (badge == null) {
            holder.statusBadge.setVisibility(GONE);
        } else {
            holder.statusBadge.setVisibility(VISIBLE);
            holder.statusBadge.setText(badge);
            holder.statusBadge.setTextColor(color);
        }
    }

    static String title(Iperf3Parameter p) {
        String port = p.getPort() > 0 ? ":" + p.getPort() : "";
        if (p.getMode() == Iperf3Parameter.Iperf3Mode.SERVER) return "Server" + port;
        return (p.getHost() == null || p.getHost().isEmpty() ? "?" : p.getHost()) + port;
    }

    static String directionLabel(Iperf3Parameter p) {
        switch (p.getDirection()) {
            case UP: return "Upload";
            case DOWN: return "Download";
            case BIDIR: return "Both directions";
            default: return "–";
        }
    }

    /** Intervals a time-based test will report, or 0 if unknown (bytes limit). */
    static int expectedIntervals(Iperf3Parameter p) {
        if (p.getBytes() != null && !p.getBytes().trim().isEmpty()) return 0;
        int time = p.getTime() == null || p.getTime() <= 0 ? 10 : p.getTime(); // iperf3 default
        double interval = p.getInterval() > 0 ? p.getInterval() : 1.0;
        return (int) Math.ceil(time / interval);
    }

    /** Throughput in Mbit/s, compact: 296, 41.5, 6.29. */
    static String mbps(double bitsPerSecond) {
        double m = bitsPerSecond / 1e6;
        if (m >= 100) return String.format(Locale.getDefault(), "%.0f", m);
        if (m >= 10) return String.format(Locale.getDefault(), "%.1f", m);
        return String.format(Locale.getDefault(), "%.2f", m);
    }

    private static String throughputSummary(@Nullable MetricCalculator ul, @Nullable MetricCalculator dl,
                                            boolean showUL, boolean showDL, boolean current) {
        StringBuilder sb = new StringBuilder();
        if (showUL && ul != null) sb.append("↑ ").append(mbps(current ? ul.getLast() : ul.getMean()));
        if (showDL && dl != null) {
            if (sb.length() > 0) sb.append("  ");
            sb.append("↓ ").append(mbps(current ? dl.getLast() : dl.getMean()));
        }
        if (sb.length() == 0) return "";
        return sb.append(" Mbit/s").toString();
    }

    private void bindParameters(ViewHolder holder, Iperf3Parameter p) {
        String bandwidth = p.getBitrate();
        String bytes = p.getBytes();
        int streams = p.getParallel() == null ? 0 : p.getParallel();
        int srcPort = p.getCport() == null ? 0 : p.getCport();
        int duration = p.getTime() == null ? 0 : p.getTime();
        updateParam(holder.host, "HOST", p.getHost());
        updateParam(holder.dstPort, "PORT", p.getPort() == 0 ? null : String.valueOf(p.getPort()));
        updateParam(holder.bandwidth, "BW", (bandwidth == null || bandwidth.isEmpty() || bandwidth.equals("null")) ? null : bandwidth + " Mbit/s");
        updateParam(holder.duration, "TIME", duration == 0 ? null : duration + "s");
        updateParam(holder.interval, "INT", p.getInterval() == 0.0 ? null : p.getInterval() + "s");
        updateParam(holder.bytes, "LIMIT", (bytes == null || bytes.isEmpty() || bytes.equals("null")) ? null : bytes);
        updateParam(holder.streams, "STREAMS", streams == 0 ? null : String.valueOf(streams));
        updateParam(holder.srcPort, "CPORT", srcPort == 0 ? null : String.valueOf(srcPort));
        updateParam(holder.mode, "MODE", String.valueOf(p.getMode()));
        updateParam(holder.protocol, "PROTO", String.valueOf(p.getProtocol()));
        updateParam(holder.direction, "DIR", String.valueOf(p.getDirection()));
    }

    private void updateParam(LinearLayout container, String label, String value) {
        if (value == null || value.isEmpty() || value.equalsIgnoreCase("null") || value.equals("0") || value.equals("0s") || value.equals("0.0s")) {
            container.setVisibility(GONE);
        } else {
            container.setVisibility(VISIBLE);
            ((TextView) container.findViewById(R.id.parameter_label)).setText(label);
            ((TextView) container.findViewById(R.id.text_parameter)).setText(value);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView statusIcon;
        private final TextView title;
        private final TextView subtitle;
        private final TextView statusBadge;
        private final TextView resultSummary;
        private final ImageView chevron;
        private final LinearProgressIndicator progress;
        private final TextView errorLine;
        private final LinearLayout details;
        private final MaterialButton rerun;
        private final MaterialButton delete;
        private final MaterialButton copy;
        private final MetricView metricViewDL;
        private final MetricView metricViewUL;

        private LinearLayout host, dstPort, bandwidth, duration, interval, bytes, streams, srcPort,
                mode, protocol, direction;

        public ViewHolder(View itemView) {
            super(itemView);
            statusIcon = itemView.findViewById(R.id.iperf3_status_icon);
            title = itemView.findViewById(R.id.iperf3_title);
            subtitle = itemView.findViewById(R.id.iperf3_subtitle);
            statusBadge = itemView.findViewById(R.id.iperf3_status_badge);
            resultSummary = itemView.findViewById(R.id.iperf3_result_summary);
            chevron = itemView.findViewById(R.id.iperf3_expand_chevron);
            progress = itemView.findViewById(R.id.progress_indicator);
            errorLine = itemView.findViewById(R.id.iperf3_error_line);
            details = itemView.findViewById(R.id.iperf3_details);
            rerun = itemView.findViewById(R.id.iperf3_rerun_btn);
            delete = itemView.findViewById(R.id.iperf3_delete_btn);
            copy = itemView.findViewById(R.id.iperf3_copy_btn);
            setupParameterFlexBox(LayoutInflater.from(context));

            LinearLayout metricLL = itemView.findViewById(R.id.metrics_iperf3_ll);
            metricViewUL = new MetricView(context);
            metricViewDL = new MetricView(context);
            metricViewUL.setup("Upload [Mbit/s]");
            metricViewDL.setup("Download [Mbit/s]");
            metricLL.addView(metricViewUL);
            metricLL.addView(metricViewDL);
        }

        private void setupParameterFlexBox(LayoutInflater li) {
            FlexboxLayout fl = itemView.findViewById(R.id.parameter_iperf3_fl);
            host = add(li, fl);
            dstPort = add(li, fl);
            direction = add(li, fl);
            protocol = add(li, fl);
            duration = add(li, fl);
            bytes = add(li, fl);
            bandwidth = add(li, fl);
            streams = add(li, fl);
            interval = add(li, fl);
            srcPort = add(li, fl);
            mode = add(li, fl);
        }

        private LinearLayout add(LayoutInflater li, FlexboxLayout fl) {
            LinearLayout v = (LinearLayout) li.inflate(R.layout.parameter_view, fl, false);
            fl.addView(v);
            return v;
        }
    }
}
