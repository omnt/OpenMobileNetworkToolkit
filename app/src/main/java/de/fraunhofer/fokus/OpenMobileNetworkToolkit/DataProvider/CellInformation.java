/*
 *  SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider;


// https://developer.android.com/reference/android/telephony/CellIdentityLte

import android.content.Context;
import android.text.Html;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import java.util.Objects;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.GlobalVars;

public class CellInformation {
    private long timeStamp;
    private String cellType;
    private String alphaLong;
    private String bands;
    private long ci;
    private String mnc;
    private int pci;
    private int tac;
    private int level;

    private boolean isRegistered;
    private int cellConnectionStatus;
    // LTE
    private int earfcn;
    private int bandwidth;
    private int cqi;
    private int rsrp;
    private int rsrq;
    private int rssi;
    private int rssnr;
    // 5G
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
    private int arfcn;
    private int lac;
    private int timingAdvance;

    public CellInformation() {
    }

    public CellInformation(long timeStamp, String cellType, String bands, long ci, String mnc,
                           int pci, int tac, int level) {
        this.timeStamp = timeStamp;
        this.cellType = cellType;
        this.bands = bands;
        this.ci = ci;
        this.mnc = mnc;
        this.pci = pci;
        this.tac = tac;
        this.level = level;
    }

    public int getARFCN() {
        return arfcn;
    }

    public void setARFCN(int arfcn) {
        this.arfcn = arfcn;
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

    public void setDbm(int dbm) {
        this.dbm = dbm;
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

    public int getCsirsrp() {
        return csirsrp;
    }

    public void setCsirsrp(int csirsrp) {
        this.csirsrp = csirsrp;
    }

    public String getAlphaLong() {
        return alphaLong;
    }

    public void setAlphaLong(String alphaLong) {
        this.alphaLong = alphaLong;
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
        this.rsrp = ssrsrp;
    }

    public int getSsrsrq() {
        return ssrsrq;
    }

    public void setSsrsrq(int ssrsrq) {
        this.ssrsrq = ssrsrq;
        this.rsrq = ssrsrq;
    }

    public int getSssinr() {
        return sssinr;
    }

    public void setSssinr(int sssinr) {
        this.sssinr = sssinr;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getCellType() {
        return cellType;
    }

    public void setCellType(String cellType) {
        this.cellType = cellType;
    }

    public String getBands() {
        return bands;
    }

    public void setBands(String bands) {
        this.bands = bands;
    }

    public int getBandwidth() {
        return bandwidth;
    }

    public void setBandwidth(int bandwidth) {
        this.bandwidth = bandwidth;
    }

    public long getCi() {
        return ci;
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

    public void setTac(int tac) {
        this.tac = tac;
    }

    public int getCqi() {
        return cqi;
    }

    public void setCqi(int cqi) {
        this.cqi = cqi;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
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

    private LinearLayout createRow(Context context){
        LinearLayout ll = new LinearLayout(context);
        ll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        ll.setOrientation(LinearLayout.HORIZONTAL);
        return ll;
    }

    private CardView createCard(String title, String value, Context context) {
        CardView card = new CardView(context);
        LinearLayout.LayoutParams cardViewLayout = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardViewLayout.weight = 1;
        card.setLayoutParams(cardViewLayout);
        card.setRadius(9);
        card.setCardElevation(9);
        card.setMaxCardElevation(9);
        card.setUseCompatPadding(true);
        card.setPreventCornerOverlap(true);
        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.VERTICAL);

        TextView titleTV = new TextView(context);
        titleTV.setText(title);
        titleTV.setTextSize(20);
        titleTV.setPadding(10, 10, 10, 10);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        titleParams.gravity = Gravity.CENTER;
        titleParams.weight = 1;
        titleTV.setLayoutParams(titleParams);
        ll.addView(titleTV);

        TextView valueTV = new TextView(context);
        LinearLayout.LayoutParams valueParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        valueParams.gravity = Gravity.CENTER;
        valueParams.weight = 1;
        valueTV.setLayoutParams(valueParams);
        valueTV.setTextSize(20);

        if (value.isEmpty() || value.equals("") || value.equals(String.valueOf(Integer.MAX_VALUE))) {
            value = "N/A";
        }
        if (value.length() > 7) { // Adjust the length threshold as needed
            valueTV.setTextSize(19);
        }

        valueTV.setText(value);
        valueTV.setPadding(10, 10, 10, 10);
        ll.addView(valueTV);

        card.addView(ll);
        return card;
    }

    public LinearLayout createQuickView(Context context) {
        LinearLayout ll = new LinearLayout(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setLayoutParams(params);

        LinearLayout statusBar = createRow(context);
        statusBar.addView(createCard("PLMN", getMcc()+"/"+getMnc(), context));
        statusBar.addView(createCard("CI", String.valueOf(getCi()), context));
        if(!cellType.equals("GSM")){
            statusBar.addView(createCard("PCI", String.valueOf(getPci()), context));
            statusBar.addView(createCard("TAC", String.valueOf(getTac()), context));
        }

        LinearLayout statusBarHeader = new LinearLayout(context);
        statusBarHeader.setOrientation(LinearLayout.VERTICAL);
        TextView statusBarTV = new TextView(context);
        statusBarTV.setText("General");
        statusBarTV.setTextSize(23);
        statusBarTV.setPadding(10, 10, 10, 10);
        statusBarHeader.addView(statusBarTV);
        statusBarHeader.addView(statusBar);

        ll.addView(statusBarHeader);


        LinearLayout evolutionSepcific = createRow(context);
        switch (getCellType()){
            case "GSM":
                evolutionSepcific.addView(createCard("LAC", String.valueOf(getLac()), context));
                break;
            case "LTE":
                evolutionSepcific.addView(createCard(GlobalVars.CQI, String.valueOf(getCqi()), context));
                evolutionSepcific.addView(createCard(GlobalVars.RSRQ, String.valueOf(getRsrq()), context));
                evolutionSepcific.addView(createCard(GlobalVars.RSRP, String.valueOf(getRsrp()), context));
                evolutionSepcific.addView(createCard(GlobalVars.RSSNR, String.valueOf(getRssnr()), context));
                break;
            case "NR":
                evolutionSepcific.addView(createCard(GlobalVars.SSRSRP, String.valueOf(getSsrsrp()), context));
                evolutionSepcific.addView(createCard(GlobalVars.SSRSRQ, String.valueOf(getSsrsrq()), context));
                evolutionSepcific.addView(createCard(GlobalVars.SSSINR, String.valueOf(getSssinr()), context));
                break;
            default:
                break;
        }

        LinearLayout evolutionSepcificHeader = new LinearLayout(context);
        evolutionSepcificHeader.setOrientation(LinearLayout.VERTICAL);
        TextView evolutionSepcificHeaderTV = new TextView(context);
        evolutionSepcificHeaderTV.setText(getCellType());
        evolutionSepcificHeaderTV.setTextSize(23);
        evolutionSepcificHeaderTV.setPadding(10, 10, 10, 10);
        evolutionSepcificHeader.addView(evolutionSepcificHeaderTV);
        evolutionSepcificHeader.addView(evolutionSepcific);

        ll.addView(evolutionSepcificHeader);
        return ll;
    }


}
