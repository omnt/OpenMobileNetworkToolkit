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

public class GSMInformationTest {

    @Test
    public void getBsic() {
        GSMInformation gsmInfo = new GSMInformation();
        gsmInfo.setBsic(5);
        assertEquals(5, gsmInfo.getBsic());
        gsmInfo.setBsic(10);
        assertEquals(10, gsmInfo.getBsic());
    }

    @Test
    public void setBsic() {
        GSMInformation gsmInfo = new GSMInformation();
        gsmInfo.setBsic(5);
        assertEquals(5, gsmInfo.getBsic());
        gsmInfo.setBsic(10);
        assertEquals(10, gsmInfo.getBsic());
    }

    @Test
    public void getMcc() {
        GSMInformation gsmInfo = new GSMInformation();
        gsmInfo.setMcc("262");
        assertEquals("262", gsmInfo.getMcc());
        gsmInfo.setMcc("310");
        assertEquals("310", gsmInfo.getMcc());
    }

    @Test
    public void setMcc() {
        GSMInformation gsmInfo = new GSMInformation();
        gsmInfo.setMcc("262");
        assertEquals("262", gsmInfo.getMcc());
        gsmInfo.setMcc("310");
        assertEquals("310", gsmInfo.getMcc());
    }

    @Test
    public void getLac() {
        GSMInformation gsmInfo = new GSMInformation();
        gsmInfo.setLac(12345);
        assertEquals(12345, gsmInfo.getLac());
        gsmInfo.setLac(67890);
        assertEquals(67890, gsmInfo.getLac());
    }

    @Test
    public void setLac() {
        GSMInformation gsmInfo = new GSMInformation();
        gsmInfo.setLac(12345);
        assertEquals(12345, gsmInfo.getLac());
        gsmInfo.setLac(67890);
        assertEquals(67890, gsmInfo.getLac());
    }

    @Test
    public void getTimingAdvance() {
        GSMInformation gsmInfo = new GSMInformation();
        gsmInfo.setTimingAdvance(5);
        assertEquals(5, gsmInfo.getTimingAdvance());
        gsmInfo.setTimingAdvance(10);
        assertEquals(10, gsmInfo.getTimingAdvance());
    }

    @Test
    public void setTimingAdvance() {
        GSMInformation gsmInfo = new GSMInformation();
        gsmInfo.setTimingAdvance(5);
        assertEquals(5, gsmInfo.getTimingAdvance());
        gsmInfo.setTimingAdvance(10);
        assertEquals(10, gsmInfo.getTimingAdvance());
    }

    @Test
    public void getBitErrorRate() {
        GSMInformation gsmInfo = new GSMInformation();
        gsmInfo.setBitErrorRate(2);
        assertEquals(2, gsmInfo.getBitErrorRate());
        gsmInfo.setBitErrorRate(5);
        assertEquals(5, gsmInfo.getBitErrorRate());
    }

    @Test
    public void setBitErrorRate() {
        GSMInformation gsmInfo = new GSMInformation();
        gsmInfo.setBitErrorRate(2);
        assertEquals(2, gsmInfo.getBitErrorRate());
        gsmInfo.setBitErrorRate(5);
        assertEquals(5, gsmInfo.getBitErrorRate());
    }

    @Test
    public void getDbm() {
        GSMInformation gsmInfo = new GSMInformation();
        gsmInfo.setDbm(-85);
        assertEquals(-85, gsmInfo.getDbm());
        gsmInfo.setDbm(-70);
        assertEquals(-70, gsmInfo.getDbm());
    }

    @Test
    public void setDbm() {
        GSMInformation gsmInfo = new GSMInformation();
        gsmInfo.setDbm(-85);
        assertEquals(-85, gsmInfo.getDbm());
        gsmInfo.setDbm(-70);
        assertEquals(-70, gsmInfo.getDbm());
    }

    @Test
    public void getRssi() {
        GSMInformation gsmInfo = new GSMInformation();
        gsmInfo.setRssi(-90);
        assertEquals(-90, gsmInfo.getRssi());
        gsmInfo.setRssi(-75);
        assertEquals(-75, gsmInfo.getRssi());
    }

    @Test
    public void getRssiString() {
        GSMInformation gsmInfo = new GSMInformation();
        gsmInfo.setRssi(-90);
        assertEquals("-90", gsmInfo.getRssiString());
        gsmInfo.setRssi(-75);
        assertEquals("-75", gsmInfo.getRssiString());
    }

    @Test
    public void getBitErrorRateString() {
        GSMInformation gsmInfo = new GSMInformation();
        gsmInfo.setBitErrorRate(3);
        assertEquals("3", gsmInfo.getBitErrorRateString());
        gsmInfo.setBitErrorRate(7);
        assertEquals("7", gsmInfo.getBitErrorRateString());
    }

    @Test
    public void getBsicString() {
        GSMInformation gsmInfo = new GSMInformation();
        gsmInfo.setBsic(5);
        assertEquals("5", gsmInfo.getBsicString());
        gsmInfo.setBsic(10);
        assertEquals("10", gsmInfo.getBsicString());
    }

    @Test
    public void getLacString() {
        GSMInformation gsmInfo = new GSMInformation();
        gsmInfo.setLac(12345);
        assertEquals("12345", gsmInfo.getLacString());
        gsmInfo.setLac(67890);
        assertEquals("67890", gsmInfo.getLacString());
    }

    @Test
    public void getTimingAdvanceString() {
        GSMInformation gsmInfo = new GSMInformation();
        gsmInfo.setTimingAdvance(5);
        assertEquals("5", gsmInfo.getTimingAdvanceString());
        gsmInfo.setTimingAdvance(10);
        assertEquals("10", gsmInfo.getTimingAdvanceString());
    }

    @Test
    public void setRssi() {
        GSMInformation gsmInfo = new GSMInformation();
        gsmInfo.setRssi(-90);
        assertEquals(-90, gsmInfo.getRssi());
        gsmInfo.setRssi(-75);
        assertEquals(-75, gsmInfo.getRssi());
    }

    @Test
    public void getPoint() {

    }

    @Test
    public void getStringBuilder() {

    }
}