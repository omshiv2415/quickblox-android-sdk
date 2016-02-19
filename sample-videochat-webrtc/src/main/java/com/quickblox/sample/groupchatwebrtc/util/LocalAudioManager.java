package com.quickblox.sample.groupchatwebrtc.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Process;
import android.util.Log;

/**
 * Created by tereha on 19.02.16.
 */
public class LocalAudioManager {

    private String TAG = LocalAudioManager.class.getSimpleName();
    private Context mContext;

    // Bluetooth audio SCO states. Example of valid state sequence:
    // SCO_INVALID -> SCO_TURNING_ON -> SCO_ON -> SCO_TURNING_OFF -> SCO_OFF.
    private static final int STATE_BLUETOOTH_SCO_INVALID = -1;
    private static final int STATE_BLUETOOTH_SCO_OFF = 0;
    private static final int STATE_BLUETOOTH_SCO_ON = 1;
    private static final int STATE_BLUETOOTH_SCO_TURNING_ON = 2;
    private static final int STATE_BLUETOOTH_SCO_TURNING_OFF = 3;

    private BroadcastReceiver mBluetoothHeadsetReceiver;
    private boolean DEVICE_BLUETOOTH_HEADSET_PLAGGED;
    private BroadcastReceiver mBluetoothScoReceiver;
    private AudioManager mAudioManager;
    private int mBluetoothScoState = STATE_BLUETOOTH_SCO_INVALID;
    private boolean isLocalAudioManagerInitialized;
    private int savedAudioMode;
    private boolean savedIsSpeakerPhoneOn;
    private boolean savedIsMicrophoneMute;


    public LocalAudioManager(Context mContext) {
        this.mContext = mContext;
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
    }

