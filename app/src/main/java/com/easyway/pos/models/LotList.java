package com.easyway.pos.models;

public class LotList {


    String RecordIndex, prodBatchNumber, prodDate, prodTotalCrop, prodBatFieldWeight;


    public LotList(String RecordIndex, String prodBatchNumber, String prodDate, String prodTotalCrop, String prodBatFieldWeight) {
        this.RecordIndex = RecordIndex;
        this.prodBatchNumber = prodBatchNumber;
        this.prodDate = prodDate;
        this.prodTotalCrop = prodTotalCrop;
        this.prodBatFieldWeight = prodBatFieldWeight;

    }


    public String RecordIndex() {
        return RecordIndex;
    }

    public String prodBatchNumber() {
        return prodBatchNumber;
    }

    public String prodDate() {
        return prodDate;
    }

    public String prodTotalCrop() {
        return prodTotalCrop;
    }

    public String prodBatFieldWeight() {
        return prodBatFieldWeight;
    }

}