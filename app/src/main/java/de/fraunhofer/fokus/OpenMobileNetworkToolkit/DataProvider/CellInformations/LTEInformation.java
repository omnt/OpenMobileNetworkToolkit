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
import android.telephony.CellIdentityLte;
import android.telephony.CellInfoLte;
import android.telephony.CellSignalStrengthLte;

import com.influxdb.client.write.Point;

import java.util.Arrays;

public class LTEInformation extends CellInformation {

    private int earfcn;
    private int bandwidth;
    private int cqi;
    private int rsrp;
    private int rsrq;
    private int rssi;
    private int rssnr;
    private int timingAdvance;
    private int dbm;
    private String mcc;


    public LTEInformation() {
        super();
    }

    public LTEInformation(long timestamp, CellSignalStrengthLte cellSignalStrengthLte){
        super(timestamp);

        this.cqi = cellSignalStrengthLte.getCqi();
        this.rsrp = cellSignalStrengthLte.getRsrp();
        this.rsrq = cellSignalStrengthLte.getRsrq();
        this.rssi = cellSignalStrengthLte.getRssi();
        this.rssnr = cellSignalStrengthLte.getRssnr();
        this.timingAdvance = cellSignalStrengthLte.getTimingAdvance();
        this.dbm = cellSignalStrengthLte.getDbm();
        this.setCellType(CellType.LTE);

    }

    private LTEInformation(CellInfoLte cellInfoLte,
                           CellIdentityLte cellIdentityLte,
                           CellSignalStrengthLte cellSignalStrengthLte,
                           long timestamp){
        super(timestamp,
                CellType.LTE,
                "N/A",
                cellIdentityLte.getCi(),
                cellIdentityLte.getMncString(),
                cellIdentityLte.getPci(),
                cellIdentityLte.getTac(),
                cellSignalStrengthLte.getLevel(),
                "N/A",
                cellSignalStrengthLte.getAsuLevel(),
                cellInfoLte.isRegistered(),
                cellInfoLte.getCellConnectionStatus());

        String bands = "N/A";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            bands = Arrays.toString(cellIdentityLte.getBands());
        }
        super.setBands(bands);
        String alphaLong = "N/A";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            alphaLong = String.valueOf(cellIdentityLte.getOperatorAlphaLong());
        }
        super.setAlphaLong(alphaLong);

        this.bandwidth = cellIdentityLte.getBandwidth();
        this.earfcn = cellIdentityLte.getEarfcn();
        this.mcc = cellIdentityLte.getMccString();

        this.cqi = cellSignalStrengthLte.getCqi();
        this.rsrp = cellSignalStrengthLte.getRsrp();
        this.rsrq = cellSignalStrengthLte.getRsrq();
        this.rssi = cellSignalStrengthLte.getRssi();
        this.rssnr = cellSignalStrengthLte.getRssnr();
        this.timingAdvance = cellSignalStrengthLte.getTimingAdvance();
        this.dbm = cellSignalStrengthLte.getDbm();

    }

    public LTEInformation(CellInfoLte cellInfoLte,
                          long timestamp){
        this(cellInfoLte,
                cellInfoLte.getCellIdentity(),
                cellInfoLte.getCellSignalStrength(),
                timestamp);
    }

    public int getDbm() {
        return dbm;
    }

    public void setDbm(int dbm) {
        this.dbm = dbm;
    }

    public String getMcc() {
        return mcc;
    }

    public void setMcc(String mcc) {
        this.mcc = mcc;
    }

    public int getBandwidth() {
        return bandwidth;
    }

    public void setBandwidth(int bandwidth) {
        this.bandwidth = bandwidth;
    }

    public int getRsrp() {
        return rsrp;
    }

    public void setRsrp(int rsrp) {
        this.rsrp = rsrp;
    }

    public int getRsrq() {
        return rsrq;
    }

    public void setRsrq(int rsrq) {
        this.rsrq = rsrq;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public int getRssnr() {
        return rssnr;
    }

    public void setRssnr(int rssnr) {
        this.rssnr = rssnr;
    }

    public int getTimingAdvance() {
        return timingAdvance;
    }

    public void setTimingAdvance(int timingAdvance) {
        this.timingAdvance = timingAdvance;
    }

    public int getCqi() {
        return cqi;
    }

    public void setCqi(int cqi) {
        this.cqi = cqi;
    }

    public int getEarfcn() {
        return earfcn;
    }

    public void setEarfcn(int earfcn) {
        this.earfcn = earfcn;
    }

    public String getEarfcnString() {
        return Integer.toString(earfcn);
    }

    public String getBandwidthString() {
        return Integer.toString(bandwidth);
    }

    public String getCqiString() {
        return Integer.toString(cqi);
    }

    public String getRsrpString() {
        return Integer.toString(rsrp);
    }

    public String getRsrqString() {
        return Integer.toString(rsrq);
    }

    public String getRssiString() {
        return Integer.toString(rssi);
    }

    public String getRssnrString() {
        return Integer.toString(rssnr);
    }

    public String getTimingAdvanceString() {
        return Integer.toString(timingAdvance);
    }

    public String getDbmString() {
        return Integer.toString(dbm);
    }

    @Override
    public Point getPoint(Point point){
        return point;
    }

    @Override
    public StringBuilder getStringBuilder(){
        StringBuilder stringBuilder = super.getStringBuilder();
        stringBuilder.append(" RSRQ: ").append(this.getRsrq());
        stringBuilder.append(" RSRP: ").append(this.getRsrp());
        stringBuilder.append(" RSSI: ").append(this.getRssi());
        stringBuilder.append(" RSSNR: ").append(this.getRssnr());
        stringBuilder.append(" CQI: ").append(this.getCqi());
        stringBuilder.append(" Bandwidth: ").append(this.getBandwidth());
        stringBuilder.append(" EARFCN: ").append(this.getEarfcn());
        stringBuilder.append(" TimingAdvance: ").append(this.getTimingAdvance());


        return stringBuilder;

    }

}
