package com.puneeth.ctabustracker;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.gms.maps.model.LatLng;

public class Stop implements Parcelable {

    private String stpId;
    private String stpName;
    private LatLng location;

    // Constructor
    public Stop(String stpId, String stpName, LatLng location) {
        this.stpId = stpId;
        this.stpName = stpName;
        this.location = location;
    }

    // Getters
    public String getStpId() {
        return stpId;
    }

    public String getStpName() {
        return stpName;
    }

    public LatLng getLocation() {
        return location;
    }

    // Parcelable implementation
    protected Stop(Parcel in) {
        stpId = in.readString();
        stpName = in.readString();
        location = in.readParcelable(LatLng.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(stpId);
        dest.writeString(stpName);
        dest.writeParcelable(location, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Stop> CREATOR = new Creator<Stop>() {
        @Override
        public Stop createFromParcel(Parcel in) {
            return new Stop(in);
        }

        @Override
        public Stop[] newArray(int size) {
            return new Stop[size];
        }
    };
}
