package com.tunjos.searchbubble.di.modules;

import android.content.Context;

import com.tunjos.searchbubble.di.scopes.PerApp;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tunjos on 30/06/2015.
 */
@Module
public class ContextModule {
    private final Context applicationContext;

    public ContextModule(Context applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Provides @PerApp
    public Context provideApplicationContext() {
        return applicationContext;
    }
}
