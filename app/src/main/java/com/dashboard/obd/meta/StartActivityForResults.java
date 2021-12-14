package com.dashboard.obd.meta;

import android.content.Intent;
import androidx.activity.result.ActivityResultLauncher;

public class StartActivityForResults {
    public static void startActivityForResults(ActivityResultLauncher<Intent> result, Intent intent, int code)
    {
        intent.putExtra("requestCode",code);
        result.launch(intent);
    }
}
