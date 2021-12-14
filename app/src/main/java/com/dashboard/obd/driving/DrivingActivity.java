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

package com.dashboard.obd.driving;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.DecelerateInterpolator;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.dashboard.obd.map.MapActivity;
import com.dashboard.obd.meta.StartActivityForResults;
import com.dashboard.obd.service.SettingsStore;
import com.dashboard.obd.service.VehicleService;
import com.dashboard.obd.R;
import com.dashboard.obd.diagnostic.DiagnosticActivity;
import com.dashboard.obd.floatinghead.FloatingHeadService;
import com.dashboard.obd.receiver.BringToFrontReceiver;
import com.dashboard.obd.service.VehicleDataBroadcaster;
import com.dashboard.obd.util.ViewCompatUtils;
import com.pokevian.lib.obd2.data.ObdData;
import com.pokevian.lib.obd2.defs.FuelType;
import com.pokevian.lib.obd2.defs.KEY;
import com.pokevian.lib.obd2.defs.VehicleEngineStatus;
import com.pokevian.lib.obd2.listener.OnObdStateListener;
import com.pokevian.lib.obd2.listener.OnObdStateListener.ObdState;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public final class DrivingActivity extends AppCompatActivity
        implements OnClickListener{

    static final String TAG = "DrivingActivity";

    final static int ENABLE_BT_REQUEST = 1;
    final static int CHOOSE_PARAMS_REQUEST = 2;
    private boolean mIsPaused;
    private BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
    private String chosenDeviceName, chosenDeviceAddress;
    public static final String EXTRA_REQUEST_EXIT = "request_exit";
    public static final String EXTRA_VES = "vehicle_engine_status";




   private DrivingService mDrivingService;
   private VehicleService mVehicleService;
   private LocalBroadcastManager mBroadcastManager;
   private SettingsStore mStoreItem;
    private IndicatorFragment mIndicatorFragment;
    private ObdClusterFragment mObdFragment;
    private TripClusterFragment mTripFragment;
    private BottomFragment mBottomFragment;
    private DrivingDetailInfoFragment mDetailInfoFragment;

    private ImageView mErsBtn;
    private ImageView mErsInPreviewBtn;



    private boolean mIsBlackboxEnabled;
    private boolean mIsBlackboxRunning;
    private boolean mIsBlackboxPaused;
    private boolean mIsErsEnabled;
    private boolean mIsDrivingPaused;
    private boolean mIsDetailInfoShown;
    private boolean mIsDetailInfoAnimating;


    private TextView mPreviewDrivingDistance;
    private TextView mPreviewDrivingTime;
    private RelativeLayout mPreviewSpeedControl;
    private ImageView mPreviewEcoIndicator;
    private SpeedMeter mPreviewSpeedMeter;
    private ImageView mPreviewOverspeed;
    private AnimationDrawable mPreviewAnimDrawableOverspeed;

    private ImageView mPreviewSteadySpeedLamp;
    private ImageView mPreviewIdleLamp;
    private ImageView mPreviewHarshAccelLamp;
    private ImageView mPreviewHarshBrakeLamp;
    private TableLayout mPreviewLampControl;
    private ToggleButton mPreviewOsdBtn;


    private int mPreviewSpeedControlShown = -1;

    boolean mIsAlreadyPreviewOverspeedAnimating = false;

    private boolean mShowDetailInfo;

    private boolean mLaunchNavi;
    private View mContentView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

       // AutoStartManager.stopAutoStartService(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driving);

        if (Build.VERSION_CODES.LOLLIPOP <= Build.VERSION.SDK_INT) {
            getWindow().setStatusBarColor(Color.BLACK);
        }

        View mContentView = findViewById(android.R.id.content);



        //mFuelType = FuelType.GASOLINE;

        mErsBtn = findViewById(R.id.iv_btn_ers);
        mErsBtn.setEnabled(false);
        mErsBtn.setOnClickListener(this);
        ImageView mPreviewBtn = findViewById(R.id.iv_btn_preview);
        mPreviewBtn.setOnClickListener(this);

        ImageButton mDiagButton = findViewById(R.id.btn_diagnostic);
        mDiagButton.setEnabled(true);
        mDiagButton.setOnClickListener(this);
        findViewById(R.id.iv_btn_exit).setOnClickListener(this);
        findViewById(R.id.btn_quick_launch_navi).setOnClickListener(this);
        findViewById(R.id.detail_info_btn).setOnClickListener(this);


        FragmentManager fm = getSupportFragmentManager();
        mIndicatorFragment = (IndicatorFragment) fm.findFragmentById(R.id.center_fragment);
        mObdFragment = (ObdClusterFragment) fm.findFragmentById(R.id.obd_fragment);
        mTripFragment = (TripClusterFragment) fm.findFragmentById(R.id.trip_fragment);
        mBottomFragment = (BottomFragment) fm.findFragmentById(R.id.bottom_fragment);
        mDetailInfoFragment = (DrivingDetailInfoFragment) fm.findFragmentById(R.id.detail_info_fragment);

        mStoreItem=SettingsStore.getInstance();
        boolean mIsNewDriving;
        if (savedInstanceState == null) {
            mIsNewDriving = true;
            mLaunchNavi = true;

        } else {
            mIsNewDriving = false;
            mShowDiagnosticDialog =  savedInstanceState.getBoolean("show-diagnostic", true);
            mShowDetailInfo = savedInstanceState.getBoolean("is-detail-info-shown", false);
            mLastMil = savedInstanceState.getBoolean("last-mil", false);
        }

        // Set volume control

//        mGreenDrawable = getResources().getDrawable(R.drawable.color_deepgreen_thick);
//        mYellowDrawable= getResources().getDrawable(R.drawable.color_yellow_thick);
//        mRedDrawable = getResources().getDrawable(R.drawable.color_red_thick);
        btAdapter=BluetoothAdapter.getDefaultAdapter();
        if(btAdapter.isEnabled()) {
            if (mStoreItem.getObdAddress() != null) {

                chosenDeviceAddress = mStoreItem.getObdAddress();
                Toast.makeText(this, mStoreItem.getObdAddress(), Toast.LENGTH_SHORT).show();
                startAndBindVehicleService();
                registerVehicleReceiver();
            } else {
                Toast.makeText(this, "Не выбрано устройство ОБД", Toast.LENGTH_SHORT).show();
                chooseBluetoothDevice();
            }
        } else{
            btAdapter.enable();
            chooseBluetoothDevice();
        }

    }

    private void launchNaviAppIfNeeded() {

    }

    protected void launchAppNavi(String appId) {


    }

    @Override
    protected void onDestroy() {

        stopService(new Intent(getApplicationContext(), FloatingHeadService.class));

        unregisterVehicleReceiver();

        unbindVehicleService();


        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("show-diagnostic", mShowDiagnosticDialog);
        outState.putBoolean("is-detail-info-shown", mIsDetailInfoShown);
        outState.putBoolean("last-mil", mLastMil);
    }

    @Override
    public void finish() {
        super.finish();

        overridePendingTransition(R.anim.fade_in, R.anim.slide_out_down);
    }

    @Override
    protected void onPause() {
        if (!isFinishing()) {
            if (mVehicleService != null) {
                mVehicleService.startDataBackup();
            }

            if (!mIsShowDiagnosticActivity) {
                Intent service = new Intent(getApplicationContext(), FloatingHeadService.class);
                service.putExtra(FloatingHeadService.EXTRA_INTENT, "com.dashboard.obd.intent.ACTION_LAUNCH_DRIVING");
                startService(service);
            }

        }


        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mIsShowDiagnosticActivity = false;
        stopService(new Intent(getApplicationContext(), FloatingHeadService.class));

        if (mVehicleService != null) {
            mVehicleService.stopDataBackup();
        }

        if (mShowDetailInfo) {
            new Handler().post(this::toggleDetailInfo);
        }
    }

    @Override
    public void onBackPressed() {
      /*  if (mVehicleService != null && mDrivingService != null) {
            // Show exit dialog
            mDrivingService.showExitDialog();
        }*/
        stopService(new Intent(getApplicationContext(), VehicleService.class));
        //unbindVehicleService();
        unregisterVehicleReceiver();


        //super.onBackPressed();
        super.finish();

    }


    private  final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                int requestCode;
                requestCode=Integer.parseInt(result.getData().getStringExtra("requestCode"));


                //super.onActivityResult(requestCode, resultCode, data);
                if (requestCode == ENABLE_BT_REQUEST) {
                    if (result.getResultCode() == RESULT_OK) {
                        continueBluetooth();
                    }
                    if (result.getResultCode() == RESULT_CANCELED) {
                        Toast.makeText(DrivingActivity.this, "Application requires Bluetooth enabled", Toast.LENGTH_LONG).show();
                    }
                }

            });

    private void startAndBindVehicleService() {
        //Toast.makeText(this,"Стартует Сервис", Toast.LENGTH_LONG).show();
        startService(new Intent(DrivingActivity.this, VehicleService.class));

        //Toast.makeText(this,"Стартует Бинд", Toast.LENGTH_LONG).show();
        bindService(new Intent(DrivingActivity.this, VehicleService.class), mVehicleServiceConnection, 0);
    }

    private void unbindVehicleService() {
        try {
            unbindService(mVehicleServiceConnection);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();

        }
        mVehicleService = null;
    }

    private final ServiceConnection mVehicleServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder binder) {
            mVehicleService = ((VehicleService.VehicleServiceBinder) binder).getService();
            Toast.makeText(DrivingActivity.this,"Service Connection",Toast.LENGTH_LONG).show();
            mVehicleService.resetVehiclePersistData("Capa_001");
            mVehicleService.disconnectVehicle();

            //connect();
            onVehicleEngineStatusChanged(mVehicleService.getVehicleEngineStatus());
            if (chosenDeviceAddress==null) {
                Toast.makeText(DrivingActivity.this, "Нет Блютуса подключаем", Toast.LENGTH_LONG).show();
                chooseBluetoothDevice();
            }
                else {
                Toast.makeText(DrivingActivity.this, chosenDeviceAddress, Toast.LENGTH_LONG).show();
                mVehicleService.connectVehicle(chosenDeviceAddress,true);

                registerVehicleReceiver();
            }


        }

        public void onServiceDisconnected(ComponentName name) {
        }
    };

    private void registerVehicleReceiver() {
        mBroadcastManager=LocalBroadcastManager.getInstance(this);
       unregisterVehicleReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(VehicleDataBroadcaster.ACTION_OBD_STATE_CHANGED);
        filter.addAction(VehicleDataBroadcaster.ACTION_VEHICLE_ENGINE_STATUS_CHANGED);
        filter.addAction(VehicleDataBroadcaster.ACTION_OBD_DATA_RECEIVED);
        filter.addAction(VehicleDataBroadcaster.ACTION_OBD_EXTRA_DATA_RECEIVED);
        filter.addAction(VehicleDataBroadcaster.ACTION_OBD_CANNOT_CONNECT);
        filter.addAction(VehicleDataBroadcaster.ACTION_OBD_DEVICE_NOT_SUPPORTED);
        filter.addAction(VehicleDataBroadcaster.ACTION_OBD_PROTOCOL_NOT_SUPPORTED);
        filter.addAction(VehicleDataBroadcaster.ACTION_OBD_BUSINIT_ERROR);
        filter.addAction(VehicleDataBroadcaster.ACTION_OBD_NO_DATA);

        filter.addAction(DrivingService.ACTION_GOTO_EXIT);
        filter.addAction(DrivingService.ACTION_GOTO_HOME);
        filter.addAction(DrivingService.ACTION_GOTO_PAUSE);
        filter.addAction(DrivingService.ACTION_READY_TO_EXIT);
        filter.addAction(DrivingService.ACTION_ERS_TARGET_CHANGED);
        filter.addAction(DrivingService.ACTION_DIALOG_DISMISSED);

//        filter.addAction(DrivingService.ACTION_GOTO_DIAGNOSTIC);
        filter.addAction(VehicleDataBroadcaster.ACTION_OBD_CLEAR_STORED_DTC);
        //filter.addAction(ImpactDetector.ACTION_IMPACT_DETECTED);
        if (mVehicleReceiver!=null) {
            mBroadcastManager.registerReceiver(mVehicleReceiver, filter);
        }else Toast.makeText(DrivingActivity.this,"Бродкаст Чета пустой",Toast.LENGTH_LONG).show();
    }

    private void unregisterVehicleReceiver() {
        try {
            mBroadcastManager.unregisterReceiver(mVehicleReceiver);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private final BroadcastReceiver mVehicleReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {

            if (isFinishing()) return;
            final String action = intent.getAction();

            if (VehicleDataBroadcaster.ACTION_OBD_STATE_CHANGED.equals(action)) {
                OnObdStateListener.ObdState obdState = (OnObdStateListener.ObdState) intent.getSerializableExtra(VehicleDataBroadcaster.EXTRA_OBD_STATE);
                onObdStateChanged(obdState);
            } else if (VehicleDataBroadcaster.ACTION_VEHICLE_ENGINE_STATUS_CHANGED.equals(action)) {
                int ves = intent.getIntExtra(VehicleDataBroadcaster.EXTRA_VEHICLE_ENGINE_STATUS,
                        VehicleEngineStatus.UNKNOWN);
                onVehicleEngineStatusChanged(ves);
            } else if (VehicleDataBroadcaster.ACTION_OBD_DATA_RECEIVED.equals(action)) {
                ObdData obdData = (ObdData) intent.getSerializableExtra(VehicleDataBroadcaster.EXTRA_OBD_DATA);
                onObdDataReceived(obdData);
            } else if (VehicleDataBroadcaster.ACTION_OBD_EXTRA_DATA_RECEIVED.equals(action)) {
                float rpm = intent.getFloatExtra(VehicleDataBroadcaster.EXTRA_RPM, -1);
                int vss = intent.getIntExtra(VehicleDataBroadcaster.EXTRA_VSS, -1);
                onObdExtraDataReceived(rpm, vss);
            } else if (VehicleDataBroadcaster.ACTION_OBD_CANNOT_CONNECT.equals(action)) {
                BluetoothDevice obdDevice = intent.getParcelableExtra(VehicleDataBroadcaster.EXTRA_OBD_DEVICE);
                boolean isBlocked = intent.getBooleanExtra(VehicleDataBroadcaster.EXTRA_OBD_BLOCKED, false);
                onObdCannotConnect(obdDevice, isBlocked);
            } else if (VehicleDataBroadcaster.ACTION_OBD_DEVICE_NOT_SUPPORTED.equals(action)) {
                BluetoothDevice obdDevice = intent.getParcelableExtra(VehicleDataBroadcaster.EXTRA_OBD_DEVICE);
                onObdDeviceNotSupported(obdDevice);
            } else if (VehicleDataBroadcaster.ACTION_OBD_PROTOCOL_NOT_SUPPORTED.equals(action)) {
                BluetoothDevice obdDevice = intent.getParcelableExtra(VehicleDataBroadcaster.EXTRA_OBD_DEVICE);
                onObdProtocolNotSupported(obdDevice);
            } else if (VehicleDataBroadcaster.ACTION_OBD_BUSINIT_ERROR.equals(action)) {
                Logger.getLogger(TAG).warn("BUSINIT_ERROR#" + intent.getIntExtra(VehicleDataBroadcaster.EXTRA_OBD_PROTOCOL, -1));
            } else if (VehicleDataBroadcaster.ACTION_OBD_NO_DATA.equals(action)) {
                Logger.getLogger(TAG).warn("No data!!");
            } else if (DrivingService.ACTION_GOTO_EXIT.equals(action)) {
                onGogoExit();
            } else if (DrivingService.ACTION_GOTO_HOME.equals(action)) {
                onGotoHome();
            } else if (DrivingService.ACTION_GOTO_PAUSE.equals(action)) {
                onGotoPause();
            } else if (DrivingService.ACTION_READY_TO_EXIT.equals(action)) {
                onReadyToExit();
            } /*else if (DrivingService.ACTION_ERS_TARGET_CHANGED.equals(action)) {
                ErsTarget target = (ErsTarget) intent.getSerializableExtra(DrivingService.EXTRA_ERS_TARGET);
                boolean userSelect = intent.getBooleanExtra(DrivingService.EXTRA_USER_SELECT, false);
                onErsTargetChanged(target, userSelect);
            } */
            else if (DrivingService.ACTION_DIALOG_DISMISSED.equals(action)) {
                Log.i(TAG, "onReceive#" + action);
                int type = intent.getIntExtra(DrivingService.EXTRA_DIALOG_TYPE, -1);
                onDialogDismiss(type);
            }/*else if (DrivingService.ACTION_GOTO_DIAGNOSTIC.equals(action)) {
                Log.i(TAG, "onReceive#" + action);
//                mLaunchNavi = false;
                startDiagnosticActivity(mDtc, false);
            }*/
            else if (VehicleDataBroadcaster.ACTION_OBD_CLEAR_STORED_DTC.equals(action)) {
                if (mVehicleService != null) {
                    mVehicleService.clearStoredDTC();
                }
            }

        }
    };

    private boolean mIsShowDiagnosticActivity;
    protected void startDiagnosticActivity(String dtc, boolean showDialog) {
        Log.w(TAG, "startDiagnosticActivity#" + dtc);
        mIsShowDiagnosticActivity = true;
        Intent i = new Intent(getApplicationContext(), DiagnosticActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra("dtc", dtc);
        i.putExtra("show-dialog", showDialog);
        startActivity(i);
    }
    private boolean mIsShowMapActivity;
    protected void startMapActivity( ) {
        Log.w(TAG, "startMapActivity#");
        mIsShowDiagnosticActivity = true;
        Intent i = new Intent(this, MapActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }
    private void onObdStateChanged(ObdState obdState) {


        mIndicatorFragment.onObdStateChanged(obdState);
        mObdFragment.onObdStateChanged(obdState);
        mTripFragment.onObdStateChanged(obdState);

        if (obdState == ObdState.SCANNING) {
            if (mDrivingService != null) {
                mDrivingService.dismissObdRecoveryDialog();
            }
        }
        if (obdState==ObdState.CONNECTED&& mStoreItem.getObdAddress()==null)
        {

            mStoreItem.storeObdAddress(chosenDeviceAddress);
        }
    }

    private int mVes = VehicleEngineStatus.UNKNOWN;
   private void onVehicleEngineStatusChanged(int ves) {
       // logger.debug("onVehicleEngineStatusChanged#" + VehicleEngineStatus.toString(ves) + " < " + VehicleEngineStatus.toString(mVes));

        if (mDrivingService != null) {
            mDrivingService.dismissObdRecoveryDialog();
        }

        if (VehicleEngineStatus.isOnDriving(ves) && !VehicleEngineStatus.isOnDriving(mVes)) {
            moveTaskToFront();

            if (mVehicleService != null && mDrivingService != null) {
                mDrivingService.dismissDrivingOffDialog();

                if (mIsDrivingPaused) {
                    mIsDrivingPaused = false;

                    mVehicleService.resumeDriving();
                    mDrivingService.resumeDriving();

                    // Resume blackbox if needed
                    // FIXME: Need delay for screen rotation

                }
            }
        } else if (VehicleEngineStatus.isOffDriving(ves)) {
            moveTaskToFront();

            if (mVehicleService != null && mDrivingService != null) {
                if (!mDrivingService.isExitDialogShowing() && !mDrivingService.isDrivingOffDialogShowing()
                        && !mDrivingService.isWaitForExitDialogShowing()) {
                    boolean engineOnDetectionEnabled = isEngineOn();
                    if (!engineOnDetectionEnabled) mDrivingService.showDrivingOffDialog();

                        // Disable impact detector (will be enabled when driving off dialog dismissed)
                       // mVehicleService.setImpactDetectorEnabled(false);

                }
            }
        }

        mVes = ves;
    }

    private boolean mShowDiagnosticDialog = true;
    private boolean mLastMil = false;
    protected String mDtc;

    private void onObdDataReceived(ObdData data) {
        mIndicatorFragment.onObdDataReceived(data);
        mObdFragment.onObdDataReceived(data);
        mTripFragment.onObdDataReceived(data);
        mBottomFragment.onObdDataReceived(data);

        mDtc = getDTC(data);
        if (mDtc!=null) Toast.makeText(this, mDtc, Toast.LENGTH_SHORT).show();
        if (mShowDiagnosticDialog) {
            if (mDrivingService != null) {
                showDiagnosticProcess(mDtc);
                mShowDiagnosticDialog = false;
                mLastMil = isMilOn(data);
            }
        } else if (isMilOn(data) && !mLastMil) {
            mLastMil = true;
            showDiagnosticProcess(mDtc);
        }

        if (mIsDetailInfoShown) {
            mDetailInfoFragment.onObdDataReceived(data);
        }
    }

    String getDTC(ObdData data) {
        return data.getBoolean(KEY.SAE_MIL, false) ? data.getString(KEY.SAE_DTC) : null;
    }

    boolean isMilOn(ObdData data) {
        return !TextUtils.isEmpty(getDTC(data));
    }

    private void showDiagnosticProcess(String dtc) {
        if (mDrivingService != null) {
            mDrivingService.showDiagnosticProcess(dtc);
        }
    }




    int mLastVss = -1;
    float mLastRpm = -1;
    private void onObdExtraDataReceived(float rpm, int vss) {
        mObdFragment.onObdExtraDataReceived(rpm, vss);
        mTripFragment.onObdExtraDataReceived(rpm, vss);

        if (mIsDetailInfoShown) {
            mDetailInfoFragment.onObdExtraDataReceived(rpm, vss);
        }

        mLastVss = vss ;
        mLastRpm = rpm;

        runOnUiThread(mUpdateVssRunnable);
    }

    private final Runnable mUpdateVssRunnable = new Runnable() {
        public void run() {

            if(mPreviewSpeedMeter != null) {
                mPreviewSpeedMeter.setValueText(String.valueOf(mLastVss));
            }

            if (mIsDetailInfoShown) {
                mDetailInfoFragment.onObdExtraDataReceived(mLastRpm, mLastVss);
            }

            if(mLastRpm > 0 && mLastVss > 0) {
                animatePreviewSpeedControl(View.VISIBLE);
            }else {
                animatePreviewSpeedControl(View.GONE);
            }

        }
    };

    private void onObdCannotConnect(BluetoothDevice obdDevice, boolean isBlocked) {
//        logger.debug("onObdCannotConnect(): isBlocked=" + isBlocked);

        mIndicatorFragment.onObdCannotConnect();
        mObdFragment.onObdCannotConnect();
        mTripFragment.onObdCannotConnect();
        mBottomFragment.onObdCannotConnect();

//        logger.debug("onObdCannotConnect#" + mVehicleService);
        // Retry!
        if (mVehicleService != null) {
            mVehicleService.disconnectVehicle();

            if (obdDevice.getAddress()!=null) {
                mVehicleService.connectVehicle(chosenDeviceAddress);//chosenDeviceAddress

            }
        }

        if (isBlocked) {
            Logger.getLogger(TAG).info("device is blocked@onObdCannotConnect");
           if (mDrivingService != null) {
                mDrivingService.showObdRecoveryDialog();
            }
        }
    }

    private void onObdDeviceNotSupported(BluetoothDevice obdDevice) {

        onObdCannotConnect(obdDevice, false);
    }

    private void onObdProtocolNotSupported(BluetoothDevice obdDevice) {

        onObdCannotConnect(obdDevice, false);
    }

    private void onGogoExit() {
      //  logger.debug("onGogoExit()");

        if (mVehicleService != null) {
            mVehicleService.disconnectVehicle();
        }

        Intent result = new Intent();
        result.putExtra(EXTRA_REQUEST_EXIT, true);
        setResult(RESULT_OK, result);

        waitForExit();
    }

    private void onGotoHome() {
        //log.debug("onGotoHome()");

        // Home button
        Intent result = new Intent();
        if (mVehicleService != null) {
            result.putExtra(EXTRA_VES, mVehicleService.getVehicleEngineStatus());
        }
        setResult(RESULT_OK, result);

        waitForExit();
    }

    private void onGotoPause() {
       // logger.debug("onGotoPause()");

        // Pause button
        moveTaskToBack(true);

        if (!mIsDrivingPaused) {
            mIsDrivingPaused = true;

            if (mVehicleService != null) {
                mVehicleService.pauseDriving();
            }
            if (mDrivingService != null) {
                mDrivingService.pauseDriving();
            }

        }
    }

    private void onReadyToExit() {


        unregisterVehicleReceiver();

        // Finish at next thread time
        mContentView.post(this::finish);
    }



    private void onDialogDismiss(int type) {
       // logger.debug("onDialogDismiss(): type=" + type);


    }

    private void moveTaskToFront() {
       if (isPaused()) {
            Intent bringToFront = new Intent(BringToFrontReceiver.ACTION_BRING_TO_FRONT);
            sendBroadcast(bringToFront);
        }
    }



    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_btn_ers ) {
//            if (mBlackboxControlFragment != null) {
//                mBlackboxControlFragment.onEmergency();
//            } else {
//                if (mVehicleService != null) {
//                    mVehicleService.onEmergency();
//                }
//            }

        /*    if (mVehicleService != null) {
                mVehicleService.onEmergency();
            }*/

            // Show ERS dialog

        }  /*else if (id == R.id.btn_car_monitor) {
            if (mDrivingService != null) {
                BlackboxPreview preview = mDrivingService.getBlackboxPreview();
                preview.setVisibility(View.GONE);
            }
        }*/ else if (id == R.id.detail_info_btn) {
            toggleDetailInfo();
        }else if (id==R.id.iv_btn_preview)
        {
            chooseBluetoothDevice();

        }else if (id==R.id.btn_diagnostic)
        {
            startDiagnosticActivity(mDtc, false);
        }else if (id==R.id.iv_btn_exit)
        {
           finish();
        }else if (id==R.id.btn_quick_launch_navi)
        {
            Toast.makeText(this, "Press Navy Button", Toast.LENGTH_LONG).show();
            startMapActivity();
        }

    }
    private void chooseBluetoothDevice(){
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if(btAdapter == null){
            Toast.makeText(this, "Device doesn't support Bluetooth", Toast.LENGTH_LONG).show();
            return;
        }
        if(!btAdapter.isEnabled()){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            StartActivityForResults.startActivityForResults(activityResultLauncher,enableBtIntent, ENABLE_BT_REQUEST);
        } else{
            continueBluetooth();
        }
    }
    private void continueBluetooth(){
        final ArrayList<String> pairedDevicesNames = new ArrayList<>();
        final ArrayList<String> pairedDevicesAddresses = new ArrayList<>();

        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                pairedDevicesNames.add(device.getName());
                pairedDevicesAddresses.add(device.getAddress());
            }

            final String[] devicesString = pairedDevicesNames.toArray(new String[0]);
            final String[] devicesAddress = pairedDevicesAddresses.toArray(new String[0]);


            AlertDialog.Builder mBuilder = new AlertDialog.Builder(this,R.style.MyDialog);

            mBuilder.setTitle("Choose OBD device:");

            mBuilder.setSingleChoiceItems(devicesString, -1, (dialog, i) -> {
                dialog.dismiss();
                int position = ((AlertDialog) dialog).getListView().getCheckedItemPosition();


                chosenDeviceAddress = pairedDevicesAddresses.get(position);
                chosenDeviceName = pairedDevicesNames.get(position);
                Toast.makeText(this, "Chosen: " + chosenDeviceName, Toast.LENGTH_SHORT).show();


                mErsBtn.setEnabled(true);
                if (chosenDeviceAddress!=null) {

                    startAndBindVehicleService();
                    registerVehicleReceiver();
                }
            });

            AlertDialog mDialog = mBuilder.create();
            mDialog.show();

        } else{
            Toast.makeText(this, "No paired devices found", Toast.LENGTH_SHORT).show();
        }
        if (chosenDeviceAddress!=null) {

            startAndBindVehicleService();
            registerVehicleReceiver();
        }
    }
    private void toggleDetailInfo() {
        if (mIsDetailInfoAnimating) {
            return;
        }

        if (mIsDetailInfoShown && !mIsDetailInfoAnimating) {
            mIsDetailInfoShown = false;

            AnimatorSet animSet = new AnimatorSet();
            animSet.setInterpolator(new DecelerateInterpolator());
            animSet.setDuration(500);

            View leftView = mObdFragment.getView();
            ObjectAnimator leftAnim = ObjectAnimator.ofFloat(leftView, "translationX", 0);

            View rightView = mTripFragment.getView();
            ObjectAnimator rightAnim = ObjectAnimator.ofFloat(rightView, "translationX", 0);

            View centerView1 = mIndicatorFragment.getView().findViewById(R.id.indicator_pane);
            ObjectAnimator centerAnim1 = ObjectAnimator.ofFloat(centerView1, "alpha", 1);
//            View centerView2 = mIndicatorFragment.getView().findViewById(R.id.trip_pane);
//            ObjectAnimator centerAnim2 = ObjectAnimator.ofFloat(centerView2, "alpha", 1);

            View detailInfoView = mDetailInfoFragment.getView();
            ObjectAnimator detailInfoAnim = ObjectAnimator.ofFloat(detailInfoView, "alpha", 0);

            animSet.playTogether(leftAnim, rightAnim, centerAnim1, /*centerAnim2,*/ detailInfoAnim);
            animSet.addListener(new Animator.AnimatorListener() {
                public void onAnimationRepeat(Animator animation) {}
                public void onAnimationCancel(Animator animation) {}

                @Override
                public void onAnimationEnd(Animator animation) {
                    mIsDetailInfoAnimating = false;

                    FragmentManager fm = getSupportFragmentManager();
                    fm.beginTransaction().hide(mDetailInfoFragment).commitAllowingStateLoss();

                    ((CheckBox)findViewById(R.id.detail_info_btn)).setChecked(false);

                }

                @Override
                public void onAnimationStart(Animator animation) {
                    mIsDetailInfoAnimating = true;

                }
            });
            animSet.start();

        } else if (!mIsDetailInfoShown && !mIsDetailInfoAnimating) {
            mIsDetailInfoShown = true;

            AnimatorSet animSet = new AnimatorSet();
            animSet.setInterpolator(new DecelerateInterpolator());
            animSet.setDuration(500);

            View leftView = mObdFragment.getView();
            ObjectAnimator leftAnim = ObjectAnimator.ofFloat(leftView, "translationX", -leftView.getWidth());

            View rightView = mTripFragment.getView();
            ObjectAnimator rightAnim = ObjectAnimator.ofFloat(rightView, "translationX", rightView.getWidth());

           // Logger.getLogger(TAG).trace("toggleDetailInfo#" + leftView.getWidth());

            View centerView1 = mIndicatorFragment.getView().findViewById(R.id.indicator_pane);
            ObjectAnimator centerAnim1 = ObjectAnimator.ofFloat(centerView1, "alpha", 0);
//            View centerView2 = mIndicatorFragment.getView().findViewById(R.id.trip_pane);
//            ObjectAnimator centerAnim2 = ObjectAnimator.ofFloat(centerView2, "alpha", 0);

            if (isPortrait()) {
                centerAnim1 = ObjectAnimator.ofFloat(centerView1, "alpha", 1);
//                centerAnim2 = ObjectAnimator.ofFloat(centerView2, "alpha", 1);
            }

            View detailInfoView = mDetailInfoFragment.getView();
            ObjectAnimator detailInfoAnim = ObjectAnimator.ofFloat(detailInfoView, "alpha", 1);

            animSet.playTogether(leftAnim, rightAnim, centerAnim1, /*centerAnim2,*/ detailInfoAnim);
            animSet.addListener(new Animator.AnimatorListener() {
                public void onAnimationStart(Animator animation) {
                    mIsDetailInfoAnimating = true;

                    FragmentManager fm = getSupportFragmentManager();
                    fm.beginTransaction().show(mDetailInfoFragment).commitAllowingStateLoss();
                }

                public void onAnimationEnd(Animator animation) {
                    mIsDetailInfoAnimating = false;

                    ((CheckBox)findViewById(R.id.detail_info_btn)).setChecked(true);
                }

                public void onAnimationRepeat(Animator animation) {}
                public void onAnimationCancel(Animator animation) {}
            });
            animSet.start();
        }
    }



    private void waitForExit() {

    }


    // Called by IndicatorFragment or DrivingDetailInfoFragment
    public void clearStoredDTC() {
        if (mVehicleService != null /*&& mIndicatorFragment.isMilOn()*/) {
          mVehicleService.clearStoredDTC();

            Toast.makeText(this, R.string.driving_clear_stored_dtc, Toast.LENGTH_LONG).show();
        }
    }


    private void animatePreviewSpeedControl(int visible) {
        if(visible == View.VISIBLE) {
            if(mPreviewSpeedControl != null && mPreviewSpeedControlShown != View.VISIBLE) {
                View v = mPreviewSpeedControl;
                //mPreviewSpeedControl.setVisibility(View.VISIBLE);
                ObjectAnimator leftAnim = ObjectAnimator.ofFloat(v, "translationX", 0);
                leftAnim.setDuration(500);
//				leftAnim.addListener(new AnimatorListener() {
//					public void onAnimationRepeat(Animator animation) {}
//					public void onAnimationCancel(Animator animation) {}
//
//					@Override
//					public void onAnimationEnd(Animator animation) {
//						mIsSpeedControlAnimating = false;
//					}
//
//					@Override
//					public void onAnimationStart(Animator animation) {
//						mIsSpeedControlAnimating = true;
//
//					}
//				});
                leftAnim.start();
                mPreviewSpeedControlShown = View.VISIBLE;
            }
        } else if(visible == View.GONE) {
            if(mPreviewSpeedControl != null && mPreviewSpeedControlShown != View.GONE) {
                View v = (View) mPreviewSpeedControl;
                int mPreviewSpeedControlW = 0;
                ObjectAnimator leftAnim = ObjectAnimator.ofFloat(v, "translationX", -mPreviewSpeedControlW);
                leftAnim.setDuration(500);
                leftAnim.addListener(new Animator.AnimatorListener() {
                    public void onAnimationRepeat(Animator animation) {}
                    public void onAnimationCancel(Animator animation) {}

                    @Override
                    public void onAnimationEnd(Animator animation) {
//						mIsSpeedControlAnimating = false;
//                        animatePreviewAdsControl(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationStart(Animator animation) {
//						mIsSpeedControlAnimating = true;
                    }
                });
                leftAnim.start();
                mPreviewSpeedControlShown = View.GONE;
            }
        }
    }




    private void setHarshAccelLamp(boolean enabled) {
        if (enabled) {
            if (mPreviewHarshAccelLamp.isEnabled()) {
                mPreviewHarshAccelLamp.removeCallbacks(HarshAccelLampOffRunnable);
            }
            mPreviewHarshAccelLamp.setEnabled(true);
            mPreviewHarshAccelLamp.postDelayed(HarshAccelLampOffRunnable, 5000);
//            if (mTripFragment != null) {
//                mTripFragment.setHarshAccelLamp(true);
//            }

            if (mPreviewHarshBrakeLamp.isEnabled()) {
                mPreviewHarshBrakeLamp.removeCallbacks(HarshBrakeLampOffRunnable);
                mPreviewHarshBrakeLamp.post(HarshBrakeLampOffRunnable);
            }
        }
    }


    private final Runnable HarshAccelLampOffRunnable = new Runnable() {

        @Override
        public void run() {
            mPreviewHarshAccelLamp.setEnabled(false);
//            if (mTripFragment != null) {
//                mTripFragment.setHarshAccelLamp(false);
//            }
        }
    };

    private void setHarshBrakeLamp(boolean enabled) {
        if (enabled) {
            if (mPreviewHarshBrakeLamp.isEnabled()) {
                mPreviewHarshBrakeLamp.removeCallbacks(HarshBrakeLampOffRunnable);
            }
            mPreviewHarshBrakeLamp.setEnabled(true);
            mPreviewHarshBrakeLamp.postDelayed(HarshBrakeLampOffRunnable, 5000);
//            if (mTripFragment != null) {
//                mTripFragment.setHarshBrakeLamp(true);
//            }

            if (mPreviewHarshAccelLamp.isEnabled()) {
                mPreviewHarshAccelLamp.removeCallbacks(HarshAccelLampOffRunnable);
                mPreviewHarshAccelLamp.post(HarshAccelLampOffRunnable);
            }
        }
    }

    private final Runnable HarshBrakeLampOffRunnable = new Runnable() {

        @Override
        public void run() {
            mPreviewHarshBrakeLamp.setEnabled(false);
//            if (mTripFragment != null) {
//                mTripFragment.setHarshBrakeLamp(false);
//            }
        }
    };

    private FuelType mFuelType;

    protected int calcEcoLevel(ObdData data) {
        int ecoLevel = 0;

        if (mFuelType == FuelType.GASOLINE || mFuelType == FuelType.LPG) {
            if (data.isValid(KEY.SAE_VSS) && data.isValid(KEY.SAE_LOAD_PCT)) {
                int vss = data.getInteger(KEY.SAE_VSS);
                if (vss > 0) {
                    float loadPct = data.getFloat(KEY.SAE_LOAD_PCT);
                    if (loadPct < 50) {
                        ecoLevel = 1;
                    } else if (50 <= loadPct && loadPct < 80) {
                        ecoLevel = 2;
                    } else if (80 <= loadPct && loadPct < 90) {
                        ecoLevel = 3;
                    } else if (90 <= loadPct) {
                        ecoLevel = 4;
                    }
                }
            }
        } else if (mFuelType == FuelType.DIESEL) {
            if (data.isValid(KEY.SAE_VSS) && data.isValid(KEY.SAE_MAP)) {
                int vss = data.getInteger(KEY.SAE_VSS);
                if (vss > 0) {
                    int map = data.getInteger(KEY.SAE_MAP);
                    int baro = data.getInteger(KEY.SAE_BARO, 100);
                    int mapBaroDiff = (map - baro);

                    if (mapBaroDiff < 50) {
                        ecoLevel = 1;
                    } else if (mapBaroDiff < 80) {
                        ecoLevel = 2;
                    } else if (mapBaroDiff < 100) {
                        //by jake 09.16 (90->100)
                        ecoLevel = 3;
                    } else {
                        ecoLevel = 4;
                    }
                }
            }
        }

        return ecoLevel;
    }

    private void animatePreviewOverspeedWarning(boolean isAnimate) {
        if (isAnimate) {
            if(!mIsAlreadyPreviewOverspeedAnimating) {
                mPreviewOverspeed.setVisibility(View.VISIBLE);
                ViewCompatUtils.setBackground(mPreviewOverspeed, mPreviewAnimDrawableOverspeed);
                mPreviewAnimDrawableOverspeed.start();
                mIsAlreadyPreviewOverspeedAnimating = true;
            }
        } else {
            if(mIsAlreadyPreviewOverspeedAnimating) {
                mIsAlreadyPreviewOverspeedAnimating = false;
                mPreviewAnimDrawableOverspeed.stop();
            }
            mPreviewOverspeed.setVisibility(View.GONE);
        }
    }

    private void setAlpha(int alpha) {
        if (isLandscape()) {

        }
    }

    private boolean isLandscape() {
        return getWindowManager().getDefaultDisplay().getWidth() > getWindowManager().getDefaultDisplay().getHeight();
    }

    private boolean isPortrait() {
        return getWindowManager().getDefaultDisplay().getWidth() < getWindowManager().getDefaultDisplay().getHeight();
    }

    private int getScreenOrientation() {
        return getWindowManager().getDefaultDisplay().getRotation();

    }

    protected boolean isEngineOn() {
        return true;
    }


    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
    }



    public boolean isPaused() {
        return mIsPaused;
    }
    private void startFloatingHeadIfNeeded() {
      /*  if (mSettingsStore.isFloatingWindowEnabled() && !mIsShowDiagnosticActivty) {
            Intent service = new Intent(getApplicationContext(), FloatingHeadService.class);
            service.putExtra(FloatingHeadService.EXTRA_INTENT, "com.pokevian.intent.ACTION_LAUNCH_DRIVING");
            startService(service);
//                mDrivingService.startFloatingHead("com.pokevian.intent.ACTION_LAUNCH_DRIVING");
        }*/
    }

}
