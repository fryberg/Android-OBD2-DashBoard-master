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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;



import com.dashboard.obd.setup.StorageType;
import com.google.gson.Gson;

import com.pokevian.lib.obd2.defs.Unit;

import java.util.HashSet;
import java.util.Set;

public final class SettingsStore {


    public static final String PREF_ACCOUNT_ID = "account_id";
    public static final String PREF_ACCOUNT_NAME = "account_name";
    public static final String PREF_ACCOUNT_IMAGE_URL = "account_image_url";

    public static final String PREF_VEHICLE_ID = "vehicle_id";
    public static final String PREF_VEHICLE = "vehicle::";
    public static final String PREF_VEHICLE_ID_LIST = "vehicle_id_list";

    public static final String PREF_UNIT_INITIALIZED = "unit_initialized";
    public static final String PREF_DISTANCE_UNIT = "distance_unit";
    public static final String PREF_SPEED_UNIT = "speed_unit";
    public static final String PREF_VOLUME_UNIT = "volume_unit";
    public static final String PREF_FUEL_ECONOMY_UNIT = "fuel_economy_unit";

    public static final String PREF_IMPACT_SENSITIVITY = "impact_sensitivity";

    public static final String PREF_QUICK_LAUNCH_APP_NAVI = "quick_launch_app_navi";
    public static final String PREF_AUTO_LAUNCH_APP_NAVI_ENABLED = "auto_launch_navi_app_enabled";
    public static final String PREF_QUICK_LAUNCH_APP_CUSTOM = "quick_launch_app_custom";
    public static final String PREF_OVERSPEED_THRESHOLD = "overspeed_threshold";

    public static final String PREF_TRIP_A_DISTANCE = "trip_a_distance";
    public static final String PREF_TRIP_A_FUEL_CONSUMPTION = "trip_a_fuel_consumption";
    public static final String PREF_TRIP_B_DISTANCE = "trip_b_distance";
    public static final String PREF_TRIP_B_FUEL_CONSUMPTION = "trip_a_fuel_consumption";

    public static final String PREF_DRIVING_HELP_DONE = "driving_help_done";

    public static final String PREF_ENGINE_OFF_DETECTION_ENABLED = "engine_off_detection_enabled";
    public static final String PREF_ENGINE_ON_DETECTION_ENABLED = "engine_on_detection_enabled";

    public static final String PREF_BLACKBOX_FOCUS_MODE = "blackbox-focus-mode";
    public static final String PREF_BLACKBOX_EXPOSURE_EXTRA = "blackbox-exposure-extra";
    public static final String PREF_BLACKBOX_OSD_ENABLED = "osd-enabled";

    public static final String PREF_WAIT_UNTIL_ENGINE_OFF = "wait_until_engine_off";
    public static final String PREF_ODB_ADDRESS_CONNECT = "odb_address_connect";

    private final SharedPreferences mPrefs;
    private final Gson mGson;

    private static SettingsStore mInstance;

    private SettingsStore(Context context) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        mGson = new Gson();
    }

    public static void init(Context context) {
        if (mInstance == null) {
            mInstance = new SettingsStore(context);
        }
    }

    public static SettingsStore getInstance() {
        if (mInstance == null) {
            throw new RuntimeException("Not initialized!");
        }
        return mInstance;
    }

    public static void commit() {
        if (mInstance != null) {
            boolean result = mInstance.mPrefs.edit().commit();
            Log.i("SettingsStore", "commit=" + result);
        }
    }

//    public void putWaitUntilEngineOff(boolean wait) {
//        SharedPreferences.Editor editor = mPrefs.edit();
//        editor.putBoolean(PREF_WAIT_UNTIL_ENGINE_OFF, wait);
//        editor.apply();
//    }

