package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


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
    protected void onDestroy() {
        super.onDestroy();
        //todo add listView.onSaveInstanceState() to db and retrieve it onCreate when savedInstanceState == null
        //https://stackoverflow.com/questions/18000093/how-to-marshall-and-unmarshall-a-parcelable-to-a-byte-array-with-help-of-parcel
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("ListState", listView.onSaveInstanceState());
    }

}
