/*
 *  SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.CellInformations;


// https://developer.android.com/reference/android/telephony/CellIdentityLte

import android.content.Context;
import android.os.Build;
import android.telephony.CellIdentityNr;
import android.telephony.CellSignalStrengthNr;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.telephony.CellInfoNr;
import android.widget.TableLayout;

import com.google.android.material.divider.MaterialDivider;
import com.influxdb.client.write.Point;

import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.PrettyPrintMap;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.GlobalVars;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.JSONtoUI;

public class NR extends CellInformation {
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
    private int lac;
    private int timingAdvance;
    private List<Integer> cqis;

    public NR() {
        super();
    }

    private NR(CellInfoNr cellInfoNr,
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
        this.lac = cellIdentityNr.getTac();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            this.timingAdvance = cellSignalStrengthNr.getTimingAdvanceMicros();
        }
    }

    public NR(CellInfoNr cellInfoNr, long timestamp){
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

    public void setLac(int lac) {
        this.lac = lac;
    }

    public int getLac() {
        return lac;
    }

    public void setTimingAdvance(int timingAdvance) {
        this.timingAdvance = timingAdvance;
    }

    public int getTimingAdvance() {
        return this.timingAdvance;
    }


    @Override
    public TableLayout getTable(TableLayout tl, Context context) {
        addRows(tl, context, new String[][]{
                {PrettyPrintMap.Keys.alphaLong.toString(), String.valueOf(this.getAlphaLong())},
                {PrettyPrintMap.Keys.mcc.toString(), String.valueOf(this.getMcc())},
                {PrettyPrintMap.Keys.mnc.toString(), String.valueOf(this.getMnc())},
                {PrettyPrintMap.Keys.cellType.toString(), String.valueOf(this.getCellType())},
                {PrettyPrintMap.Keys.pci.toString(), String.valueOf(this.getPci())},
                {PrettyPrintMap.Keys.tac.toString(), String.valueOf(this.getTac())},
                {PrettyPrintMap.Keys.ci.toString(), String.valueOf(this.getCi())},
                {PrettyPrintMap.Keys.isRegistered.toString(), String.valueOf(this.isRegistered())},
                {PrettyPrintMap.Keys.cellConnectionStatus.toString(), String.valueOf(this.getCellConnectionStatus())},
        });

        addDivider(tl, context);

        addRows(tl, context, new String[][]{
                {PrettyPrintMap.Keys.bands.toString(), String.valueOf(this.getBands())},
                {PrettyPrintMap.Keys.nrarfcn.toString(), String.valueOf(this.getNrarfcn())},
                {PrettyPrintMap.Keys.lac.toString(), String.valueOf(this.getLac())},
                {PrettyPrintMap.Keys.timingAdvance.toString(), String.valueOf(this.getTimingAdvance())},
        });

        addDivider(tl, context);

        addRows(tl, context, new String[][]{
                {PrettyPrintMap.Keys.dbm.toString(), String.valueOf(this.getDbm())},
                {PrettyPrintMap.Keys.level.toString(), String.valueOf(this.getLevel())},
                {PrettyPrintMap.Keys.asuLevel.toString(), String.valueOf(this.getAsuLevel())},
                {PrettyPrintMap.Keys.csirsrp.toString(), String.valueOf(this.getCsirsrp())},
                {PrettyPrintMap.Keys.csirsrq.toString(), String.valueOf(this.getCsirsrq())},
                {PrettyPrintMap.Keys.csisinr.toString(), String.valueOf(this.getCsisinr())},
                {PrettyPrintMap.Keys.cqi.toString(), String.valueOf(this.getCqis())}
        });

        addDivider(tl, context);

        addRows(tl, context, new String[][]{
                {PrettyPrintMap.Keys.ssrsrp.toString(), String.valueOf(this.getSsrsrp())},
                {PrettyPrintMap.Keys.ssrsrq.toString(), String.valueOf(this.getSsrsrq())},
                {PrettyPrintMap.Keys.sssinr.toString(), String.valueOf(this.getSssinr())},
        });

        return tl;
    }

    @Override
    public Point getPoint(Point point){
        super.getPoint(point);
        point.addField("NRARFCN", this.getNrarfcn());
        point.addField("MCC", this.getMcc());
        point.addField("Lac", this.getLac());
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
        StringBuilder sb = super.getStringBuilder();
        sb.append(" SSRSQ: ").append(this.getSsrsrq());
        sb.append(" SSRSRP: ").append(this.getSsrsrp());
        sb.append(" SSSINR: ").append(this.getSssinr());
        return sb;
    }

}
