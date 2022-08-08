package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import android.content.Context;
import android.util.Log;


import androidx.annotation.NonNull;
import androidx.room.Entity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

import kotlin.jvm.Transient;

public class iperf3Runner implements Serializable {
    private String[] command;
    private String id;
    private String timestamp;

    private transient Context context;
    private transient ThreadGroup iperf3TG;
    private transient Thread iperf3Thread;
    private transient static final String TAG = "iperf3Runner";


    static {
        System.loadLibrary("iperf3.11");
        Log.i(TAG, "iperf.so loaded!");
    }


    public iperf3Runner(String[] command, Context context, ThreadGroup iperf3TG) {
        super();
        this.iperf3TG = iperf3TG;
        this.command = command;
        this.context = context;
        this.id = UUID.randomUUID().toString();
    }


    private native int iperf3Wrapper(String[] argv, String cache);

    public String getCommand() {
        return String.join(" ", command);
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
        if (!(o instanceof iperf3Runner)) return false;
        iperf3Runner that = (iperf3Runner) o;
        return id.equals(that.id);
    }


    public byte[] makeByte(iperf3Runner iperf3R) {
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

    public iperf3Runner readBytes(byte[] data){
        try {
            ByteArrayInputStream baip = new ByteArrayInputStream(data);
            ObjectInputStream ois = new ObjectInputStream(baip);
            iperf3Runner iperf3R = (iperf3Runner) ois.readObject();
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
