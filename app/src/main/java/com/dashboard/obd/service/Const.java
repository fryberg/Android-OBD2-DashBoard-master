package com.dashboard.obd.service;
import com.dashboard.obd.setup.StorageType;

import com.pokevian.lib.obd2.defs.Unit;

public class Const {
    //
    // Max OBD connection failure count
    //
//    public static final int MAX_OBD_CONNECTION_FAILURE_COUNT = 5;
    public static final int MAX_OBD_CONNECTION_FAILURE_COUNT = Integer.MAX_VALUE;
    public static final Unit DEFAULT_DISTANCE_UNIT = Unit.KM;
    public static final Unit DEFAULT_SPEED_UNIT = Unit.KPH;
    public static final Unit DEFAULT_VOLUME_UNIT = Unit.L;
    public static final Unit DEFAULT_FUEL_ECONOMY_UNIT = Unit.KPL;

    public static final int DEFAULT_ECO_OVERSPEED_THRESHOLD = 110; // km/h
    public static final float DEFAULT_ECO_HARSH_ACCEL_THRESHOLD = 7.0f; // km/h/s
    public static final float DEFAULT_ECO_HARSH_BRAKE_THRESHOLD = -9.0f; // km/h/s
    public static final int DEFAULT_ECO_LOW_FUEL_THRESHOLD = 10; // %
    public static final int DEFAULT_ECO_OVERHEAT_THRESHOLD = 115; // °C
    public static final int DEFAULT_ECO_MIN_ECO_SPEED = 60; // km/h
    public static final int DEFAULT_ECO_MAX_ECO_SPEED = 80; // km/h
    public static final int DEFAULT_ECO_MIN_LONG_TERM_OVERSPEED_TIME = 3 * 60 * 1000; // 3 minutes
    public static final float DEFAULT_ECO_MIN_AUX_BATTERY_LEVEL_VES_OFF = 10.5f;
    public static final float DEFAULT_ECO_MIN_AUX_BATTERY_LEVEL_VES_ON = 13.2f;
    public static final float DEFAULT_ECO_MAX_AUX_BATTERY_LEVEL_VES_ON = 14.8f;
    public static final int DEFAULT_ECO_GASOLINE_IDLING_RESTRICTED_TIME = 3 * 60 * 1000; // gasoline, 3 minutes
    public static final int DEFAULT_ECO_DIESEL_IDLING_RESTRICTED_TIME = 5 * 60 * 1000; // diesel, 5 minutes
    public static final int DEFAULT_ECO_MAX_IDLING_RESTRICTED_TIME = 10 * 60 * 1000; // <5'C or >25'C, 10 minutes
    public static final int DEFAULT_ECO_MIN_HARSH_TURN_SPEED = 15; // km/h
    public static final int DEFAULT_ECO_MIN_HARSH_TURN_INTERVAL = 2 * 1000; // 2 seconds
    public static final int DEFAULT_ECO_MAX_HARSH_TURN_INTERVAL = 4 * 1000; // 4 seconds
    public static final int DEFAULT_ECO_HARSH_LEFT_TURN_FROM_DEGREE = 240; // degree
    public static final int DEFAULT_ECO_HARSH_LEFT_TURN_TO_DEGREE = 300; // degree
    public static final int DEFAULT_ECO_HARSH_RIGHT_TURN_FROM_DEGREE = 60; // degree
    public static final int DEFAULT_ECO_HARSH_RIGHT_TURN_TO_DEGREE = 120; // degree
    public static final int DEFAULT_ECO_HARSH_U_TURN_FROM_DEGREE = 160; // degree
    public static final int DEFAULT_ECO_HARSH_U_TURN_TO_DEGREE = 200; // degree
    public static final int DEFAULT_ECO_MIN_GEAR_SHIFT_TIME = 10 * 1000; // 10 seconds
    public static final int DEFAULT_ECO_MAX_GEAR_SHIFT_ALARM_COUNT = 3;
    public static final int DEFAULT_TOUCH_ALLOWED_SPEED = 20; // km/h
}
