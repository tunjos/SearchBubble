package com.tunjos.searchbubble.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.tunjos.searchbubble.R;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by tunjos on 26/06/2015.
 */
public class MyPreferenceManager {
    private final SharedPreferences preferences;
    private Set<String> popupSearchSet;
    public MyPreferenceManager(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        popupSearchSet = new HashSet<String>(Arrays.asList(context.getResources().getStringArray(R.array.pref_popup_bubbles_values_array)));
    }

    public boolean getBubbleActivePref() {
        return preferences.getBoolean(MyConstants.PREF_BUBBLE_ACTIVE, true);
    }

    public void setBubbleActivePref(boolean active) {
        preferences.edit().putBoolean(MyConstants.PREF_BUBBLE_ACTIVE, active).apply();
    }

    public String getDefaultSearchPref() {
        return preferences.getString(MyConstants.PREF_DEFAULT_SEARCH, MyConstants.GOOGLE_PREF_VALUE);
    }

    /*public void setDefaultSearchPref(boolean active) {
        preferences.edit().putBoolean(MyConstants.PREF_DEFAULT_SEARCH, active).apply();
    }*/

    public boolean getAutoLaunchBubblePref() {
        return preferences.getBoolean(MyConstants.PREF_AUTO_LAUNCH_BUBBLE, false);
    }

    public void setAutoLaunchBubblePref(boolean autolaunch) {
        preferences.edit().putBoolean(MyConstants.PREF_AUTO_LAUNCH_BUBBLE, autolaunch).apply();
    }

    public boolean getAutoClosePref() {
        return preferences.getBoolean(MyConstants.PREF_AUTO_CLOSE, true);
    }

    public void setAutoClosePref(boolean autoclose) {
        preferences.edit().putBoolean(MyConstants.PREF_AUTO_CLOSE, autoclose).apply();
    }

    public boolean getPinToNotificationPref() {
        return preferences.getBoolean(MyConstants.PREF_PIN_TO_NOTIFICATION, true);
    }

    public void setPinToNotificationPref(boolean pin) {
        preferences.edit().putBoolean(MyConstants.PREF_PIN_TO_NOTIFICATION, pin).apply();
    }

    public boolean getStoreSearchesPref() {
        return preferences.getBoolean(MyConstants.PREF_STORE_SEARCHES, true);
    }

    public void setStoreSearchesPref(boolean storesearches) {
        preferences.edit().putBoolean(MyConstants.PREF_STORE_SEARCHES, storesearches).apply();
    }

    public int getBubbleViewPref() {
        return preferences.getInt(MyConstants.PREF_BUBBLE_VIEW, 0);
    }

    public void setBubbleViewPref(int view) {
        preferences.edit().putInt(MyConstants.PREF_BUBBLE_VIEW, view).apply();
    }

    public boolean getAppRatedPref() {
        return preferences.getBoolean(MyConstants.PREF_APP_RATED, false);
    }

    public void setAppRatedPref(boolean rated) {
        preferences.edit().putBoolean(MyConstants.PREF_APP_RATED, rated).apply();
    }

    public boolean getFirstRunPref() {
        return preferences.getBoolean(MyConstants.PREF_FIRST_RUN, true);
    }

    public void setFirstRunPref(boolean firstrun) {
        preferences.edit().putBoolean(MyConstants.PREF_FIRST_RUN, firstrun).apply();
    }

    public boolean getDonateCompletePref() {
        return preferences.getBoolean(MyConstants.PREF_DONATE_COMPLETE, false);
    }

    public void setDonateCompletePref(boolean completed) {
        preferences.edit().putBoolean(MyConstants.PREF_DONATE_COMPLETE, completed).apply();
    }

    public Set<String> getPopupSearchPref() {
        return preferences.getStringSet(MyConstants.PREF_POPUP_BUBBLES, popupSearchSet);
    }
}