/*
 * SPDX-FileCopyrightText:  2025 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.DataProvider.CellInformations;

import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthWcdma;

import com.influxdb.client.write.Point;

import java.util.Objects;

public class WCDMAInformation extends CellInformation {
    private int Dbm;
    private int EcNo;
    private int Level;
    private int AsuLevel;

    public WCDMAInformation() {
        super();
    }

    public WCDMAInformation(long timestamp, CellSignalStrengthWcdma cellSignalStrengthWcdma) {
        super(timestamp);
        Dbm = cellSignalStrengthWcdma.getDbm();
        EcNo = cellSignalStrengthWcdma.getEcNo();
        Level = cellSignalStrengthWcdma.getLevel();
        AsuLevel = cellSignalStrengthWcdma.getAsuLevel();
        this.setCellType(CellType.WCDMA);

    }

    private WCDMAInformation(CellInfoWcdma cellInfoWcdma, CellIdentityWcdma cellIdentityWcdma, CellSignalStrengthWcdma cellSignalStrengthWcdma, long timestamp) {
        super(timestamp, CellType.WCDMA, "N/A", -1, "N/A", -1, -1, cellSignalStrengthWcdma.getLevel(), Objects.requireNonNull(cellIdentityWcdma.getOperatorAlphaLong()).toString(), cellSignalStrengthWcdma.getAsuLevel(), cellInfoWcdma.isRegistered(), cellInfoWcdma.getCellConnectionStatus());
        Dbm = cellSignalStrengthWcdma.getDbm();
        EcNo = cellSignalStrengthWcdma.getEcNo();
        Level = cellSignalStrengthWcdma.getLevel();
        AsuLevel = cellSignalStrengthWcdma.getAsuLevel();
    }

    public WCDMAInformation(CellInfoWcdma cellInfoWcdma, long timestamp) {
        this(cellInfoWcdma, cellInfoWcdma.getCellIdentity(), cellInfoWcdma.getCellSignalStrength(), timestamp);
    }

    public int getDbm() {
        return Dbm;
    }

    public void setDbm(int dbm) {
        this.Dbm = dbm;
    }

    public int getEcNo() {
        return EcNo;
    }

    public void setEcNo(int ecNo) {
        this.EcNo = ecNo;
    }

    public int getLevel() {
        return Level;
    }

    public void setLevel(int level) {
        this.Level = level;
    }

    public int getAsuLevel() {
        return AsuLevel;
    }

    public void setAsuLevel(int asuLevel) {
        this.AsuLevel = asuLevel;
    }

    public String getCmdaDbmString() {
        return Integer.toString(Dbm);
    }

    public String getCmdaEcnoString() {
        return Integer.toString(EcNo);
    }

    public String getLevelString() {
        return Integer.toString(Level);
    }

    public String getAsuLevelString() {
        return Integer.toString(AsuLevel);
    }


    @Override
    public Point getPoint(Point point) {
        super.getPoint(point);
        point.addField("CMDA_DBM", Dbm);
        point.addField("CMDA_ECIO", EcNo);
        point.addField("EVDO_DBM", Level);
        point.addField("EVDO_ECIO", AsuLevel);
        return point;
    }


}
