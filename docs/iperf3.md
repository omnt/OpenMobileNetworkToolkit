## iPerf3
iPerf3 has been compiled with a [JNI](https://developer.android.com/training/articles/perf-jni) interface to enable OMNT to call it using the specified parameters.
To see how to integrate iPerf3 into your app, look at [iPerf3 Repo](https://github.com/omnt/iperf)

![iPerf3](images/iperf3.png)

Possible iPerf3 run Configurations are:

| **Protocol** | **Single Stream**  | **Multiple Streams** |
|--------------|--------------------|----------------------|
| **UDP**      |                    |                      |
|              | UL                 | UL                   |
|              | DL                 | DL                   |
|              | BIDIR              | BIDIR                |
| **TCP**      |                    |                      |
|              | UL                 | UL                   |
|              | DL                 | DL                   |
|              | BIDIR              | BIDIR                |


| **Protocol** | **Single Stream** | **Multiple Streams** |
|--------------|-------------------|----------------------|
| **UDP**      |                   |                      |
|              | ``` ````              | UL                   |
|              | DL                | DL                   |
|              | BIDIR             | BIDIR                |
| **TCP**      |                   |                      |
|              | UL                | UL                   |
|              | DL                | DL                   |
|              | BIDIR             | BIDIR                |


TCP UL Single Stream
```json
{"event":"interval","data":{"streams":[{"socket":5,"start":0,"end":1.001069,"seconds":1.0010689496994019,"bytes":3994157056,"bits_per_second":31919136496.636753,"retransmits":0,"snd_cwnd":1506109,"snd_wnd":6192128,"rtt":59,"rttvar":14,"pmtu":65535,"omitted":false,"sender":true}],"sum":{"start":0,"end":1.001069,"seconds":1.0010689496994019,"bytes":3994157056,"bits_per_second":31919136496.636753,"retransmits":0,"omitted":false,"sender":true}}}
```
TCP DL Single Stream
```JSON
{"event":"interval","data":{"streams":[{"socket":5,"start":0,"end":1.000363,"seconds":1.0003629922866821,"bytes":3878158336,"bits_per_second":31014008842.011257,"omitted":false,"sender":false}],"sum":{"start":0,"end":1.000363,"seconds":1.0003629922866821,"bytes":3878158336,"bits_per_second":31014008842.011257,"omitted":false,"sender":false}}}
```
TCP BIDIR Single Stream
```json
{"event":"interval","data":{"streams":[{"socket":5,"start":0,"end":1.000228,"seconds":1.0002280473709106,"bytes":3935043584,"bits_per_second":31473171298.031265,"retransmits":0,"snd_cwnd":1309660,"snd_wnd":6192128,"rtt":34,"rttvar":2,"pmtu":65535,"omitted":false,"sender":true},{"socket":7,"start":0,"end":1.025515,"seconds":1.0255149602890015,"bytes":3690070016,"bits_per_second":28786084329.458031,"omitted":false,"sender":false}],"sum":{"start":0,"end":1.000228,"seconds":1.0002280473709106,"bytes":3935043584,"bits_per_second":31473171298.031265,"retransmits":0,"omitted":false,"sender":true},"sum_bidir_reverse":{"start":0,"end":1.000228,"seconds":1.0002280473709106,"bytes":3690070016,"bits_per_second":29513829576.759514,"omitted":false,"sender":false}}}
```

UDP UL Single Stream
```json
{"event":"interval","data":{"streams":[{"socket":5,"start":0,"end":1.000097,"seconds":1.0000970363616943,"bytes":135168,"bits_per_second":1081239.0804935072,"packets":8,"omitted":false,"sender":true}],"sum":{"start":0,"end":1.000097,"seconds":1.0000970363616943,"bytes":135168,"bits_per_second":1081239.0804935072,"packets":8,"omitted":false,"sender":true}}}
```

UDP DL Single Stream
```json
{"event":"interval","data":{"streams":[{"socket":5,"start":1.00107,"end":2.001073,"seconds":1.0000029802322388,"bytes":135168,"bits_per_second":1081340.7773533543,"jitter_ms":0.023491129322622144,"lost_packets":0,"packets":8,"lost_percent":0,"omitted":false,"sender":false}],"sum":{"start":1.00107,"end":2.001073,"seconds":1.0000029802322388,"bytes":135168,"bits_per_second":1081340.7773533543,"jitter_ms":0.023491129322622144,"lost_packets":0,"packets":8,"lost_percent":0,"omitted":false,"sender":false}}}
```

UDP BIDIR Single Stream
```json
{"event":"interval","data":{"streams":[{"socket":5,"start":0,"end":1.000089,"seconds":1.0000890493392944,"bytes":135168,"bits_per_second":1081247.7156053118,"packets":8,"omitted":false,"sender":true},{"socket":7,"start":0,"end":1.000093,"seconds":1.0000929832458496,"bytes":135168,"bits_per_second":1081243.4624733056,"jitter_ms":0.021732455059885976,"lost_packets":0,"packets":8,"lost_percent":0,"omitted":false,"sender":false}],"sum":{"start":0,"end":1.000089,"seconds":1.0000890493392944,"bytes":135168,"bits_per_second":1081247.7156053118,"packets":8,"omitted":false,"sender":true},"sum_bidir_reverse":{"start":0,"end":1.000089,"seconds":1.0000890493392944,"bytes":135168,"bits_per_second":1081247.7156053118,"jitter_ms":0.021732455059885976,"lost_packets":0,"packets":8,"lost_percent":0,"omitted":false,"sender":false}}}
```
[Home](OpenMobileNetworkToolkit.md)