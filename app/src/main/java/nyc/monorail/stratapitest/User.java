package nyc.monorail.stratapitest;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {

    String userID;
    String userApiKey;
    String credits;

    public User(String id, String apiKey, String creds) {
        userID = id;
        userApiKey = apiKey;
        credits = creds;
    }

    public User(Parcel in) {
        this.userID = in.readString();
        this.userApiKey = in.readString();
        this.credits = in.readString();
    }

    public String getUserID() {
        return userID;
    }

    public String getUserApiKey() {
        return userApiKey;
    }

    public String getCredits() {
        return credits;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userID);
        dest.writeString(userApiKey);
        dest.writeString(credits);
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}