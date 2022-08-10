package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.content.ActivityNotFoundException;
import android.content.Context;
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
import java.io.FileWriter;


import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;


public class Iperf3ListActivity extends AppCompatActivity {
    ListView listView;
    private Iperf3DBHandler iperf3DBHandler;
    String[] ids;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.iperf3DBHandler = Iperf3DBHandler.getInstance(getApplicationContext());


        setContentView(R.layout.activity_iperf3_list);
        if (getIntent().hasExtra("json")) {
            this.ids = getIntent().getStringArrayExtra("json");
        }


        listView = findViewById(R.id.runners_list);
        Iperf3ListAdapter iperf3ListAdapter = new Iperf3ListAdapter(getApplicationContext(), this.ids);
        listView.setAdapter(iperf3ListAdapter);

        Parcelable mListInstanceState;
        if(savedInstanceState!=null) {
            mListInstanceState = savedInstanceState.getParcelable("ListState");
            listView.onRestoreInstanceState(mListInstanceState);
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> listView, View itemView, int itemPosition, long itemId)
            {

            }
        });

    }





    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("ListState", listView.onSaveInstanceState());
    }

}
