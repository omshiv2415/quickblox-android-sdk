package com.quickblox.simplesample.messages.main.models;

import com.qb.gson.annotations.SerializedName;

/**
 * Created by tereha on 17.09.15.
 */
public class Credentials {

//    @SerializedName("instances")
//    private String instances;


    private String title;
    private String appId;
    private String authKey;
    private String authSecret;
    private String userLogin;
    private String userID;
    private String userPass;
    private String serverApiDomain;

    public Credentials() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAuthKey() {
        return authKey;
    }

    public void setAuthKey(String authKey) {
        this.authKey = authKey;
    }

    public String getAuthSecret() {
        return authSecret;
    }

    public void setAuthSecret(String authSecret) {
        this.authSecret = authSecret;
    }

    public String getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }

    public String getUserPass() {
        return userPass;
    }

    public void setUserPass(String userPass) {
        this.userPass = userPass;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getServerApiDomain() {
        return serverApiDomain;
    }

    public void setServerApiDomain(String serverApiDomain) {
        this.serverApiDomain = serverApiDomain;
    }
}
