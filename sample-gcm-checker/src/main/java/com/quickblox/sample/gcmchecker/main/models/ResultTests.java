package com.quickblox.sample.gcmchecker.main.models;

/**
 * Created by tereha on 24.09.15.
 */
public class ResultTests {
    private String errorTime;
    private String errorText;

    public ResultTests(String errorTime, String errorText) {
        this.errorTime = errorTime;
        this.errorText = errorText;
    }

    public String getErrorTime() {
        return errorTime;
    }

    public void setErrorTime(String errorTime) {
        this.errorTime = errorTime;
    }

    public String getErrorText() {
        return errorText;
    }

    public void setErrorText(String errorText) {
        this.errorText = errorText;
    }
}
