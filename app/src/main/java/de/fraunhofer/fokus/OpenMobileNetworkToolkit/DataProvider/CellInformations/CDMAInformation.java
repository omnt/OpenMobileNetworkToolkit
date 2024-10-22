package de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.CellInformations;

import android.telephony.CellIdentityCdma;
import android.telephony.CellInfoCdma;
import android.telephony.CellSignalStrengthCdma;

import com.influxdb.client.write.Point;

import java.util.Objects;

public class CDMAInformation extends CellInformation {
    private int cmdaDbm;
    private int cmdaEcio;
    private int evdoDbm;
    private int evdoEcio;
    private int evdoSnr;


    public CDMAInformation() {super();}
    public CDMAInformation(long timestamp, CellSignalStrengthCdma cellSignalStrengthCdma){
        super(timestamp);
        cmdaDbm = cellSignalStrengthCdma.getCdmaDbm();
        cmdaEcio = cellSignalStrengthCdma.getCdmaEcio();
        evdoDbm = cellSignalStrengthCdma.getEvdoDbm();
        evdoEcio = cellSignalStrengthCdma.getEvdoEcio();
        evdoSnr = cellSignalStrengthCdma.getEvdoSnr();
        this.setCellType(CellType.CDMA);

    }
    private CDMAInformation(CellInfoCdma cellInfoCdma,
                            CellIdentityCdma cellIdentityCdma,
                            CellSignalStrengthCdma cellSignalStrengthCdma,
                            long timestamp) {
        super(timestamp,
                CellType.CDMA,
                "N/A",
                -1,
                "N/A",
                -1,
                -1,
                cellSignalStrengthCdma.getLevel(),
                Objects.requireNonNull(cellIdentityCdma.getOperatorAlphaLong()).toString(),
                cellSignalStrengthCdma.getAsuLevel(),
                cellInfoCdma.isRegistered(),
                cellInfoCdma.getCellConnectionStatus());
        cmdaDbm = cellSignalStrengthCdma.getCdmaDbm();
        cmdaEcio = cellSignalStrengthCdma.getCdmaEcio();
        evdoDbm = cellSignalStrengthCdma.getEvdoDbm();
        evdoEcio = cellSignalStrengthCdma.getEvdoEcio();
        evdoSnr = cellSignalStrengthCdma.getEvdoSnr();
    }
    public CDMAInformation(CellInfoCdma cellInfoCdma, long timestamp) {
        this(cellInfoCdma, cellInfoCdma.getCellIdentity(), cellInfoCdma.getCellSignalStrength(), timestamp);
    }
    public void setCmdaDbm(int cmdaDbm) {
        this.cmdaDbm = cmdaDbm;
    }

    public void setCmdaEcio(int cmdaEcio) {
        this.cmdaEcio = cmdaEcio;
    }

    public void setEvdoDbm(int evdoDbm) {
        this.evdoDbm = evdoDbm;
    }

    public void setEvdoEcio(int evdoEcio) {
        this.evdoEcio = evdoEcio;
    }

    public void setEvdoSnr(int evdoSnr) {
        this.evdoSnr = evdoSnr;
    }

    public int getCmdaDbm() {
        return cmdaDbm;
    }

    public int getCmdaEcio() {
        return cmdaEcio;
    }

    public int getEvdoDbm() {
        return evdoDbm;
    }

    public int getEvdoEcio() {
        return evdoEcio;
    }

    public int getEvdoSnr() {
        return evdoSnr;
    }

    public String getCmdaDbmString() {
        return Integer.toString(cmdaDbm);
    }

    public String getCmdaEcioString() {
        return Integer.toString(cmdaEcio);
    }

    public String getEvdoDbmString() {
        return Integer.toString(evdoDbm);
    }

    public String getEvdoEcioString() {
        return Integer.toString(evdoEcio);
    }

    public String getEvdoSnrString() {
        return Integer.toString(evdoSnr);
    }

    @Override
    public Point getPoint(Point point){
        super.getPoint(point);
        point.addField("CMDA_DBM", cmdaDbm);
        point.addField("CMDA_ECIO", cmdaEcio);
        point.addField("EVDO_DBM", evdoDbm);
        point.addField("EVDO_ECIO", evdoEcio);
        point.addField("EVDO_SNR", evdoSnr);
        return point;
    }


}
