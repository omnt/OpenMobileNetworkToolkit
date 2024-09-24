package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Interval.Streams;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public class Stream implements Parcelable {
    private int socket;
    private int start;
    private double end;
    private double seconds;
    private long bytes;
    private double bits_per_second;
    private boolean omitted;
    private boolean sender;

    private STREAM_TYPE streamType;

    public Stream(){
    }

    protected Stream(Parcel in) {
        socket = in.readInt();
        start = in.readInt();
        end = in.readDouble();
        seconds = in.readDouble();
        bytes = in.readLong();
        bits_per_second = in.readDouble();
        omitted = in.readBoolean();
        sender = in.readBoolean();
    }

    public static final Creator<Stream> CREATOR = new Creator<Stream>() {
        @Override
        public Stream createFromParcel(Parcel in) {
            return new Stream(in);
        }

        @Override
        public Stream[] newArray(int size) {
            return new Stream[size];
        }
    };

    public void parse(JSONObject data) throws JSONException {
        this.socket = data.getInt("socket");
        this.start = data.getInt("start");
        this.end = data.getDouble("end");
        this.seconds = data.getDouble("seconds");
        this.bytes = data.getLong("bytes");
        this.bits_per_second = data.getDouble("bits_per_second");
        this.omitted = data.getBoolean("omitted");
        this.sender = data.getBoolean("sender");
    }

    public int getSocket() {
        return socket;
    }
    public int getStart() {
        return start;
    }
    public double getEnd() {
        return end;
    }
    public double getSeconds() {
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

    public STREAM_TYPE getStreamType() {
        return streamType;
    }

    public void setStreamType(
        STREAM_TYPE streamType) {
        this.streamType = streamType;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeInt(socket);
        parcel.writeInt(start);
        parcel.writeDouble(end);
        parcel.writeDouble(seconds);
        parcel.writeLong(bytes);
        parcel.writeDouble(bits_per_second);
        parcel.writeBoolean(omitted);
        parcel.writeBoolean(sender);
    }
}
