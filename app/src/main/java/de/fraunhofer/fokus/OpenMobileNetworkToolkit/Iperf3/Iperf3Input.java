package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Fragments.Input.Iperf3CardFragment;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class Iperf3Input {
    private static final String[] EXCLUDED_FIELDS = {
            "measurementName", "rawFile", "logFileName", "command", "lineProtocolFile",
            "context", "timestamp", "uuid", "cardView", "main", "EXCLUDED_FIELDS"
    };

    private boolean isBidir;
    private boolean isReverse;
    private boolean isJson;
    private boolean isOneOff;
    private int idxMode;
    private int idxProtocol;
    private String uuid;
    private String command;
    private String rawFile;
    private String logFileName;
    private String measurementName;
    private String ip;
    private String port;
    private String bandwidth;
    private String lineProtocolFile;
    private String duration;
    private String interval;
    private String bytes;
    private String streams;
    private String cport;
    private Timestamp timestamp;
    private LinearLayout main;
    private Context context;
    private CardView cardView;

    public Iperf3Input() {
        this(null);
    }

    public Iperf3Input(Context context) {
        this.isBidir = false;
        this.isReverse = false;
        this.isJson = false;
        this.isOneOff = false;
        this.idxMode = 0;
        this.idxProtocol = 0;
        this.uuid = "";
        this.command = "";
        this.rawFile = "";
        this.logFileName = "";
        this.measurementName = "";
        this.ip = "";
        this.port = "";
        this.bandwidth = "";
        this.lineProtocolFile = "";
        this.duration = "";
        this.interval = "";
        this.bytes = "";
        this.timestamp = new Timestamp(System.currentTimeMillis());
        this.streams = "";
        this.cport = "";
        this.context = context;
        this.main = context != null ? new LinearLayout(context) : null;
    }

    public MaterialButtonToggleGroup.OnButtonCheckedListener getModeButtonCheckedListener(MaterialButton modeClient, MaterialButton modeServer) {
        return (group, checkedId, isChecked) -> {
            if (isChecked) {
                switch (checkedId) {
                    case R.id.iperf3_client_button:
                        updateButtonState(modeClient, modeServer, 0);
                        break;
                    case R.id.iperf3_server_button:
                        updateButtonState(modeServer, modeClient, 1);
                        break;
                }
            }
        };
    }

    public MaterialButtonToggleGroup.OnButtonCheckedListener getProtocolButtonCheckedListener(MaterialButton protocolTCP, MaterialButton protocolUDP) {
        return (group, checkedId, isChecked) -> {
            if (isChecked) {
                switch (checkedId) {
                    case R.id.iperf3_tcp_button:
                        updateButtonState(protocolTCP, protocolUDP, 0);
                        break;
                    case R.id.iperf3_udp_button:
                        updateButtonState(protocolUDP, protocolTCP, 1);
                        break;
                }
            }
        };
    }

    public MaterialButtonToggleGroup.OnButtonCheckedListener getDirectionButtonCheckedListener(MaterialButton directionUp, MaterialButton directionDown, MaterialButton directionBidir) {
        return (group, checkedId, isChecked) -> {
            if (isChecked) {
                switch (checkedId) {
                    case R.id.iperf3_upload_button:
                        updateDirectionState(directionUp, directionDown, directionBidir, false, false);
                        break;
                    case R.id.iperf3_download_button:
                        updateDirectionState(directionDown, directionUp, directionBidir, true, false);
                        break;
                    case R.id.iperf3_bidir_button:
                        updateDirectionState(directionBidir, directionUp, directionDown, false, true);
                        break;
                }
            }
        };
    }

    private void updateButtonState(MaterialButton activeButton, MaterialButton inactiveButton, int mode) {
        activeButton.setBackgroundColor(context.getResources().getColor(R.color.purple_500, null));
        inactiveButton.setBackgroundColor(Color.TRANSPARENT);
        setIdxMode(mode);
    }

    private void updateDirectionState(MaterialButton activeButton, MaterialButton inactiveButton1, MaterialButton inactiveButton2, boolean reverse, boolean bidir) {
        activeButton.setBackgroundColor(context.getResources().getColor(R.color.purple_500, null));
        inactiveButton1.setBackgroundColor(Color.TRANSPARENT);
        inactiveButton2.setBackgroundColor(Color.TRANSPARENT);
        setReverse(reverse);
        setBidir(bidir);
    }

    public TextWatcher getTextWatcher(TextWatcherCallback callback) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                callback.onTextChanged(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };
    }

    // In Iperf3Input.java

    public TextWatcher getIpTextWatcher() {
        return getTextWatcher(new TextWatcherCallback() {
            @Override
            public void onTextChanged(String text) {
                setIp(text);
            }
        });
    }

    public TextWatcher getPortTextWatcher() {
        return getTextWatcher(new TextWatcherCallback() {
            @Override
            public void onTextChanged(String text) {
                setPort(text);
            }
        });
    }

    public TextWatcher getBandwidthTextWatcher() {
        return getTextWatcher(new TextWatcherCallback() {
            @Override
            public void onTextChanged(String text) {
                setBandwidth(text);
            }
        });
    }

    public TextWatcher getDurationTextWatcher() {
        return getTextWatcher(new TextWatcherCallback() {
            @Override
            public void onTextChanged(String text) {
                setDuration(text);
            }
        });
    }

    public TextWatcher getIntervalTextWatcher() {
        return getTextWatcher(new TextWatcherCallback() {
            @Override
            public void onTextChanged(String text) {
                setInterval(text);
            }
        });
    }

    public TextWatcher getBytesTextWatcher() {
        return getTextWatcher(new TextWatcherCallback() {
            @Override
            public void onTextChanged(String text) {
                setBytes(text);
            }
        });
    }

    public TextWatcher getStreamsTextWatcher() {
        return getTextWatcher(new TextWatcherCallback() {
            @Override
            public void onTextChanged(String text) {
                setStreams(text);
            }
        });
    }

    public TextWatcher getCportTextWatcher() {
        return getTextWatcher(new TextWatcherCallback() {
            @Override
            public void onTextChanged(String text) {
                setCport(text);
            }
        });
    }

    public interface TextWatcherCallback {
        void onTextChanged(String text);
    }

    public void setBidir(boolean bidir) {
        this.isBidir = bidir;
    }

    public void setReverse(boolean reverse) {
        this.isReverse = reverse;
    }

    public void setJson(boolean json) {
        this.isJson = json;
    }

    public void setOneOff(boolean oneOff) {
        this.isOneOff = oneOff;
    }

    public void setIdxMode(int idxMode) {
        this.idxMode = idxMode;
    }

    public void setIdxProtocol(int idxProtocol) {
        this.idxProtocol = idxProtocol;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public void setRawFile(String rawFile) {
        this.rawFile = rawFile;
    }

    public void setLogFileName(String logFileName) {
        this.logFileName = logFileName;
    }

    public void setMeasurementName(String measurementName) {
        this.measurementName = measurementName;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public void setBandwidth(String bandwidth) {
        this.bandwidth = bandwidth;
    }

    public void setLineProtocolFile(String lineProtocolFile) {
        this.lineProtocolFile = lineProtocolFile;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public void setBytes(String bytes) {
        this.bytes = bytes;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public void setStreams(String streams) {
        this.streams = streams;
    }

    public void setCport(String cport) {
        this.cport = cport;
    }

    public void setMain(LinearLayout main) {
        this.main = main;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public boolean isReverse() {
        return isReverse;
    }

    public boolean isJson() {
        return isJson;
    }

    public boolean isOneOff() {
        return isOneOff;
    }

    public int getIdxMode() {
        return idxMode;
    }

    public int getIdxProtocol() {
        return idxProtocol;
    }

    public String getUuid() {
        return uuid;
    }

    public String getCommand() {
        return command;
    }

    public String getRawFile() {
        return rawFile;
    }

    public String getLogFileName() {
        return logFileName;
    }

    public String getMeasurementName() {
        return measurementName;
    }

    public String getIp() {
        return ip;
    }

    public String getPort() {
        return port;
    }

    public String getBandwidth() {
        return bandwidth;
    }

    public String getLineProtocolFile() {
        return lineProtocolFile;
    }

    public String getDuration() {
        return duration;
    }

    public String getInterval() {
        return interval;
    }

    public String getBytes() {
        return bytes;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public String getStreams() {
        return streams;
    }

    public String getCport() {
        return cport;
    }

    public LinearLayout getMain() {
        return main;
    }

    public Context getContext() {
        return context;
    }

    public boolean isBidir() {
        return isBidir;
    }

    public void update() {
        //getInputAsLinearLayoutKeyValue();
    }

    private List<Field> getFields() {
        List<Field> fields = Arrays.asList(Iperf3Input.class.getDeclaredFields());
        fields.sort((o1, o2) -> o1.toGenericString().compareTo(o2.toGenericString()));
        return fields;
    }

    private LinearLayout getTextView(String name, String value, Context ct) {
        LinearLayout mainLL = new LinearLayout(ct);
        mainLL.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mainLL.setOrientation(LinearLayout.HORIZONTAL);

        TextView parameterName = createTextView(ct, name, 1F);
        TextView parameterValue = createTextView(ct, value, 1F);

        mainLL.addView(parameterName);
        mainLL.addView(parameterValue);
        return mainLL;
    }

    private TextView createTextView(Context ct, String text, float weight) {
        TextView textView = new TextView(ct);
        textView.setText(text);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.weight = weight;
        textView.setLayoutParams(layoutParams);
        return textView;
    }

    private LinearLayout getTextViewValue(String key, String value, Context ct) {
        LinearLayout mainLL = new LinearLayout(ct);
        mainLL.setOrientation(LinearLayout.HORIZONTAL);
        mainLL.setFocusable(false);
        mainLL.setFocusedByDefault(false);

        TextView parameterValue = createTextView(ct, value, 1F);
        parameterValue.setTextIsSelectable(true);
        parameterValue.setPadding(5, 5, 5, 5);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) parameterValue.getLayoutParams();
        layoutParams.setMargins(0, 0, 10, 10);

        mainLL.addView(parameterValue);
        return mainLL;
    }

    public CardView getCardView() {
        if (context == null) return null;
        //getInputAsLinearLayoutKeyValue();
        if (cardView != null) return cardView;
        cardView = new CardView(this.context);
        cardView.setRadius(10);
        cardView.setCardElevation(10);
        cardView.setContentPadding(10, 10, 10, 10);
        GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams(GridLayout.spec(GridLayout.UNDEFINED, 2F), GridLayout.spec(GridLayout.UNDEFINED, 2F));
        layoutParams.setGravity(Gravity.FILL);
        layoutParams.setMargins(10, 10, 10, 10);
        cardView.setLayoutParams(layoutParams);
        cardView.setTag("valueholder");
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.fragment_iperf3_card, cardView, true);
        view.setScaleX(0.3f);
        view.setScaleY(0.3f);
        return cardView;
    }

    public void getInputAsLinearLayoutKeyValue() {
        if (context == null) return;
        if (main == null) main = new LinearLayout(context);
        main.removeAllViews();
        main.setOrientation(LinearLayout.VERTICAL);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        main.setLayoutParams(layoutParams);
        String[] protocol = this.context.getResources().getStringArray(R.array.iperf_protocol);
        String[] mode = this.context.getResources().getStringArray(R.array.iperf_mode);
        for (Field parameter : getFields()) {
            try {
                Object parameterValueObj = parameter.get(this);
                if (parameterValueObj == null) {
                    continue;
                }

                String parameterName = parameter.getName().replace("iperf3", "");
                if (Arrays.asList(EXCLUDED_FIELDS).contains(parameterName)) continue;

                String parameterValue = parameter.get(this).toString();
                if (parameterValue.equals("false") || parameterValue.isEmpty()) {
                    continue;
                }

                if (parameterName.equals("idxProtocol")) {
                    parameterName = "Protocol";
                    parameterValue = protocol[Integer.parseInt(parameterValue)];
                }

                if (parameterName.equals("idxMode")) {
                    parameterName = "Mode";
                    parameterValue = mode[Integer.parseInt(parameterValue)];
                }
                main.addView(getTextView(parameterName, parameterValue, this.context));

            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public LinearLayout getInputAsLinearLayoutValue(LinearLayout mainLL, Context ct) {
        mainLL.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.weight = 10F;
        mainLL.setLayoutParams(layoutParams);
        String[] protocol = ct.getResources().getStringArray(R.array.iperf_protocol);
        String[] mode = ct.getResources().getStringArray(R.array.iperf_mode);
        for (Field parameter : getFields()) {
            try {
                Object parameterValueObj = parameter.get(this);
                if (parameterValueObj == null) {
                    continue;
                }

                String parameterName = parameter.getName().replace("iperf3", "");
                if (Arrays.asList(EXCLUDED_FIELDS).contains(parameterName)) continue;

                String parameterValue = parameter.get(this).toString();
                if (parameterValue.equals("false")) {
                    continue;
                }
                if (parameterName.equals("idxProtocol")) {
                    parameterName = "Protocol";
                    parameterValue = protocol[Integer.parseInt(parameterValue)];
                }

                if (parameterName.equals("idxMode")) {
                    parameterName = "Mode";
                    parameterValue = mode[Integer.parseInt(parameterValue)];
                }

                if (parameterValue.equals("true")) {
                    parameterValue = parameterName;
                }

                mainLL.addView(getTextViewValue(parameterName, parameterValue, ct));

            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return mainLL;
    }
}