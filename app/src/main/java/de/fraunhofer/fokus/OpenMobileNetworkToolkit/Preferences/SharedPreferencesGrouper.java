package de.fraunhofer.fokus.OpenMobileNetworkToolkit.Preferences;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
    private ConcurrentHashMap <SPType, Set<SharedPreferences.OnSharedPreferenceChangeListener>> spMap = new ConcurrentHashMap<>();
    public String getSharedPreferenceIdentifier(SPType key)  {
        return this.ct.getPackageName()+"_"+key.toString();
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
            instance = new SharedPreferencesGrouper(ct.getApplicationContext());
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

    /**
     * Removes a particular {@code listener} for a particular {@code key}
     * @param key the key holding the listener
     * @param listener the listener to remove
     */
    public synchronized void removeListener(@NonNull SPType key, @NonNull SharedPreferences.OnSharedPreferenceChangeListener listener) {
        SharedPreferences sp = this.getSharedPreference(key);
        if(sp == null) {
            Log.e(TAG, "SharedPreferences not found for "+key);
            return;
        }
        Set<SharedPreferences.OnSharedPreferenceChangeListener> listenerList = this.spMap.get(key);
        if(listenerList == null || !listenerList.contains(listener)) {
            Log.e(TAG, "Listener not found for "+key);
            return;
        }
        sp.unregisterOnSharedPreferenceChangeListener(listener);
        listenerList.remove(listener);
        // If listener list isn't empty, write in the new reduced list, otherwise just delete the whole key
        if (!listenerList.isEmpty()) {
            this.spMap.put(key, listenerList);
        } else {
            this.spMap.remove(key);
        }
        Log.i(TAG, "Listener removed for "+key);
    }

    /**
     * Removes all listeners for a particular {@link SPType} {@code key}
     * @param key the key for which to remove all listeners
     */
    public synchronized void removeAllListeners(SPType key){
        SharedPreferences sp = this.getSharedPreference(key);
        if(sp == null) {
            Log.e(TAG, "SharedPreferences not found for "+key);
            return;
        }
        Set<SharedPreferences.OnSharedPreferenceChangeListener> listenerList = this.spMap.get(key);
        if(listenerList == null) {
            Log.e(TAG, "Listener(s) not found for "+key);
            return;
        }
        // Unregister all listeners
        for (SharedPreferences.OnSharedPreferenceChangeListener listener : listenerList) {
            sp.unregisterOnSharedPreferenceChangeListener(listener);
        }
        // Delete them all from the map
        this.spMap.remove(key);
        Log.i(TAG, "Listener(s) removed for "+key);
    }

    /**
     * Searches for the passed-in {@code listener} and removes it if found. The listener
     * <b>must have been</b> already added via {@link #setListener(SharedPreferences.OnSharedPreferenceChangeListener, SPType)}
     * @param listener the listener to add
     */
    public synchronized void removeListener(@NonNull SharedPreferences.OnSharedPreferenceChangeListener listener){
        for (SPType potentialMatch : spMap.keySet()) {
            SharedPreferences sp = this.getSharedPreference(potentialMatch);
            if (sp == null) {
                Log.e(TAG, "SharedPreferences not found for " + potentialMatch);
                continue;
            }
            Set<SharedPreferences.OnSharedPreferenceChangeListener> listenerList = this.spMap.get(potentialMatch);
            if (listener == null || !listenerList.contains(listener)) {
                Log.e(TAG, "Listener not found for " + potentialMatch);
                continue;
            }
            // Now the easy part, when we know which SharedPref owns the listener and it's in the
            // registered listenerList, just remove it from the list and cleanup the key if the list
            // remains empty
            listenerList.remove(listener);
            sp.unregisterOnSharedPreferenceChangeListener(listener);
            // If the listenerList is empty, remove the whole key from the map, otherwise update value
            if (listenerList.isEmpty()) {
                this.spMap.remove(potentialMatch);
            } else {
                this.spMap.put(potentialMatch, listenerList);
            }
            Log.i(TAG, "Listener removed for " + potentialMatch);
        }
    }
    public HashMap<SPType,SharedPreferences> getAllSharedPreferences() {

        HashMap<SPType,SharedPreferences> spList = new HashMap<>();
        for(SPType key: SPType.values()) {
            spList.put(key, this.getSharedPreference(key));
        }
        return spList;
    }
    public synchronized void setListener(@NonNull SharedPreferences.OnSharedPreferenceChangeListener listener, @NonNull SPType key) {
        SharedPreferences sp = this.getSharedPreference(key);
        if(sp == null) {
            Log.e(TAG, "SharedPreferences not found for "+key);
            return;
        }
        Set<SharedPreferences.OnSharedPreferenceChangeListener> listenerList = this.spMap.get(key);
        if (listenerList == null) {
            listenerList = new HashSet<SharedPreferences.OnSharedPreferenceChangeListener>();
        }

        sp.registerOnSharedPreferenceChangeListener(listener);
        listenerList.add(listener);
        this.spMap.put(key, listenerList);
        Log.i(TAG, "Listener registered for "+key);
    }

}