//    public boolean isWaitUnitlEngineOff() {
//        return mPrefs.getBoolean(PREF_WAIT_UNTIL_ENGINE_OFF, false);
//    }


    public String getAccountId() {
        return mPrefs.getString(PREF_ACCOUNT_ID, null);
    }


    public String getVehicleId() {
        return mPrefs.getString(PREF_VEHICLE_ID, null);
    }



    private String buildVehicleKey(String vehicleId) {
        return PREF_VEHICLE + vehicleId;
    }

    public Set<String> getVehicleIds() {
        return mPrefs.getStringSet(PREF_VEHICLE_ID_LIST, new HashSet<String>());
    }

    public void addVehicleId(String vehicleId) {
        Set<String> ids = getVehicleIds();
        if (!ids.contains(vehicleId)) {
            ids.add(vehicleId);
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.putStringSet(PREF_VEHICLE_ID_LIST, ids);
            editor.apply();
        }
    }

    public void removeVehicleId(String vehicleId) {
        Set<String> ids = getVehicleIds();
        if (ids.remove(vehicleId)) {
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.putStringSet(PREF_VEHICLE_ID_LIST, ids);
            editor.apply();
        }
    }

    public boolean isUpdatedObdAddress() {
        return mPrefs.getBoolean("updated-obd-address", false);
    }

    public void storeUpdatedObdAddress(boolean update) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean("updated-obd-address", update);
        editor.apply();
    }

    public void storeObdAddress(String obd_address) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(PREF_ODB_ADDRESS_CONNECT, obd_address);
        editor.apply();
    }
    public String getObdAddress() {
        return mPrefs.getString(PREF_ODB_ADDRESS_CONNECT,null);

    }

    public void storeUnitInitialied(boolean initialized) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(PREF_UNIT_INITIALIZED, initialized);
        editor.apply();
    }

    public boolean isUnitInitialized() {
        return mPrefs.getBoolean(PREF_UNIT_INITIALIZED, false);
    }

    public void storeDistanceUnit(Unit distanceUnit) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(PREF_DISTANCE_UNIT, distanceUnit.name());
        editor.apply();
    }

    public Unit getDistanceUnit() {
        try {
            return Unit.valueOf(mPrefs.getString(PREF_DISTANCE_UNIT,
                    Const.DEFAULT_DISTANCE_UNIT.name()));
        } catch (IllegalArgumentException e) {
            return Const.DEFAULT_DISTANCE_UNIT;
        }
    }

    public void storeSpeedUnit(Unit speedUnit) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(PREF_SPEED_UNIT, speedUnit.name());
        editor.apply();
    }

    public Unit getSpeedUnit() {
        try {
            return Unit.valueOf(mPrefs.getString(PREF_SPEED_UNIT,
                    Const.DEFAULT_SPEED_UNIT.name()));
        } catch (IllegalArgumentException e) {
            return Const.DEFAULT_SPEED_UNIT;
        }
    }

    public void storeVolumeUnit(Unit volumeUnit) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(PREF_VOLUME_UNIT, volumeUnit.name());
        editor.apply();
    }

    public Unit getVolumeUnit() {
        try {
            return Unit.valueOf(mPrefs.getString(PREF_VOLUME_UNIT,
                    Const.DEFAULT_VOLUME_UNIT.name()));
        } catch (IllegalArgumentException e) {
            return Const.DEFAULT_VOLUME_UNIT;
        }
    }

    public void storeFuelEconomyUnit(Unit fuelEconomyUnit) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(PREF_FUEL_ECONOMY_UNIT, fuelEconomyUnit.name());
        editor.apply();
    }

    public Unit getFuelEconomyUnit() {
        try {
            return Unit.valueOf(mPrefs.getString(PREF_FUEL_ECONOMY_UNIT,
                    Const.DEFAULT_FUEL_ECONOMY_UNIT.name()));
        } catch (IllegalArgumentException e) {
            return Const.DEFAULT_FUEL_ECONOMY_UNIT;
        }
    }




    public int getOverspeedThreshold() {
        try {
            return Integer.parseInt(mPrefs.getString(PREF_OVERSPEED_THRESHOLD,
                    String.valueOf(Const.DEFAULT_ECO_OVERSPEED_THRESHOLD)));
        } catch (NumberFormatException e) {
            return Const.DEFAULT_ECO_OVERSPEED_THRESHOLD;
        }
    }

    public void storeTripA(float distanceInKm, float consumptionInLiters) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putFloat(PREF_TRIP_A_DISTANCE, distanceInKm);
        editor.putFloat(PREF_TRIP_A_FUEL_CONSUMPTION, consumptionInLiters);
        editor.apply();
    }

    public float getTripADistance() {
        return mPrefs.getFloat(PREF_TRIP_A_DISTANCE, 0);
    }

    public float getTripAFuelConsumption() {
        return mPrefs.getFloat(PREF_TRIP_A_FUEL_CONSUMPTION, 0);
    }

    public void resetTripA() {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.remove(PREF_TRIP_A_DISTANCE);
        editor.remove(PREF_TRIP_A_FUEL_CONSUMPTION);
        editor.apply();
    }

    public void storeTripB(float distanceInKm, float consumptionInLiters) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putFloat(PREF_TRIP_B_DISTANCE, distanceInKm);
        editor.putFloat(PREF_TRIP_B_FUEL_CONSUMPTION, consumptionInLiters);
        editor.apply();
    }

    public float getTripBDistance() {
        return mPrefs.getFloat(PREF_TRIP_B_DISTANCE, 0);
    }

    public float getTripBFuelConsumption() {
        return mPrefs.getFloat(PREF_TRIP_B_FUEL_CONSUMPTION, 0);
    }

    public void resetTripB() {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.remove(PREF_TRIP_B_DISTANCE);
        editor.remove(PREF_TRIP_B_FUEL_CONSUMPTION);
        editor.apply();
    }

    public void storeDrivingHelpDone(boolean done) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(PREF_DRIVING_HELP_DONE, done);
        editor.apply();
    }

    public boolean isDrivingHelpDone() {
        return mPrefs.getBoolean(PREF_DRIVING_HELP_DONE, false);
    }

    public void storeEngineOffDetectionEnabled(boolean enabled) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(PREF_ENGINE_OFF_DETECTION_ENABLED, enabled);
        editor.apply();
    }

    public boolean isEngineOffDetectionEnabled() {
        return mPrefs.getBoolean(PREF_ENGINE_OFF_DETECTION_ENABLED, true);
    }

    /*public boolean isIsgSupport() {
        return mPrefs.getBoolean("engine_off_detection_isg", false);
    }*/

    public boolean isEngineOnDetectionEnabled() {
        return mPrefs.getBoolean(PREF_ENGINE_ON_DETECTION_ENABLED, true);
    }



    public boolean getSoundEffectEnabled() {
        return mPrefs.getBoolean("sound_effect_enabled", true);
    }

    public boolean hasNewEvent() {
        return getNewEventCount() > 0;
    }

    public int getNewEventCount() {
        return mPrefs.getInt("new-event-count", 0);
    }

    public void storeNewEventCount(int count) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putInt("new-event-count",count);
        editor.apply();
    }

    public boolean hasNewNoti() {
        return getNewNotiCount() > 0;
    }

    public int getNewNotiCount() {
        return mPrefs.getInt("new-noti-count", 0);
    }



    public void storeVehicleMakerTag(String name) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString("pref-tag-vehicle-maker", name);
        editor.apply();
    }

    public String getVehicleMakerTag() {
        return mPrefs.getString("pref-tag-vehicle-maker", null);
    }

    public void storeVehicleFuelTag(String name) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString("pref-tag-vehicle-fuel", name);
        editor.apply();
    }

    public String getVehicleFuelTag() {
        return mPrefs.getString("pref-tag-vehicle-fuel", null);
    }

    public void storeAccountSexTag(String sex) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString("pref-tag-account-sex", sex);
        editor.apply();
    }

    public String getAccountSexTag() {
        return mPrefs.getString("pref-tag-account-sex", null);
    }

    public void storeAccountRegionTag(String region) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString("pref-tag-account-region", region);
        editor.apply();
    }

    public String getAccountRegionTag() {
        return mPrefs.getString("pref-tag-account-region", null);
    }

    public void storeIdentity(String identity) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString("pref-identity", identity);
        editor.apply();
    }

    public String getIdentity() {
        return mPrefs.getString("pref-identity", null);
    }

    public boolean isFloatingWindowEnabled() {
        return mPrefs.getBoolean("floating_enabled", false);
    }

    public void storeConfigVersion(String version) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString("pref-config-version", version);
        editor.apply();
    }

    public String getConfigVersion() {
        return mPrefs.getString("pref-config-version", null);
    }

//    public boolean isPowerSave() {
//        return mPrefs.getBoolean("engine_on_detection_power_save", false);
//    }

    public String getAutoStartMode() {
        return mPrefs.getString("engine_on_detection_enabled_marshmallow", "HIGH");
    }

}
