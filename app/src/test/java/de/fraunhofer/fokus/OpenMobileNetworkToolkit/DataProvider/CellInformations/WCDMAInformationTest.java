/*
 * SPDX-FileCopyrightText:  2025 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.CellInformations;

import static org.junit.Assert.*;

import org.junit.Test;

public class WCDMAInformationTest {

    @Test
    public void getDbm() {
        WCDMAInformation wcdmaInfo = new WCDMAInformation();
        wcdmaInfo.setDbm(-85);
        assertEquals(-85, wcdmaInfo.getDbm());
        wcdmaInfo.setDbm(-90);
        assertEquals(-90, wcdmaInfo.getDbm());
    }

    @Test
    public void setDbm() {
        WCDMAInformation wcdmaInfo = new WCDMAInformation();
        wcdmaInfo.setDbm(-85);
        assertEquals(-85, wcdmaInfo.getDbm());
        wcdmaInfo.setDbm(-90);
        assertEquals(-90, wcdmaInfo.getDbm());
    }

    @Test
    public void getEcNo() {
        WCDMAInformation wcdmaInfo = new WCDMAInformation();
        wcdmaInfo.setEcNo(-10);
        assertEquals(-10, wcdmaInfo.getEcNo());
        wcdmaInfo.setEcNo(-15);
        assertEquals(-15, wcdmaInfo.getEcNo());
    }

    @Test
    public void setEcNo() {
        WCDMAInformation wcdmaInfo = new WCDMAInformation();
        wcdmaInfo.setEcNo(-10);
        assertEquals(-10, wcdmaInfo.getEcNo());
        wcdmaInfo.setEcNo(-15);
        assertEquals(-15, wcdmaInfo.getEcNo());
    }

    @Test
    public void getLevel() {
        WCDMAInformation wcdmaInfo = new WCDMAInformation();
        wcdmaInfo.setLevel(5);
        assertEquals(5, wcdmaInfo.getLevel());
        wcdmaInfo.setLevel(10);
        assertEquals(10, wcdmaInfo.getLevel());
    }

    @Test
    public void setLevel() {
        WCDMAInformation wcdmaInfo = new WCDMAInformation();
        wcdmaInfo.setLevel(5);
        assertEquals(5, wcdmaInfo.getLevel());
        wcdmaInfo.setLevel(10);
        assertEquals(10, wcdmaInfo.getLevel());
    }

    @Test
    public void getAsuLevel() {
        WCDMAInformation wcdmaInfo = new WCDMAInformation();
        wcdmaInfo.setAsuLevel(15);
        assertEquals(15, wcdmaInfo.getAsuLevel());
        wcdmaInfo.setAsuLevel(20);
        assertEquals(20, wcdmaInfo.getAsuLevel());
    }

    @Test
    public void setAsuLevel() {
        WCDMAInformation wcdmaInfo = new WCDMAInformation();
        wcdmaInfo.setAsuLevel(15);
        assertEquals(15, wcdmaInfo.getAsuLevel());
        wcdmaInfo.setAsuLevel(20);
        assertEquals(20, wcdmaInfo.getAsuLevel());
    }

    @Test
    public void getCmdaDbmString() {
        WCDMAInformation wcdmaInfo = new WCDMAInformation();
        wcdmaInfo.setDbm(-85);
        assertEquals("-85", wcdmaInfo.getCmdaDbmString());
        wcdmaInfo.setDbm(-90);
        assertEquals("-90", wcdmaInfo.getCmdaDbmString());
    }

    @Test
    public void getCmdaEcnoString() {
        WCDMAInformation wcdmaInfo = new WCDMAInformation();
        wcdmaInfo.setEcNo(-10);
        assertEquals("-10", wcdmaInfo.getCmdaEcnoString());
        wcdmaInfo.setEcNo(-15);
        assertEquals("-15", wcdmaInfo.getCmdaEcnoString());
    }

    @Test
    public void getLevelString() {
        WCDMAInformation wcdmaInfo = new WCDMAInformation();
        wcdmaInfo.setLevel(5);
        assertEquals("5", wcdmaInfo.getLevelString());
        wcdmaInfo.setLevel(10);
        assertEquals("10", wcdmaInfo.getLevelString());
    }

    @Test
    public void getAsuLevelString() {
        WCDMAInformation wcdmaInfo = new WCDMAInformation();
        wcdmaInfo.setAsuLevel(15);
        assertEquals("15", wcdmaInfo.getAsuLevelString());
        wcdmaInfo.setAsuLevel(20);
        assertEquals("20", wcdmaInfo.getAsuLevelString());
    }

    @Test
    public void getPoint() {

    }
}