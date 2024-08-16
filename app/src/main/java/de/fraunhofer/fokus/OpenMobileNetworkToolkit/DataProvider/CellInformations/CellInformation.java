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
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.telephony.CellInfo;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.influxdb.client.write.Point;

import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.Objects;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.Information;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.JSONtoUI;

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
    private boolean isRegistered;
    private int cellConnectionStatus;
    private int asuLevel;


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

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public Point getPoint(Point point){
        if(point == null) {
            Log.e(TAG, "getPoint: given point == null!");
            return null;
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
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && this.getAlphaLong() != null){
            stringBuilder.append(this.getAlphaLong());
        }
        stringBuilder.append(" Type: " + this.getCellType());
        if(this.getPci() != -1) stringBuilder.append(" PCI: ").append(this.getPci());
        if(!this.getAlphaLong().equals("N/A")) stringBuilder.append(" Alpha Long: ").append(this.getAlphaLong());


        return stringBuilder;
    }

    private TableRow rowBuilder(String column1, String column2, Context context) {
        if (Objects.equals(column2, String.valueOf(CellInfo.UNAVAILABLE))) {
            column2 = "N/A";
        }
        TableRow tr = new TableRow(context);
        tr.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams
                .MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        tr.setPadding(2, 2, 2, 2);
        TextView tv1 = new TextView(context);
        tv1.setPadding(20, 0, 20, 0);
        TextView tv2 = new TextView(context);
        tv2.setPadding(0, 0, 0, 0);
        tv2.setTextIsSelectable(true);
        tv1.append(column1);
        tv2.append(Objects.requireNonNullElse(column2, "N/A"));
        tv2.setTextIsSelectable(true);
        tr.addView(tv1);
        tr.addView(tv2);
        return tr;
    }

    public TableLayout getTable(TableLayout tl, Context context){
        for (Field field : this.getClass().getDeclaredFields()) {
            String name = field.getName();
            String value = null;
            try {
                value = field.get(this).toString();
            } catch (Exception e){}
            if (value == null) continue;
            tl.addView(rowBuilder(name, value, context));
        }
        return tl;
    }

    public LinearLayout createQuickView(Context context) {
        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setGravity(Gravity.CENTER);
        ll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        JSONtoUI JsonToUI = new JSONtoUI();

        JSONObject generalJson = JsonToUI.loadJsonFromAsset(context, "cell_information_general.json");
        if(generalJson == null) return ll;

        JSONObject evolution = JsonToUI.loadJsonFromAsset(context, getPath());
        if(evolution == null) return ll;


        ll.addView(JsonToUI.createUIFromJSON(context, generalJson, this));
        ll.addView(JsonToUI.createUIFromJSON(context, evolution, this));
        return ll;
    }

}
