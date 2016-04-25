package com.quickblox.sample.groupchatwebrtc.definitions;

/**
 * QuickBlox team
 */
public interface Consts {
    String APP_ID = "92";
    String AUTH_KEY = "wJHdOcQSxXQGWx5";
    String AUTH_SECRET = "BTFsj7Rtt27DAmT";
    String ACCOUNT_KEY = "rz2sXxBt5xgSxGjALDW6";

    String DEFAULT_USER_PASSWORD = "x6Bt0VDy5";

    String VERSION_NUMBER = "1.0";

    int CALL_ACTIVITY_CLOSE = 1000;

    //CALL ACTIVITY CLOSE REASONS
    int CALL_ACTIVITY_CLOSE_WIFI_DISABLED = 1001;
    String WIFI_DISABLED = "wifi_disabled";

    String OPPONENTS = "opponents";
    String CONFERENCE_TYPE = "conference_type";
    String EXTRA_TAG = "currentRoomName";
    String EXTRA_USER_LOGIN = "extraUserLogin";
    String EXTRA_USER_PASSWORD = "extraUserPassword";

    int AUTO_SEND_PRESENCE_INTERVAL = 60;

    // Configure QBRTCClient
    //
    Integer MAX_OPPONENTS_COUNT = 6;
    Integer DISCONNECT_TIME = 30;
    long ANSWER_TIME_INTERVAL = 30l;
    boolean DEBUG_ENABLED = true;





    public static final String EMPTY_STRING = "";

    public static final String INCOME_CALL_FRAGMENT = "income_call_fragment";
    public static final String CONVERSATION_CALL_FRAGMENT = "conversation_call_fragment";

    public static final int CONNECTION_TYPE_WIFI = 1;
    public static final int CONNECTION_TYPE_MOBILE = 2;
    public static final int CONNECTION_TYPE_NOT_CONNECTED = 0;

    public static final int NOTIFICATION_FORAGROUND = 1004;
    public static final int NOTIFICATION_CONNECTION_LOST = 1005;

    public final static int LOGIN_TASK_CODE = 1002;
    public final static int LOGIN_RESULT_CODE = 1003;
    public final static int RESULT_CODE_1 = 1;
    public final static int RESULT_CODE_2 = 2;
    public final static int RESULT_CODE_3 = 3;

    //Start service variant
    public final static String START_SERVICE_VARIANT = "start_service_variant";
    public final static int AUTOSTART = 1004;
    public final static int RELOGIN = 1005;
    public final static int LOGIN = 1006;



    public final static String PARAM_PINTENT = "pendingIntent";
    public final static String LOGIN_RESULT = "result";

    //Shared Preferences constants
    public static final String USER_LOGIN = "user_login";
    public static final String USER_PASSWORD = "user_password";
    public static final String USER_IS_LOGINED = "is_logined";
    public static final long HUNG_UP_TIME_OUT = 3000l;

    //CALL ACTIVITY CLOSE REASONS

    public static final String CALLER_NAME = "caller_name";
    public static final String SESSION_ID = "sessionID";
    public static final String START_CONVERSATION_REASON = "start_conversation_reason";
    static final String ADD_OPPONENTS_FRAGMENT_HANDLER = "opponentHandlerTask";
    static final long TIME_BEGORE_CLOSE_CONVERSATION_FRAGMENT = 3;
    static final String INCOME_WINDOW_SHOW_TASK_THREAD = "INCOME_WINDOW_SHOW";
    public static final String OPPONENTS_CALL_FRAGMENT = "opponents_call_fragment";
    public static final String OPPONENTS_LIST_EXTRAS = "opponents_list";
    public static final String CALL_DIRECTION_TYPE_EXTRAS = "call_direction_type";
    public static final String CALL_TYPE_EXTRAS = "call_type";
    public static final String QBRTCSESSION_EXTRAS = "qbrtcsession";
    public static final String USER_INFO_EXTRAS = "user_info";
    public static final String IS_SERVICE_AUTOSTARTED = "autostart";
    public static final String SHARED_PREFERENCES = "preferences";


    String EXTRA_CALLDIRECTION_TYPE = "extra_call_direction_type";

    enum CALL_DIRECTION_TYPE {
        INCOMING,
        OUTGOING
    }
}
