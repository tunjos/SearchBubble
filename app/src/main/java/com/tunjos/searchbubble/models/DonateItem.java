package com.tunjos.searchbubble.models;

/**
 * Created by tunjos on 15/07/2015.
 */
public class DonateItem {
    public String sku;
    public int itemName;
    public String price;
    public boolean purchased;

    public DonateItem(String sku, int itemName, String price) {
        this.sku = sku;
        this.itemName = itemName;
        this.price = price;
        purchased = false;
    }
}
