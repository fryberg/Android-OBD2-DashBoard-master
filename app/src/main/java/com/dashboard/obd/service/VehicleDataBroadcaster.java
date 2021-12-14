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

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.pokevian.lib.obd2.data.ObdData;
import com.pokevian.lib.obd2.listener.OnObdStateListener.ObdState;

public class VehicleDataBroadcaster {

    public static final String ACTION_OBD_STATE_CHANGED
            = "com.dashboard.obd.action.OBD_STATE_CHANGED";
    public static final String EXTRA_OBD_STATE
            = "com.dashboard.obd.extra.OBD_STATE";
    public static final String EXTRA_OBD_STATE_EXTRA
            = "com.dashboard.obd.extra.OBD_STATE_EXTRA";

    public static final String ACTION_VEHICLE_ENGINE_STATUS_CHANGED
            = "com.dashboard.obd.action.VEHICLE_ENGINE_STATUS_CHANGED";
    public static final String EXTRA_VEHICLE_ENGINE_STATUS
            = "com.dashboard.obd.extra.VEHICLE_ENGINE_STATUS";

    public static final String ACTION_OBD_DATA_RECEIVED
            = "com.dashboard.obd.action.OBD_DATA_RECEIVED";
    public static final String EXTRA_OBD_DATA
            = "com.dashboard.obd.extra.OBD_DATA";

    public static final String ACTION_OBD_EXTRA_DATA_RECEIVED
            = "com.dashboard.obd.action.OBD_EXTRA_DATA_RECEIVED";
    public static final String EXTRA_RPM
            = "com.dashboard.obd.extra.RPM";
    public static final String EXTRA_VSS
            = "com.dashboard.obd.extra.VSS";

    public static final String ACTION_OBD_CANNOT_CONNECT
            = "com.dashboard.obd.action.OBD_CANNOT_CONNECT";
    public static final String EXTRA_OBD_DEVICE
            = "com.dashboard.obd.extra.OBD_DEVICE";
    public static final String EXTRA_OBD_BLOCKED
            = "com.dashboard.obd.extra.OBD_BLOCKED";

    public static final String ACTION_OBD_CONNECTION_FAILED
            = "com.dashboard.obd.action.OBD_CONNECTION_FAILED";
    /*public static final String EXTRA_OBD_DEVICE
            = "com.dashboard.obd.extra.OBD_DEVICE";*/
    public static final String EXTRA_OBD_CONNECTION_FAILURE_COUNT
            = "com.dashboard.obd.extra.OBD_CONNECTION_FAILURE_COUNT";

    public static final String ACTION_OBD_DEVICE_NOT_SUPPORTED
            = "com.dashboard.obd.action.OBD_DEVICE_NOT_SUPPORTED";
    /*public static final String EXTRA_OBD_DEVICE
            = "com.dashboard.obd.extra.OBD_DEVICE";*/

    public static final String ACTION_OBD_PROTOCOL_NOT_SUPPORTED
            = "com.dashboard.obd.action.OBD_PROTOCOL_NOT_SUPPORTED";
    /*public static final String EXTRA_OBD_DEVICE
            = "com.dashboard.obd.extra.OBD_DEVICE";*/

    public static final String ACTION_OBD_INSUFFICIENT_PID
            = "com.dashboard.obd.action.OBD_INSUFFICIENT_PID";

    public static final String ACTION_OBD_ENGINE_DISTANCE_SUPPORTED
            = "com.dashboard.obd.action.OBD_ENGINE_DISTANCE_SUPPORTED";
    public static final String EXTRA_OBD_SUPPORTED_FLAG
            = "com.dashboard.obd.extra.OBD_SUPPORTED_FLAG";

    public static final String ACTION_OBD_BUSINIT_ERROR
            = "com.dashboard.obd.action.OBD_BUSINIT_ERROR";
    public static final String EXTRA_OBD_PROTOCOL
            = "com.dashboard.obd.extra.OBD_PROTOCOL";

    public static final String ACTION_OBD_NO_DATA
            = "com.dashboard.obd.action.OBD_NO_DATA";



