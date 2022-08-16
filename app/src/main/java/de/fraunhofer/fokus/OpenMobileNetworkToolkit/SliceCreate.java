package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import android.telephony.data.NetworkSliceInfo;

public class SliceCreate {

    public NetworkSliceInfo sliceCreate(int serviceType, int sliceDiff) {
        NetworkSliceInfo sliceInfo = new NetworkSliceInfo.Builder()
                .setSliceServiceType(serviceType)
                .setSliceDifferentiator(sliceDiff)
                .build();

        return sliceInfo;
    }

    public NetworkSliceInfo sliceCreate(int serviceType, int sliceDiff, int status) {
        NetworkSliceInfo sliceInfo = new NetworkSliceInfo.Builder()
                .setSliceServiceType(serviceType)
                .setSliceDifferentiator(sliceDiff)
                .setStatus(status)
                .build();

        return sliceInfo;
    }

    public NetworkSliceInfo sliceCreate(int serviceType, int sliceDiff, int status, int mapped_hplmn_service_type, int mapped_hplmn_diff){
        NetworkSliceInfo sliceInfo = new NetworkSliceInfo.Builder()
                .setMappedHplmnSliceDifferentiator(mapped_hplmn_diff)
                .setMappedHplmnSliceServiceType(mapped_hplmn_service_type)
                .setSliceServiceType(serviceType)
                .setSliceDifferentiator(sliceDiff)
                .setStatus(status)
                .build();

        return sliceInfo;
    }


    public int getMappedHplmnSliceServiceType() {
        return 0;
    }

    public int getMmappedHplmnSliceDifferentiator() {
        return 0;
    }

    public void sliceCreateMapped(int serviceType, int sliceDiff) {
        NetworkSliceInfo sliceInfo = new NetworkSliceInfo.Builder()
                .setMappedHplmnSliceServiceType(serviceType)
                .setMappedHplmnSliceDifferentiator(sliceDiff)
                .build();
    }
}