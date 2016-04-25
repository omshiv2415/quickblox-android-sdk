package com.quickblox.sample.groupchatwebrtc.fragments;

import android.app.Fragment;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.quickblox.chat.QBChatService;
import com.quickblox.sample.core.utils.UiUtils;
import com.quickblox.sample.groupchatwebrtc.R;
import com.quickblox.sample.groupchatwebrtc.SessionManager;
import com.quickblox.sample.groupchatwebrtc.activities.CallActivity;
import com.quickblox.sample.groupchatwebrtc.holder.DataHolder;
import com.quickblox.sample.groupchatwebrtc.util.OpponentsManager;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by tereha on 16.02.15.
 */
public class IncomeCallFragment extends Fragment implements Serializable, View.OnClickListener {

    private static final String TAG = IncomeCallFragment.class.getSimpleName();
    private TextView typeIncCallView;
    private TextView callerName;
    private TextView otherIncUsers;
    private ImageButton rejectBtn;
    private ImageButton takeBtn;

    private List<Integer> opponents;
    private List<QBUser> opponentsFromCall = new ArrayList<>();
    private MediaPlayer ringtone;
    private Vibrator vibrator;
    private QBRTCTypes.QBConferenceType conferenceType;
    private View view;
    private boolean isVideoCall;
    private Integer callerId;
    private ImageView callerAvatar;
    private long lastClickTime = 0l;
    private static final long CLICK_DELAY = TimeUnit.SECONDS.toMillis(2);


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (savedInstanceState == null) {

            view = inflater.inflate(R.layout.fragment_income_call, container, false);

            ((CallActivity) getActivity()).initActionBar();

            initCallData();
            initUI(view);
//            initButtonsListener();
        }

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setRetainInstance(true);

        Log.d(TAG, "onCreate() from IncomeCallFragment");
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        startCallNotification();
    }

    private void initCallData(){
        QBRTCSession currentSession = SessionManager.getCurrentSession();
        if ( currentSession != null){
            opponents = currentSession.getOpponents();
            conferenceType = currentSession.getConferenceType();
            callerId = currentSession.getCallerID();
        } else {
            Log.d(TAG, "Incoming fragment not started, because current session = null");
        }
    }

    private void initUI(View view) {
        isVideoCall = QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO.equals(conferenceType);

        typeIncCallView = (TextView) view.findViewById(R.id.type_inc_call);
        typeIncCallView.setText(isVideoCall ? R.string.incoming_video_call : R.string.incoming_audio_call);

        callerName = (TextView) view.findViewById(R.id.caller_name);
        callerName.setText(OpponentsManager.getUserNameByID(callerId));

        callerAvatar = (ImageView) view.findViewById(R.id.caller_avatar);
        callerAvatar.setBackgroundDrawable(OpponentsManager.getUserIndexByID(callerId) != -1
                ? UiUtils.getColorCircleDrawable(OpponentsManager.getUserIndexByID(callerId))
                :  UiUtils.getRandomColorCircleDrawable());
        callerAvatar.setImageResource(R.drawable.ic_person_big);

        otherIncUsers = (TextView) view.findViewById(R.id.other_inc_users);
        otherIncUsers.setText(getOtherIncUsersNames(opponents));

        rejectBtn = (ImageButton) view.findViewById(R.id.rejectBtn);
        rejectBtn.setOnClickListener(this);

        takeBtn = (ImageButton) view.findViewById(R.id.takeBtn);
        takeBtn.setOnClickListener(this);
        takeBtn.setImageResource(QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO.equals(conferenceType) ?
                R.drawable.ic_videocam_w : R.drawable.ic_accept_call);
    }

    private void initButtonsListener() {
            rejectBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    rejectBtn.setClickable(false);
                    takeBtn.setClickable(false);
                    Log.d(TAG, "Call is rejected");
                    stopCallNotification();
                    ((CallActivity) getActivity()).rejectCurrentSession();
                    getActivity().finish();
                }
            });

            takeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    takeBtn.setClickable(false);
                    rejectBtn.setClickable(false);
                    stopCallNotification();
//                    ((CallActivity) getActivity())
//                            .addConversationFragment(
//                                    opponents, conferenceType, Consts.CALL_DIRECTION_TYPE.INCOMING);

                    Log.d(TAG, "Call is started");
                }
            });
    }

    public void startCallNotification() {

        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        ringtone = MediaPlayer.create(getActivity(), notification);

//        ringtone.setLooping(true);
        ringtone.start();

        vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);

        long[] vibrationCycle = {0, 1000, 1000};
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(vibrationCycle, 1);
        }

    }

    private void stopCallNotification() {
        if (ringtone != null) {
            try {
                ringtone.stop();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            ringtone.release();
            ringtone = null;
        }

        if (vibrator != null) {
            vibrator.cancel();
        }
    }

    private String getOtherIncUsersNames(List<Integer> opponents) {
        List<Integer> otherOpponents = new ArrayList<>(opponents);
        StringBuffer s = new StringBuffer("");
        opponentsFromCall.addAll(DataHolder.getUsersList());
        otherOpponents.remove(QBChatService.getInstance().getUser().getId());

        for (Integer i : otherOpponents) {
            for (QBUser usr : opponentsFromCall) {
                if (usr.getId().equals(i)) {
                    if (otherOpponents.indexOf(i) == (otherOpponents.size() - 1)) {
                        s.append(usr.getFullName() + " ");
                        break;
                    } else {
                        s.append(usr.getFullName() + ", ");
                    }
                }
            }
        }
        return s.toString();
    }

    public void onStop() {
        stopCallNotification();
        super.onDestroy();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if ((SystemClock.uptimeMillis() - lastClickTime) < CLICK_DELAY) {
            return;
        }
        lastClickTime = SystemClock.uptimeMillis();

        switch (v.getId()) {
            case R.id.rejectBtn:
                reject();
                break;
            case R.id.takeBtn:
                accept();
                break;
            default:
                break;
        }
    }

    private void accept() {
        takeBtn.setClickable(false);
        rejectBtn.setClickable(false);
        stopCallNotification();

        ((CallActivity) getActivity())
                .addConversationFragmentReceiveCall();
        Log.d(TAG, "Call is started");
    }

    private void reject() {
        rejectBtn.setClickable(false);
        takeBtn.setClickable(false);
        Log.d(TAG, "Call is rejected");

        stopCallNotification();

        SessionManager.getCurrentSession().rejectCall(null);
    }
}
