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

import java.util.List;

public class NRInformationTest {

    @Test
    public void getNrarfcn() {
        NRInformation nrInfo = new NRInformation();
        nrInfo.setNrarfcn(123456);
        assertEquals(123456, nrInfo.getNrarfcn());
        nrInfo.setNrarfcn(654321);
        assertEquals(654321, nrInfo.getNrarfcn());
    }

    @Test
    public void setNrarfcn() {
        NRInformation nrInfo = new NRInformation();
        nrInfo.setNrarfcn(123456);
        assertEquals(123456, nrInfo.getNrarfcn());
        nrInfo.setNrarfcn(654321);
        assertEquals(654321, nrInfo.getNrarfcn());
    }

    @Test
    public void getMcc() {
        NRInformation nrInfo = new NRInformation();
        nrInfo.setMcc("262");
        assertEquals("262", nrInfo.getMcc());
        nrInfo.setMcc("310");
        assertEquals("310", nrInfo.getMcc());
    }

    @Test
    public void setMcc() {
        NRInformation nrInfo = new NRInformation();
        nrInfo.setMcc("262");
        assertEquals("262", nrInfo.getMcc());
        nrInfo.setMcc("310");
        assertEquals("310", nrInfo.getMcc());
    }

    @Test
    public void getAsuLevel() {
        NRInformation nrInfo = new NRInformation();
        nrInfo.setAsuLevel(10);
        assertEquals(10, nrInfo.getAsuLevel());
        nrInfo.setAsuLevel(20);
        assertEquals(20, nrInfo.getAsuLevel());
    }

    @Test
    public void setAsuLevel() {
        NRInformation nrInfo = new NRInformation();
        nrInfo.setAsuLevel(10);
        assertEquals(10, nrInfo.getAsuLevel());
        nrInfo.setAsuLevel(20);
        assertEquals(20, nrInfo.getAsuLevel());
    }

    @Test
    public void getDbm() {
        NRInformation nrInfo = new NRInformation();
        nrInfo.setDbm(-85);
        assertEquals(-85, nrInfo.getDbm());
        nrInfo.setDbm(-90);
        assertEquals(-90, nrInfo.getDbm());
    }

    @Test
    public void getCqis() {
        NRInformation nrInfo = new NRInformation();
        nrInfo.setCqis(List.of(10, 20, 30));
        assertEquals(List.of(10, 20, 30), nrInfo.getCqis());
        nrInfo.setCqis(List.of(40, 50));
        assertEquals(List.of(40, 50), nrInfo.getCqis());
    }

    @Test
    public void getFirstCqi() {
        NRInformation nrInfo = new NRInformation();
        nrInfo.setCqis(List.of(10, 20, 30));
        assertEquals(10, nrInfo.getFirstCqi());
        nrInfo.setCqis(List.of(40, 50));
        assertEquals(40, nrInfo.getFirstCqi());
    }

    @Test
    public void getFirstCqiString() {
        NRInformation nrInfo = new NRInformation();
        nrInfo.setCqis(List.of(10, 20, 30));
        assertEquals("10", nrInfo.getFirstCqiString());
        nrInfo.setCqis(List.of(40, 50));
        assertEquals("40", nrInfo.getFirstCqiString());
    }

    @Test
    public void setCqis() {
        NRInformation nrInfo = new NRInformation();
        nrInfo.setCqis(List.of(10, 20, 30));
        assertEquals(List.of(10, 20, 30), nrInfo.getCqis());
        nrInfo.setCqis(List.of(40, 50));
        assertEquals(List.of(40, 50), nrInfo.getCqis());
    }

    @Test
    public void setDbm() {
        NRInformation nrInfo = new NRInformation();
        nrInfo.setDbm(-85);
        assertEquals(-85, nrInfo.getDbm());
        nrInfo.setDbm(-90);
        assertEquals(-90, nrInfo.getDbm());
    }

    @Test
    public void getCsirsrp() {
        NRInformation nrInfo = new NRInformation();
        nrInfo.setCsirsrp(-95);
        assertEquals(-95, nrInfo.getCsirsrp());
        nrInfo.setCsirsrp(-100);
        assertEquals(-100, nrInfo.getCsirsrp());
    }

