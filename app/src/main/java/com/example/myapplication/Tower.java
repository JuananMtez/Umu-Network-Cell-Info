package com.example.myapplication;

import com.google.android.gms.maps.model.LatLng;

public class Tower {

    private String tecnologia;
    private int mcc;
    private int mnc;
    private int lac;
    private int cellId;

    private LatLng latLng;
    private String range;


    public Tower(int mcc, int mnc, int lac, int cellId, String tecnologia) {

        this.mcc = mcc;
        this.mnc = mnc;
        this.lac = lac;
        this.cellId = cellId;

        this.tecnologia = tecnologia;

        latLng = null;
        range = null;
    }


    public int getMcc() {
        return mcc;
    }

    public void setMcc(int mcc) {
        this.mcc = mcc;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public int getCellId() {
        return cellId;
    }

    public void setCellId(int cellId) {
        this.cellId = cellId;
    }

    public int getLac() {
        return lac;
    }

    public void setLac(int lac) {
        this.lac = lac;
    }

    public int getMnc() {
        return mnc;
    }

    public void setMnc(int mnc) {
        this.mnc = mnc;
    }

    public String getTecnologia() {
        return tecnologia;
    }

    public void setTecnologia(String tecnologia) {
        this.tecnologia = tecnologia;
    }
}
