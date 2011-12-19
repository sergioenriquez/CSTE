package org.fern.rest.android.user;

import java.net.URI;
import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable, Comparable<User> {
    String mUserName  = null;
    String mPassword = null;
    URI mUri  = null;
    String mEtag  = null;
    Date mEtagUpdateTime = null;
    
    public void setURI(String s){
    	mUri = URI.create(s);
    }
    
    public void setEtag(String etag){
    	this.mEtag = etag;
    }
    
    public void setEtagUpdateTime( Date time){
    	this.mEtagUpdateTime = time;
    }
    
    public void setPassword(String password){
    	this.mPassword = password;
    }
    
    public String getPassword(){
    	return mPassword;
    }
    
    public String getEtag(){
    	return mEtag;
    }
    
    public Date getLastEtagUpdate(){
    	return mEtagUpdateTime;
    }

    public User(String name, String password, URI uri) {
        this.mUserName = name;
        this.mPassword = password;
        this.mUri = uri;
    }
    
    public User(String name, 
    		String password, 
    		String uri,
    		String ETag,
    		Date lastETagUpdate) {
        this.mUserName = name;
        this.mPassword = password;
        this.mUri = URI.create(uri);
        this.mEtag = ETag;
        this.mEtagUpdateTime = lastETagUpdate;
    }

    public User(String name, String uri) {
        mUserName = name;
        mUri = URI.create(uri);
    }

    public User(Parcel in) {
        String[] data = new String[4];
        in.readStringArray(data);
        this.mUserName = data[0];
        this.mUri = URI.create(data[1]);
        this.mPassword = data[2];
        this.mEtag = data[3];
        long l = in.readLong();
        if (l != -1) this.mEtagUpdateTime = new Date(l);
    }

    public String getName() {
        return mUserName;
    }

    public String getServer() {
        return mUri.getHost();
    }

    public URI getURI() {
        return mUri;
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[] { this.mUserName,
                this.mUri.toString(), this.mPassword, this.mEtag });
        if (mEtagUpdateTime != null) {
            dest.writeLong(this.mEtagUpdateTime.getTime());
        } else {
            dest.writeLong(-1);
        }
    }
    
    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public int compareTo(User another) {
        return this.getName().compareTo(another.getName());
    }
}
