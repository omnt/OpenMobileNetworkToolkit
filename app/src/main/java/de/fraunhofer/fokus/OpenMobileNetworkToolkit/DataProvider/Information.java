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
