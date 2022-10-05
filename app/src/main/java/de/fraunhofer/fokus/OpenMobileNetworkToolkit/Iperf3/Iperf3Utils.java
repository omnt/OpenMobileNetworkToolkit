package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.InetAddresses;
import android.net.Network;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.content.res.ResourcesCompat;

import java.net.InetAddress;
import java.net.NetPermission;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class Iperf3Utils {

    private static final String TAG = "Iperf3Utils";

    public static Drawable getDrawable(Context context, int iperf3RunResult){
        Drawable drawable;
        if (iperf3RunResult == -100) {
            drawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_directions_run, null);
        } else if(iperf3RunResult != 0) {
            drawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_error_outline, null);
        } else {
            drawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_done_all, null);
        }
        return drawable;
    }

}