    @Test
    public void setCsirsrp() {
        NRInformation nrInfo = new NRInformation();
        nrInfo.setCsirsrp(-95);
        assertEquals(-95, nrInfo.getCsirsrp());
        nrInfo.setCsirsrp(-100);
        assertEquals(-100, nrInfo.getCsirsrp());
    }

    @Test
    public void getCsirsrq() {
        NRInformation nrInfo = new NRInformation();
        nrInfo.setCsirsrq(-105);
        assertEquals(-105, nrInfo.getCsirsrq());
        nrInfo.setCsirsrq(-110);
        assertEquals(-110, nrInfo.getCsirsrq());
    }

    @Test
    public void setCsirsrq() {
        NRInformation nrInfo = new NRInformation();
        nrInfo.setCsirsrq(-105);
        assertEquals(-105, nrInfo.getCsirsrq());
        nrInfo.setCsirsrq(-110);
        assertEquals(-110, nrInfo.getCsirsrq());
    }

    @Test
    public void getCsisinr() {
        NRInformation nrInfo = new NRInformation();
        nrInfo.setCsisinr(15);
        assertEquals(15, nrInfo.getCsisinr());
        nrInfo.setCsisinr(20);
        assertEquals(20, nrInfo.getCsisinr());
    }

    @Test
    public void setCsisinr() {
        NRInformation nrInfo = new NRInformation();
        nrInfo.setCsisinr(15);
        assertEquals(15, nrInfo.getCsisinr());
        nrInfo.setCsisinr(20);
        assertEquals(20, nrInfo.getCsisinr());
    }

    @Test
    public void getSsrsrp() {
        NRInformation nrInfo = new NRInformation();
        nrInfo.setSsrsrp(-90);
        assertEquals(-90, nrInfo.getSsrsrp());
        nrInfo.setSsrsrp(-95);
        assertEquals(-95, nrInfo.getSsrsrp());
    }

    @Test
    public void setSsrsrp() {
        NRInformation nrInfo = new NRInformation();
        nrInfo.setSsrsrp(-90);
        assertEquals(-90, nrInfo.getSsrsrp());
        nrInfo.setSsrsrp(-95);
        assertEquals(-95, nrInfo.getSsrsrp());
    }

    @Test
    public void getSsrsrq() {
        NRInformation nrInfo = new NRInformation();
        nrInfo.setSsrsrq(-100);
        assertEquals(-100, nrInfo.getSsrsrq());
        nrInfo.setSsrsrq(-105);
        assertEquals(-105, nrInfo.getSsrsrq());
    }

    @Test
    public void setSsrsrq() {
        NRInformation nrInfo = new NRInformation();
        nrInfo.setSsrsrq(-100);
        assertEquals(-100, nrInfo.getSsrsrq());
        nrInfo.setSsrsrq(-105);
        assertEquals(-105, nrInfo.getSsrsrq());
    }

    @Test
    public void getSssinr() {
        NRInformation nrInfo = new NRInformation();
        nrInfo.setSssinr(18);
        assertEquals(18, nrInfo.getSssinr());
        nrInfo.setSssinr(22);
        assertEquals(22, nrInfo.getSssinr());
    }

    @Test
    public void setSssinr() {
        NRInformation nrInfo = new NRInformation();
        nrInfo.setSssinr(18);
        assertEquals(18, nrInfo.getSssinr());
        nrInfo.setSssinr(22);
        assertEquals(22, nrInfo.getSssinr());
    }

    @Test
    public void setTac() {
        NRInformation nrInfo = new NRInformation();
        nrInfo.setTac(123);
        assertEquals(123, nrInfo.getTac());
        nrInfo.setTac(456);
        assertEquals(456, nrInfo.getTac());
    }

    @Test
    public void getTac() {
        NRInformation nrInfo = new NRInformation();
        nrInfo.setTac(123);
        assertEquals(123, nrInfo.getTac());
        nrInfo.setTac(456);
        assertEquals(456, nrInfo.getTac());
    }

    @Test
    public void setTimingAdvance() {
        NRInformation nrInfo = new NRInformation();
        nrInfo.setTimingAdvance(5);
        assertEquals(5, nrInfo.getTimingAdvance());
        nrInfo.setTimingAdvance(10);
        assertEquals(10, nrInfo.getTimingAdvance());
    }

