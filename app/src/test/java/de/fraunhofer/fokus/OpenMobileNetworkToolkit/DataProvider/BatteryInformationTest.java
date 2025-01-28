/*
 * SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 * SPDX-License-Identifier: apache2
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class BatteryInformationTest {

    private BatteryInformation batteryInformation;

    @Before
    public void setUp() throws Exception {
        batteryInformation = new BatteryInformation();

    }
    @Test
    public void getScale() {
        assertEquals( "Should return null", 0, batteryInformation.getScale());
        batteryInformation.setScale(10);
        assertEquals( "Should return 10", 10, batteryInformation.getScale());
    }

    @Test
    public void setScale() {
        batteryInformation.setScale(10);
        assertEquals( "Should return 10", 10, batteryInformation.getScale());
    }

    @Test
    public void getLevel() {
        assertEquals("Should return 0", 0, batteryInformation.getLevel());
        batteryInformation.setLevel(10);
        assertEquals("Should return 10", 10, batteryInformation.getLevel());
    }

    @Test
    public void setLevel() {
        batteryInformation.setLevel(10);
        assertEquals("Should return 10", 10, batteryInformation.getLevel());
    }

    @Test
    public void getCharge_type() {
        assertEquals("Should return 0", 0, batteryInformation.getCharge_type());
        batteryInformation.setCharge_type(10);
        assertEquals("Should return 10", 10, batteryInformation.getCharge_type());
    }

    @Test
    public void setCharge_type() {
        batteryInformation.setCharge_type(10);
        assertEquals("Should return 10", 10, batteryInformation.getCharge_type());
    }

    @Test
    public void getPercent() {
        assertEquals("Should return NaN", Double.NaN, batteryInformation.getPercent(),0.0);
        batteryInformation.setScale(10);
        batteryInformation.setLevel(10);
        assertEquals("Should return 100.0", 100.0, batteryInformation.getPercent(),0.0);
    }

}