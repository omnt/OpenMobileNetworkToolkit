package de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider;

import java.util.HashMap;
import java.util.Map;


public class PrettyPrintMap {
    public static Map<String, PrettyPrintValue> dataMap;
    public enum cellInformation {
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

    public enum deviceInformation{
        MODEL,
        Manufacturer,
        SOCManufacturer,
        SOCModel,
        RadioVersion,
        SupportedModemCount,
        AndroidSDK,
        AndroidRelease,
        DeviceSoftwareVersion,
        SecurityPatchLevel,
        IMEI,
        IMSI,
        MEID,
        SimSerial,
        SubscriberId,
        NetworkAccessIdentifier,
        SubscriptionId
    }

    public enum locationInformation{
        longitude,
        latitude,
        altitude,
        provider,
        accuracy,
        speed,
        pl
    }

    public enum networkInformation {
        networkOperatorName,
        simOperatorName,
        networkSpecifier,
        dataState,
        dataNetworkType,
        phoneType,
        preferredOpportunisticDataSubscriptionId
    }

    public enum wifiInformation {
        ssid,
        bssid,
        rssi,
        frequency,
        link_speed,
        tx_link_speed,
        max_tx_link_speed,
        rx_link_speed,
        max_rx_link_speed,
        standard,
        channel_bandwidth
    }

