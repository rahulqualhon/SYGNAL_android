package com.android.models;

import android.os.Parcel;
import android.os.Parcelable;

public class BluetoothDeviceModel implements Parcelable {
    public String bluetooth_name;
    public String bluetooth_rssi;
    public String distance;
    public double distancevalue;
    public String date;
    public String gasScale;
    public String minValue;
    public String maxValue;
    public boolean isDistance;


    public BluetoothDeviceModel(Parcel in) {
        bluetooth_name = in.readString();
        bluetooth_rssi = in.readString();
        distance = in.readString();
        date = in.readString();
        gasScale = in.readString();
        minValue = in.readString();
        maxValue = in.readString();
        isDistance = in.readBoolean();
    }

    public static final Creator<BluetoothDeviceModel> CREATOR = new Creator<BluetoothDeviceModel>() {
        @Override
        public BluetoothDeviceModel createFromParcel(Parcel in) {
            return new BluetoothDeviceModel(in);
        }

        @Override
        public BluetoothDeviceModel[] newArray(int size) {
            return new BluetoothDeviceModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(bluetooth_name);
        parcel.writeString(bluetooth_rssi);
        parcel.writeString(distance);
        parcel.writeString(date);
        parcel.writeString(gasScale);
        parcel.writeString(minValue);
        parcel.writeString(maxValue);
        parcel.writeBoolean(isDistance);
    }
}
