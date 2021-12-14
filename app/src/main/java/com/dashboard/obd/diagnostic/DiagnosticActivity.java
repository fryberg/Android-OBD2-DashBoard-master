package com.dashboard.obd.diagnostic;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.dashboard.obd.service.VehicleDataBroadcaster;
import com.dashboard.obd.floatinghead.FloatingHeadService;

import com.dashboard.obd.R;


import org.apache.log4j.Logger;
/**
 * Created by fry on 2021-12-05.
 */
public class DiagnosticActivity extends AppCompatActivity implements DialogFragmentInterface.OnClickListener {
    public static final String TAG = "DiagnosticActivity";

    private Handler mHandler;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.dialog_diagnostic);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2e2e2e")));
        if (Build.VERSION_CODES.LOLLIPOP <= Build.VERSION.SDK_INT) {
            getWindow().setStatusBarColor(Color.parseColor("#2e2e2e"));
        }

        mHandler = new Handler();

        String dtc = null;
        boolean showDialog = false;
        if (getIntent() != null) {
            dtc = getIntent().getExtras().getString("dtc");
            showDialog = getIntent().getExtras().getBoolean("show-dialog", false);
        }

        Logger.getLogger(TAG).trace("onCreate#" + dtc);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentByTag(DiagnosticFragment.TAG);
        if (fragment == null) {
            fm.beginTransaction().replace(R.id.container, DiagnosticFragment.newInstance(dtc, showDialog), DiagnosticFragment.TAG)
                    .commitAllowingStateLoss();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        if (!isFinishing() ) {
            Intent service = new Intent(getApplicationContext(), FloatingHeadService.class);
            service.putExtra(FloatingHeadService.EXTRA_INTENT, "com.dashboard.obd.intent.ACTION_LAUNCH_DIAGNOSTIC");
            startService(service);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        stopService(new Intent(getApplicationContext(), FloatingHeadService.class));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            FragmentManager fm = getSupportFragmentManager();
            if (fm.getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
            } else {
                finish();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Override
    public void onClick(DialogFragment fragment, int which) {
        if (DiagnosticFragment.DtcResetWarningFragment.TAG.equals(fragment.getTag())) {
            Intent data = new Intent(VehicleDataBroadcaster.ACTION_OBD_CLEAR_STORED_DTC);
            LocalBroadcastManager.getInstance(this).sendBroadcast(data);

            mHandler.postDelayed(this::finish, 1000);
        }
    }
}
