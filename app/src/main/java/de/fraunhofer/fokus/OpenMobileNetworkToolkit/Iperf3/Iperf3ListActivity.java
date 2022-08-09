package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;


public class Iperf3ListActivity extends AppCompatActivity {
    ListView listView;
    private Iperf3DBHandler iperf3DBHandler;
    String[] ids;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        this.iperf3DBHandler = Iperf3DBHandler.getInstance(getApplicationContext());
        Parcelable mListInstanceState;
        if(savedInstanceState!=null) {
            mListInstanceState = savedInstanceState.getParcelable("ListState");
            listView.onRestoreInstanceState(mListInstanceState);
        }


        setContentView(R.layout.activity_iperf3_list);
        if (getIntent().hasExtra("json")) {
            this.ids = getIntent().getStringArrayExtra("json");
        }


        listView = findViewById(R.id.runners_list);
        Iperf3ListAdapter iperf3ListAdapter = new Iperf3ListAdapter(getApplicationContext(), this.ids);
        listView.setAdapter(iperf3ListAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            private Iperf3DBHandler iperf3DBHandler;

            public void onItemClick(AdapterView<?> listView, View itemView, int itemPosition, long itemId)
            {
                this.iperf3DBHandler = Iperf3DBHandler.getInstance(getApplicationContext());
                Iperf3Runner iperf3R = new Iperf3Runner(null, null, null, null).readBytes(this.iperf3DBHandler.getRunnerByID(ids[itemPosition]));
                if(iperf3R.getLogFilePath() == null){
                    return;
                }
                File url = new File(iperf3R.getLogFilePath());
                openFile(url);
            }
        });

    }
    private void openFile(File url) {

        try {

            Uri uri = Uri.fromFile(url);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            if (url.toString().contains(".doc") || url.toString().contains(".docx")) {
                // Word document
                intent.setDataAndType(uri, "application/msword");
            } else if (url.toString().contains(".pdf")) {
                // PDF file
                intent.setDataAndType(uri, "application/pdf");
            } else if (url.toString().contains(".ppt") || url.toString().contains(".pptx")) {
                // Powerpoint file
                intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
            } else if (url.toString().contains(".xls") || url.toString().contains(".xlsx")) {
                // Excel file
                intent.setDataAndType(uri, "application/vnd.ms-excel");
            } else if (url.toString().contains(".zip")) {
                // ZIP file
                intent.setDataAndType(uri, "application/zip");
            } else if (url.toString().contains(".rar")){
                // RAR file
                intent.setDataAndType(uri, "application/x-rar-compressed");
            } else if (url.toString().contains(".rtf")) {
                // RTF file
                intent.setDataAndType(uri, "application/rtf");
            } else if (url.toString().contains(".wav") || url.toString().contains(".mp3")) {
                // WAV audio file
                intent.setDataAndType(uri, "audio/x-wav");
            } else if (url.toString().contains(".gif")) {
                // GIF file
                intent.setDataAndType(uri, "image/gif");
            } else if (url.toString().contains(".jpg") || url.toString().contains(".jpeg") || url.toString().contains(".png")) {
                // JPG file
                intent.setDataAndType(uri, "image/jpeg");
            } else if (url.toString().contains(".txt") || url.toString().contains(".log")) {
                // Text file
                intent.setDataAndType(uri, "text/plain");
            } else if (url.toString().contains(".3gp") || url.toString().contains(".mpg") ||
                    url.toString().contains(".mpeg") || url.toString().contains(".mpe") || url.toString().contains(".mp4") || url.toString().contains(".avi")) {
                // Video files
                intent.setDataAndType(uri, "video/*");
            } else {
                intent.setDataAndType(uri, "*/*");
            }

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplicationContext().startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getApplicationContext(), "No application found which can open the file", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("ListState", listView.onSaveInstanceState());
    }

}
