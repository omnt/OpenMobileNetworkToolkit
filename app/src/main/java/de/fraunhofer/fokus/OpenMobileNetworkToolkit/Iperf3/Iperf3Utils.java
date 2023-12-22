/*
 *  SPDX-FileCopyrightText: 2023 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 *  SPDX-FileCopyrightText: 2023 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Iperf3;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.core.content.res.ResourcesCompat;

import de.fraunhofer.fokus.OpenMobileNetworkToolkit.R;

public class Iperf3Utils {

    private static final String TAG = "Iperf3Utils";

    public static Drawable getDrawable(Context context, int iperf3RunResult) {
        Drawable drawable;
        if (iperf3RunResult == -100) {
            drawable =
                ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_directions_run,
                    null);
        } else if (iperf3RunResult != 0) {
            drawable =
                ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_error_outline,
                    null);
        } else {
            drawable =
                ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_done_all, null);
        }
        return drawable;
    }

}
