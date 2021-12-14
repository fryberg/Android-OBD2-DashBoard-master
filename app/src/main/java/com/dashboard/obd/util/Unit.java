package com.dashboard.obd.util;
public enum Unit {
    KM("km"),
    MI("mi"),
    KPH("km/h"),
    MPH("mph"),
    L("L"),
    GAL_US("gal(US)"),
    GAL_UK("gal(UK)"),
    KPL("km/L"),
    LP100KM("L/100km"),
    MPG_US("mpg(US)"),
    MPG_UK("mpg(UK)"),
    C("°C"),
    F("°F"),
    KG("kg"),
    LB("lb"),
    CC("cc"),
    VOLT("v"),
    PERCENT("%"),
    RPM("rpm"),
    GS("g/s"),
    KPA("kPa"),
    GRAD("°"),
    EA("ea"),

    UNKNOWN("?");

    private String a;

    private Unit(String var3) {
        this.a = var3;
    }

    public final String toString() {
        return this.a;
    }

    public static Unit fromString(String var0) {
        if (KM.toString().equals(var0)) {
            return KM;
        } else if (MI.toString().equals(var0)) {
            return MI;
        } else if (KPH.toString().equals(var0)) {
            return KPH;
        } else if (MPH.toString().equals(var0)) {
            return MPH;
        } else if (L.toString().equals(var0)) {
            return L;
        } else if (GAL_US.toString().equals(var0)) {
            return GAL_US;
        } else if (GAL_UK.toString().equals(var0)) {
            return GAL_UK;
        } else if (KPL.toString().equals(var0)) {
            return KPL;
        } else if (LP100KM.toString().equals(var0)) {
            return LP100KM;
        } else if (MPG_US.toString().equals(var0)) {
            return MPG_US;
        } else if (MPG_UK.toString().equals(var0)) {
            return MPG_UK;
        } else if (CC.toString().equals(var0)) {
            return CC;
        } else if (VOLT.toString().equals(var0)) {
            return VOLT;
        } else if (PERCENT.toString().equals(var0)) {
            return PERCENT;
        } else if (RPM.toString().equals(var0)) {
            return RPM;
        } else if (KPA.toString().equals(var0)) {
            return KPA;
        } else if (GS.toString().equals(var0)) {
            return GS;
        } else if (GRAD.toString().equals(var0)) {
            return GRAD;
        } else {
            return EA.toString().equals(var0) ? EA : UNKNOWN;
        }
    }
}

