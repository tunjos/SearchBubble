package com.tunjos.searchbubble.fragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tunjos.searchbubble.R;
import com.tunjos.searchbubble.activities.MainActivity;
import com.tunjos.searchbubble.models.DonateItem;
import com.tunjos.searchbubble.models.MyConstants;
import com.tunjos.searchbubble.models.MyPreferenceManager;
import com.tunjos.searchbubble.others.IabUtil;
import com.tunjos.searchbubble.others.iabutils.IabHelper;
import com.tunjos.searchbubble.others.iabutils.IabResult;
import com.tunjos.searchbubble.others.iabutils.Purchase;

import java.util.HashMap;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by tunjos on 28/06/2015.
 */
public class DonateDialogFragment extends DialogFragment {
    @Inject MyPreferenceManager myPreferenceManager;
    @Inject IabHelper mHelper;
    @Inject IabUtil iabUtil;

    @InjectView(R.id.rlMilk) RelativeLayout rlMilk;
    @InjectView(R.id.tvMilk) TextView tvMilk;
    @InjectView(R.id.tvMilkPrice) TextView tvMilkPrice;

    @InjectView(R.id.rlWaterMelon) RelativeLayout rlWaterMelon;
    @InjectView(R.id.tvWaterMelon) TextView tvWaterMelon;
    @InjectView(R.id.tvWaterMelonPrice) TextView tvWaterMelonPrice;

    @InjectView(R.id.rlPizza) RelativeLayout rlPizza;
    @InjectView(R.id.tvPizza) TextView tvPizza;
    @InjectView(R.id.tvPizzaPrice) TextView tvPizzaPrice;

    @InjectView(R.id.rlSushi) RelativeLayout rlSushi;
    @InjectView(R.id.tvSushi) TextView tvSushi;
    @InjectView(R.id.tvSushiPrice) TextView tvSushiPrice;

    @InjectView(R.id.rlFryingPan) RelativeLayout rlFryingPan;
    @InjectView(R.id.tvFryingPan) TextView tvFryingPan;
    @InjectView(R.id.tvFryingPanPrice) TextView tvFryingPanPrice;


    public DonateDialogFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() != null) {
            ((MainActivity) getActivity()).getIabComponent().inject(this);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_donate, null);
        ButterKnife.inject(this, view);

        HashMap<String, DonateItem> items = iabUtil.getItems();

        Drawable doneDrawable = DrawableCompat.wrap(getActivity().getResources().getDrawable(R.drawable.ic_done));
        DrawableCompat.setTint(doneDrawable, getResources().getColor(R.color.sb_red));

        tvMilk.setText(items.get(MyConstants.SKU_MILK).itemName);
        tvMilkPrice.setText(items.get(MyConstants.SKU_MILK).price);
        if (items.get(MyConstants.SKU_MILK).purchased) {
            rlMilk.setClickable(false);
            tvMilkPrice.setCompoundDrawablesWithIntrinsicBounds(doneDrawable, null, null, null);
        }

        tvWaterMelon.setText(items.get(MyConstants.SKU_WATERMELON).itemName);
        tvWaterMelonPrice.setText(items.get(MyConstants.SKU_WATERMELON).price);
        if (items.get(MyConstants.SKU_WATERMELON).purchased) {
            rlWaterMelon.setClickable(false);
            tvWaterMelonPrice.setCompoundDrawablesWithIntrinsicBounds(doneDrawable, null, null, null);
        }

        tvPizza.setText(items.get(MyConstants.SKU_PIZZA).itemName);
        tvPizzaPrice.setText(items.get(MyConstants.SKU_PIZZA).price);
        if (items.get(MyConstants.SKU_PIZZA).purchased) {
            rlPizza.setClickable(false);
            tvPizzaPrice.setCompoundDrawablesWithIntrinsicBounds(doneDrawable, null, null, null);
        }

        tvSushi.setText(items.get(MyConstants.SKU_SUSHI).itemName);
        tvSushiPrice.setText(items.get(MyConstants.SKU_SUSHI).price);
        if (items.get(MyConstants.SKU_SUSHI).purchased) {
            rlSushi.setClickable(false);
            tvSushiPrice.setCompoundDrawablesWithIntrinsicBounds(doneDrawable, null, null, null);
        }

        tvFryingPan.setText(items.get(MyConstants.SKU_FRYINGPAN).itemName);
        tvFryingPanPrice.setText(items.get(MyConstants.SKU_FRYINGPAN).price);
        if (items.get(MyConstants.SKU_FRYINGPAN).purchased) {
            rlFryingPan.setClickable(false);
            tvFryingPanPrice.setCompoundDrawablesWithIntrinsicBounds(doneDrawable, null, null, null);
        }

        if (items.get(MyConstants.SKU_MILK).purchased && items.get(MyConstants.SKU_WATERMELON).purchased &&
                items.get(MyConstants.SKU_PIZZA).purchased && items.get(MyConstants.SKU_SUSHI).purchased &&
                items.get(MyConstants.SKU_FRYINGPAN).purchased) {
            myPreferenceManager.setDonateCompletePref(true);
        }

        AlertDialog.Builder b = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                .setTitle(R.string.tx_kindness_brings_happiness)
                .setMessage(R.string.tx_donate_and_help)
                .setNegativeButton(R.string.tx_later_c,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        }
                );
        b.setView(view);
        return b.create();
    }

    @OnClick({R.id.rlMilk, R.id.rlWaterMelon, R.id.rlPizza, R.id.rlSushi, R.id.rlFryingPan})
    public void OnClickItem(View v) {
        final String sku;
        switch (v.getId()) {
            case R.id.rlMilk:
                sku = MyConstants.SKU_MILK;
                break;
            case R.id.rlWaterMelon:
                sku = MyConstants.SKU_WATERMELON;
                break;
            case R.id.rlPizza:
                sku = MyConstants.SKU_PIZZA;
                break;
            case R.id.rlSushi:
                sku = MyConstants.SKU_SUSHI;
                break;
            case R.id.rlFryingPan:
                sku = MyConstants.SKU_FRYINGPAN;
                break;
            default:
                return;
        }

/**
 * reserved product IDs for testing static In-app Billing responses:
 * "android.test.purchased"
 * "android.test.canceled"
 * "android.test.refunded"
 * "android.test.item_unavailable"
 */
        getDialog().dismiss();
        mHelper.launchPurchaseFlow(getActivity(), sku, 10001,
                new IabHelper.OnIabPurchaseFinishedListener() {
                    @Override
                    public void onIabPurchaseFinished(IabResult result, Purchase info) {
                        if (result.isFailure()) {
                            if (getActivity() != null) {
                                ((MainActivity) getActivity()).showErrorMsg();
                            }
                            return;
                        } else if (info.getSku().equals(sku)) {
                            iabUtil.retrieveData(mHelper);
                            if (getActivity() != null) {
                                ((MainActivity) getActivity()).showThankYouMsg();
                            }
                        }
                    }
                }, "bGoa+V7g/yqDXvKRqq+JTFn4uQZbPiQJo4pf9RzJ");
    }
}