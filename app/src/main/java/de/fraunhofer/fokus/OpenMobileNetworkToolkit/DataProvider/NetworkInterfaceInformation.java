/*
 *  SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider;

import android.content.Context;
import android.widget.TableLayout;

public class NetworkInterfaceInformation extends Information{
    private String interfaceName;
    private String address;


    public NetworkInterfaceInformation() {
    }

    public NetworkInterfaceInformation(String interfaceName, String address, long timestamp) {
        super(timestamp);
        this.interfaceName = interfaceName;
        this.address = address;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public TableLayout getTable(TableLayout tl, Context context, boolean displayNull) {
        return tl;
    }
}
