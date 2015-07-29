package nyc.monorail.stratapitest;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;

public class Card implements Parcelable {

    HashMap properties;

    public Card(HashMap map) {
        properties = map;
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
        dest.writeMap(properties);
    }

    public Card(Parcel in) {
        this.properties = in.readHashMap(ClassLoader.getSystemClassLoader());
    }

    public static final Parcelable.Creator<Card> CREATOR = new Parcelable.Creator<Card>() {
        @Override
        public Card createFromParcel(Parcel in) {
            return new Card(in);
        }

        @Override
        public Card[] newArray(int size) {
            return new Card[size];
        }
    };
}
