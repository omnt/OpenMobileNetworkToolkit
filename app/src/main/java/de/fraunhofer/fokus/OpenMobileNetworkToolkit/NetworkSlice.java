package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import static android.telephony.data.NetworkSliceInfo.SLICE_SERVICE_TYPE_URLLC;

import android.os.Build;
import android.os.PersistableBundle;
import android.service.carrier.CarrierIdentifier;
import android.telephony.TelephonyManager;
import android.telephony.data.NetworkSliceInfo;
import android.telephony.data.NetworkSlicingConfig;
import android.telephony.data.RouteSelectionDescriptor;
import android.telephony.data.TrafficDescriptor;
import android.telephony.data.UrspRule;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.List;
import java.util.Objects;

@RequiresApi(api = Build.VERSION_CODES.S)
public class NetworkSlice extends TelephonyManager {
    private static final String TAG = "NetworkSlicing";


    public NetworkSlice(){
        super();
        Log.d(TAG, "Network Slicing");
    }

    @Override
    public PersistableBundle onSliceConfig(CarrierIdentifier id){
        int sdk_version = Build.VERSION.SDK_INT;
        Log.i(TAG, "CarrierIdentifier id " + id.toString());
        PersistableBundle configForSlicing = new PersistableBundle();

        NetworkSliceInfo sliceInfo = new NetworkSliceInfo.Builder()
                .setSliceServiceType(SLICE_SERVICE_TYPE_URLLC)
                .setStatus(NetworkSliceInfo.SLICE_STATUS_DEFAULT_CONFIGURED)
                .build();

        NetworkSlicingConfig slicingConfig = new NetworkSlicingConfig();
        slicingConfig.getSliceInfo();
        slicingConfig.getUrspRules();
        slicingConfig.describeContents();

        RouteSelectionDescriptor routeSelectionDescriptor = new RouteSelectionDescriptor();
        routeSelectionDescriptor.getSliceInfo();
        routeSelectionDescriptor.getDataNetworkName();
        routeSelectionDescriptor.getSessionType();
        routeSelectionDescriptor.getPrecedence();
        routeSelectionDescriptor.getSscMode();



        TrafficDescriptor trafficDescriptor = new TrafficDescriptor.Builder()
                .setDataNetworkName("")
                .build();

        UrspRule urspRule = new UrspRule();
        urspRule.getRouteSelectionDescriptor();
        urspRule.getTrafficDescriptors();


        return null; //replace with network slice info
    }

    
}
