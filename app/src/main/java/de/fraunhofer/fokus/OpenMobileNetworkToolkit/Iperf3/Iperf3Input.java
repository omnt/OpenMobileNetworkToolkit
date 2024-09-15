package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.work.Data;


import org.checkerframework.checker.units.qual.A;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;


public class Iperf3Input implements Parcelable {
    public static final String[] EXCLUDED_FIELDS = {
            "measurementName", "rawFile", "logFileName", "command", "lineProtocolFile",
            "context", "timestamp", "uuid", "cardView", "main", "EXCLUDED_FIELDS"
    };

    protected Iperf3Input(Parcel in) {
        isJson = in.readBoolean();
        isOneOff = in.readBoolean();
        mode = Iperf3Mode.valueOf(in.readString());
        protocol = Iperf3Protocol.valueOf(in.readString());
        direction = Iperf3Direction.valueOf(in.readString());
        rawFile = in.readString();
        ip = in.readString();
        port = in.readString();
        bandwidth = in.readString();
        duration = in.readString();
        interval = in.readString();
        bytes = in.readString();
        streams = in.readString();
        cport = in.readString();
        uuid = in.readString();
        logFileName = in.readString();
        measurementName = in.readString();
        lineProtocolFile = in.readString();
        timestamp = (Timestamp) in.readSerializable();
    }

    public static final Creator<Iperf3Input> CREATOR = new Creator<>() {
        @Override
        public Iperf3Input createFromParcel(Parcel in) {
            return new Iperf3Input(in);
        }

        @Override
        public Iperf3Input[] newArray(int size) {
            return new Iperf3Input[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeBoolean(isJson);
        parcel.writeBoolean(isOneOff);
        parcel.writeString(mode.toString());
        parcel.writeString(protocol.toString());
        parcel.writeString(direction.toString());
        parcel.writeString(rawFile);
        parcel.writeString(ip);
        parcel.writeString(port);
        parcel.writeString(bandwidth);
        parcel.writeString(duration);
        parcel.writeString(interval);
        parcel.writeString(bytes);
        parcel.writeString(streams);
        parcel.writeString(cport);
        parcel.writeString(uuid);
        parcel.writeString(logFileName);
        parcel.writeString(measurementName);
        parcel.writeString(lineProtocolFile);
        parcel.writeSerializable(timestamp);
    }

    public boolean isValid(){
        return !ip.isEmpty();
    }

    public enum Iperf3Mode {
        CLIENT,
        SERVER;
        public String toPrettyPrint() {
            return this.name().substring(0, 1).toUpperCase() + this.name().toLowerCase().substring(1);
        }
    }

    public enum Iperf3Protocol {
        TCP,
        UDP
    }

    public enum Iperf3Direction {
        UP,
        DOWN,
        BIDIR;
        public String toPrettyPrint() {
            return this.name().toLowerCase();
        }
    }

    private boolean isJson;
    private boolean isOneOff;
    private Iperf3Mode mode;
    private Iperf3Protocol protocol;
    private Iperf3Direction direction;
    private String rawFile;
    private String ip;
    private String port;
    private String bandwidth;
    private String duration;
    private String interval;
    private String bytes;
    private String streams;
    private String cport;

    private String uuid;
    private String logFileName;
    private String measurementName;
    private String lineProtocolFile;
    private Timestamp timestamp;


    public Iperf3Input() {
        String rootPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        this.isJson = false;
        this.isOneOff = false;
        //if(uuid == null) this.uuid = UUID.randomUUID().toString();
        this.uuid = UUID.randomUUID().toString();
        this.rawFile = rootPath+"/omnt/iperf3/json/";;
        this.logFileName = "";
        this.measurementName = "";
        this.ip = "";
        this.port = "";
        this.bandwidth = "";
        this.lineProtocolFile = rootPath+"/omnt/iperf3/lineprotocol/";
        this.duration = "";
        this.interval = "";
        this.bytes = "";
        this.timestamp = new Timestamp(System.currentTimeMillis());
        this.streams = "";
        this.cport = "";
    }

    public void setMode(Iperf3Mode mode) {
        this.mode = mode;
    }

    public void setJson(boolean json) {
        this.isJson = json;
    }

    public void setOneOff(boolean oneOff) {
        this.isOneOff = oneOff;
    }

    public void setDirection(Iperf3Direction direction) {
        this.direction = direction;
    }

    public void setProtocol(Iperf3Protocol protocol) {
        this.protocol = protocol;
    }

    public Iperf3Direction getDirection() {
        return direction;
    }


    public Iperf3Protocol getProtocol() {
        return protocol;
    }

    public Iperf3Mode getMode() {
        return mode;
    }


    public void setUuid(String uuid) {
        this.uuid = uuid;
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


    public boolean isJson() {
        return isJson;
    }

    public boolean isOneOff() {
        return isOneOff;
    }

    public String getUuid() {
        return uuid;
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

    public List<Field> getFields() {
        List<Field> fields = Arrays.asList(Iperf3Input.class.getDeclaredFields());
        fields.sort((o1, o2) -> o1.toGenericString().compareTo(o2.toGenericString()));
        return fields;
    }


    public Data.Builder getInputAsDataBuilder() {
        Data.Builder data = new Data.Builder();
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

                data.putString(parameterName, parameterValue);

            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        data.putStringArray("command", getInputAsCommand());
        data.putString("uuid", uuid);
        return data;
    }

    public String[] getInputAsCommand(){
        ArrayList<String> command = new ArrayList<>();
        command.add("iperf3");
        switch (mode) {
            case CLIENT:
                command.add("-c");
                if(!ip.isEmpty()) command.add(ip);
                break;
            case SERVER:
                command.add("-s");
                break;
            }
        if(port != null && !port.isEmpty()){
            command.add("-p");
            command.add(port);
        }
        if(bandwidth != null && !bandwidth.isEmpty()){
            command.add("-b");
            command.add(bandwidth);
        }
        if(duration != null && !duration.isEmpty()){
            command.add("-t");
            command.add(duration);
        }
        if(interval != null && !interval.isEmpty()){
            command.add("-i");
            command.add(interval);
        }
        if(bytes != null && !bytes.isEmpty()){
            command.add("-n");
            command.add(bytes);
        }
        if(streams != null && !streams.isEmpty()){
            command.add("-P");
            command.add(streams);
        }
        if(cport != null && !cport.isEmpty()){
            command.add("-B");
            command.add(cport);
        }

        switch (direction){
            case DOWN:
                command.add("--reverse");
                break;
            case BIDIR:
                command.add("--bidir");
                break;
            case UP:
                break;
        }

        switch (protocol){
            case UDP:
                command.add("-u");
                break;
            case TCP:
                break;
        }

        command.add("--file");
        command.add(rawFile);
        command.add("--json-stream");
        command.add("--connect-timeout 500");
        return command.toArray(new String[0]);
    }






}