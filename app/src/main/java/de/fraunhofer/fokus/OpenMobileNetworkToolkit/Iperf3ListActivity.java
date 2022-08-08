package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;


public class Iperf3ListActivity extends AppCompatActivity {
    ListView listView;
    private Iperf3DBHandler iperf3DBHandler;

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
        String[] ids = new String[0];
        if (getIntent().hasExtra("json")) {
            ids = getIntent().getStringArrayExtra("json");
        }

        listView = findViewById(R.id.runners_list);
        Iperf3ListAdapter iperf3ListAdapter = new Iperf3ListAdapter(getApplicationContext(), ids);
        listView.setAdapter(iperf3ListAdapter);
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("ListState", listView.onSaveInstanceState());
    }

}
