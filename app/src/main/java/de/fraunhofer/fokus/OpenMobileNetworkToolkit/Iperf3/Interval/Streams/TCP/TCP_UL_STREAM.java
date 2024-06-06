package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Interval.Streams.TCP;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3.Interval.Streams.Stream;
import org.json.JSONException;
import org.json.JSONObject;

public class TCP_UL_STREAM extends TCP_STREAM {
    private int retransmits;
    private int snd_cwnd;
    private int snd_wnd;
    private int rtt;
    private int rttvar;
    private int pmtu;

    public TCP_UL_STREAM(){
        super();
    }
    public void parse(JSONObject data) throws JSONException {
        super.parse(data);
        this.retransmits = data.getInt("retransmits");
        this.snd_cwnd = data.getInt("snd_cwnd");
        this.snd_wnd = data.getInt("snd_wnd");
        this.rtt = data.getInt("rtt");
        this.rttvar = data.getInt("rttvar");
        this.pmtu = data.getInt("pmtu");
    }
}
