package com.cs9033.songbird.models;

import android.os.Parcel;
import android.os.Parcelable;

public class SongDetailsModel  implements Parcelable{
    private int song_id;
    private String song_name;
    private String song_link;
    private String creator_name;
    
    public static final Parcelable.Creator<SongDetailsModel> CREATOR = new Parcelable.Creator<SongDetailsModel>() {
        public SongDetailsModel createFromParcel(Parcel p){
            return new SongDetailsModel(p);
        }

        @Override
        public SongDetailsModel[] newArray(int size) {
            // TODO Auto-generated method stub
            return null;
        }
    };
    public SongDetailsModel(Parcel p) {
        song_id = p.readInt();
        song_name = p.readString();
        song_link = p.readString();
        creator_name = p.readString();
    }
    public SongDetailsModel(int id,String name,String link, String creator) {
        this.song_id = id;
        song_name = name;
        song_link = link;
        creator_name = creator;
    }
    public int getId() {
        return song_id;
    }
    public String getSongName() {
        return song_name;
    }
    public String getSong_link() {
        return song_link;
    }
    public String getCreator_name() {
        return creator_name;
    }
    @Override
    public int describeContents() {
        // Do not implement
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(song_id);
        dest.writeString(song_name);
        dest.writeString(song_link);
        dest.writeString(creator_name);
    }
}
