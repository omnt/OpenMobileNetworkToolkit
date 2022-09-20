/*
 * SPDX-FileCopyrightText: 2021 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2021 Fraunhofer FOKUS
 *
 * SPDX-License-Identifier: apache2
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.Model.CellInformation;

@Dao
public interface CellInfoDao {
    @Insert
    void insertAll(CellInformation... cellInfo);

    @Delete
    void delete(CellInformation cellInfo);

    @Query("SELECT * FROM cell_info")
    List<CellInformation> getAll();
}
