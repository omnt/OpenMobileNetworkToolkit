package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;


public class Iperf3Runner implements Serializable {
    private String logFileName;
    private String[] command;
    private String id;
    private String timestamp;
    private String state;
    private String logFilePath = null;

    private transient Context context;
    private transient ThreadGroup iperf3TG;
    private transient Thread iperf3Thread;
    private transient static final String TAG = "iperf3Runner";


    static {
        System.loadLibrary("iperf3.11");
        Log.i(TAG, "iperf.so loaded!");
    }


    public String getState(){
        return this.state;
    }

    public void checkState(){
        this.state = this.iperf3Thread.getState().toString();
    }

    public Iperf3Runner(String[] command, Context context, ThreadGroup iperf3TG, String logFilePath, String logFileName) {
        super();
        this.iperf3TG = iperf3TG;
        this.command = command;
        this.context = context;
        this.logFilePath = logFilePath;
        this.logFileName = logFileName;
        this.id = UUID.randomUUID().toString();
    }


    private native int iperf3Wrapper(String[] argv, String cache);

    public String getCommand() {
        return String.join(" ", command);
    }

    public String getLogFilePath(){
        return this.logFilePath;
    }

    private Runnable createRunable() {
        return () -> iperf3Wrapper(command, context.getApplicationInfo().nativeLibraryDir);
    }

    public String getTimestamp(){
        return this.timestamp;
    }


    public int start(){
        iperf3Thread = new Thread(this.iperf3TG, createRunable(), this.id);
        iperf3Thread.setPriority(Thread.MAX_PRIORITY);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        this.timestamp = timestamp.toString();
        iperf3Thread.start();
        return 0;
    }

    public String getThreadState(){
        return this.iperf3Thread.getState().toString();
    }

    @NonNull
    @Override
    public String toString() {
        return "iperf3Runner{" +
                "command=" + Arrays.toString(command) +
                ", context=" + context.toString() +
                ", iperf3TG=" + iperf3TG.toString() +
                '}';
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Iperf3Runner)) return false;
        Iperf3Runner that = (Iperf3Runner) o;
        return id.equals(that.id);
    }

    public byte[] makeByte(Iperf3Runner iperf3R) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out;
        byte[] bytes = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(iperf3R);
            out.flush();
            bytes = bos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                bos.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return bytes;
    }

    public Iperf3Runner readBytes(byte[] data){
        try {
            ByteArrayInputStream baip = new ByteArrayInputStream(data);
            ObjectInputStream ois = new ObjectInputStream(baip);
            Iperf3Runner iperf3R = (Iperf3Runner) ois.readObject();
            return iperf3R;
        } catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
