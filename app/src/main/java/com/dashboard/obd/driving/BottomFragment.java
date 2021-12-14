package com.dashboard.obd.driving;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.dashboard.obd.R;
import com.pokevian.lib.obd2.data.ObdData;
import com.pokevian.lib.obd2.defs.KEY;
import com.pokevian.lib.obd2.defs.VehicleEngineStatus;




public class BottomFragment extends Fragment {
	
	private DrivingActivity mDrivingActivity;
	protected View mBtnDiagonostic;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setRetainInstance(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_driving_bottom, container, false);
	}
	
	@Override
	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		mDrivingActivity = (DrivingActivity)getActivity();
		View navi = view.findViewById(R.id.btn_quick_launch_navi);
		if (TextUtils.isEmpty("True")) {
			navi.setEnabled(false);
		} else {
			navi.setOnClickListener(v -> {

			});
		}

		mBtnDiagonostic = view.findViewById(R.id.btn_diagnostic);
		mBtnDiagonostic.setOnClickListener(v ->
				mDrivingActivity.startDiagnosticActivity(mDrivingActivity.mDtc, true));
		mBtnDiagonostic.setEnabled(false);
	}

	public void onObdDataReceived(ObdData data) {
		if (isVisible()) {
			int ves = data.getInteger(KEY.CALC_VES, VehicleEngineStatus.UNKNOWN);
			mBtnDiagonostic.setEnabled(VehicleEngineStatus.isOnDriving(ves));

		}
	}

	public void onObdCannotConnect() {
		if (isVisible()) {
			mBtnDiagonostic.setEnabled(false);
		}
	}


}
