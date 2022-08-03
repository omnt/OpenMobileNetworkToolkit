package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import android.os.Bundle;
import android.os.Parcelable;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;


public class Iperf3ListActivity extends AppCompatActivity {
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Parcelable mListInstanceState;
        if(savedInstanceState!=null) {
            mListInstanceState = savedInstanceState.getParcelable("ListState");
            listView.onRestoreInstanceState(mListInstanceState);
        }
        setContentView(R.layout.activity_iperf3_list);
        JSONArray mJsonObject = null;

        if (getIntent().hasExtra("json")) {
            try {
                mJsonObject = new JSONArray(getIntent().getStringExtra("json"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        ArrayList<String> listdata = new ArrayList<>();
        JSONArray jArray = (JSONArray)mJsonObject;
        if (jArray != null) {
            for (int i=0;i<jArray.length();i++){
                try {
                    listdata.add(jArray.getString(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.activity_listview, listdata);

        listView = (ListView) findViewById(R.id.runners_list);
        listView.setAdapter(adapter);

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("ListState", listView.onSaveInstanceState());
    }

}
