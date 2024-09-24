package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Sum;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public class Sum implements Parcelable {
    private int start;
    private float end;
    private float seconds;
    private long bytes;
    private double bits_per_second;
    private boolean omitted;
    private boolean sender;
    private SUM_TYPE sumType;
    public Sum(){
    }

    protected Sum(Parcel in) {
        start = in.readInt();
        end = in.readFloat();
        seconds = in.readFloat();
        bytes = in.readLong();
        bits_per_second = in.readDouble();
        omitted = in.readBoolean();
        sender = in.readBoolean();
        sumType = SUM_TYPE.values()[in.readInt()];
    }

    public static final Creator<Sum> CREATOR = new Creator<Sum>() {
        @Override
        public Sum createFromParcel(Parcel in) {
            return new Sum(in);
        }

        @Override
        public Sum[] newArray(int size) {
            return new Sum[size];
        }
    };

    public void parse(JSONObject data) throws JSONException {
        this.start = data.getInt("start");
        this.end = (float) data.getDouble("end");
        this.seconds = (float) data.getDouble("seconds");
        this.bytes = data.getLong("bytes");
        this.bits_per_second = data.getDouble("bits_per_second");
        this.omitted = data.getBoolean("omitted");
        this.sender = data.getBoolean("sender");
    }
    public int getStart() {
        return start;
    }
    public float getEnd() {
        return end;
    }
    public float getSeconds() {
        return seconds;
    }
    public long getBytes() {
        return bytes;
    }
    public double getBits_per_second() {
        return bits_per_second;
    }
    public boolean getOmitted() {
        return omitted;
    }
    public boolean getSender() {
        return sender;
    }
    public SUM_TYPE getSumType() {
        return sumType;
    }
    public void setSumType(SUM_TYPE sumType) {
        this.sumType = sumType;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeInt(this.start);
        parcel.writeFloat(this.end);
        parcel.writeFloat(this.seconds);
        parcel.writeLong(this.bytes);
        parcel.writeDouble(this.bits_per_second);
        parcel.writeBoolean(this.omitted);
        parcel.writeBoolean(this.sender);
        parcel.writeInt(this.sumType.ordinal());

    }
}
