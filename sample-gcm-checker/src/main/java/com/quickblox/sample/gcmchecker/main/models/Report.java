package com.quickblox.sample.gcmchecker.main.models;

/**
 * Created by tereha on 15.09.15.
 */
public class Report {
    private String sendDate;
    private String deliveryDate;
    private String travelingTime;
    private String serverTitle;

    public Report(String sendDate, String deliveryDate, String travelingTime){
        this.sendDate = sendDate;
        this.deliveryDate = deliveryDate;
        this.travelingTime = travelingTime;
    }

    public Report (){
    }

    public String getSendDate() {
        return sendDate;
    }

    public void setSendDate(String sendDate) {
        this.sendDate = sendDate;
    }

    public String getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(String deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public String getTravelingTime() {
        return travelingTime;
    }

    public void setTravelingTime(String travelingTime) {
        this.travelingTime = travelingTime;
    }

    public String getServerTitle() {
        return serverTitle;
    }

    public void setServerTitle(String serverTitle) {
        this.serverTitle = serverTitle;
    }
}
