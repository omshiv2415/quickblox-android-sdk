package com.quickblox.sample.gcmchecker;


import com.quickblox.core.RestMethod;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.query.JsonQuery;
import com.quickblox.core.rest.RestRequest;
import com.quickblox.sample.gcmchecker.main.Consts;

import java.io.IOException;
import java.util.Map;

/**
 * Created by tereha on 23.09.15.
 */
public class QuerySendReport extends JsonQuery<Void> {

    String serverTitle;
    String unixTime;
    long pushTime;
    String error;
    int errorCode;
    String errorStatus;
    String errorMessage;
    String errorDetails;

    public QuerySendReport(){
        this.unixTime = String.valueOf(System.currentTimeMillis());
    }

    public QuerySendReport(String serverTitle, long pushTime) {
        this();
        this.serverTitle = serverTitle;
        this.pushTime = pushTime;
        if (pushTime == -1){
            this.error = "TIMEOUT";
        }
    }

    public QuerySendReport(String serverTitle, IOException qbException){
        this();
        this.serverTitle = serverTitle;
        this.pushTime = -1;
        if (qbException instanceof QBResponseException) {
            this.errorCode = ((QBResponseException)qbException).getHttpStatusCode();
        }
        this.errorMessage = qbException.getMessage();
    }

    public QuerySendReport(String serverTitle) {
        this();
        this.serverTitle = serverTitle;
        this.pushTime = -1;
        this.error = "TIMEOUT";
    }


    @Override
    public String getUrl() {
        return buildQueryUrl(Consts.REPORT_URL);
    }

    @Override
    protected String buildQueryUrl(Object... specificParts) {
        StringBuilder urlBuilder = new StringBuilder(Consts.REPORT_URL);
        return urlBuilder.toString();
    }

    @Override
    protected void setParams(RestRequest request) {
        super.setParams(request);
        Map<String, Object> parametersMap = request.getParameters();

        putValue(parametersMap, Consts.REPORT_PARAMETER_SERVER, serverTitle);
        putValue(parametersMap, Consts.REPORT_PARAMETER_PUSH_TIME, pushTime);
        putValue(parametersMap, Consts.REPORT_PARAMETER_PLATFORM, Consts.PLATFORM_VALUE);
        putValue(parametersMap, Consts.REPORT_PARAMETER_ERROR, error);
        if (errorCode != 0) {
            putValue(parametersMap, Consts.REPORT_PARAMETER_ERROR_CODE, errorCode);
            putValue(parametersMap, Consts.REPORT_PARAMETER_ERROR_STATUS, errorStatus);
            putValue(parametersMap, Consts.REPORT_PARAMETER_ERROR_MESSAGE, errorMessage);
            putValue(parametersMap, Consts.REPORT_PARAMETER_ERROR_DETAILS, errorDetails);
        }
    }

    @Override
    protected void setMethod(RestRequest request) {
        request.setMethod(RestMethod.PUT);
    }

    @Override
    public Class getResultClass() {
        return QuerySendReport.class;
    }
}
