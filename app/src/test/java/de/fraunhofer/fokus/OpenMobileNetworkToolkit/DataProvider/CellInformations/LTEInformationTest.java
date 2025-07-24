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

public class LTEInformationTest {

    @Test
    public void getDbm() {
        LTEInformation lteInfo = new LTEInformation();
        lteInfo.setDbm(-85);
        assertEquals(-85, lteInfo.getDbm());
        lteInfo.setDbm(-90);
        assertEquals(-90, lteInfo.getDbm());
    }

    @Test
    public void setDbm() {
        LTEInformation lteInfo = new LTEInformation();
        lteInfo.setDbm(-85);
        assertEquals(-85, lteInfo.getDbm());
        lteInfo.setDbm(-90);
        assertEquals(-90, lteInfo.getDbm());
    }

    @Test
    public void getMcc() {
        LTEInformation lteInfo = new LTEInformation();
        lteInfo.setMcc("262");
        assertEquals("262", lteInfo.getMcc());
        lteInfo.setMcc("310");
        assertEquals("310", lteInfo.getMcc());
    }

    @Test
    public void setMcc() {
        LTEInformation lteInfo = new LTEInformation();
        lteInfo.setMcc("262");
        assertEquals("262", lteInfo.getMcc());
        lteInfo.setMcc("310");
        assertEquals("310", lteInfo.getMcc());
    }

    @Test
    public void getBandwidth() {
        LTEInformation lteInfo = new LTEInformation();
        lteInfo.setBandwidth(20);
        assertEquals(20, lteInfo.getBandwidth());
        lteInfo.setBandwidth(10);
        assertEquals(10, lteInfo.getBandwidth());
    }

    @Test
    public void setBandwidth() {
        LTEInformation lteInfo = new LTEInformation();
        lteInfo.setBandwidth(20);
        assertEquals(20, lteInfo.getBandwidth());
        lteInfo.setBandwidth(10);
        assertEquals(10, lteInfo.getBandwidth());
    }

    @Test
    public void getRsrp() {
        LTEInformation lteInfo = new LTEInformation();
        lteInfo.setRsrp(-95);
        assertEquals(-95, lteInfo.getRsrp());
        lteInfo.setRsrp(-100);
        assertEquals(-100, lteInfo.getRsrp());
    }

    @Test
    public void setRsrp() {
        LTEInformation lteInfo = new LTEInformation();
        lteInfo.setRsrp(-95);
        assertEquals(-95, lteInfo.getRsrp());
        lteInfo.setRsrp(-100);
        assertEquals(-100, lteInfo.getRsrp());
    }

    @Test
    public void getRsrq() {
        LTEInformation lteInfo = new LTEInformation();
        lteInfo.setRsrq(-12);
        assertEquals(-12, lteInfo.getRsrq());
        lteInfo.setRsrq(-15);
        assertEquals(-15, lteInfo.getRsrq());
    }

    @Test
    public void setRsrq() {
        LTEInformation lteInfo = new LTEInformation();
        lteInfo.setRsrq(-12);
        assertEquals(-12, lteInfo.getRsrq());
        lteInfo.setRsrq(-15);
        assertEquals(-15, lteInfo.getRsrq());
    }

    @Test
    public void getRssi() {
        LTEInformation lteInfo = new LTEInformation();
        lteInfo.setRssi(-80);
        assertEquals(-80, lteInfo.getRssi());
        lteInfo.setRssi(-85);
        assertEquals(-85, lteInfo.getRssi());
    }

    @Test
    public void setRssi() {
        LTEInformation lteInfo = new LTEInformation();
        lteInfo.setRssi(-80);
        assertEquals(-80, lteInfo.getRssi());
        lteInfo.setRssi(-85);
        assertEquals(-85, lteInfo.getRssi());
    }

    @Test
    public void getRssnr() {
        LTEInformation lteInfo = new LTEInformation();
        lteInfo.setRssnr(-10);
        assertEquals(-10, lteInfo.getRssnr());
        lteInfo.setRssnr(-15);
        assertEquals(-15, lteInfo.getRssnr());
    }

    @Test
    public void setRssnr() {
        LTEInformation lteInfo = new LTEInformation();
        lteInfo.setRssnr(-10);
        assertEquals(-10, lteInfo.getRssnr());
        lteInfo.setRssnr(-15);
        assertEquals(-15, lteInfo.getRssnr());
    }

    @Test
    public void getTimingAdvance() {
        LTEInformation lteInfo = new LTEInformation();
        lteInfo.setTimingAdvance(5);
        assertEquals(5, lteInfo.getTimingAdvance());
        lteInfo.setTimingAdvance(10);
        assertEquals(10, lteInfo.getTimingAdvance());
    }

