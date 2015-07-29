package nyc.monorail.stratapitest;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;

public class Pack implements Parcelable {

    String packID;
    HashMap properties;

    public Pack(String id, HashMap map) {
        packID = id;
        properties = map;
    }

    public String getPackID() {
        return packID;
    }

    public HashMap getProperties() {
        return properties;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(packID);
        dest.writeMap(properties);

    }

    public Pack(Parcel in) {
        this.packID = in.readString();
        this.properties = in.readHashMap(ClassLoader.getSystemClassLoader());
    }

    public static final Parcelable.Creator<Pack> CREATOR = new Parcelable.Creator<Pack>() {
        @Override
        public Pack createFromParcel(Parcel in) {
            return new Pack(in);
        }

        @Override
        public Pack[] newArray(int size) {
            return new Pack[size];
        }
    };
}