    @Test
    public void getTimingAdvance() {
        NRInformation nrInfo = new NRInformation();
        nrInfo.setTimingAdvance(5);
        assertEquals(5, nrInfo.getTimingAdvance());
        nrInfo.setTimingAdvance(10);
        assertEquals(10, nrInfo.getTimingAdvance());
    }

    @Test
    public void getTimingAdvanceString() {
        NRInformation nrInfo = new NRInformation();
        nrInfo.setTimingAdvance(5);
        assertEquals("5", nrInfo.getTimingAdvanceString());
        nrInfo.setTimingAdvance(10);
        assertEquals("10", nrInfo.getTimingAdvanceString());
    }

    @Test
    public void getDbmString() {
        NRInformation nrInfo = new NRInformation();
        nrInfo.setDbm(-85);
        assertEquals("-85", nrInfo.getDbmString());
        nrInfo.setDbm(-90);
        assertEquals("-90", nrInfo.getDbmString());
    }

    @Test
    public void getAsuLevelString() {
        NRInformation nrInfo = new NRInformation();
        nrInfo.setAsuLevel(10);
        assertEquals("10", nrInfo.getAsuLevelString());
        nrInfo.setAsuLevel(20);
        assertEquals("20", nrInfo.getAsuLevelString());
    }

    @Test
    public void getCsirsrpString() {
        NRInformation nrInfo = new NRInformation();
        nrInfo.setCsirsrp(-95);
        assertEquals("-95", nrInfo.getCsirsrpString());
        nrInfo.setCsirsrp(-100);
        assertEquals("-100", nrInfo.getCsirsrpString());
    }

    @Test
    public void getCsirsrqString() {
        NRInformation nrInfo = new NRInformation();
        nrInfo.setCsirsrq(-105);
        assertEquals("-105", nrInfo.getCsirsrqString());
        nrInfo.setCsirsrq(-110);
        assertEquals("-110", nrInfo.getCsirsrqString());
    }

    @Test
    public void getCsisinrString() {
        NRInformation nrInfo = new NRInformation();
        nrInfo.setCsisinr(15);
        assertEquals("15", nrInfo.getCsisinrString());
        nrInfo.setCsisinr(20);
        assertEquals("20", nrInfo.getCsisinrString());
    }

    @Test
    public void getSsrsrpString() {
        NRInformation nrInfo = new NRInformation();
        nrInfo.setSsrsrp(-90);
        assertEquals("-90", nrInfo.getSsrsrpString());
        nrInfo.setSsrsrp(-95);
        assertEquals("-95", nrInfo.getSsrsrpString());
    }

    @Test
    public void getSsrsrqString() {
        NRInformation nrInfo = new NRInformation();
        nrInfo.setSsrsrq(-100);
        assertEquals("-100", nrInfo.getSsrsrqString());
        nrInfo.setSsrsrq(-105);
        assertEquals("-105", nrInfo.getSsrsrqString());
    }

    @Test
    public void getSssinrString() {
        NRInformation nrInfo = new NRInformation();
        nrInfo.setSssinr(18);
        assertEquals("18", nrInfo.getSssinrString());
        nrInfo.setSssinr(22);
        assertEquals("22", nrInfo.getSssinrString());
    }

    @Test
    public void getNrarfcnString() {
        NRInformation nrInfo = new NRInformation();
        nrInfo.setNrarfcn(123456);
        assertEquals("123456", nrInfo.getNrarfcnString());
        nrInfo.setNrarfcn(654321);
        assertEquals("654321", nrInfo.getNrarfcnString());
    }

    @Test
    public void getTacString() {
        NRInformation nrInfo = new NRInformation();
        nrInfo.setTac(123);
        assertEquals("123", nrInfo.getTacString());
        nrInfo.setTac(456);
        assertEquals("456", nrInfo.getTacString());
    }

    @Test
    public void getPlmn() {
        NRInformation nrInfo = new NRInformation();
        nrInfo.setMcc("262");
        nrInfo.setMnc("01");
        assertEquals("26201", nrInfo.getPlmn());
    }

    @Test
    public void getMccString() {
        NRInformation nrInfo = new NRInformation();
        nrInfo.setMcc("262");
        assertEquals("262", nrInfo.getMccString());
        nrInfo.setMcc("310");
        assertEquals("310", nrInfo.getMccString());
    }

    @Test
    public void getPoint() {
    }

    @Test
    public void getStringBuilder() {
    }
}