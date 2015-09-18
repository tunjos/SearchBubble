package com.tunjos.searchbubble.others;

import android.util.Log;

import com.tunjos.searchbubble.R;
import com.tunjos.searchbubble.models.DonateItem;
import com.tunjos.searchbubble.models.MyConstants;
import com.tunjos.searchbubble.others.iabutils.IabHelper;
import com.tunjos.searchbubble.others.iabutils.IabResult;
import com.tunjos.searchbubble.others.iabutils.Inventory;
import com.tunjos.searchbubble.others.iabutils.Purchase;
import com.tunjos.searchbubble.others.iabutils.SkuDetails;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by tunjos on 15/07/2015.
 */
public class IabUtil {

    private HashMap<String, DonateItem> items;

    String p1="1.00€";
    String p2="2.00€";
    String p3="5.00€";
    String p4="10.00€";
    String p5="20.00€";

    public IabUtil() {
        items = new HashMap<>();
        items.put(MyConstants.SKU_MILK, new DonateItem(MyConstants.SKU_MILK, R.string.tx_donate_milk, p1));
        items.put(MyConstants.SKU_WATERMELON, new DonateItem(MyConstants.SKU_WATERMELON, R.string.tx_donate_watermelon, p2));
        items.put(MyConstants.SKU_PIZZA, new DonateItem(MyConstants.SKU_PIZZA, R.string.tx_donate_pizza, p3));
        items.put(MyConstants.SKU_SUSHI, new DonateItem(MyConstants.SKU_SUSHI, R.string.tx_donate_sushi, p4));
        items.put(MyConstants.SKU_FRYINGPAN, new DonateItem(MyConstants.SKU_FRYINGPAN, R.string.tx_donate_fryingpan, p5));
    }


    /**
     * Retrieves data about in-app items
     *
     * @param mHelper
     */
    public void retrieveData(final IabHelper mHelper) {

        if (mHelper == null) return;

        List additionalSkuList = new ArrayList();
        additionalSkuList.add(MyConstants.SKU_MILK);
        additionalSkuList.add(MyConstants.SKU_WATERMELON);
        additionalSkuList.add(MyConstants.SKU_PIZZA);
        additionalSkuList.add(MyConstants.SKU_SUSHI);
        additionalSkuList.add(MyConstants.SKU_FRYINGPAN);

        IabHelper.QueryInventoryFinishedListener
                mQueryFinishedListener = new IabHelper.QueryInventoryFinishedListener() {
            public void onQueryInventoryFinished(IabResult result, Inventory inventory) {

                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null) return;

                if (result == null || result.isFailure()) {
                    //Log.e(TAG,"Error refreshing items " +result);
                    // handle error
                    return;
                }

                SkuDetails milkSkuDetails, waterMelonSkuDetails, pizzaSkuDetails, sushiSkuDetails, fryingpanSkuDetails;

                if (inventory!=null){
                    milkSkuDetails = inventory.getSkuDetails(MyConstants.SKU_MILK);
                    waterMelonSkuDetails = inventory.getSkuDetails(MyConstants.SKU_WATERMELON);
                    pizzaSkuDetails = inventory.getSkuDetails(MyConstants.SKU_PIZZA);
                    sushiSkuDetails = inventory.getSkuDetails(MyConstants.SKU_SUSHI);
                    fryingpanSkuDetails = inventory.getSkuDetails(MyConstants.SKU_FRYINGPAN);


                // update data

                    DonateItem itemMilk = items.get(MyConstants.SKU_MILK);
                    if (itemMilk != null && milkSkuDetails != null) {
                        itemMilk.purchased = inventory.hasPurchase(MyConstants.SKU_MILK);
                        itemMilk.price = milkSkuDetails.getPrice();
                        items.put(MyConstants.SKU_MILK, itemMilk);

                        /* if (mHelper!=null && inventory!=null && inventory.hasPurchase(MyConstants.SKU_MILK))
                                consumeItem(mHelper, MyConstants.SKU_MILK,inventory.getPurchase(MyConstants.SKU_MILK));
                        }*/
                    }

                    DonateItem itemWaterMelon = items.get(MyConstants.SKU_WATERMELON);
                    if (itemWaterMelon != null && waterMelonSkuDetails != null) {
                        itemWaterMelon.purchased = inventory.hasPurchase(MyConstants.SKU_WATERMELON);
                        itemWaterMelon.price = waterMelonSkuDetails.getPrice();
                        items.put(MyConstants.SKU_WATERMELON, itemWaterMelon);
                    }

                    DonateItem itemPizza = items.get(MyConstants.SKU_PIZZA);
                    if (itemPizza != null && pizzaSkuDetails != null) {
                        itemPizza.purchased =  inventory.hasPurchase(MyConstants.SKU_PIZZA);
                        itemPizza.price = pizzaSkuDetails.getPrice();
                        items.put(MyConstants.SKU_PIZZA, itemPizza);
                    }

                    DonateItem itemSushi = items.get(MyConstants.SKU_SUSHI);
                    if (itemSushi != null && sushiSkuDetails != null) {
                        itemSushi.purchased = inventory.hasPurchase(MyConstants.SKU_SUSHI);
                        itemSushi.price = sushiSkuDetails.getPrice();
                        items.put(MyConstants.SKU_SUSHI, itemSushi);
                    }

                    DonateItem itemFryingPan = items.get(MyConstants.SKU_FRYINGPAN);
                    if (itemFryingPan != null && fryingpanSkuDetails != null) {
                        itemFryingPan.purchased = inventory.hasPurchase(MyConstants.SKU_FRYINGPAN);
                        itemFryingPan.price = fryingpanSkuDetails.getPrice();
                        items.put(MyConstants.SKU_FRYINGPAN, itemFryingPan);
                    }

                }
                }
        };


        try{
            mHelper.queryInventoryAsync(true, additionalSkuList,
                    mQueryFinishedListener);
        } catch(IllegalStateException il){
            Log.e("Purchase", "Error ", il);
        } catch(NullPointerException ne){
            //it is bad, but it is due to a bug in Iab
            Log.e("Purchase","Error ",ne);
        }
    }

    private void setItemConsumed(String keyItem){
            DonateItem itemSmall = items.get(keyItem);
            if (itemSmall != null) {
                itemSmall.purchased = false;
                items.put(keyItem, itemSmall);
        }
    }

    private void consumeItem(IabHelper helper,final String keyItem,Purchase purchase){
        if (helper==null) return;

        helper.consumeAsync(purchase,
                new IabHelper.OnConsumeFinishedListener() {
                    @Override
                    public void onConsumeFinished(Purchase purchase, IabResult result) {
                        setItemConsumed(keyItem);
                    }
                });
    }

    public HashMap<String, DonateItem> getItems() {
        return items;
    }
}