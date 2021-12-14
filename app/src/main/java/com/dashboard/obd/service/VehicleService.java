/*
 * Copyright (c) 20214. Anthony Fryberg.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dashboard.obd.service;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcelable;


import com.pokevian.lib.obd2.data.ObdData;
import com.pokevian.lib.obd2.data.PersistData;
import com.pokevian.lib.obd2.listener.OnObdStateListener.ObdState;

import org.apache.log4j.Logger;

public class VehicleService extends Service implements ObdManager.ObdManagerCallbacks
         {

    static final String TAG = "VehicleService";
    final Logger logger = Logger.getLogger(TAG);

   // private TripManager mTripManager;
    private ObdManager mObdManager;

    //private ImpactDetector mImpactDetector;
    private VehicleDataBroadcaster mDataBroadcaster;
    //private SoundEffectNotifier mSoundEffectNotifier;

    private final int DRIVING_STATE_IDLE = 0;
    private final int DRIVING_STATE_DRIVING = 1;
    private final int DRIVING_STATE_PAUSED = 2;
    private int mDrivingState = DRIVING_STATE_IDLE;

    @Override
    public void onCreate() {
        Logger.getLogger(TAG).debug("onCreate@service");

        super.onCreate();

       // mSoundEffectNotifier = new SoundEffectNotifier(this);

        //mTripManager = new TripManager(this);

        mObdManager = new ObdManager(getApplication(), this);



        //mImpactDetector = new ImpactDetector(this, this);
        //mImpactDetector.setSensitivity(SettingsStore.getInstance().getImpactSensitivity());

        mDataBroadcaster = new VehicleDataBroadcaster(this);
    }

    @Override
    public void onDestroy() {
        Logger.getLogger(TAG).debug("onDestroy@service");

//        mSoundEffectNotifier.stopLoopSound();
       //mSoundEffectNotifier.release();

        mObdManager.stop();
        mObdManager.exit(true);

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // if this service's process is killed while it is started (after returning from onStartCommand),
        // and there are no new start intents to deliver to it, then take the service out of the started state
        // and don't recreate until a future explicit call to Context.startService(Intent).
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new VehicleServiceBinder();
    }

    public void connectVehicle(String obd_adr) {
        mObdManager.run(obd_adr, true);
    }

    public void connectVehicle(String obd_adr, boolean allowBluetoothControl) {
        mObdManager.run(obd_adr, true);
    }

    public void disconnectVehicle() {
        mObdManager.stop();
    }

   /*public void startDriving(String accountId, Vehicle vehicle, boolean isNew) {
        if (mDrivingState == DRIVING_STATE_IDLE) {
            mObdManager.resetIfNeeded(isNew);

            mTripManager.openTrip(accountId, vehicle);

            mImpactDetector.setEnabled(true);
            mImpactDetector.run();

            mDrivingState = DRIVING_STATE_DRIVING;
            Logger.getLogger(TAG).info("mDrivingState=DRIVING_STATE_DRIVING");
        }
    }*/

   /* public VehicleTrip stopDriving() {
        VehicleTrip trip = null;

        if (mDrivingState == DRIVING_STATE_DRIVING || mDrivingState == DRIVING_STATE_PAUSED) {
            mImpactDetector.stop();
            trip = mTripManager.closeTrip();

            mDrivingState = DRIVING_STATE_IDLE;
            logger.info("mDrivingState=DRIVING_STATE_IDLE");
        }

        mSoundEffectNotifier.stopLoopSound();

        return trip;
    }*/

   public void pauseDriving() {
        if (mDrivingState == DRIVING_STATE_DRIVING) {
            mObdManager.pause();
          //  mImpactDetector.stop();

            mDrivingState = DRIVING_STATE_PAUSED;
            logger.info("mDrivingState=DRIVING_STATE_PAUSED");
        }
    }

    public void resumeDriving() {
        if (mDrivingState == DRIVING_STATE_PAUSED) {
            mObdManager.resume();
          // mImpactDetector.run();

            mDrivingState = DRIVING_STATE_DRIVING;
            logger.info("mDrivingState=DRIVING_STATE_DRIVING");
        }
    }

    public void clearStoredDTC() {
        mObdManager.clearStoredDTC();
    }

    public void resetVehiclePersistData(String vehicleId) {
        PersistData.clearSupportedPids(this, vehicleId);
        PersistData.clearLastData(this);
    }

    public void startDataBackup() {
        mObdManager.startDataBackup();
    }

    public void stopDataBackup() {
        mObdManager.stopDataBackup();
    }

    public ObdState getObdState() {
        return mObdManager.getObdState();
    }

    public int getVehicleEngineStatus() {
        return mObdManager.getVehicleEngineStatus();
    }

   /* public void setImpactDetectorEnabled(boolean enabled) {
        mImpactDetector.setEnabled(enabled);
    }

    public void onEmergency() {
        logger.info("onEmergency()");

        mBlackboxManager.onEmergency(BlackboxEventType.USER);

        mTripManager.onEmergencyEvent();
    }

    public VehicleLocation getLastLocation() {
        return mTripManager.getLastLocation();
    }*/

    /*public void uploadVideo(VideoInfo videoInfo) {
        Intent intent = new Intent(this, YoutubeUploadService.class);

        String metaJson = mTripManager.getLocationMetaJson(
                videoInfo.getBeginTime().getTime(), videoInfo.getEndTime().getTime());
        videoInfo.setMetaJson(metaJson);

        intent.putExtra(YoutubeUploadService.EXTRA_VIDEO_INFO, videoInfo);

        startService(intent);
    }*/

    @Override
    public void onObdStateChanged(ObdState state, Parcelable extra) {
        mDataBroadcaster.onObdStateChanged(state, extra);
    }

    @Override
    public void onObdVehicleEngineStatusChanged(int ves) {
        mDataBroadcaster.onVehicleEngineStatusChanged(ves);

       /* mTripManager.onVehicleEngineStatusChanged(ves);

        mSoundEffectNotifier.onObdVehicleEngineStatusChanged(ves);*/
    }

    @Override
    public void onObdDataReceived(ObdData data) {
        mDataBroadcaster.onObdDataReceived(data);

      //  mTripManager.onObdDataReceived(data);

      //  mBlackboxManager.onObdDataReceived(data);

       /* if (mDrivingState == DRIVING_STATE_DRIVING) {
          //  mSoundEffectNotifier.onObdDataReceived(data);
        }*/
    }

    @Override
    public void onObdExtraDataReceived(float rpm, int vss) {
        mDataBroadcaster.onObdExtraDataReceived(rpm, vss);
    }

    @Override
    public void onObdConnectionFailed(BluetoothDevice obdDevice, int failureCount) {
        mDataBroadcaster.onObdConnectionFailed(obdDevice, failureCount);
    }

    @Override
    public void onObdCannotConnect(BluetoothDevice obdDevice, boolean isBlocked) {
        mDataBroadcaster.onObdCannotConnect(obdDevice, isBlocked);
    }

    @Override
    public void onObdDeviceNotSupported(BluetoothDevice obdDevice) {
        mDataBroadcaster.onObdDeviceNotSupported(obdDevice);
    }

    @Override
    public void onObdProtocolNotSupported(BluetoothDevice obdDevice) {
        mDataBroadcaster.onObdProtocolNotSupported(obdDevice);
    }

    @Override
    public void onObdInsufficientPid() {
        mDataBroadcaster.onObdInsufficientPid();
    }

    @Override
    public void onObdEngineDistanceSupported(boolean isSupported) {
        mDataBroadcaster.onObdEngineDistanceSupported(isSupported);
    }

    @Override
    public void onObdBusInitError(int protocol) {
        mDataBroadcaster.onObdBusInitError(protocol);
    }

    @Override
    public void onObdNoData() {
        mDataBroadcaster.onObdNoData();
    }



    public class VehicleServiceBinder extends Binder {
        public VehicleService getService() {
            return VehicleService.this;
        }
    }



//    public List<Integer> getControlAfAvailableModes() {
//        return mBlackboxManager.getEngine().getControlAfAvailableModes();
//    }

}
