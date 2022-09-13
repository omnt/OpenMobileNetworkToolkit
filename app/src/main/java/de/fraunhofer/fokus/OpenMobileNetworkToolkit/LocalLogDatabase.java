/*
 * SPDX-FileCopyrightText: 2021 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2021 Fraunhofer FOKUS
 *
 * SPDX-License-Identifier: apache2
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Model.CellInformation;


@Database(entities = {CellInformation.class}, version = 1)
abstract class LocalLogDatabase extends RoomDatabase {
    //public abstract PointDao PointDao();
    public abstract CellInfoDao CellInfoDao();
}