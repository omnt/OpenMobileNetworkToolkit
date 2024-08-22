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

import androidx.cardview.widget.CardView;

import com.influxdb.client.write.Point;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.Information;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.PrettyPrintMap;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.PrettyPrintValue;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.GlobalVars;
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

    @Override
    public LinearLayout createQuickView(Context context) {
        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setGravity(Gravity.CENTER);
        ll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        JSONtoUI JsonToUI = new JSONtoUI();

        JSONObject generalJson = JsonToUI.loadJsonFromAsset(context, "cell_information_general.json");
        if (generalJson == null) return ll;

        JSONObject evolution = JsonToUI.loadJsonFromAsset(context, getPath());
        if (evolution == null) return ll;

        mergeJsonObjects(generalJson, evolution);

        CardView card = new CardView(context);
        card.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        card.setRadius(10);
        card.setCardElevation(10);
        card.setMaxCardElevation(10);
        card.setUseCompatPadding(true);
        card.setPreventCornerOverlap(true);

        LinearLayout cardContent = new LinearLayout(context);
        cardContent.setOrientation(LinearLayout.VERTICAL);

        cardContent.addView(JsonToUI.createUIFromJSON(context, generalJson, this));
        cardContent.addView(JsonToUI.createUIFromJSON(context, evolution, this));
        card.addView(cardContent);
        ll.addView(card);
        return ll;
    }

    private void mergeJsonObjects(JSONObject generalJson, JSONObject evolution) {
        JSONObject table = evolution.optJSONObject("table");
        JSONObject generalTable = generalJson.optJSONObject("table");
        if (table == null) return;
        JSONArray rows = table.optJSONArray("row");
        JSONArray generalRows = generalTable.optJSONArray("row");
        if (rows == null) return;
        if (generalRows == null) return;

        ArrayList <Integer> rowsToRemove = new ArrayList<>();

        for (int i = 0; i < rows.length(); i++) {
            JSONArray row = rows.optJSONArray(i);
            JSONArray generalRow = generalRows.optJSONArray(i);
            if (row == null) continue;
            if(generalRow == null) break;
            for(int j = 0; j < row.length(); j++){
                JSONObject column = row.optJSONObject(j);
                if(column == null) continue;
                String override = column.optString("override");
                if(override == null || override.isEmpty()) continue;
                for(int k = 0; k < generalRow.length(); k++){
                    JSONObject generalColumn = generalRow.optJSONObject(k);
                    if(generalColumn == null) continue;
                    String key = generalColumn.optString("parameter");
                    if(key == null || key.isEmpty()) continue;
                    if(key.equals(override)){
                        try {
                            generalRow.put(k, column);
                            rowsToRemove.add(j);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
        }
        for(int i = 0; i < rowsToRemove.size(); i++){
            rows.remove(rowsToRemove.get(i));
        }

    }

}
