package de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class PrettyPrintMap {
    public static Map<String, PrettyPrintValue> dataMap;
    public enum Keys{
        cmdaDbm,
        cmdaEcio,
        evdoDbm,
        evdoEcio,
        evdoSnr,
        cellType,
        alphaLong,
        bands,
        ci,
        TAG,
        mnc,
        pci,
        tac,
        level,
        isRegistered,
        cellConnectionStatus,
        asuLevel,
        lac,
        timingAdvance,
        bitErrorRate,
        dbm,
        rssi,
        earfcn,
        bandwidth,
        cqi,
        rsrp,
        rsrq,
        rssnr,
        mcc,
        nrarfcn,
        csirsrp,
        csirsrq,
        csisinr,
        ssrsrp,
        ssrsrq,
        sssinr
    }

    static {
        dataMap = new HashMap<>();
        dataMap.put("cmdaDbm", new PrettyPrintValue(true, "CMDA dBm"));
        dataMap.put("cmdaEcio", new PrettyPrintValue(true, "CMDA Ec/Io"));
        dataMap.put("evdoDbm", new PrettyPrintValue(true, "EVDO dBm"));
        dataMap.put("evdoEcio", new PrettyPrintValue(true, "EVDO Ec/Io"));
        dataMap.put("evdoSnr", new PrettyPrintValue(true, "EVDO SNR"));
        dataMap.put("cellType", new PrettyPrintValue(true, "Cell Type"));
        dataMap.put("alphaLong", new PrettyPrintValue(true, "Alpha Long"));
        dataMap.put("bands", new PrettyPrintValue(true, "Bands"));
        dataMap.put("ci", new PrettyPrintValue(true, "CI"));
        dataMap.put("TAG", new PrettyPrintValue(false, "TAG"));
        dataMap.put("mnc", new PrettyPrintValue(true, "MNC"));
        dataMap.put("pci", new PrettyPrintValue(true, "PCI"));
        dataMap.put("tac", new PrettyPrintValue(true, "TAC"));
        dataMap.put("level", new PrettyPrintValue(true, "Level"));
        dataMap.put("isRegistered", new PrettyPrintValue(true, "Is Registered"));
        dataMap.put("cellConnectionStatus", new PrettyPrintValue(true, "Cell Connection Status"));
        dataMap.put("asuLevel", new PrettyPrintValue(true, "ASU Level"));
        dataMap.put("lac", new PrettyPrintValue(true, "LAC"));
        dataMap.put("timingAdvance", new PrettyPrintValue(true, "Timing Advance"));
        dataMap.put("bitErrorRate", new PrettyPrintValue(true, "Bit Error Rate"));
        dataMap.put("dbm", new PrettyPrintValue(true, "dBm"));
        dataMap.put("rssi", new PrettyPrintValue(true, "RSSI"));
        dataMap.put("earfcn", new PrettyPrintValue(true, "EARFCN"));
        dataMap.put("bandwidth", new PrettyPrintValue(true, "Bandwidth"));
        dataMap.put("cqi", new PrettyPrintValue(true, "CQI"));
        dataMap.put("rsrp", new PrettyPrintValue(true, "RSRP"));
        dataMap.put("rsrq", new PrettyPrintValue(true, "RSRQ"));
        dataMap.put("rssnr", new PrettyPrintValue(true, "RSSNR"));
        dataMap.put("mcc", new PrettyPrintValue(true, "MCC"));
        dataMap.put("nrarfcn", new PrettyPrintValue(true, "NRARFCN"));
        dataMap.put("csirsrp", new PrettyPrintValue(true, "CSI RSRP"));
        dataMap.put("csirsrq", new PrettyPrintValue(true, "CSI RSRQ"));
        dataMap.put("csisinr", new PrettyPrintValue(true, "CSI SINR"));
        dataMap.put("ssrsrp", new PrettyPrintValue(true, "SS RSRP"));
        dataMap.put("ssrsrq", new PrettyPrintValue(true, "SS RSRQ"));
        dataMap.put("sssinr", new PrettyPrintValue(true, "SS SINR"));
    }

    public static PrettyPrintValue getPrettyPrint(String key) {
        return dataMap.getOrDefault(key, null);
    }


}
