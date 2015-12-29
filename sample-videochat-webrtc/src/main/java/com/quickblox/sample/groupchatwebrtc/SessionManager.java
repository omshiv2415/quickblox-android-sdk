package com.quickblox.sample.groupchatwebrtc;

import com.quickblox.videochat.webrtc.QBRTCSession;


public class SessionManager {
    private static QBRTCSession currentSession;
    private static final String TAG = SessionManager.class.getSimpleName();

    public static QBRTCSession getCurrentSession() {
        return currentSession;
    }

    public static void setCurrentSession(QBRTCSession qbCurrentSession) {
        currentSession = qbCurrentSession;
    }
}
