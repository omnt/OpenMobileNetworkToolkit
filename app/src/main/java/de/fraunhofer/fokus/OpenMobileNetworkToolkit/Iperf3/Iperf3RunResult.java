package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.JSON.Timestamp;

@Entity(tableName = "iperf3_result_database")
public class Iperf3RunResult {
    @NonNull
    @PrimaryKey
    public String uid;

    @ColumnInfo(name = "result")
    public int result;

    @ColumnInfo(name = "uploaded")
    public boolean uploaded;

    @ColumnInfo(name = "input")
    @TypeConverters({Iperf3InputConverter.class})
    public Iperf3Fragment.Iperf3Input input;

    public Iperf3RunResult(String uid, int result, boolean upload, Iperf3Fragment.Iperf3Input input){
        this.uid = uid;
        this.result = result;
        this.uploaded = upload;
        this.input = input;
    }


    public Iperf3RunResult() {

    }
}
