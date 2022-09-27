package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;


import java.util.ArrayList;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;


public class Iperf3ListFragment extends Fragment {
    ListView listView;
    Iperf3ListAdapter iperf3ListAdapter;

    @RequiresApi(api = 33)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iperf3ListAdapter = new Iperf3ListAdapter(getActivity().getApplicationContext(),
                this.getArguments().getStringArrayList("iperf3List"));
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




        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> listView, View itemView, int itemPosition, long itemId)
            {

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
