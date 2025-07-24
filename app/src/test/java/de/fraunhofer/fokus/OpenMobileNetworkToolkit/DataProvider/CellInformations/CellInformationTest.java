/*
 * SPDX-FileCopyrightText:  2025 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.CellInformations;

import static org.junit.Assert.*;

import android.telephony.CellInfo;

import org.junit.Test;

public class CellInformationTest {

    @Test
    public void getCellConnectionStatus() {
        CellInformation cellInfo = new CellInformation();
        cellInfo.setCellConnectionStatus(CellInfo.CONNECTION_NONE);
        assertEquals(CellInfo.CONNECTION_NONE, cellInfo.getCellConnectionStatus());
        cellInfo.setCellConnectionStatus(CellInfo.CONNECTION_UNKNOWN);
        assertEquals(CellInfo.CONNECTION_UNKNOWN, cellInfo.getCellConnectionStatus());
    }

    @Test
    public void setCellConnectionStatus() {
        CellInformation cellInfo = new CellInformation();
        cellInfo.setCellConnectionStatus(CellInfo.CONNECTION_NONE);
        assertEquals(CellInfo.CONNECTION_NONE, cellInfo.getCellConnectionStatus());
        cellInfo.setCellConnectionStatus(CellInfo.CONNECTION_UNKNOWN);
        assertEquals(CellInfo.CONNECTION_UNKNOWN, cellInfo.getCellConnectionStatus());
    }

    @Test
    public void isRegistered() {
        CellInformation cellInfo = new CellInformation();
        cellInfo.setRegistered(true);
        assertTrue(cellInfo.isRegistered());
        cellInfo.setRegistered(false);
        assertFalse(cellInfo.isRegistered());
    }

    @Test
    public void setRegistered() {
        CellInformation cellInfo = new CellInformation();
        cellInfo.setRegistered(true);
        assertTrue(cellInfo.isRegistered());
        cellInfo.setRegistered(false);
        assertFalse(cellInfo.isRegistered());
    }

    @Test
    public void getAlphaLong() {
        CellInformation cellInfo = new CellInformation();
        cellInfo.setAlphaLong("Test Alpha");
        assertEquals("Test Alpha", cellInfo.getAlphaLong());
        cellInfo.setAlphaLong("New Alpha");
        assertEquals("New Alpha", cellInfo.getAlphaLong());
    }

    @Test
    public void setAlphaLong() {
        CellInformation cellInfo = new CellInformation();
        cellInfo.setAlphaLong("Test Alpha");
        assertEquals("Test Alpha", cellInfo.getAlphaLong());
        cellInfo.setAlphaLong("New Alpha");
        assertEquals("New Alpha", cellInfo.getAlphaLong());
    }

    @Test
    public void getAsuLevel() {
        CellInformation cellInfo = new CellInformation();
        cellInfo.setAsuLevel(10);
        assertEquals(10, cellInfo.getAsuLevel());
        cellInfo.setAsuLevel(20);
        assertEquals(20, cellInfo.getAsuLevel());
    }

    @Test
    public void setAsuLevel() {
        CellInformation cellInfo = new CellInformation();
        cellInfo.setAsuLevel(10);
        assertEquals(10, cellInfo.getAsuLevel());
        cellInfo.setAsuLevel(20);
        assertEquals(20, cellInfo.getAsuLevel());
    }

    @Test
    public void getCellType() {
        CellInformation cellInfo = new CellInformation();
        cellInfo.setCellType(CellType.NR);
        assertEquals(CellType.NR, cellInfo.getCellType());
        cellInfo.setCellType(CellType.GSM);
        assertEquals(CellType.GSM, cellInfo.getCellType());
    }

    @Test
    public void setCellType() {
        CellInformation cellInfo = new CellInformation();
        cellInfo.setCellType(CellType.NR);
        assertEquals(CellType.NR, cellInfo.getCellType());
        cellInfo.setCellType(CellType.GSM);
        assertEquals(CellType.GSM, cellInfo.getCellType());
    }

    @Test
    public void getBands() {
        CellInformation cellInfo = new CellInformation();
        cellInfo.setBands("Band 20");
        assertEquals("Band 20", cellInfo.getBands());
        cellInfo.setBands("Band 28");
        assertEquals("Band 28", cellInfo.getBands());
    }

    @Test
    public void setBands() {
        CellInformation cellInfo = new CellInformation();
        cellInfo.setBands("Band 20");
        assertEquals("Band 20", cellInfo.getBands());
        cellInfo.setBands("Band 28");
        assertEquals("Band 28", cellInfo.getBands());
    }

    @Test
    public void getCi() {
        CellInformation cellInfo = new CellInformation();
        cellInfo.setCi(123456);
        assertEquals(123456, cellInfo.getCi());
        cellInfo.setCi(654321);
        assertEquals(654321, cellInfo.getCi());
    }

    @Test
    public void getCiString() {
        CellInformation cellInfo = new CellInformation();
        cellInfo.setCi(123456);
        assertEquals("123456", cellInfo.getCiString());
    }

    @Test
    public void getPciString() {
        CellInformation cellInfo = new CellInformation();
        cellInfo.setPci(100);
        assertEquals("100", cellInfo.getPciString());
    }

    @Test
    public void setCi() {
        CellInformation cellInfo = new CellInformation();
        cellInfo.setCi(123456);
        assertEquals(123456, cellInfo.getCi());
        cellInfo.setCi(654321);
        assertEquals(654321, cellInfo.getCi());
    }

    @Test
    public void getMnc() {
        CellInformation cellInfo = new CellInformation();
        cellInfo.setMnc("01");
        assertEquals("01", cellInfo.getMnc());
        cellInfo.setMnc("02");
        assertEquals("02", cellInfo.getMnc());
    }

    @Test
    public void setMnc() {
        CellInformation cellInfo = new CellInformation();
        cellInfo.setMnc("01");
        assertEquals("01", cellInfo.getMnc());
        cellInfo.setMnc("02");
        assertEquals("02", cellInfo.getMnc());
    }

    @Test
    public void getPci() {
        CellInformation cellInfo = new CellInformation();
        cellInfo.setPci(100);
        assertEquals(100, cellInfo.getPci());
        cellInfo.setPci(200);
        assertEquals(200, cellInfo.getPci());
    }

    @Test
    public void setPci() {
        CellInformation cellInfo = new CellInformation();
        cellInfo.setPci(100);
        assertEquals(100, cellInfo.getPci());
        cellInfo.setPci(200);
        assertEquals(200, cellInfo.getPci());
    }

    @Test
    public void getTac() {
        CellInformation cellInfo = new CellInformation();
        cellInfo.setTac(123);
        assertEquals(123, cellInfo.getTac());
        cellInfo.setTac(456);
        assertEquals(456, cellInfo.getTac());
    }

    @Test
    public void getTacString() {
        CellInformation cellInfo = new CellInformation();
        cellInfo.setTac(123);
        assertEquals("123", cellInfo.getTacString());
    }

    @Test
    public void setTac() {
        CellInformation cellInfo = new CellInformation();
        cellInfo.setTac(123);
        assertEquals(123, cellInfo.getTac());
        cellInfo.setTac(456);
        assertEquals(456, cellInfo.getTac());
    }

    @Test
    public void getLevel() {
        CellInformation cellInfo = new CellInformation();
        cellInfo.setLevel(5);
        assertEquals(5, cellInfo.getLevel());
        cellInfo.setLevel(10);
        assertEquals(10, cellInfo.getLevel());
    }

    @Test
    public void setLevel() {
        CellInformation cellInfo = new CellInformation();
        cellInfo.setLevel(5);
        assertEquals(5, cellInfo.getLevel());
        cellInfo.setLevel(10);
        assertEquals(10, cellInfo.getLevel());
    }

    @Test
    public void getPoint() {
    }

    @Test
    public void getStringBuilder() {
    }
}