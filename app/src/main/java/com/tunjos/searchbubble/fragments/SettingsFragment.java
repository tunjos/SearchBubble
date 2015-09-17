package com.tunjos.searchbubble.fragments;

import android.app.FragmentManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.support.v4.app.NotificationCompat;
import android.view.View;

import com.tunjos.searchbubble.BuildConfig;
import com.tunjos.searchbubble.MyApplication;
import com.tunjos.searchbubble.R;
import com.tunjos.searchbubble.models.MyConstants;
import com.tunjos.searchbubble.models.MyPreferenceManager;
import com.tunjos.searchbubble.services.ClipboardService;
import com.tunjos.searchbubble.services.FloatingBubbleService;

import javax.inject.Inject;


public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener{
    @Inject MyPreferenceManager myPreferenceManager;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MyApplication)getActivity().getApplication()).getPersistenceComponent().inject(this);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findPreference(MyConstants.PREF_CHANGE_LOG).setSummary("V " + BuildConfig.VERSION_NAME);

        if (!myPreferenceManager.getAppRatedPref()) {
            ((PreferenceCategory)findPreference(MyConstants.PREF_ABOUT)).removePreference(findPreference(MyConstants.PREF_RATE_SEARCH_BUBBLE));
        }

        setListeners();
    }

    private void setListeners() {
        findPreference(MyConstants.PREF_BUBBLE_ACTIVE).setOnPreferenceChangeListener(this);
        findPreference(MyConstants.PREF_PIN_TO_NOTIFICATION).setOnPreferenceChangeListener(this);

        findPreference(MyConstants.PREF_CLEAR_CLIPHISTORY).setOnPreferenceClickListener(this);
        findPreference(MyConstants.PREF_SEND_FEEDBACK).setOnPreferenceClickListener(this);
        findPreference(MyConstants.PREF_LIBRARIES_USED).setOnPreferenceClickListener(this);
        findPreference(MyConstants.PREF_CHANGE_LOG).setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()) {
            case MyConstants.PREF_CLEAR_CLIPHISTORY:
                FragmentManager fm = getFragmentManager();
                ClearClipHistoryDialogFragment clearClipHistoryDialogFragment = new ClearClipHistoryDialogFragment();
                clearClipHistoryDialogFragment.show(fm, "fragment_clearClipHistoryDialog");
                return true;
            case MyConstants.PREF_SEND_FEEDBACK:
                return true;
            case MyConstants.PREF_LIBRARIES_USED:
                FragmentManager fm1 = getFragmentManager();
                LibrariesUsedDialogFragment librariesUsedDialogFragment = new LibrariesUsedDialogFragment();
                librariesUsedDialogFragment.show(fm1, "fragment_librariesUsedDialog");
                return true;
            case MyConstants.PREF_CHANGE_LOG:
                FragmentManager fm2 = getFragmentManager();
                ChangeLogDialogFragment changeLogDialogFragment = new ChangeLogDialogFragment();
                changeLogDialogFragment.show(fm2, "fragment_changelogDialog");
                return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        switch (preference.getKey()) {
            case MyConstants.PREF_BUBBLE_ACTIVE:
                boolean active = (Boolean)newValue;
                if (!active) {
                    stopClipService();
                    stopFloatingBubbleService();
                    showPinnedNotification(false);
                } else {
                    startClipService();
                    showPinnedNotification(true);
                }
                return true;
            case MyConstants.PREF_PIN_TO_NOTIFICATION:
                boolean pin = (boolean) newValue;
                showPinnedNotification(pin);
                return true;
        }
        return false;
    }

    private void showPinnedNotification(boolean pin) {
        NotificationManager notificationManager = (NotificationManager)getActivity().getSystemService(Context.NOTIFICATION_SERVICE);

        if (pin) {
            Intent floatingBubbleIntent = new Intent(getActivity(), FloatingBubbleService.class);
            PendingIntent pendingFloatingBubbleIntent =
                    PendingIntent.getService(getActivity(), 100, floatingBubbleIntent, PendingIntent.FLAG_ONE_SHOT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity())
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(getString(R.string.tx_tap_to_show_bubble))
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                    .setColor(getResources().getColor(R.color.sb_red))
                    .setContentIntent(pendingFloatingBubbleIntent)
                    .setOngoing(true)
                    .setAutoCancel(false);
            Notification notification = builder.build();
            notificationManager.notify(1, notification);
        } else {
            notificationManager.cancelAll();
        }
    }

    private void stopClipService() {
        Intent clipServiceIntent = new Intent(getActivity().getApplicationContext(), ClipboardService.class);
        getActivity().stopService(clipServiceIntent);
    }

    private void startClipService() {
        Intent clipServiceIntent = new Intent(getActivity().getApplicationContext(), ClipboardService.class);
        getActivity().startService(clipServiceIntent);
    }

    //TODO check if getacnotnull[Use Context =]
    private void startFloatingBubbleService() {
        Intent floatingBubbleServiceIntent = new Intent(getActivity().getApplicationContext(), FloatingBubbleService.class);
        getActivity().startService(floatingBubbleServiceIntent);
    }

    private void stopFloatingBubbleService() {
        Intent floatingBubbleServiceIntent = new Intent(getActivity().getApplicationContext(), FloatingBubbleService.class);
        getActivity().stopService(floatingBubbleServiceIntent);
    }
}