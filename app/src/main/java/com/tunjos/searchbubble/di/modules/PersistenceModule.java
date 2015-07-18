package com.tunjos.searchbubble.di.modules;

import android.content.Context;

import com.tunjos.searchbubble.di.scopes.PerApp;
import com.tunjos.searchbubble.models.MyPreferenceManager;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tunjos on 26/06/2015.
 */

@Module
public class PersistenceModule {

    @Provides @PerApp
    public MyPreferenceManager provideMyPreferenceManager(Context context) {
        return new MyPreferenceManager(context);
    }
}