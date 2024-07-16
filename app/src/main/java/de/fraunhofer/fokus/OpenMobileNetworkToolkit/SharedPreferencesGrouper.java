package de.fraunhofer.fokus.OpenMobileNetworkToolkit;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.preference.PreferenceManager;

import java.util.HashMap;

public class SharedPreferencesGrouper {
    private static final String TAG = "SharedPreferencesGrouper";
    private static SharedPreferencesGrouper instance;

    private final SharedPreferences loggingSP;
    private final SharedPreferences carrierSP;
    private final SharedPreferences iperf3SP;
    private final SharedPreferences mobileNetworkSP;
    private final SharedPreferences defaultSP;
    private final SharedPreferences pingSP;
    private final Context ct;
    private HashMap <SPType, SharedPreferences.OnSharedPreferenceChangeListener> spMap = new HashMap<>();
    public String getSharedPreferenceIdentifier(SPType key)  {
        return this.ct.getPackageName()+"."+key.toString();
    }

    public void clearConfig(){
        for(SharedPreferences sp: this.getAllSharedPreferences().values()){
            sp.edit().clear().apply();
        }
    }


    private SharedPreferencesGrouper(Context ct) {
        this.ct = ct;
        loggingSP = ct.getSharedPreferences(getSharedPreferenceIdentifier(SPType.logging_sp), Context.MODE_PRIVATE);
        carrierSP = ct.getSharedPreferences(getSharedPreferenceIdentifier(SPType.carrier_sp), Context.MODE_PRIVATE);
        iperf3SP = ct.getSharedPreferences(getSharedPreferenceIdentifier(SPType.iperf3_sp), Context.MODE_PRIVATE);
        pingSP = ct.getSharedPreferences(getSharedPreferenceIdentifier(SPType.ping_sp), Context.MODE_PRIVATE);
        mobileNetworkSP = ct.getSharedPreferences(getSharedPreferenceIdentifier(SPType.mobile_network_sp), Context.MODE_PRIVATE);
        defaultSP = PreferenceManager.getDefaultSharedPreferences(ct);
    }

    public static SharedPreferencesGrouper getInstance(Context ct) {
        if(instance == null) {
            instance = new SharedPreferencesGrouper(ct);
        }
        return instance;
    }

    public SharedPreferences getSharedPreference(SPType key){
        SharedPreferences sp;
        switch (key) {
            case logging_sp:
                sp = loggingSP;
                break;
            case carrier_sp:
                sp = carrierSP;
                break;
            case iperf3_sp:
                sp = iperf3SP;
                break;
            case ping_sp:
                sp = pingSP;
                break;
            case mobile_network_sp:
                sp = mobileNetworkSP;
                break;
            default:
                sp = defaultSP;
                break;
        }
        return sp;
    }

    public void removeListener(SPType key){
        SharedPreferences sp = this.getSharedPreference(key);
        if(sp == null) {
            Log.e(TAG, "SharedPreferences not found for "+key);
            return;
        }
        SharedPreferences.OnSharedPreferenceChangeListener listener = this.spMap.get(key);
        if(listener == null) {
            Log.e(TAG, "Listener not found for "+key);
            return;
        }
        sp.unregisterOnSharedPreferenceChangeListener(listener);
        this.spMap.remove(key);
        Log.i(TAG, "Listener removed for "+key);
    }
    public HashMap<SPType,SharedPreferences> getAllSharedPreferences() {

        HashMap<SPType,SharedPreferences> spList = new HashMap<>();
        for(SPType key: SPType.values()) {
            spList.put(key, this.getSharedPreference(key));
        }
        return spList;
    }
    public void setListener(SharedPreferences.OnSharedPreferenceChangeListener listener, SPType key) {
        SharedPreferences sp = this.getSharedPreference(key);
        if(sp == null) {
            Log.e(TAG, "SharedPreferences not found for "+key);
            return;
        }
        sp.registerOnSharedPreferenceChangeListener(listener);
        this.spMap.put(key, listener);
        Log.i(TAG, "Listener registered for "+key);
    }

}