    /**
     * Registers receiver for the broadcasted intent related to BT headset
     * availability or a change in connection state of the local Bluetooth
     * adapter. Example: triggers when the BT device is turned on or off.
     * BLUETOOTH permission is required to receive this one.
     */
    private void registerForBluetoothHeadsetIntentBroadcast() {
        IntentFilter filter = new IntentFilter(
                android.bluetooth.BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
        /** Receiver which handles changes in BT headset availability. */
        mBluetoothHeadsetReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // A change in connection state of the Headset profile has
                // been detected, e.g. BT headset has been connected or
                // disconnected. This broadcast is *not* sticky.
                int profileState = intent.getIntExtra(
                        android.bluetooth.BluetoothHeadset.EXTRA_STATE,
                        android.bluetooth.BluetoothHeadset.STATE_DISCONNECTED);

                switch (profileState) {
                    case android.bluetooth.BluetoothProfile.STATE_DISCONNECTED:
                        // We do not have to explicitly call stopBluetoothSco()
                        // since BT SCO will be disconnected automatically when
                        // the BT headset is disabled.
//                        synchronized (mLock) {
//                            // Remove the BT device from the list of devices.
//                            mAudioDevices[DEVICE_BLUETOOTH_HEADSET] = false;
//                        }
                        DEVICE_BLUETOOTH_HEADSET_PLAGGED = false;
                        break;
                    case android.bluetooth.BluetoothProfile.STATE_CONNECTED:
                        // Add the BT device to the list of devices.
                        DEVICE_BLUETOOTH_HEADSET_PLAGGED = true;
                        break;
                    case android.bluetooth.BluetoothProfile.STATE_CONNECTING:
                        // Bluetooth service is switching from off to on.
                        DEVICE_BLUETOOTH_HEADSET_PLAGGED = false;
                        break;
                    case android.bluetooth.BluetoothProfile.STATE_DISCONNECTING:
                        // Bluetooth service is switching from on to off.
                        DEVICE_BLUETOOTH_HEADSET_PLAGGED = false;
                        break;
                    default:
//                        loge("Invalid state!");
                        break;
                }
            }
        };
        mContext.registerReceiver(mBluetoothHeadsetReceiver, filter);
    }

    private void unregisterForBluetoothHeadsetIntentBroadcast() {
        mContext.unregisterReceiver(mBluetoothHeadsetReceiver);
        mBluetoothHeadsetReceiver = null;
    }

    /**
     * Registers receiver for the broadcasted intent related the existence
     * of a BT SCO channel. Indicates if BT SCO streaming is on or off.
     */
    private void registerForBluetoothScoIntentBroadcast() {
        IntentFilter filter = new IntentFilter(
                AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED);
        /** BroadcastReceiver implementation which handles changes in BT SCO. */
        mBluetoothScoReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int state = intent.getIntExtra(
                        AudioManager.EXTRA_SCO_AUDIO_STATE,
                        AudioManager.SCO_AUDIO_STATE_DISCONNECTED);

                switch (state) {
                    case AudioManager.SCO_AUDIO_STATE_CONNECTED:
                        mBluetoothScoState = STATE_BLUETOOTH_SCO_ON;
                        break;
                    case AudioManager.SCO_AUDIO_STATE_DISCONNECTED:
                        mBluetoothScoState = STATE_BLUETOOTH_SCO_OFF;
                        break;
                    case AudioManager.SCO_AUDIO_STATE_CONNECTING:
                        // do nothing
                        break;
                    default:
                }
            }
        };
        mContext.registerReceiver(mBluetoothScoReceiver, filter);
    }

    private void unregisterForBluetoothScoIntentBroadcast() {
        mContext.unregisterReceiver(mBluetoothScoReceiver);
        mBluetoothScoReceiver = null;
    }

    /**
     * Enables BT audio using the SCO audio channel.
     */
    private void startBluetoothSco() {
        Log.d(TAG, "startBluetoothSco()");
        if (!hasBluetoothPermission()) {
            Log.d(TAG, "!hasBluetoothPermission()");
            return;
        }
//        if (mBluetoothScoState == STATE_BLUETOOTH_SCO_ON ||
//                mBluetoothScoState == STATE_BLUETOOTH_SCO_TURNING_ON) {
//            // Unable to turn on BT in this state.
//            return;
//        }
//        // Check if audio is already routed to BT SCO; if so, just update
//        // states but don't try to enable it again.
//        if (mAudioManager.isBluetoothScoOn()) {
//            Log.d(TAG, "mAudioManager.isBluetoothScoOn()");
//            mBluetoothScoState = STATE_BLUETOOTH_SCO_ON;
//            return;
//        }
//        mBluetoothScoState = STATE_BLUETOOTH_SCO_TURNING_ON;
//        mAudioManager.setMode(0);
//        mAudioManager.setBluetoothScoOn(true);
//        mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        mAudioManager.startBluetoothSco();
        Log.d(TAG, "isBluetoothScoOn()" + mAudioManager.isBluetoothScoOn());
        Log.d(TAG, "startBluetoothSco() all done");
    }

    /**
     * Disables BT audio using the SCO audio channel.
     */
    private void stopBluetoothSco() {
        Log.d(TAG, "stopBluetoothSco()");
        if (!hasBluetoothPermission()) {
            Log.d(TAG, "!hasBluetoothPermission()");
            return;
        }
//        if (mBluetoothScoState != STATE_BLUETOOTH_SCO_ON &&
//                mBluetoothScoState != STATE_BLUETOOTH_SCO_TURNING_ON) {
//            // No need to turn off BT in this state.
//            return;
//        }
//        if (!mAudioManager.isBluetoothScoOn()) {
//            // TODO(henrika): can we do anything else than logging here?
//            return;
//        }
//        mBluetoothScoState = STATE_BLUETOOTH_SCO_TURNING_OFF;
//        mAudioManager.setBluetoothScoOn(false);
        mAudioManager.stopBluetoothSco();
//        mAudioManager.setMode(AudioManager.MODE_NORMAL);
        Log.d(TAG, "isBluetoothScoOn()" + mAudioManager.isBluetoothScoOn());
        Log.d(TAG, "stopBluetoothSco() all done");
    }

    /**
     * Checks if the process has BLUETOOTH permission or not.
     */
    private boolean hasBluetoothPermission() {
        boolean hasBluetooth = mContext.checkPermission(
                android.Manifest.permission.BLUETOOTH,
                android.os.Process.myPid(),
                android.os.Process.myUid()) == PackageManager.PERMISSION_GRANTED;
        return hasBluetooth;
    }

        public void toggleBluetoothSco(boolean on) {
//        if (!DEVICE_BLUETOOTH_HEADSET_PLAGGED){
//            Log.d(TAG, )
//            return;
//        }



        if (on){
            Log.d(TAG, "bluetooth enable");
            startBluetoothSco();
            Log.d(TAG, "bluetooth enabled");
        } else {
            Log.d(TAG, "bluetooth disable");
            stopBluetoothSco();
            Log.d(TAG, "bluetooth disabled");
        }
    }

    public void initAudioManager() {
        if (isLocalAudioManagerInitialized) {
            return;
        }

        // Store current audio state so we can restore it when closePeerConnection() is called.
        savedAudioMode = mAudioManager.getMode();
        savedIsSpeakerPhoneOn = mAudioManager.isSpeakerphoneOn();
        savedIsMicrophoneMute = mAudioManager.isMicrophoneMute();

        // Request audio focus before making any device switch.
        mAudioManager.requestAudioFocus(null, AudioManager.STREAM_VOICE_CALL,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);

        // The AppRTC demo shall always run in COMMUNICATION mode since it will
        // result in best possible "VoIP settings", like audio routing, volume
        // control etc.
        mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);

        // Register receiver for broadcast intents related to adding/removing a
        // wired headset (Intent.ACTION_HEADSET_PLUG).
        registerForBluetoothHeadsetIntentBroadcast();
        registerForBluetoothScoIntentBroadcast();

        isLocalAudioManagerInitialized = true;
    }

    public void closeAudioManager() {
        if (!isLocalAudioManagerInitialized) {
            return;
        }

        unregisterForBluetoothHeadsetIntentBroadcast();
        unregisterForBluetoothScoIntentBroadcast();

        // Restore previously stored audio states.
        mAudioManager.setSpeakerphoneOn(savedIsSpeakerPhoneOn);
        mAudioManager.setMicrophoneMute(savedIsMicrophoneMute);
        mAudioManager.setMode(savedAudioMode);
        mAudioManager.abandonAudioFocus(null);

        isLocalAudioManagerInitialized = false;
    }

    public boolean isBluetoothScoEnabled(){
        return mBluetoothScoState == STATE_BLUETOOTH_SCO_ON;
    }

    public AudioManager getmAudioManager() {
        return mAudioManager;
    }
}
