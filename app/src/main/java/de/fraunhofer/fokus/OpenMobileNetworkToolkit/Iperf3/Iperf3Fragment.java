package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.system.ErrnoException;
import android.system.Os;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.Operation;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class Iperf3Fragment extends Fragment {
    private CheckBox iperf3BiDir;
    private CheckBox iperf3Reverse;
    private CheckBox iperf3Json;
    private CheckBox iperf3OneOff;

    private ToggleButton iperf3Client;


    private EditText iperf3LogFileName;
    private EditText iperf3IP;
    private EditText iperf3Port;
    private EditText iperf3Bandwidth;
    private EditText iperf3Duration;
    private EditText iperf3Interval;
    private EditText iperf3Bytes;

    private Button sendBtn;
    private Button instancesBtn;
    private Button moveBtn;

    private LinkedList<EditText> editTexts;

    private static final String TAG = "iperf3Activity";

    private ThreadGroup iperf3TG;
    private Thread iperf3MThread;
    private Iperf3OverView iperf3OverView;
    private Iperf3DBHandler iperf3DBHandler;
    private String logFilePath;
    private String logFileName;
    private Iperf3ListFragment iperf3ListFragment;
    private View v;
    private SharedPreferences preferences;
    private Iperf3Input input;
    private ListElem[] iperf3List;

    public static class Iperf3Input implements Parcelable{
        public boolean iperf3BiDir;
        public boolean iperf3Reverse;
        public boolean iperf3Json;

        public boolean iperf3OneOff;
        public boolean iperf3Client;

        public String iperf3LogFilePath;
        public String iperf3LogFileName;
        public String measurementName;
        public String iperf3IP;
        public String iperf3Port;
        public String iperf3Bandwidth;
        public String iperf3Duration;
        public String iperf3Interval;
        public String iperf3Bytes;

        public Iperf3Input() {
        }

        public boolean isIperf3BiDir() {
            return iperf3BiDir;
        }

        public boolean isIperf3Reverse() {
            return iperf3Reverse;
        }

        public boolean isIperf3Json() {
            return iperf3Json;
        }

        public boolean isIperf3OneOff() {
            return iperf3OneOff;
        }

        public boolean isIperf3Client() {
            return iperf3Client;
        }

        public String getIperf3LogFilePath() {
            return iperf3LogFilePath;
        }

        public String getIperf3LogFileName() {
            return iperf3LogFileName;
        }

        public String getMeasurementName() {
            return measurementName;
        }

        public String getIperf3IP() {
            return iperf3IP;
        }

        public String getIperf3Port() {
            return iperf3Port;
        }

        public String getIperf3Bandwidth() {
            return iperf3Bandwidth;
        }

        public String getIperf3Duration() {
            return iperf3Duration;
        }

        public String getIperf3Interval() {
            return iperf3Interval;
        }

        public String getIperf3Bytes() {
            return iperf3Bytes;
        }

        protected Iperf3Input(Parcel in) {
            iperf3BiDir = in.readBoolean();
            iperf3Reverse = in.readBoolean();
            iperf3Json = in.readBoolean();
            iperf3OneOff = in.readBoolean();
            iperf3Client = in.readBoolean();
            iperf3LogFilePath = in.readString();
            iperf3LogFileName = in.readString();
            measurementName = in.readString();
            iperf3IP = in.readString();
            iperf3Port = in.readString();
            iperf3Bandwidth = in.readString();
            iperf3Duration = in.readString();
            iperf3Interval = in.readString();
            iperf3Bytes = in.readString();
        }

        public static final Creator<Iperf3Input> CREATOR = new Creator<Iperf3Input>() {
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
            parcel.writeByte((byte) (iperf3BiDir ? 1 : 0));
            parcel.writeByte((byte) (iperf3Reverse ? 1 : 0));
            parcel.writeByte((byte) (iperf3Json ? 1 : 0));
            parcel.writeByte((byte) (iperf3OneOff ? 1 : 0));
            parcel.writeByte((byte) (iperf3Client ? 1 : 0));
            parcel.writeString(iperf3LogFilePath);
            parcel.writeString(iperf3LogFileName);
            parcel.writeString(measurementName);
            parcel.writeString(iperf3IP);
            parcel.writeString(iperf3Port);
            parcel.writeString(iperf3Bandwidth);
            parcel.writeString(iperf3Duration);
            parcel.writeString(iperf3Interval);
            parcel.writeString(iperf3Bytes);
        }
    }

    public static class ListElem implements Parcelable {
        public int result;
        public boolean uploaded;
        public boolean moved;
        public Iperf3Input input;

        protected ListElem(Parcel in) {
            result = in.readInt();
            uploaded = in.readBoolean();
            moved = in.readBoolean();
            input = in.readParcelable(Iperf3Input.class.getClassLoader());
        }

        public static final Creator<ListElem> CREATOR = new Creator<ListElem>() {
            @Override
            public ListElem createFromParcel(Parcel in) {
                return new ListElem(in);
            }

            @Override
            public ListElem[] newArray(int size) {
                return new ListElem[size];
            }
        };

        public ListElem(int result, boolean uploaded, boolean moved, Iperf3Input input) {
            this.result = result;
            this.uploaded = uploaded;
            this.moved = moved;
            this.input = input;
        }


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(@NonNull Parcel parcel, int i) {
            parcel.writeInt(result);
            parcel.writeBoolean(uploaded);
            parcel.writeBoolean(moved);
            parcel.writeParcelable(input, i);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.iperf3TG = new ThreadGroup("iperf3ThreadGroup");
        this.iperf3DBHandler = Iperf3DBHandler.getInstance(getActivity().getApplicationContext());
        this.iperf3OverView = new Iperf3OverView(this.iperf3TG, this.iperf3DBHandler);
        this.iperf3List = new ListElem[0];
        this.input = new Iperf3Input();

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_iperf3_input, parent, false);


        iperf3LogFileName = v.findViewById(R.id.iperf3_logfile);
        iperf3IP = v.findViewById(R.id.iperf3_ip);
        iperf3Port = v.findViewById(R.id.iperf3_port);
        iperf3Bandwidth = v.findViewById(R.id.iperf3_bandwith);
        iperf3Duration = v.findViewById(R.id.iperf3_duration);
        iperf3Interval = v.findViewById(R.id.iperf3_interval);
        iperf3Bytes = v.findViewById(R.id.iperf3_bytes);

        editTexts = new LinkedList<>();
        editTexts.add(iperf3LogFileName);
        editTexts.add(iperf3IP);
        editTexts.add(iperf3Port);
        editTexts.add(iperf3Bandwidth);
        editTexts.add(iperf3Duration);
        editTexts.add(iperf3Interval);
        editTexts.add(iperf3Bytes);

        sendBtn = v.findViewById(R.id.iperf3_send);
        instancesBtn = v.findViewById(R.id.iperf3_instances_button);
        moveBtn = v.findViewById(R.id.iperf3_move_button);

        sendBtn.setOnClickListener(this::executeIperfCommand);
        instancesBtn.setOnClickListener(this::showInstances);

        iperf3BiDir = v.findViewById(R.id.iperf_bidir);
        iperf3Reverse = v.findViewById(R.id.iperf3_reverse);
        iperf3Json = v.findViewById(R.id.iperf3_json);
        iperf3OneOff = v.findViewById(R.id.iperf3_one_off);

        iperf3Client = v.findViewById(R.id.iperf3_client);

        preferences = getActivity().getSharedPreferences("iperf3Fragment", Context.MODE_PRIVATE);

        if(savedInstanceState != null){
            iperf3LogFileName.setText(savedInstanceState.getString("iperf3LogFileName"));
            iperf3IP.setText(savedInstanceState.getString("iperf3IP"));
            iperf3Port.setText(savedInstanceState.getString("iperf3Port"));
            iperf3Bandwidth.setText(savedInstanceState.getString("iperf3Bandwidth"));
            iperf3Duration.setText(savedInstanceState.getString("iperf3Duration"));
            iperf3Interval.setText(savedInstanceState.getString("iperf3Interval"));
            iperf3Bytes.setText(savedInstanceState.getString("iperf3Bytes"));

            iperf3BiDir.setChecked(savedInstanceState.getBoolean("iperf3BiDir"));

            iperf3Reverse.setChecked(savedInstanceState.getBoolean("iperf3BiDir"));
            iperf3BiDir.setChecked(savedInstanceState.getBoolean("iperf3Reverse"));
            iperf3Json.setChecked(savedInstanceState.getBoolean("iperf3Json"));
            iperf3OneOff.setChecked(savedInstanceState.getBoolean("iperf3OneOff"));

            iperf3Client.setChecked(savedInstanceState.getBoolean("iperf3Client"));
        } else {
            iperf3LogFileName.setText(preferences.getString("iperf3LogFileName", null));
            iperf3IP.setText(preferences.getString("iperf3IP",null));
            iperf3Port.setText(preferences.getString("iperf3Port",null));
            iperf3Bandwidth.setText(preferences.getString("iperf3Bandwidth",null));
            iperf3Duration.setText(preferences.getString("iperf3Duration",null));
            iperf3Interval.setText(preferences.getString("iperf3Interval",null));
            iperf3Bytes.setText(preferences.getString("iperf3Bytes",null));

            iperf3BiDir.setChecked(preferences.getBoolean("iperf3BiDir",false));

            iperf3Reverse.setChecked(preferences.getBoolean("iperf3BiDir",false));
            iperf3BiDir.setChecked(preferences.getBoolean("iperf3Reverse",false));
            iperf3Json.setChecked(preferences.getBoolean("iperf3Json",false));
            iperf3OneOff.setChecked(preferences.getBoolean("iperf3OneOff",false));

            iperf3Client.setChecked(preferences.getBoolean("iperf3Client",false));
        }

        try {
            Os.setenv("TMPDIR", String.valueOf(getActivity().getCacheDir()), true);
        } catch (ErrnoException e) {
            e.printStackTrace();
        }
        return v;
    }

    public void showInstances(View view){
        Bundle bundle = new Bundle();
        bundle.putParcelableArray("iperf3List", iperf3List);
        if(iperf3ListFragment == null){
            iperf3ListFragment = new Iperf3ListFragment();
        }
        iperf3ListFragment.setArguments(bundle);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainerView, iperf3ListFragment, "iperf3ListFragment")
                .addToBackStack("findThisFragment").commit();
    }



    public void executeIperfCommand(View view) {
        String[] command =  parseInput().split(" ");

        //  Iperf3Runner iperf3R = new Iperf3Runner(command, getActivity().getApplicationContext(), this.iperf3TG, this.logFilePath, this.logFileName);
        //  this.iperf3OverView.addRunner(iperf3R);
        //  iperf3R.start();


        Data.Builder iperf3Data = new Data.Builder();
        iperf3Data.putStringArray("commands", command);
        OneTimeWorkRequest iperf3WR = new OneTimeWorkRequest.Builder(Iperf3Worker.class).setInputData(iperf3Data.build()).build();
        WorkManager iperf3WM =  WorkManager.getInstance(getActivity().getApplicationContext());
        iperf3Data.putString("logfilepath", logFilePath);
        iperf3Data.putString("measurementName", input.measurementName);
        iperf3Data.putString("ip", input.iperf3IP);
        iperf3Data.putString("port", input.iperf3Port);
        iperf3Data.putString("bandwidth", input.iperf3Bandwidth);
        iperf3Data.putString("duration", input.iperf3Duration);
        iperf3Data.putString("interval", input.iperf3Interval);
        iperf3Data.putString("bytes", input.iperf3Bytes);

        iperf3Data.putBoolean("rev", input.iperf3Reverse);
        iperf3Data.putBoolean("biDir", input.iperf3BiDir);
        iperf3Data.putBoolean("oneOff", input.iperf3OneOff);
        iperf3Data.putBoolean("client", input.iperf3Client);




        OneTimeWorkRequest iperf3UP = new OneTimeWorkRequest.Builder(Iperf3UploadWorker.class).setInputData(iperf3Data.build()).build();
        OneTimeWorkRequest iperf3Move = new OneTimeWorkRequest.Builder(Iperf3MoveWorker.class).setInputData(iperf3Data.build()).build();

        Operation op = iperf3WM.beginWith(iperf3WR)
                .then(iperf3UP)
                .then(iperf3Move)
                .enqueue();

        final Observer<WorkInfo> nameObserver = new Observer<WorkInfo>() {
            @Override
            public void onChanged(@Nullable final WorkInfo workInfo) {
                int iperf3_result;
                boolean iperf3_upload, iperf3_move;
                iperf3_result = workInfo.getOutputData().getInt("iperf3_result", -100);
                iperf3_upload = workInfo.getOutputData().getBoolean("iperf3_upload", false);
                iperf3_move = workInfo.getOutputData().getBoolean("iperf3_move", false);
                Log.d(TAG, "onChanged: iperf3_result: "+iperf3_result);
                Log.d(TAG, "onChanged: iperf3_upload: "+iperf3_upload);
                Log.d(TAG, "onChanged: iperf3_move: "+iperf3_move);

                if(iperf3_result == -100)
                    return;
                ListElem tmp = new ListElem(iperf3_result, iperf3_upload, iperf3_move, input);
                iperf3List = addX(iperf3List, tmp);
            }
        };
        iperf3WM.getWorkInfoByIdLiveData(iperf3WR.getId()).observe(getViewLifecycleOwner(), nameObserver);
    }
    public static ListElem[] addX( ListElem arr[], ListElem x)
    {
        int i;
        int n = arr.length;
        // create a new array of size n+1
        ListElem newarr[] = new ListElem[n + 1];

        // insert the elements from
        // the old array into the new array
        // insert all elements till n
        // then insert x at n+1
        for (i = 0; i < n; i++)
            newarr[i] = arr[i];

        newarr[n] = x;

        return newarr;
    }
    private String getKeyFromId(String s, String value){
        String key = "";
        switch (s){
            case "iperf3_logfile":
                key = "--logfile";
                input.measurementName = value;
                break;
            case "iperf3_ip":
                key = "-c";
                input.iperf3IP = value;
                break;
            case "iperf3_port":
                key = "-p";
                input.iperf3Port = value;
                break;
            case "iperf3_bandwidth":
                key = "-b";
                input.iperf3Bandwidth = value;
                break;
            case "iperf3_duration":
                key = "-t";
                input.iperf3Duration = value;
                break;
            case "iperf3_interval":
                key = "-i";
                input.iperf3Interval = value;
                break;
            case "iperf3_bytes":
                key = "-n";
                input.iperf3Bytes = value;
                break;
        }
        return key;
    }

    private String parseInput(){
        List<String> stb = new LinkedList<>();
        for (EditText et: editTexts) {
            String value = et.getText().toString();
            if(!value.equals("")){
                String s = getResources().getResourceEntryName(et.getId());
                String key = getKeyFromId(s, value);
                stb.add(key);
                if(key.equals("--logfile")){
                    Timestamp iperfT = new Timestamp(System.currentTimeMillis());
                    String iperf3TS = "_"+iperfT.toString().replace(" ", "_").replace(":", "_");
                    this.logFileName = value+iperf3TS+".log";
                    input.iperf3LogFileName = this.logFileName;
                    value = getActivity().getFilesDir() +"/"+value+iperf3TS+".log";
                    this.logFilePath = value;
                    input.iperf3LogFilePath = this.logFilePath;
                }
                stb.add(value);
            }
        }

        input.iperf3Client = false;
        input.iperf3BiDir = false;
        input.iperf3Reverse = false;
        input.iperf3OneOff = false;
        input.iperf3Json = false;

        if(iperf3Client.isChecked()){
            stb.add("-s");
            input.iperf3Client = true;
        }
        if(iperf3BiDir.isChecked()){
            stb.add("--bidir");
            input.iperf3BiDir = true;
        }
        if(iperf3Reverse.isChecked()){
            stb.add("--reverse");
            input.iperf3Reverse = true;
        }
        if(iperf3OneOff.isChecked()){
            stb.add("--one-off");
            input.iperf3OneOff = true;
        }
        if(iperf3Json.isChecked()){
            stb.add("--json");
            input.iperf3Json = true;
        }

        String joined = String.join(" ", stb);
        Log.d(TAG, "parseInput: joined command "+ joined);
        return joined;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("iperf3LogFileName", iperf3LogFileName.getText().toString());
        outState.putString("iperf3IP", iperf3IP.getText().toString());
        outState.putString("iperf3Port", iperf3Port.getText().toString());
        outState.putString("iperf3Bandwidth", iperf3Bandwidth.getText().toString());
        outState.putString("iperf3Duration", iperf3Duration.getText().toString());
        outState.putString("iperf3Interval", iperf3Interval.getText().toString());
        outState.putString("iperf3Bytes", iperf3Bytes.getText().toString());

        outState.putBoolean("iperf3BiDir", iperf3BiDir.isChecked());

        outState.putBoolean("iperf3BiDir", iperf3BiDir.isChecked());
        outState.putBoolean("iperf3Reverse", iperf3Reverse.isChecked());
        outState.putBoolean("iperf3Json", iperf3Json.isChecked());
        outState.putBoolean("iperf3OneOff", iperf3OneOff.isChecked());
        outState.putBoolean("iperf3Client", iperf3Client.isChecked());


    }


    @Override
    public void onDestroy(){
        super.onDestroy();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("iperf3LogFileName", iperf3LogFileName.getText().toString());
        editor.putString("iperf3IP", iperf3IP.getText().toString());
        editor.putString("iperf3Port", iperf3Port.getText().toString());
        editor.putString("iperf3Bandwidth", iperf3Bandwidth.getText().toString());
        editor.putString("iperf3Duration", iperf3Duration.getText().toString());
        editor.putString("iperf3Interval", iperf3Interval.getText().toString());
        editor.putString("iperf3Bytes", iperf3Bytes.getText().toString());

        editor.putBoolean("iperf3BiDir", iperf3BiDir.isChecked());

        editor.putBoolean("iperf3BiDir", iperf3BiDir.isChecked());
        editor.putBoolean("iperf3Reverse", iperf3Reverse.isChecked());
        editor.putBoolean("iperf3Json", iperf3Json.isChecked());
        editor.putBoolean("iperf3OneOff", iperf3OneOff.isChecked());
        editor.putBoolean("iperf3Client", iperf3Client.isChecked());
        editor.apply();
    }


}