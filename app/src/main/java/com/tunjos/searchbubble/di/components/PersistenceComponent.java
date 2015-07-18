package com.tunjos.searchbubble.di.components;

import android.content.Context;

import com.tunjos.searchbubble.di.modules.ContextModule;
import com.tunjos.searchbubble.di.modules.PersistenceModule;
import com.tunjos.searchbubble.di.scopes.PerApp;
import com.tunjos.searchbubble.fragments.RateDialogFragment;
import com.tunjos.searchbubble.fragments.SettingsFragment;
import com.tunjos.searchbubble.models.MyPreferenceManager;
import com.tunjos.searchbubble.receivers.BootCompletedBroadcastReceiver;
import com.tunjos.searchbubble.services.ClipboardService;
import com.tunjos.searchbubble.services.FloatingBubbleService;

import dagger.Component;

/**
 * Created by tunjos on 26/06/2015.
 */
@PerApp
@Component(
        modules = {
                PersistenceModule.class,
                ContextModule.class
        }

)
public interface PersistenceComponent {
    void inject(SettingsFragment fragment);
    void inject(RateDialogFragment fragment);
    void inject(FloatingBubbleService service);
    void inject(ClipboardService service);
    void inject(BootCompletedBroadcastReceiver broadcastReceiver);
    MyPreferenceManager myPreferenceManager();
    Context applicationContext();
}