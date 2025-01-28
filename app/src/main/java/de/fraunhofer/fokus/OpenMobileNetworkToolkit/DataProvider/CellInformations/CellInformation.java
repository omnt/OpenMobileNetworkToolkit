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
import android.telephony.CellInfo;
import android.util.Log;

import com.influxdb.client.write.Point;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.Information;

public class CellInformation extends Information {
    private static final String TAG = "CellInformation";
    private CellType cellType;
    private String alphaLong;
    private String bands;

    private long ci;
    private String mnc;
    private int pci;
    private int tac;

    private int level;
    private int asuLevel;


    private boolean isRegistered;
    private int cellConnectionStatus;




    private String getPath(){
        switch (cellType){
            case GSM:
                return "cell_information_gsm.json";
            case LTE:
                return "cell_information_lte.json";
            case NR:
                return "cell_information_nr.json";
            case CDMA:
                return "cell_information_cdma.json";
            case UNKNOWN:
                return "cell_information_unknown.json";
            default:
                return "cell_information_unknown.json";
        }
    }

    public CellInformation() {
        super();
    }

    public CellInformation(long timestamp){
        super(timestamp);
    }

    public CellInformation(long timeStamp,
                           CellType cellType,
                           String bands,
                           long ci,
                           String mnc,
                           int pci,
                           int tac,
                           int level,
                           String alphaLong,
                           int asuLevel,
                           boolean isRegistered,
                           int cellConnectionStatus) {
        super(timeStamp);
        this.cellType = cellType;
        this.bands = bands;
        this.ci = ci;
        this.mnc = mnc;
        this.pci = pci;
        this.tac = tac;
        this.level = level;
        this.asuLevel = asuLevel;
        this.isRegistered = isRegistered;
        this.alphaLong = alphaLong;
        this.cellConnectionStatus = cellConnectionStatus;
    }


    public int getCellConnectionStatus() {
        return cellConnectionStatus;
    }

    public void setCellConnectionStatus(int cellConnectionStatus) {
        this.cellConnectionStatus = cellConnectionStatus;
    }

    public boolean isRegistered() {
        return isRegistered;
    }

    public void setRegistered(boolean registered) {
        isRegistered = registered;
    }


    public String getAlphaLong() {
        return alphaLong;
    }

    public void setAlphaLong(String alphaLong) {
        this.alphaLong = alphaLong;
    }

    public int getAsuLevel() {
        return asuLevel;
    }

    public void setAsuLevel(int asuLevel) {
        this.asuLevel = asuLevel;
    }

    public CellType getCellType() {
        return cellType;
    }

    public void setCellType(CellType cellType) {
        this.cellType = cellType;
    }

    public String getBands() {
        return bands;
    }

    public void setBands(String bands) {
        this.bands = bands;
    }

    public long getCi() {
        return ci;
    }

    public String getCiString() {
        return String.valueOf(ci);
    }

    public String getPciString() {
        return String.valueOf(pci);
    }

    public void setCi(long ci) {
        this.ci = ci;
    }

    public String getMnc() {
        return mnc;
    }

    public void setMnc(String mnc) {
        this.mnc = mnc;
    }

    public int getPci() {
        return pci;
    }

    public void setPci(int pci) {
        this.pci = pci;
    }

    public int getTac() {
        return tac;
    }

    public String getTacString() {
        return String.valueOf(tac);
    }

    public void setTac(int tac) {
        this.tac = tac;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public Point getPoint(Point point){
        if(point == null) {
            Log.e(TAG, "getPoint: given point == null!");
            point = Point.measurement("CellInformation");
        }
        point.addField("CellType", cellType.toString());
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            point.addField("Bands", this.getBands());
        }
        long ci = this.getCi();
        if (ci != CellInfo.UNAVAILABLE) {
            point.addTag("CI", String.valueOf(ci));
        }
        point.addField("MNC", this.getMnc());
        if(this.getPci() != -1) point.addField("PCI", this.getPci());
        point.addField("TAC", this.getTac());
        point.addField("Level", this.getLevel());
        point.addField("OperatorAlphaLong", this.getAlphaLong());
        point.addField("ASULevel", this.getAsuLevel());
        point.addField("IsRegistered", this.isRegistered());
        point.addField("CellConnectionStatus", this.getCellConnectionStatus());
        return point;
    }

    public StringBuilder getStringBuilder(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" Type: ").append(this.getCellType()).append("\n");
        if(this.getPci() != -1) stringBuilder.append(" PCI: ").append(this.getPci()).append("\n");
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && this.getAlphaLong() != null && !this.getAlphaLong().equals("N/A") && !this.getAlphaLong().isEmpty()){
            stringBuilder.append(" Alpha Long: ").append(this.getAlphaLong()).append("\n");
        }
        return stringBuilder;
    }

}
