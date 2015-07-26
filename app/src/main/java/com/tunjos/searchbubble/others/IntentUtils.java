package com.tunjos.searchbubble.others;

import android.content.Context;
import android.content.Intent;

import com.tunjos.searchbubble.services.FloatingBubbleService;

/**
 * Created by tunjos on 25/07/2015.
 */
public class IntentUtils {

    private IntentUtils() {
        // No instances
    }

    public static void startFloatingBubbleService(Context context) {
        Intent floatingBubbleServiceIntent = new Intent(context.getApplicationContext(), FloatingBubbleService.class);
        context.startService(floatingBubbleServiceIntent);
    }

    public static void stopFloatingBubbleService(Context context) {
        Intent floatingBubbleServiceIntent = new Intent(context.getApplicationContext(), FloatingBubbleService.class);
        context.stopService(floatingBubbleServiceIntent);
    }
}
