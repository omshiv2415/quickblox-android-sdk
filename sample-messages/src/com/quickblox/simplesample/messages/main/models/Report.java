package com.quickblox.simplesample.messages.main.models;

/**
 * Created by tereha on 15.09.15.
 */
public class Report {
    private String sendTime;
    private String deliveryTime;
    private String travelingTime;


    public Report(String sendTime, String deliveryTime, String travelingTime){
        this.sendTime = sendTime;
        this.deliveryTime = deliveryTime;
        this.travelingTime = travelingTime;
    }

    public String getSendTime() {
        return sendTime;
    }

    public void setSendTime(String sendTime) {
        this.sendTime = sendTime;
    }

    public String getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(String deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public String getTravelingTime() {
        return travelingTime;
    }

    public void setTravelingTime(String travelingTime) {
        this.travelingTime = travelingTime;
    }
}
