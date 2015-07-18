package com.tunjos.searchbubble.di.modules;

import android.content.Context;

import com.tunjos.searchbubble.di.scopes.PerActivity;
import com.tunjos.searchbubble.others.IabUtil;
import com.tunjos.searchbubble.others.iabutils.IabHelper;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tunjos on 30/06/2015.
 */
@Module
public class IabModule {
    private final String base64EncodedPublicKey;

    public IabModule(String base64EncodedPublicKey) {
        this.base64EncodedPublicKey = base64EncodedPublicKey;
    }

    @Provides @PerActivity
    public IabHelper provideIabHelper(Context applicationContext) {
        return new IabHelper(applicationContext, base64EncodedPublicKey);
    }

    @Provides @PerActivity
    public IabUtil provideIabUtil() {
        return new IabUtil();
    }
}
