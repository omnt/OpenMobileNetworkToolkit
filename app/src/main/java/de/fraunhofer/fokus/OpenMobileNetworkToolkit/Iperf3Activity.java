package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Map;

public class Iperf3Activity extends AppCompatActivity {

    private EditText iperf3CmdInput;
    private Button SendButton;
    private DataInputStream inputStream;

    static{
        System.loadLibrary("iperf3.10.1");
    }
    private static final String TAG = "iperf3Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iperf3);
        View v = findViewById(android.R.id.content).getRootView();
        SendButton = (Button) v.findViewById(R.id.iperf3commandButton);
        //SendButton.setOnClickListener(this);

        iperf3CmdInput = (EditText) v.findViewById(R.id.iperf3command);
        //iperf3CmdInput.setOnClickListener(this);

    }

    private String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public String getOutputString(){
        return convertStreamToString(this.inputStream);
    }

    public int startProcess(String bin, String command){
        try {
            ProcessBuilder pb = new ProcessBuilder(bin, command);
            Map<String, String> m = pb.environment();
            m.put("TMPDIR", "/data/data/de.fraunhofer.fokus.OpenMobileNetworkToolkit/cache/iperf3.10.1");
            Process p = pb.start();
            this.inputStream = new DataInputStream(p.getErrorStream());
            return p.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return 1;
        }
    }

    public void sendMessage(View view) {
        Log.i(TAG, "onClick: button clicked");
        String inputText = iperf3CmdInput.getText().toString();
        Log.i(TAG, "onClick: command:"+inputText);
        Log.d(TAG, "onClick: iperf3 bin path "+getApplicationContext().getApplicationInfo().nativeLibraryDir);
        Log.d(TAG, "onClick: command exec: "+startProcess(getApplicationContext().getApplicationInfo().nativeLibraryDir+"/iperf3.10.1.so", inputText));
        Log.d(TAG, "onClick: Output: "+getOutputString());

    }
}