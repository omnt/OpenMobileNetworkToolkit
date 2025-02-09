package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Parameter;

import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;

public abstract class Parameter implements Parcelable {
    public static final String rootPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath();
    protected Parameter(Parcel in) {
    }


    public int describeContents() {
        return 0;
    }

    public void writeToParcel(android.os.Parcel dest, int flags) {

    }

}
