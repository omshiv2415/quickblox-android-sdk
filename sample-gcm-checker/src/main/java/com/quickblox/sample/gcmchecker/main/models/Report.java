package com.quickblox.sample.gcmchecker.main.models;

/**
 * Created by tereha on 15.09.15.
 */
public class Report {
    private int successPushes;
    private int sendedPushes;
    private int colorStatusOval;
    private String serverTitle;
    private String deliveryDateLastPush;


    public Report() {
    }

    public int getSuccessPushes() {
        return successPushes;
    }

    public void setSuccessPushes(int successPushes) {
        this.successPushes = successPushes;
    }

    public int getSendedPushes() {
        return sendedPushes;
    }

    public void setSendedPushes(int sendedPushes) {
        this.sendedPushes = sendedPushes;
    }

    public int getColorStatusOval() {
        return colorStatusOval;
    }

    public void setColorStatusOval(int colorStatusOval) {
        this.colorStatusOval = colorStatusOval;
    }

    public String getServerTitle() {
        return serverTitle;
    }

    public void setServerTitle(String serverTitle) {
        this.serverTitle = serverTitle;
    }

    public String getDeliveryDateLastPush() {
        return deliveryDateLastPush;
    }

    public void setDeliveryDateLastPush(String deliveryDateLastPush) {
        this.deliveryDateLastPush = deliveryDateLastPush;
    }
}
