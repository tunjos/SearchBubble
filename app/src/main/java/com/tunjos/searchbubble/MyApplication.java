package com.tunjos.searchbubble;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.squareup.leakcanary.LeakCanary;
import com.tunjos.searchbubble.di.components.DaggerPersistenceComponent;
import com.tunjos.searchbubble.di.components.PersistenceComponent;
import com.tunjos.searchbubble.di.modules.ContextModule;

import io.fabric.sdk.android.Fabric;

/**
 * Created by tunjos on 28/06/2015.
 */


public class MyApplication extends Application {

    private PersistenceComponent persistenceComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        LeakCanary.install(this);

//        Disable Debug Crash Logging
        CrashlyticsCore core = new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build();
        Fabric.with(this, new Crashlytics.Builder().core(core).build());
//        Fabric.with(this, new Crashlytics());

        persistenceComponent = DaggerPersistenceComponent.builder()
                .contextModule(new ContextModule(this))
                .build();
    }

    public PersistenceComponent getPersistenceComponent() {
        return persistenceComponent;
    }
}