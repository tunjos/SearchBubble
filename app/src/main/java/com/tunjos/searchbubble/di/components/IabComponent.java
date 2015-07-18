package com.tunjos.searchbubble.di.components;

import com.tunjos.searchbubble.activities.MainActivity;
import com.tunjos.searchbubble.di.modules.IabModule;
import com.tunjos.searchbubble.di.scopes.PerActivity;
import com.tunjos.searchbubble.fragments.DonateDialogFragment;

import dagger.Component;

/**
 * Created by tunjos on 26/06/2015.
 */
@PerActivity
@Component(
        modules = {
                IabModule.class
        },
        dependencies = {
            PersistenceComponent.class
        }
)
public interface IabComponent {
    void inject(MainActivity activity);
    void inject(DonateDialogFragment fragment);
}