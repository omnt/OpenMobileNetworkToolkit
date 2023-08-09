package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Model;

public class SignalStrengthInformation {
    private long timestamp;
    private int Level;
    private int CsiRSRP;
    private int CsiRSRQ;
    private int CsiSINR;
    private int SSRSRP;
    private int SSRSRQ;
    private int SSSINR;
    private int CQI;
    private int RSRP;
    private int RSRQ;
    private int RSSI;
    private int RSSNR;
    private int EvoDbm;
    private int AsuLevel;
    private int Dbm;

    private connectionTypes connectionType;

    public SignalStrengthInformation(long timestamp) {
        this.timestamp = timestamp;
    }

    public connectionTypes getConnectionType() {
        return this.connectionType;
    }

    public void setConnectionType(connectionTypes connectionType) {
        this.connectionType = connectionType;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getLevel() {
        return Level;
    }

    public void setLevel(int level) {
        Level = level;
    }

    public int getCsiRSRP() {
        return CsiRSRP;
    }

    public void setCsiRSRP(int csiRSRP) {
        CsiRSRP = csiRSRP;
    }

    public int getCsiRSRQ() {
        return CsiRSRQ;
    }

    public void setCsiRSRQ(int csiRSRQ) {
        CsiRSRQ = csiRSRQ;
    }

    public int getCsiSINR() {
        return CsiSINR;
    }

    public void setCsiSINR(int csiSINR) {
        CsiSINR = csiSINR;
    }

    public int getSSRSRP() {
        return SSRSRP;
    }

    public void setSSRSRP(int SSRSRP) {
        this.SSRSRP = SSRSRP;
    }

    public int getSSRSRQ() {
        return SSRSRQ;
    }

    public void setSSRSRQ(int SSRSRQ) {
        this.SSRSRQ = SSRSRQ;
    }

    public int getSSSINR() {
        return SSSINR;
    }

    public void setSSSINR(int SSSINR) {
        this.SSSINR = SSSINR;
    }

    public int getCQI() {
        return CQI;
    }

    public void setCQI(int CQI) {
        this.CQI = CQI;
    }

    public int getRSRP() {
        return RSRP;
    }

    public void setRSRP(int RSRP) {
        this.RSRP = RSRP;
    }

    public int getRSRQ() {
        return RSRQ;
    }

    public void setRSRQ(int RSRQ) {
        this.RSRQ = RSRQ;
    }

    public int getRSSI() {
        return RSSI;
    }

    public void setRSSI(int RSSI) {
        this.RSSI = RSSI;
    }

    public int getRSSNR() {
        return RSSNR;
    }

    public void setRSSNR(int RSSNR) {
        this.RSSNR = RSSNR;
    }

    public int getEvoDbm() {
        return EvoDbm;
    }

    public void setEvoDbm(int evoDbm) {
        EvoDbm = evoDbm;
    }

    public int getAsuLevel() {
        return AsuLevel;
    }

    public void setAsuLevel(int asuLevel) {
        AsuLevel = asuLevel;
    }

    public int getDbm() {
        return Dbm;
    }

    public void setDbm(int dbm) {
        Dbm = dbm;
    }

    public enum connectionTypes {
        NR,
        LTE,
        CDMA,
        GSM
    }
}
