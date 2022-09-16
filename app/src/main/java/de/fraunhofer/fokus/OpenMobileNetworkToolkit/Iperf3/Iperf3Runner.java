package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
    private int iperf3State;

    //flag for uploaded to influxdb
    private boolean uploaded;

    //flag for moved to /Documents/iperf3
    private boolean moved;


    private transient Context context;
    private transient ThreadGroup iperf3TG;
    private transient Thread iperf3Thread;
    private transient static final String TAG = "iperf3Runner";

    static {
        System.loadLibrary("iperf3.11");
        Log.i(TAG, "iperf.so loaded!");
    }


    public String getThreadState(){
        return this.state;
    }

    public int getIperf3State() {return this.iperf3State;}

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
        this.uploaded = false;
        this.moved = false;
    }

    public String getLogFileName() {
        return logFileName;
    }

    private native int iperf3Wrapper(String[] argv, String cache);

    public String getCommand() {
        return String.join(" ", command);
    }

    public String getLogFilePath(){
        return this.logFilePath;
    }

    private Runnable createRunable() {
        return () -> {
            this.iperf3State = iperf3Wrapper(command, context.getApplicationInfo().nativeLibraryDir);
            if(iperf3Thread.getState() == Thread.State.TERMINATED){
                if(logFilePath != null && iperf3State == 0){
                    FileInputStream fis = null;
                    try {
                        fis = new FileInputStream(new File(logFilePath));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    BufferedReader bfr = new BufferedReader(new InputStreamReader(fis));

                }
            }
        };
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
