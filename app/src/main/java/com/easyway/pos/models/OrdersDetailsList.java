package com.easyway.pos.models;

public class OrdersDetailsList {


    String RecordIndex, iItem, ItemCode, ItemName, iApprovedQtty, iApprovedPrice, iDeliveredQtty, iBalance;

    public OrdersDetailsList(String RecordIndex, String iItem, String ItemCode, String ItemName, String iApprovedQtty, String iApprovedPrice,
                             String iDeliveredQtty, String iBalance) {
        this.RecordIndex = RecordIndex;
        this.iItem = iItem;
        this.ItemCode = ItemCode;
        this.ItemName = ItemName;
        this.iApprovedQtty = iApprovedQtty;
        this.iApprovedPrice = iApprovedPrice;
        this.iDeliveredQtty = iDeliveredQtty;
        this.iBalance = iBalance;

    }


    public String RecordIndex() {
        return RecordIndex;
    }

    public String iItem() {
        return iItem;
    }

    public String ItemCode() {
        return ItemCode;
    }

    public String ItemName() {
        return ItemName;
    }

    public String iApprovedQtty() {
        return iApprovedQtty;
    }

    public String iApprovedPrice() {
        return iApprovedPrice;
    }

    public String iDeliveredQtty() {
        return iDeliveredQtty;
    }

    public String iBalance() {
        return iBalance;
    }
}