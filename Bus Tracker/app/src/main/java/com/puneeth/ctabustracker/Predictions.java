package com.puneeth.ctabustracker;

import java.io.Serializable;

public class Predictions implements Serializable {

    String vid;
    String rtdir;
    String des;
    String prdtm;
    boolean dlys;
    String prdctdn;

    public Predictions(String vid, String rtdir, String des, String prdtm, boolean dlys, String prdctdn) {
        this.vid = vid;
        this.rtdir = rtdir;
        this.des = des;
        this.prdtm = prdtm;
        this.dlys = dlys;
        this.prdctdn = prdctdn;
    }

    public String getVid() {
        return vid;
    }

    public String getRtdir() {
        return rtdir;
    }

    public String getDes() {
        return des;
    }

    public String getPrdtm() {
        return prdtm;
    }

    public boolean isDlys() {
        return dlys;
    }

    public String getPrdctdn() {
        return prdctdn;
    }
}
