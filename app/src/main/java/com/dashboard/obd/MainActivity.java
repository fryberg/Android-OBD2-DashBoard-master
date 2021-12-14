package com.dashboard.obd;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.dashboard.obd.driving.DrivingActivity;
import com.dashboard.obd.service.SettingsStore;


public class MainActivity extends Application {






    @Override
    public void onCreate() {
        super.onCreate();

        SettingsStore.init(this);

    }




}




//If you are connecting to a Bluetooth serial board then try using the well-known SPP UUID 00001101-0000-1000-8000-00805F9B34FB. However if you are connecting to an Android peer then please generate your own unique UUID.
