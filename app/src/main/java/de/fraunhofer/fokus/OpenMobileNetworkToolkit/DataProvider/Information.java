package de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider;

import android.content.Context;
import android.view.Gravity;
import android.widget.LinearLayout;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;
import de.fraunhofer.fokus.OpenMobileNetworkToolkit.XMLtoUI;

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
}
