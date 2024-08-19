package de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider;

import android.content.Context;
import android.telephony.CellInfo;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.material.divider.MaterialDivider;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class Information {
    private long timeStamp;
    public Information() {
    }

    /**
     * Get the current timestamp
     * @return last updated timestamp
     */
    public long getTimeStamp() {
        return timeStamp;
    }

    /**
     * Update the timestamp
     * @param timeStamp new timestamp
     */
    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public Information(long timeStamp) {
        this.timeStamp = timeStamp;
    }
    public LinearLayout createQuickView(Context context) {
        return null;
    }

    public TableRow rowBuilder(String column1, String column2, Context context) {
        if (Objects.equals(column2, String.valueOf(CellInfo.UNAVAILABLE))) {
            column2 = "N/A";
        }
        TableRow tr = new TableRow(context);
        tr.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams
                .MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        tr.setPadding(50, 2, 2, 2);
        TextView tv1 = new TextView(context);
        tv1.setPadding(20, 0, 20, 0);
        TextView tv2 = new TextView(context);
        tv2.setPadding(0, 0, 0, 0);
        tv2.setTextIsSelectable(true);
        PrettyPrintValue prettyPrint = PrettyPrintMap.getPrettyPrint(column1);
        if(prettyPrint != null){
            if(!prettyPrint.getToShow()) return tr;
            tv1.setText(prettyPrint.getName());
        } else {
            tv1.setText(column1);
        }
        tv2.append(Objects.requireNonNullElse(column2, "N/A"));
        tv2.setTextIsSelectable(true);
        tr.addView(tv1);
        tr.addView(tv2);
        return tr;
    }

    public void addRows(TableLayout tl, Context context, String[][] rows) {
        for (String[] row : rows) {
            tl.addView(rowBuilder(row[0], row[1], context));
        }
    }

    public void addDivider(TableLayout tl, Context context) {
        MaterialDivider divider = new MaterialDivider(context);
        tl.addView(divider);
    }


    public ArrayList<TableRow> getTableRows(Context context){
        ArrayList<TableRow> tableRows = new ArrayList<>();
        HashMap <String, String> cellInformation = this.getInformation();
        for (String key : cellInformation.keySet()) {
            tableRows.add(rowBuilder(key, cellInformation.get(key), context));
        }
        return tableRows;
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

    public HashMap<String, String> getInformation() {
        HashMap<String, String> hashMapInformation = new HashMap<>();
        for (Field field : this.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(this);
                if(value == null) value = "N/A";

                hashMapInformation.put(field.getName(), value.toString());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        for (Field field : Objects.requireNonNull(this.getClass().getSuperclass()).getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(this);
                if(value == null) value = "N/A";

                hashMapInformation.put(field.getName(), value.toString());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }



        return hashMapInformation;
    }
}
