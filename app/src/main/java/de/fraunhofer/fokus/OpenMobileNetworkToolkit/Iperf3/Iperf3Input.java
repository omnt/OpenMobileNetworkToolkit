package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class Iperf3Input {
    public boolean iperf3BiDir;
    public boolean iperf3Reverse;
    public boolean iperf3Json;
    public boolean iperf3OneOff;
    public int iperf3IdxMode;
    public int iperf3IdxProtocol;
    public String uuid;
    public String iperf3Command;
    public String iperf3rawIperf3file;
    public String iperf3LogFileName;
    public String measurementName;
    public String iperf3IP;
    public String iperf3Port;
    public String iperf3Bandwidth;
    public String iperf3LineProtocolFile;
    public String iperf3Duration;
    public String iperf3Interval;
    public String iperf3Bytes;
    public Timestamp timestamp;
    public String streams;
    public String iperf3Cport;
    private List<Field> getFields(){
        List<Field> fields = Arrays.asList(Iperf3Input.class.getDeclaredFields());
        fields.sort((o1, o2) -> {
            return o1.toGenericString().compareTo(o2.toGenericString());
        });
        return fields;
    }
    private LinearLayout getTextView(String name, String value, Context ct){
        LinearLayout mainLL = new LinearLayout(ct);
        mainLL.setOrientation(LinearLayout.HORIZONTAL);


        LinearLayout.LayoutParams parameterLayoutName = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        parameterLayoutName.weight = 1F;
        TextView parameterName = new TextView(ct);
        parameterName.setTextIsSelectable(true);
        parameterName.setText(String.format("%s", name));
        parameterName.setLayoutParams(parameterLayoutName);
        TextView parameterValue = new TextView(ct);
        parameterValue.setTextIsSelectable(true);
        parameterValue.setText(String.format("%s", value));
        LinearLayout.LayoutParams parameterLayoutValue = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        parameterLayoutValue.weight = 3F;
        parameterValue.setLayoutParams(parameterLayoutValue);

        mainLL.addView(parameterName);
        mainLL.addView(parameterValue);
        return mainLL;
    }
    private LinearLayout getTextViewValue(String key, String value, Context ct){
        LinearLayout mainLL = new LinearLayout(ct);
        mainLL.setOrientation(LinearLayout.HORIZONTAL);
        mainLL.setFocusable(false);
        mainLL.setFocusedByDefault(false);

        TextView parameterValue = new TextView(ct);

        parameterValue.setTextIsSelectable(true);
        parameterValue.setText(String.format("%s", value));
        LinearLayout.LayoutParams parameterLayoutValue = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        parameterValue.setPadding(5, 5, 5, 5);
        parameterLayoutValue.setMargins(0, 0, 10, 10);
        parameterLayoutValue.weight = 1F;
        parameterValue.setLayoutParams(parameterLayoutValue);

        mainLL.addView(parameterValue);
        return mainLL;
    }
    public LinearLayout getInputAsLinearLayoutKeyValue(LinearLayout mainLL, Context ct){
        mainLL.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.weight = 8F;
        mainLL.setLayoutParams(layoutParams);
        String[] protocol =
                ct.getResources().getStringArray(R.array.iperf_protocol);
        String[] mode = ct.getResources().getStringArray(R.array.iperf_mode);
        for(Field parameter: getFields()){
            try {
                Object parameterValueObj = parameter.get(this);
                if(parameterValueObj == null){
                    continue;
                }

                String parameterName = parameter.getName().replace("iperf3", "");
                if(parameterName.equals("measurementName")
                        || parameterName.equals("rawIperf3file")
                        || parameterName.equals("LogFileName")
                        || parameterName.equals("Command")
                        || parameterName.equals("LineProtocolFile")) continue;

                String parameterValue = parameter.get(this).toString();
                if(parameterValue.equals("false")){
                    continue;
                }
                if(parameterName.equals("IdxProtocol")){
                    parameterName = "Protocol";
                    parameterValue = protocol[Integer.parseInt(parameterValue)];
                }

                if(parameterName.equals("IdxMode")){
                    parameterName = "Mode";
                    parameterValue = mode[Integer.parseInt(parameterValue)];
                }
                mainLL.addView(getTextView(
                        parameterName,
                        parameterValue,
                        ct));

            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return mainLL;
    }

    public LinearLayout getInputAsLinearLayoutValue(LinearLayout mainLL, Context ct){
        mainLL.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.weight = 10F;
        mainLL.setLayoutParams(layoutParams);
        String[] protocol =
                ct.getResources().getStringArray(R.array.iperf_protocol);
        String[] mode = ct.getResources().getStringArray(R.array.iperf_mode);
        for(Field parameter: getFields()){
            try {
                Object parameterValueObj = parameter.get(this);
                if(parameterValueObj == null){
                    continue;
                }

                String parameterName = parameter.getName().replace("iperf3", "");
                if(parameterName.equals("measurementName")
                        || parameterName.equals("rawIperf3file")
                        || parameterName.equals("LogFileName")
                        || parameterName.equals("Command")
                        || parameterName.equals("LineProtocolFile")
                        || parameterName.equals("timestamp")
                        || parameterName.equals("uuid")) continue;

                String parameterValue = parameter.get(this).toString();
                if(parameterValue.equals("false")){
                    continue;
                }
                if(parameterName.equals("IdxProtocol")){
                    parameterName = "Protocol";
                    parameterValue = protocol[Integer.parseInt(parameterValue)];
                }

                if(parameterName.equals("IdxMode")){
                    parameterName = "Mode";
                    parameterValue = mode[Integer.parseInt(parameterValue)];
                }

                if(parameterValue.equals("true")){
                    parameterValue = parameterName;
                }

                mainLL.addView(getTextViewValue(
                        parameterName,
                        parameterValue,
                        ct));

            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return mainLL;
    }
}