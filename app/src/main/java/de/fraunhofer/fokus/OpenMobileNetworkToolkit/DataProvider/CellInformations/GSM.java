package de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.CellInformations;


import android.content.Context;
import android.telephony.CellIdentityGsm;
import android.telephony.CellInfoGsm;
import android.telephony.CellSignalStrengthGsm;
import android.widget.TableLayout;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.PrettyPrintMap;

public class GSM extends CellInformation {
    private int lac;
    private int timingAdvance;
    private int bitErrorRate;
    private int dbm;
    private int rssi;

    public GSM() {super();}
    public GSM(long timestamp, CellSignalStrengthGsm cellSignalStrengthGsm){
        super(timestamp);
        this.timingAdvance = cellSignalStrengthGsm.getTimingAdvance();
        this.bitErrorRate = cellSignalStrengthGsm.getBitErrorRate();
        this.dbm = cellSignalStrengthGsm.getDbm();
        this.rssi = cellSignalStrengthGsm.getRssi();
        this.setCellType(CellType.GSM);
    }

    private GSM(CellInfoGsm cellInfoGsm,
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
        timingAdvance = cellSignalStrengthGsm.getTimingAdvance();
        bitErrorRate =  cellSignalStrengthGsm.getBitErrorRate();
        dbm = cellSignalStrengthGsm.getDbm();
        rssi = cellSignalStrengthGsm.getRssi();

    }
    public GSM(CellInfoGsm cellInfoGSM, long timestamp) {
        this(cellInfoGSM, cellInfoGSM.getCellIdentity(), cellInfoGSM.getCellSignalStrength(), timestamp);
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

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    @Override
    public TableLayout getTable(TableLayout tl, Context context, boolean displayNull) {
        addRows(tl, context, new String[][]{
                {PrettyPrintMap.Keys.alphaLong.toString(), String.valueOf(this.getAlphaLong())},
                {PrettyPrintMap.Keys.mnc.toString(), String.valueOf(this.getMnc())},
                {PrettyPrintMap.Keys.cellType.toString(), String.valueOf(this.getCellType())},
                {PrettyPrintMap.Keys.ci.toString(), String.valueOf(this.getCi())},
                {PrettyPrintMap.Keys.isRegistered.toString(), String.valueOf(this.isRegistered())},
                {PrettyPrintMap.Keys.cellConnectionStatus.toString(), String.valueOf(this.getCellConnectionStatus())},
        }, displayNull);

        addDivider(tl, context);

        addRows(tl, context, new String[][]{
                {PrettyPrintMap.Keys.bands.toString(), String.valueOf(this.getBands())},
                {PrettyPrintMap.Keys.lac.toString(), String.valueOf(this.getLac())},
                {PrettyPrintMap.Keys.timingAdvance.toString(), String.valueOf(this.getTimingAdvance())},
        }, displayNull);

        addDivider(tl, context);

        addRows(tl, context, new String[][]{
                {PrettyPrintMap.Keys.dbm.toString(), String.valueOf(this.getDbm())},
                {PrettyPrintMap.Keys.level.toString(), String.valueOf(this.getLevel())},
                {PrettyPrintMap.Keys.asuLevel.toString(), String.valueOf(this.getAsuLevel())},
                {PrettyPrintMap.Keys.bitErrorRate.toString(), String.valueOf(this.getBitErrorRate())},
                {PrettyPrintMap.Keys.rssi.toString(), String.valueOf(this.getRssi())},
        }, displayNull);

        return tl;
    }
}