    public static final String ACTION_OBD_CLEAR_STORED_DTC = "com.dashboard.obd.action.OBD_CLEAR_STORED_DTC";

    private final LocalBroadcastManager mBroadcastManager;

    public VehicleDataBroadcaster(Context context) {
        mBroadcastManager = LocalBroadcastManager.getInstance(context);
    }

    public void onObdStateChanged(ObdState state, Parcelable extra) {
        Intent intent = new Intent(ACTION_OBD_STATE_CHANGED);
        intent.putExtra(EXTRA_OBD_STATE, state);
        intent.putExtra(EXTRA_OBD_STATE_EXTRA, extra);
        mBroadcastManager.sendBroadcast(intent);
    }

    public void onVehicleEngineStatusChanged(int ves) {
        Intent intent = new Intent(ACTION_VEHICLE_ENGINE_STATUS_CHANGED);
        intent.putExtra(EXTRA_VEHICLE_ENGINE_STATUS, ves);
        mBroadcastManager.sendBroadcast(intent);
    }

    public void onObdDataReceived(ObdData obdData) {
        Intent intent = new Intent(ACTION_OBD_DATA_RECEIVED);
        intent.putExtra(EXTRA_OBD_DATA, obdData);
        mBroadcastManager.sendBroadcast(intent);
    }

    public void onObdExtraDataReceived(float rpm, int vss) {
        Intent intent = new Intent(ACTION_OBD_EXTRA_DATA_RECEIVED);
        intent.putExtra(EXTRA_RPM, rpm);
        intent.putExtra(EXTRA_VSS, vss);
        mBroadcastManager.sendBroadcast(intent);
    }

    public void onObdCannotConnect(BluetoothDevice obdDevice, boolean isBlocked) {
        Intent i = new Intent(ACTION_OBD_CANNOT_CONNECT);
        i.putExtra(EXTRA_OBD_DEVICE, obdDevice);
        i.putExtra(EXTRA_OBD_BLOCKED, isBlocked);
        mBroadcastManager.sendBroadcast(i);
    }

    public void onObdConnectionFailed(BluetoothDevice obdDevice, int failureCount) {
        Intent intent = new Intent(ACTION_OBD_CONNECTION_FAILED);
        intent.putExtra(EXTRA_OBD_DEVICE, obdDevice);
        intent.putExtra(EXTRA_OBD_CONNECTION_FAILURE_COUNT, failureCount);
        mBroadcastManager.sendBroadcast(intent);
    }

    public void onObdDeviceNotSupported(BluetoothDevice obdDevice) {
        Intent intent = new Intent(ACTION_OBD_DEVICE_NOT_SUPPORTED);
        intent.putExtra(EXTRA_OBD_DEVICE, obdDevice);
        mBroadcastManager.sendBroadcast(intent);
    }

    public void onObdProtocolNotSupported(BluetoothDevice obdDevice) {
        Intent intent = new Intent(ACTION_OBD_PROTOCOL_NOT_SUPPORTED);
        intent.putExtra(EXTRA_OBD_DEVICE, obdDevice);
        mBroadcastManager.sendBroadcast(intent);
    }

    public void onObdInsufficientPid() {
        Intent intent = new Intent(ACTION_OBD_INSUFFICIENT_PID);
        mBroadcastManager.sendBroadcast(intent);
    }

    public void onObdBusInitError(int protocol) {
        Intent intent = new Intent(ACTION_OBD_BUSINIT_ERROR);
        intent.putExtra(EXTRA_OBD_PROTOCOL, protocol);
        mBroadcastManager.sendBroadcast(intent);
    }

    public void onObdNoData() {
        Intent intent = new Intent(ACTION_OBD_NO_DATA);
        mBroadcastManager.sendBroadcast(intent);
    }

    public void onObdEngineDistanceSupported(boolean isSupported) {
        Intent intent = new Intent(ACTION_OBD_ENGINE_DISTANCE_SUPPORTED);
        intent.putExtra(EXTRA_OBD_SUPPORTED_FLAG, isSupported);
        mBroadcastManager.sendBroadcast(intent);
    }


}
