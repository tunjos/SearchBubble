package com.tunjos.searchbubble.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.tunjos.searchbubble.MyApplication;
import com.tunjos.searchbubble.models.MyPreferenceManager;
import com.tunjos.searchbubble.services.ClipboardService;

import javax.inject.Inject;

/**
 * Created by tunjos on 30/06/2015.
 */
public class BootCompletedBroadcastReceiver extends BroadcastReceiver {
    @Inject MyPreferenceManager myPreferenceManager;


    @Override
    public void onReceive(Context context, Intent intent) {
        ((MyApplication)context.getApplicationContext()).getPersistenceComponent().inject(this);

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
            if (myPreferenceManager.getBubbleActivePref() && myPreferenceManager.getAutoLaunchBubblePref()) {
                startClipService(context);
            }
        }
    }

    private void startClipService(Context context) {
        Intent clipServiceIntent = new Intent(context, ClipboardService.class);
        context.startService(clipServiceIntent);
    }
}