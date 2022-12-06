package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class Iperf3RecyclerViewAdapter extends RecyclerView.Adapter<Iperf3RecyclerViewAdapter.ViewHolder> {
    private Iperf3ResultsDataBase db;
    private ArrayList<String> uids;
    private Context context;
    private FragmentActivity c;
    private View v;
    private final String TAG = "Iperf3RecyclerViewAdapter";


    public Iperf3RecyclerViewAdapter(FragmentActivity c, ArrayList<String> uids){
        this.c = c;
        this.uids = uids;
        this.db = Iperf3ResultsDataBase.getDatabase(context);
    }


    @NonNull
    @Override
    public Iperf3RecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        v = inflater.inflate(R.layout.fragment_iperf3_row_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);




        return viewHolder;
    }

    public int getPositionFromUid(String uid){
        return uids.indexOf(uid);
    }

    public void setUids(ArrayList<String> uids){
        this.uids = uids;
    }
    private Iperf3RunResult getItem(int position){

        return this.db.iperf3RunResultDao().getRunResult(this.uids.get(position));
    }

    @Override
    public void onBindViewHolder(@NonNull Iperf3RecyclerViewAdapter.ViewHolder holder, int position) {

        Iperf3RunResult test = getItem(position);

        TextView command = holder.command;
        TextView uploaded = holder.uploaded;
        TextView timestamp = holder.timestamp;
        TextView iperf3State = holder.iperf3State;
        ImageView icon = holder.icon;

        command.setText(test.input.measurementName);
        iperf3State.setText(""+test.result);
        if(test.result == -100)
            iperf3State.setText("RUN");
        timestamp.setText(test.input.timestamp.toString());
        uploaded.setText("Uploaded: "+ test.uploaded);

        Drawable drawable = Iperf3Utils.getDrawable(context, test.result);
        icon.setImageDrawable(drawable);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onCreateView: CLICKED!");
                int itemPosition = holder.getLayoutPosition();
                Bundle bundle = new Bundle();
                bundle.putString("uid", uids.get(itemPosition));
                Iperf3LogFragment test = new Iperf3LogFragment();
                test.setArguments(bundle);
                c.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainerView, test, "iperf3LogFragment")
                        .addToBackStack("findThisFragment").commit();

            }
        });
    }



    @Override
    public int getItemCount() {
        return uids.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView command = itemView.findViewById(R.id.firstLine);
        public TextView uploaded = itemView.findViewById(R.id.secondLine);
        public TextView timestamp = itemView.findViewById(R.id.thirdLine);
        public TextView iperf3State = itemView.findViewById(R.id.iperf3State);
        public ImageView icon = itemView.findViewById(R.id.iperf3RunningIndicator);

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);
            command = itemView.findViewById(R.id.firstLine);
            uploaded = itemView.findViewById(R.id.secondLine);
            timestamp = itemView.findViewById(R.id.thirdLine);
            iperf3State = itemView.findViewById(R.id.iperf3State);
            icon = itemView.findViewById(R.id.iperf3RunningIndicator);

        }

    }
}
