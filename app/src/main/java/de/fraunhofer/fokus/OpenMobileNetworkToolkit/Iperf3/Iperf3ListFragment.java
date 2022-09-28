package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;


import java.lang.reflect.Array;
import java.util.ArrayList;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;


public class Iperf3ListFragment extends Fragment {
    ListView listView;
    Iperf3ListAdapter iperf3ListAdapter;
    private Iperf3ResultsDataBase db;

    @RequiresApi(api = 33)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iperf3ListAdapter = new Iperf3ListAdapter(getActivity().getApplicationContext(),
                this.getArguments().getStringArrayList("iperf3List"));

        this.db = Iperf3ResultsDataBase.getDatabase(getActivity().getApplicationContext());
    }

    public Iperf3ListAdapter getIperf3ListAdapter(){
        return this.iperf3ListAdapter;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_iperf3_list, parent, false);
    }
    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        listView = v.findViewById(R.id.runners_list);
        listView.setAdapter(iperf3ListAdapter);


        if (savedInstanceState != null) {
            listView.onRestoreInstanceState(savedInstanceState.getParcelable("ListState"));
            iperf3ListAdapter.setUids(savedInstanceState.getStringArrayList("iperf3List"));
        }

        ArrayList<String> uids = this.getArguments().getStringArrayList("iperf3List");


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> listView, View itemView, int itemPosition, long itemId)
            {

                //todo make intent right
                Iperf3RunResult tmp = db.iperf3RunResultDao().getRunResult(uids.get(itemPosition));
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(tmp.input.iperf3LogFilePath), "text/plain");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(intent);
            }
        });

    }

    @Override
    public void onDestroy() {
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
