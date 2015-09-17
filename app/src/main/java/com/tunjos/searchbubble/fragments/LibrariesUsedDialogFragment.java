package com.tunjos.searchbubble.fragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.Html;

import com.tunjos.searchbubble.R;

/**
 * Created by tunjos on 28/06/2015.
 */
public class LibrariesUsedDialogFragment extends DialogFragment {

    public LibrariesUsedDialogFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder b =  new  AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                .setTitle(R.string.tx_cool_c)
                .setMessage(Html.fromHtml(getString(R.string.tx_libraries_used_desc, getString(R.string.tx_libraries_used_urls))))
                .setPositiveButton(R.string.tx_ok_c,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        }
                );
        return b.create();
    }
}