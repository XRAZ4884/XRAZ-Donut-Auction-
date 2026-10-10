package com.xraz.donutauction;

public class AuctionRecord {
    public String itemId = "minecraft:diamond";
    public String displayName = "Diamond";
    public long startingPrice = 1000;
    public long estimatedValue = 1000;
    public long minimumPrice = 0;
    public int quantity = 1;
    public int durationSeconds = 60;
    public long timestamp = System.currentTimeMillis();
    public String result = "PRESET";
    public String notes = "";

    public AuctionRecord() {}
    public AuctionRecord(AuctionRecord other) {
        itemId = other.itemId; displayName = other.displayName;
        startingPrice = other.startingPrice; estimatedValue = other.estimatedValue;
        minimumPrice = other.minimumPrice; quantity = other.quantity;
        durationSeconds = other.durationSeconds; timestamp = System.currentTimeMillis();
        result = other.result; notes = other.notes;
    }
}
