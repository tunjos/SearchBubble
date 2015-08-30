package com.tunjos.searchbubble.fragments;

import android.support.v7.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.tunjos.searchbubble.MyApplication;
import com.tunjos.searchbubble.R;
import com.tunjos.searchbubble.models.MyConstants;
import com.tunjos.searchbubble.models.MyPreferenceManager;

import javax.inject.Inject;

/**
 * Created by tunjos on 28/06/2015.
 */
public class RateDialogFragment extends DialogFragment {
    @Inject MyPreferenceManager myPreferenceManager;

    public RateDialogFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MyApplication)getActivity().getApplication()).getPersistenceComponent().inject(this);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder b =  new  AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                .setTitle(R.string.tx_life_is_easy)
                .setMessage(R.string.tx_rate_5)
                .setPositiveButton(R.string.tx_i_love_it_c,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Intent i = new Intent(Intent.ACTION_VIEW);
                                i.setData(Uri.parse(MyConstants.PLAY_URL + getActivity().getPackageName()));
                                startActivity(i);

                                myPreferenceManager.setAppRatedPref(true);
                            }
                        }
                )
                .setNegativeButton(R.string.tx_later_c,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        }
                );
        return b.create();
    }
}
