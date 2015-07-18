package com.tunjos.searchbubble.fragments;

import android.support.v7.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.tunjos.searchbubble.R;
import com.tunjos.searchbubble.models.Clip;
import com.tunjos.searchbubble.models.MyConstants;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by tunjos on 28/06/2015.
 */
public class ClearClipHistoryDialogFragment extends DialogFragment {

    public ClearClipHistoryDialogFragment() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder b =  new  AlertDialog.Builder(getActivity())
                .setTitle(R.string.tx_clear_clip_history)
                .setMessage(R.string.tx_clear_clip_history_conf)
                .setPositiveButton(R.string.tx_yes_c,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Realm realm = Realm.getInstance(getActivity());
                                RealmResults<Clip> clips = realm.where(Clip.class).findAllSorted(MyConstants.FIELD_CREATION_DATE, RealmResults.SORT_ORDER_DESCENDING);

                                realm.beginTransaction();
                                clips.clear();
                                realm.commitTransaction();
                                realm.close();
                            }
                        }
                )
                .setNegativeButton(R.string.tx_cancel_c,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        }
                );
        return b.create();
    }
}
