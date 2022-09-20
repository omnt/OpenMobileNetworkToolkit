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


import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;


public class Iperf3ListFragment extends Fragment {
    ListView listView;
    Iperf3ListAdapter iperf3ListAdapter;
    private Iperf3DBHandler iperf3DBHandler;
    Iperf3Fragment.ListElem[] ids;

    @RequiresApi(api = 33)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.ids = (Iperf3Fragment.ListElem[]) getArguments().getParcelableArray("iperf3List");
        iperf3ListAdapter = new Iperf3ListAdapter(getActivity().getApplicationContext(), this.ids);

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_iperf3_list, parent, false);
    }
    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        this.iperf3DBHandler = Iperf3DBHandler.getInstance(getActivity().getApplicationContext());

        listView = v.findViewById(R.id.runners_list);
        listView.setAdapter(iperf3ListAdapter);


        if (savedInstanceState != null) {
            listView.onRestoreInstanceState(savedInstanceState.getParcelable("ListState"));
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
