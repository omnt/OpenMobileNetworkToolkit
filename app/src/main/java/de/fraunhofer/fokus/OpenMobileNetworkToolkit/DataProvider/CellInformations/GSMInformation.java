package de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.CellInformations;


import android.telephony.CellIdentityGsm;
import android.telephony.CellInfoGsm;
import android.telephony.CellSignalStrengthGsm;

public class GSMInformation extends CellInformation {
    private int lac;
    private int timingAdvance;
    private int bitErrorRate;
    private int dbm;
    private int rssi;
    private int bsic;
    private String mcc;

    public GSMInformation() {super();}
    public GSMInformation(long timestamp, CellSignalStrengthGsm cellSignalStrengthGsm){
        super(timestamp);
        this.timingAdvance = cellSignalStrengthGsm.getTimingAdvance();
        this.bitErrorRate = cellSignalStrengthGsm.getBitErrorRate();
        this.dbm = cellSignalStrengthGsm.getDbm();
        this.rssi = cellSignalStrengthGsm.getRssi();
        this.setCellType(CellType.GSM);
    }

    private GSMInformation(CellInfoGsm cellInfoGsm,
                           CellIdentityGsm cellIdentityGsm,
                           CellSignalStrengthGsm cellSignalStrengthGsm,
                           long timestamp) {
        super(timestamp,
                CellType.GSM,
                "N/A",
                cellIdentityGsm.getCid(),
                cellIdentityGsm.getMncString(),
                -1,
                -1,
                cellSignalStrengthGsm.getLevel(),
                cellIdentityGsm.getOperatorAlphaLong().toString(),
                cellSignalStrengthGsm.getAsuLevel(),
                cellInfoGsm.isRegistered(),
                cellInfoGsm.getCellConnectionStatus());

        String band = "N/A";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            band = Integer.toString(cellIdentityGsm.getArfcn());
        }
        super.setBands(band);


        lac = cellIdentityGsm.getLac();
        bsic = cellIdentityGsm.getBsic();

        mcc = cellIdentityGsm.getMncString();
        timingAdvance = cellSignalStrengthGsm.getTimingAdvance();
        bitErrorRate =  cellSignalStrengthGsm.getBitErrorRate();
        dbm = cellSignalStrengthGsm.getDbm();
        rssi = cellSignalStrengthGsm.getRssi();

    }
    public GSMInformation(CellInfoGsm cellInfoGSM, long timestamp) {
        this(cellInfoGSM, cellInfoGSM.getCellIdentity(), cellInfoGSM.getCellSignalStrength(), timestamp);
    }

    public int getBsic() {
        return bsic;
    }

    public void setBsic(int bsic) {
        this.bsic = bsic;
    }

    public String getMcc() {
        return mcc;
    }

    public void setMcc(String mcc) {
        this.mcc = mcc;
    }

    public int getLac() {
        return lac;
    }

    public void setLac(int lac) {
        this.lac = lac;
    }

    public int getTimingAdvance() {
        return timingAdvance;
    }

    public void setTimingAdvance(int timingAdvance) {
        this.timingAdvance = timingAdvance;
    }

    public int getBitErrorRate() {
        return bitErrorRate;
    }

    public void setBitErrorRate(int bitErrorRate) {
        this.bitErrorRate = bitErrorRate;
    }

    public int getDbm() {
        return dbm;
    }

    public void setDbm(int dbm) {
        this.dbm = dbm;
    }

    public int getRssi() {
        return rssi;
    }

    public String getRssiString() {
        return Integer.toString(rssi);
    }

    public String getBitErrorRateString() {
        return Integer.toString(bitErrorRate);
    }

    public String getBsicString() {
        return Integer.toString(bsic);
    }

    public String getLacString() {
        return Integer.toString(lac);
    }

    public String getTimingAdvanceString() {
        return Integer.toString(timingAdvance);
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

}