    @Test
    public void setTimingAdvance() {
        LTEInformation lteInfo = new LTEInformation();
        lteInfo.setTimingAdvance(5);
        assertEquals(5, lteInfo.getTimingAdvance());
        lteInfo.setTimingAdvance(10);
        assertEquals(10, lteInfo.getTimingAdvance());
    }

    @Test
    public void getCqi() {
        LTEInformation lteInfo = new LTEInformation();
        lteInfo.setCqi(15);
        assertEquals(15, lteInfo.getCqi());
        lteInfo.setCqi(20);
        assertEquals(20, lteInfo.getCqi());
    }

    @Test
    public void setCqi() {
        LTEInformation lteInfo = new LTEInformation();
        lteInfo.setCqi(15);
        assertEquals(15, lteInfo.getCqi());
        lteInfo.setCqi(20);
        assertEquals(20, lteInfo.getCqi());
    }

    @Test
    public void getEarfcn() {
        LTEInformation lteInfo = new LTEInformation();
        lteInfo.setEarfcn(100);
        assertEquals(100, lteInfo.getEarfcn());
        lteInfo.setEarfcn(200);
        assertEquals(200, lteInfo.getEarfcn());
    }

    @Test
    public void setEarfcn() {
        LTEInformation lteInfo = new LTEInformation();
        lteInfo.setEarfcn(100);
        assertEquals(100, lteInfo.getEarfcn());
        lteInfo.setEarfcn(200);
        assertEquals(200, lteInfo.getEarfcn());
    }

    @Test
    public void getEarfcnString() {
        LTEInformation lteInfo = new LTEInformation();
        lteInfo.setEarfcn(100);
        assertEquals("100", lteInfo.getEarfcnString());
        lteInfo.setEarfcn(200);
        assertEquals("200", lteInfo.getEarfcnString());
    }

    @Test
    public void getBandwidthString() {
        LTEInformation lteInfo = new LTEInformation();
        lteInfo.setBandwidth(20);
        assertEquals("20 MHz", lteInfo.getBandwidthString());
        lteInfo.setBandwidth(10);
        assertEquals("10 MHz", lteInfo.getBandwidthString());
    }

    @Test
    public void getCqiString() {
        LTEInformation lteInfo = new LTEInformation();
        lteInfo.setCqi(15);
        assertEquals("15", lteInfo.getCqiString());
        lteInfo.setCqi(20);
        assertEquals("20", lteInfo.getCqiString());
    }

    @Test
    public void getRsrpString() {
        LTEInformation lteInfo = new LTEInformation();
        lteInfo.setRsrp(-95);
        assertEquals("-95", lteInfo.getRsrpString());
        lteInfo.setRsrp(-100);
        assertEquals("-100", lteInfo.getRsrpString());
    }

    @Test
    public void getRsrqString() {
        LTEInformation lteInfo = new LTEInformation();
        lteInfo.setRsrq(-12);
        assertEquals("-12", lteInfo.getRsrqString());
        lteInfo.setRsrq(-15);
        assertEquals("-15", lteInfo.getRsrqString());
    }

    @Test
    public void getRssiString() {
        LTEInformation lteInfo = new LTEInformation();
        lteInfo.setRssi(-80);
        assertEquals("-80", lteInfo.getRssiString());
        lteInfo.setRssi(-85);
        assertEquals("-85", lteInfo.getRssiString());
    }

    @Test
    public void getRssnrString() {
        LTEInformation lteInfo = new LTEInformation();
        lteInfo.setRssnr(-10);
        assertEquals("-10", lteInfo.getRssnrString());
        lteInfo.setRssnr(-15);
        assertEquals("-15", lteInfo.getRssnrString());
    }

    @Test
    public void getTimingAdvanceString() {
        LTEInformation lteInfo = new LTEInformation();
        lteInfo.setTimingAdvance(5);
        assertEquals("5", lteInfo.getTimingAdvanceString());
        lteInfo.setTimingAdvance(10);
        assertEquals("10", lteInfo.getTimingAdvanceString());
    }

    @Test
    public void getDbmString() {
        LTEInformation lteInfo = new LTEInformation();
        lteInfo.setDbm(-85);
        assertEquals("-85", lteInfo.getDbmString());
        lteInfo.setDbm(-90);
        assertEquals("-90", lteInfo.getDbmString());
    }

    @Test
    public void getPoint() {
    }

    @Test
    public void getStringBuilder() {
    }
}