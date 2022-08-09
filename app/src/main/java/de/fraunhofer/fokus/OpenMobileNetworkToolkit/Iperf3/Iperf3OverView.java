package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import java.util.LinkedList;

public class Iperf3OverView {
    private LinkedList<Iperf3Runner> iperf3Runners;

    private ThreadGroup iperf3TG;
    private Iperf3DBHandler iperf3DBHandler;

    public Iperf3OverView(ThreadGroup iperf3TG, Iperf3DBHandler iperf3DBHandler) {
        this.iperf3Runners = new LinkedList<>();
        this.iperf3TG = iperf3TG;
        this.iperf3DBHandler = iperf3DBHandler;
    }

    public boolean existRunner(Iperf3Runner iperf3R){
        return this.iperf3Runners.contains(iperf3R);
    }

    public String[] updateRunners(){
        String[] runnersID = new String[getRunnerLength()];
        int i = 0;
        for (Iperf3Runner iperf3R: this.iperf3Runners) {
            iperf3R.checkState();
            runnersID[i] = iperf3R.getId();
            i++;
            this.iperf3DBHandler.addNewRunner(iperf3R.getId(), iperf3R.makeByte(iperf3R));
        }

        return runnersID;
    }

    public int getRunnerLength(){
        return this.iperf3Runners.size();
    }

    public boolean addRunner(Iperf3Runner iperf3R){
         assert !existRunner(iperf3R) : "Runner "+iperf3R.getId()+" already exists!";
         return this.iperf3Runners.add(iperf3R);
    }
}
