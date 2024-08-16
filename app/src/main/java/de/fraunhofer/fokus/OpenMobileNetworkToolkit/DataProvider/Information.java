package de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider;

import android.content.Context;
import android.widget.LinearLayout;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Objects;

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

    public HashMap<String, String> getInformation() {
        HashMap<String, String> hashMapInformation = new HashMap<>();
        for (Field field : this.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                hashMapInformation.put(field.getName(), field.get(this).toString());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        for (Field field : Objects.requireNonNull(this.getClass().getSuperclass()).getDeclaredFields()) {
            field.setAccessible(true);
            try {
                hashMapInformation.put(field.getName(), field.get(this).toString());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }



        return hashMapInformation;
    }
}
