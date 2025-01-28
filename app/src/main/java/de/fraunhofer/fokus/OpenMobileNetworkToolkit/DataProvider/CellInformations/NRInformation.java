/*
 *  SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.CellInformations;


// https://developer.android.com/reference/android/telephony/CellIdentityLte

import android.os.Build;
import android.telephony.CellIdentityNr;
import android.telephony.CellSignalStrengthNr;
import android.telephony.CellInfoNr;

import com.influxdb.client.write.Point;

import java.util.Arrays;
import java.util.List;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.GlobalVars;

public class NRInformation extends CellInformation {
    private static final String TAG = "NR";
    private int nrarfcn;
    private int csirsrp;
    private int csirsrq;
    private int csisinr;
    private int ssrsrp;
    private int ssrsrq;
    private int sssinr;
    private int dbm;
    private int asuLevel;
    private String mcc;
    private int tac;
    private int timingAdvance;
    private List<Integer> cqis;

    public NRInformation() {
        super();
    }

    public NRInformation(long timestamp, CellSignalStrengthNr cellSignalStrengthNr){
        super(timestamp);
        this.asuLevel = cellSignalStrengthNr.getAsuLevel();
        this.dbm = cellSignalStrengthNr.getDbm();
        this.csirsrp = cellSignalStrengthNr.getCsiRsrp();
        this.csirsrq = cellSignalStrengthNr.getCsiRsrq();
        this.csisinr = cellSignalStrengthNr.getCsiSinr();
        this.ssrsrp = cellSignalStrengthNr.getSsRsrp();
        this.ssrsrq = cellSignalStrengthNr.getSsRsrq();
        this.sssinr = cellSignalStrengthNr.getSsSinr();
        this.cqis = cellSignalStrengthNr.getCsiCqiReport();
        this.setCellType(CellType.NR);
    }

    private NRInformation(CellInfoNr cellInfoNr,
                          CellIdentityNr cellIdentityNr,
                          CellSignalStrengthNr cellSignalStrengthNr, long timestamp){
        super(timestamp,
                CellType.NR,
                "N/A",
                cellIdentityNr.getNci(),
                cellIdentityNr.getMncString(),
                cellIdentityNr.getPci(),
                cellIdentityNr.getTac(),
                cellSignalStrengthNr.getLevel(),
                "N/A",
                cellSignalStrengthNr.getAsuLevel(),
                cellInfoNr.isRegistered(),
                cellInfoNr.getCellConnectionStatus());
        String bands = "N/A";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            bands = Arrays.toString(cellIdentityNr.getBands());
        }
        super.setBands(bands);
        String alphaLong = "N/A";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            alphaLong = String.valueOf(cellIdentityNr.getOperatorAlphaLong());
        }
        super.setAlphaLong(alphaLong);

        this.nrarfcn = cellIdentityNr.getNrarfcn();
        this.mcc = cellIdentityNr.getMccString();
        this.asuLevel = cellSignalStrengthNr.getAsuLevel();
        this.dbm = cellSignalStrengthNr.getDbm();
        this.csirsrp = cellSignalStrengthNr.getCsiRsrp();
        this.csirsrq = cellSignalStrengthNr.getCsiRsrq();
        this.csisinr = cellSignalStrengthNr.getCsiSinr();
        this.ssrsrp = cellSignalStrengthNr.getSsRsrp();
        this.ssrsrq = cellSignalStrengthNr.getSsRsrq();
        this.sssinr = cellSignalStrengthNr.getSsSinr();
        this.cqis = cellSignalStrengthNr.getCsiCqiReport();
        this.tac = cellIdentityNr.getTac();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            this.timingAdvance = cellSignalStrengthNr.getTimingAdvanceMicros();
        }
    }

    public NRInformation(CellInfoNr cellInfoNr, long timestamp){
        this(cellInfoNr, (CellIdentityNr) cellInfoNr.getCellIdentity(), (CellSignalStrengthNr) cellInfoNr.getCellSignalStrength(), timestamp);
    }

    public int getNrarfcn() {
        return nrarfcn;
    }

    public void setNrarfcn(int nrarfcn) {
        this.nrarfcn = nrarfcn;
    }

    public String getMcc() {
        return mcc;
    }

    public void setMcc(String mcc) {
        this.mcc = mcc;
    }

    public int getAsuLevel() {
        return asuLevel;
    }

    public void setAsuLevel(int asuLevel) {
        this.asuLevel = asuLevel;
    }

    public int getDbm() {
        return dbm;
    }

    public List<Integer> getCqis() {
        return cqis;
    }

    public int getFirstCqi() {
        try {
            return cqis.get(0);
        } catch (IndexOutOfBoundsException e) {
            return 0;
        }
    }

    public String getFirstCqiString(){
        return Integer.toString(this.getFirstCqi());
    }

    public void setCqis(List<Integer> cqis) {
        this.cqis = cqis;
    }

    public void setDbm(int dbm) {
        this.dbm = dbm;
    }

    public int getCsirsrp() {
        return csirsrp;
    }

    public void setCsirsrp(int csirsrp) {
        this.csirsrp = csirsrp;
    }

    public int getCsirsrq() {
        return csirsrq;
    }

    public void setCsirsrq(int csirsrq) {
        this.csirsrq = csirsrq;
    }

    public int getCsisinr() {
        return csisinr;
    }

    public void setCsisinr(int csisinr) {
        this.csisinr = csisinr;
    }

    public int getSsrsrp() {
        return ssrsrp;
    }

    public void setSsrsrp(int ssrsrp) {
        this.ssrsrp = ssrsrp;
    }

    public int getSsrsrq() {
        return ssrsrq;
    }

    public void setSsrsrq(int ssrsrq) {
        this.ssrsrq = ssrsrq;
    }

    public int getSssinr() {
        return sssinr;
    }

    public void setSssinr(int sssinr) {
        this.sssinr = sssinr;
    }

    public void setTac(int tac) {
        this.tac = tac;
    }

    public int getTac() {
        return tac;
    }

    public void setTimingAdvance(int timingAdvance) {
        this.timingAdvance = timingAdvance;
    }

    public int getTimingAdvance() {
        return this.timingAdvance;
    }

    public String getTimingAdvanceString() {
        return Integer.toString(this.timingAdvance);
    }

    public String getDbmString() {
        return Integer.toString(this.dbm);
    }

    public String getAsuLevelString() {
        return Integer.toString(this.asuLevel);
    }

    public String getCsirsrpString() {
        return Integer.toString(this.csirsrp);
    }

    public String getCsirsrqString() {
        return Integer.toString(this.csirsrq);
    }

    public String getCsisinrString() {
        return Integer.toString(this.csisinr);
    }

    public String getSsrsrpString() {
        return Integer.toString(this.ssrsrp);
    }

    public String getSsrsrqString() {
        return Integer.toString(this.ssrsrq);
    }

    public String getSssinrString() {
        return Integer.toString(this.sssinr);
    }

    public String getNrarfcnString() {
        return Integer.toString(this.nrarfcn);
    }

    public String getTacString() {
        return Integer.toString(this.tac);
    }

    public String getPlmn() {
        return this.getMcc() + this.getMnc();
    }

    public String getMccString() {
        return this.mcc;
    }

    @Override
    public Point getPoint(Point point){
        super.getPoint(point);
        point.addField("NRARFCN", this.getNrarfcn());
        point.addField("MCC", this.getMcc());
        point.addField("Lac", this.getTac());
        point.addField("DBM", this.getDbm());
        point.addField(GlobalVars.CSIRSRP, this.getCsirsrp());
        point.addField(GlobalVars.CSIRSRQ, this.getCsirsrq());
        point.addField(GlobalVars.CSISINR, this.getCsisinr());
        point.addField(GlobalVars.SSRSRP, this.getSsrsrp());
        point.addField(GlobalVars.SSRSRQ, this.getSsrsrq());
        point.addField(GlobalVars.SSSINR, this.getSssinr());
        point.addField("TimingAdvance", this.getTimingAdvance());
        return point;
    }

    @Override
    public StringBuilder getStringBuilder(){
        StringBuilder stringBuilder = super.getStringBuilder();
        String max = Integer.MAX_VALUE + "";
        if(!this.getSsrsrqString().equals(max)) stringBuilder.append(" SSRSRQ: ").append(this.getSsrsrqString()).append(" dB").append("\n");

        if(!this.getSsrsrpString().equals(max)) stringBuilder.append(" SSRSRP: ").append(this.getSsrsrpString()).append(" dBm").append("\n");

        if(!this.getSssinrString().equals(max)) stringBuilder.append(" SSRSRP: ").append(this.getSssinrString()).append(" dBm").append("\n");

        if(!this.getFirstCqiString().equals(max)) stringBuilder.append(" CQI: ").append(this.getFirstCqiString()).append("\n");

        if(!this.getNrarfcnString().equals(max)) stringBuilder.append(" NRARFCN: ").append(this.getNrarfcnString()).append("\n");

        if(!this.getTimingAdvanceString().equals(max)) stringBuilder.append(" TimingAdvance: ").append(this.getTimingAdvance()).append(" ns").append("\n");

        return stringBuilder;
    }
}
