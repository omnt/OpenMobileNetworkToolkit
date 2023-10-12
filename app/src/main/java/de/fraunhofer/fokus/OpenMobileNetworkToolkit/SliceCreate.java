/*
 * SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 * SPDX-License-Identifier: apache2
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import android.os.Build;
import android.telephony.data.NetworkSliceInfo;

public class SliceCreate {

    public NetworkSliceInfo sliceCreate(int serviceType, int sliceDiff) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            NetworkSliceInfo sliceInfo = new NetworkSliceInfo.Builder()
                .setSliceServiceType(serviceType)
                .setSliceDifferentiator(sliceDiff)
                .build();
            return sliceInfo;
        } else {
            return null;
        }
    }

    public NetworkSliceInfo sliceCreate(int serviceType, int sliceDiff, int status) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            NetworkSliceInfo sliceInfo = new NetworkSliceInfo.Builder()
                .setSliceServiceType(serviceType)
                .setSliceDifferentiator(sliceDiff)
                .setStatus(status)
                .build();
            return sliceInfo;
        } else {
            return null;
        }
    }

    public NetworkSliceInfo sliceCreate(int serviceType, int sliceDiff, int status,
                                        int mapped_hplmn_service_type, int mapped_hplmn_diff) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            NetworkSliceInfo sliceInfo = new NetworkSliceInfo.Builder()
                .setMappedHplmnSliceDifferentiator(mapped_hplmn_diff)
                .setMappedHplmnSliceServiceType(mapped_hplmn_service_type)
                .setSliceServiceType(serviceType)
                .setSliceDifferentiator(sliceDiff)
                .setStatus(status)
                .build();

            return sliceInfo;
        } else {
            return null;
        }
    }


    public int getMappedHplmnSliceServiceType() {
        return 0;
    }

    public int getMmappedHplmnSliceDifferentiator() {
        return 0;
    }

    public void sliceCreateMapped(int serviceType, int sliceDiff) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            NetworkSliceInfo sliceInfo = new NetworkSliceInfo.Builder()
                .setMappedHplmnSliceServiceType(serviceType)
                .setMappedHplmnSliceDifferentiator(sliceDiff)
                .build();
        }
    }
}