    static {
        dataMap = new HashMap<>();

        //cellInformation
        dataMap.put(cellInformation.cmdaDbm.toString(), new PrettyPrintValue(true, "CMDA dBm"));
        dataMap.put(cellInformation.cmdaEcio.toString(), new PrettyPrintValue(true, "CMDA Ec/Io"));
        dataMap.put(cellInformation.evdoDbm.toString(), new PrettyPrintValue(true, "EVDO dBm"));
        dataMap.put(cellInformation.evdoEcio.toString(), new PrettyPrintValue(true, "EVDO Ec/Io"));
        dataMap.put(cellInformation.evdoSnr.toString(), new PrettyPrintValue(true, "EVDO SNR"));
        dataMap.put(cellInformation.cellType.toString(), new PrettyPrintValue(true, "Cell Type"));
        dataMap.put(cellInformation.alphaLong.toString(), new PrettyPrintValue(true, "Alpha Long"));
        dataMap.put(cellInformation.bands.toString(), new PrettyPrintValue(true, "Bands"));
        dataMap.put(cellInformation.ci.toString(), new PrettyPrintValue(true, "CI"));
        dataMap.put(cellInformation.TAG.toString(), new PrettyPrintValue(false, "TAG"));
        dataMap.put(cellInformation.mnc.toString(), new PrettyPrintValue(true, "MNC"));
        dataMap.put(cellInformation.pci.toString(), new PrettyPrintValue(true, "PCI"));
        dataMap.put(cellInformation.tac.toString(), new PrettyPrintValue(true, "TAC"));
        dataMap.put(cellInformation.level.toString(), new PrettyPrintValue(true, "Level"));
        dataMap.put(cellInformation.isRegistered.toString(), new PrettyPrintValue(true, "Is Registered"));
        dataMap.put(cellInformation.cellConnectionStatus.toString(), new PrettyPrintValue(true, "Cell Connection Status"));
        dataMap.put(cellInformation.asuLevel.toString(), new PrettyPrintValue(true, "ASU Level"));
        dataMap.put(cellInformation.lac.toString(), new PrettyPrintValue(true, "LAC"));
        dataMap.put(cellInformation.timingAdvance.toString(), new PrettyPrintValue(true, "Timing Advance"));
        dataMap.put(cellInformation.bitErrorRate.toString(), new PrettyPrintValue(true, "Bit Error Rate"));
        dataMap.put(cellInformation.dbm.toString(), new PrettyPrintValue(true, "dBm"));
        dataMap.put(cellInformation.rssi.toString(), new PrettyPrintValue(true, "RSSI"));
        dataMap.put(cellInformation.earfcn.toString(), new PrettyPrintValue(true, "EARFCN"));
        dataMap.put(cellInformation.bandwidth.toString(), new PrettyPrintValue(true, "Bandwidth"));
        dataMap.put(cellInformation.cqi.toString(), new PrettyPrintValue(true, "CQI"));
        dataMap.put(cellInformation.rsrp.toString(), new PrettyPrintValue(true, "RSRP"));
        dataMap.put(cellInformation.rsrq.toString(), new PrettyPrintValue(true, "RSRQ"));
        dataMap.put(cellInformation.rssnr.toString(), new PrettyPrintValue(true, "RSSNR"));
        dataMap.put(cellInformation.mcc.toString(), new PrettyPrintValue(true, "MCC"));
        dataMap.put(cellInformation.nrarfcn.toString(), new PrettyPrintValue(true, "NRARFCN"));
        dataMap.put(cellInformation.csirsrp.toString(), new PrettyPrintValue(true, "CSI RSRP"));
        dataMap.put(cellInformation.csirsrq.toString(), new PrettyPrintValue(true, "CSI RSRQ"));
        dataMap.put(cellInformation.csisinr.toString(), new PrettyPrintValue(true, "CSI SINR"));
        dataMap.put(cellInformation.ssrsrp.toString(), new PrettyPrintValue(true, "SS RSRP"));
        dataMap.put(cellInformation.ssrsrq.toString(), new PrettyPrintValue(true, "SS RSRQ"));
        dataMap.put(cellInformation.sssinr.toString(), new PrettyPrintValue(true, "SS SINR"));

        //deviceInformation
        dataMap.put(deviceInformation.MODEL.toString(), new PrettyPrintValue(true, "Model"));
        dataMap.put(deviceInformation.Manufacturer.toString(), new PrettyPrintValue(true, "Manufacturer"));
        dataMap.put(deviceInformation.SOCManufacturer.toString(), new PrettyPrintValue(true, "SoC Manufacturer"));
        dataMap.put(deviceInformation.SOCModel.toString(), new PrettyPrintValue(true, "SoC Model"));
        dataMap.put(deviceInformation.RadioVersion.toString(), new PrettyPrintValue(true, "Radio Version"));
        dataMap.put(deviceInformation.SupportedModemCount.toString(), new PrettyPrintValue(true, "Supported Modem Count"));
        dataMap.put(deviceInformation.AndroidSDK.toString(), new PrettyPrintValue(true, "Android SDK"));
        dataMap.put(deviceInformation.AndroidRelease.toString(), new PrettyPrintValue(true, "Android Release"));
        dataMap.put(deviceInformation.DeviceSoftwareVersion.toString(), new PrettyPrintValue(true, "Device Software Version"));
        dataMap.put(deviceInformation.SecurityPatchLevel.toString(), new PrettyPrintValue(true, "Security Patch Level"));
        dataMap.put(deviceInformation.IMEI.toString(), new PrettyPrintValue(true, "IMEI"));
        dataMap.put(deviceInformation.IMSI.toString(), new PrettyPrintValue(true, "IMSI"));
        dataMap.put(deviceInformation.MEID.toString(), new PrettyPrintValue(true, "MEID"));
        dataMap.put(deviceInformation.SimSerial.toString(), new PrettyPrintValue(true, "SIM Serial"));
        dataMap.put(deviceInformation.SubscriberId.toString(), new PrettyPrintValue(true, "Subscriber ID"));
        dataMap.put(deviceInformation.NetworkAccessIdentifier.toString(), new PrettyPrintValue(true, "Network Access Identifier"));
        dataMap.put(deviceInformation.SubscriptionId.toString(), new PrettyPrintValue(true, "Subscription ID"));

        //locationInformation
        dataMap.put(locationInformation.longitude.toString(), new PrettyPrintValue(true, "Longitude"));
        dataMap.put(locationInformation.latitude.toString(), new PrettyPrintValue(true, "Latitude"));
        dataMap.put(locationInformation.altitude.toString(), new PrettyPrintValue(true, "Altitude"));
        dataMap.put(locationInformation.provider.toString(), new PrettyPrintValue(true, "Provider"));
        dataMap.put(locationInformation.accuracy.toString(), new PrettyPrintValue(true, "Accuracy"));
        dataMap.put(locationInformation.speed.toString(), new PrettyPrintValue(true, "Speed"));
        dataMap.put(locationInformation.pl.toString(), new PrettyPrintValue(true, "Provider List"));

        //networkInformation
        dataMap.put(networkInformation.networkOperatorName.toString(), new PrettyPrintValue(true, "Network Operator Name"));
        dataMap.put(networkInformation.simOperatorName.toString(), new PrettyPrintValue(true, "SIM Operator Name"));
        dataMap.put(networkInformation.networkSpecifier.toString(), new PrettyPrintValue(true, "Network Specifier"));
        dataMap.put(networkInformation.dataState.toString(), new PrettyPrintValue(true, "Data State"));
        dataMap.put(networkInformation.dataNetworkType.toString(), new PrettyPrintValue(true, "Data Network Type"));
        dataMap.put(networkInformation.phoneType.toString(), new PrettyPrintValue(true, "Phone Type"));
        dataMap.put(networkInformation.preferredOpportunisticDataSubscriptionId.toString(), new PrettyPrintValue(true, "Preferred Opportunistic Data Subscription ID"));

        //wifiInformation
        dataMap.put(wifiInformation.ssid.toString(), new PrettyPrintValue(true, "SSID"));
        dataMap.put(wifiInformation.bssid.toString(), new PrettyPrintValue(true, "BSSID"));
        dataMap.put(wifiInformation.rssi.toString(), new PrettyPrintValue(true, "RSSI"));
        dataMap.put(wifiInformation.frequency.toString(), new PrettyPrintValue(true, "Frequency"));
        dataMap.put(wifiInformation.link_speed.toString(), new PrettyPrintValue(true, "Link Speed"));
        dataMap.put(wifiInformation.tx_link_speed.toString(), new PrettyPrintValue(true, "TX Link Speed"));
        dataMap.put(wifiInformation.max_tx_link_speed.toString(), new PrettyPrintValue(true, "Max Supported TX Speed"));
        dataMap.put(wifiInformation.rx_link_speed.toString(), new PrettyPrintValue(true, "RX Link Speed"));
        dataMap.put(wifiInformation.max_rx_link_speed.toString(), new PrettyPrintValue(true, "Max Supported RX Speed"));
        dataMap.put(wifiInformation.standard.toString(), new PrettyPrintValue(true, "Standard"));
        dataMap.put(wifiInformation.channel_bandwidth.toString(), new PrettyPrintValue(true, "Channel Bandwidth"));



    }

    public static PrettyPrintValue getPrettyPrint(String key) {
        return dataMap.getOrDefault(key, null);
    }


}
