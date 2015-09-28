package com.quickblox.sample.gcmchecker;


import com.quickblox.core.RestMethod;
import com.quickblox.core.query.JsonQuery;
import com.quickblox.core.rest.RestRequest;
import com.quickblox.sample.gcmchecker.main.Consts;

/**
 * Created by tereha on 23.09.15.
 */
public class QuerySendReport extends JsonQuery<Void> {

    String serverTitle;
    String unixTime;
    long pushTimeout;

    public QuerySendReport(String serverTitle, String unixTime, long pushTimeout) {
        this.serverTitle = serverTitle;
        this.unixTime = unixTime;
        this.pushTimeout = pushTimeout;
    }

    @Override
    public String getUrl() {
        return buildQueryUrl(Consts.PLATFORM_VALUE, serverTitle, unixTime, pushTimeout);
    }

    @Override
    protected String buildQueryUrl(Object... specificParts) {
        StringBuilder urlBuilder = new StringBuilder(Consts.REPORT_URL + "?");
        urlBuilder.append(Consts.REPORT_PARAMETER_PLATFORM + "=");
        urlBuilder.append(specificParts[0] + "&");
        urlBuilder.append(Consts.REPORT_PARAMETER_SERVER + "=");
        urlBuilder.append(specificParts[1] + "&");
        urlBuilder.append(Consts.REPORT_PARAMETER_PUSH_TIME + "=");
        urlBuilder.append(specificParts[2] + "&");
        urlBuilder.append(Consts.REPORT_PARAMETER_PUSH_TIMEOUT + "=");
        urlBuilder.append(specificParts[3]);
        return urlBuilder.toString();
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
