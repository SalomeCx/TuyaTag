package project.tuyatag;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

// Class used to create a graffity entry 
public class Graffiti implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public static class Location implements Serializable {
		private static final long serialVersionUID = 1L;
		
		private Double mLatitude;
		private Double mLongitude;
		
		public Location() {
			mLatitude = 0.0;
			mLongitude = 0.0;
		}
		
		public Location( android.location.Location loc ) {
			mLatitude = loc.getLatitude();
			mLongitude = loc.getLongitude();
		}
		

		public Double getLatitude() {
			return mLatitude;
		}
		public void setLatitude(Double l) {
			mLatitude = l;
		}
		public Double getLongitude() {
			return mLongitude;
		}
		public void setLongitude(Double l) {
			mLongitude = l;
		}
	}
	private String mPhotoPath;
	private Location mLocation;
	private int mRotation; // Rotation in degree
	private int mOrientation; // Orientation in Exif code
	private String mComment;
	private String mMessage;
	private String mLocationOnServer;
	private Boolean mOnServer;
	
	private static final String DEBUG_TAG = "Discovart/Graffiti";

	public Graffiti(){
		mComment = new String();
		mPhotoPath = new String();
		mLocation = null;
		mMessage = new String();
		mOnServer = false;
		mLocationOnServer = "";
		mRotation = -1;
		mOrientation = -1;
	}

	// Set methods
	
	void setPhotoPath(String p) {
		mPhotoPath = p;
	}

	void setLocation(Location loc) {
		mLocation = loc;
	}
	public void setRotation(int r) {
		mRotation = r;
	}

	public void setOrientation(int o) {
		mOrientation = o;
	}

	void setComment(String c) {
		mComment = c;
	}

	void setMessage(String m) {
		mMessage += m;
	}

	void setLocationOnServer(String l) {
		mLocationOnServer = l;
	}

	void setOnServer(Boolean b) {
		mOnServer = b;
	}

	void setOnServer() {
		setOnServer(true);
	}
	
	// Get methods
	
	String getPhotoPath() {
		return mPhotoPath;
	}

	double getLatitude() {
		if( mLocation == null ) {
			return 0.0;
		}
		return mLocation.getLatitude();
	}

	double getLongitude() {
		if( mLocation == null ) {
			return 0.0;
		}
		return mLocation.getLongitude();
	}

	Location getLocation() {
		return mLocation;
	}
	
	int getRotation() {
		return mRotation;
	}

	int getOrientation() {
		return mOrientation;
	}

	String getComment() {
		return mComment;
	}

	String getMessage() {
		return mMessage;
	}

	String getLocationOnServer() {
		return mLocationOnServer;
	}

	Boolean isOnServer() {
		return mOnServer;
	}

	public void issueLog() {
		Log.i(DEBUG_TAG, "---  Graffiti Description --- ");
		Log.i(DEBUG_TAG, "File:        " + mPhotoPath);
		if( mLocation != null ) {
			Log.i(DEBUG_TAG, "Latitude:    " + mLocation.getLatitude());
			Log.i(DEBUG_TAG, "Longitude:   " + mLocation.getLongitude());
		}
		else {
			Log.i(DEBUG_TAG, "Latitude:    Unknown");
			Log.i(DEBUG_TAG, "Longitude:   Unknown");			
		}
		Log.i(DEBUG_TAG, "Rotation:    " + mRotation);
		Log.i(DEBUG_TAG, "Orientation: " + mOrientation);
		Log.i(DEBUG_TAG, "Comment:     " + mComment);
		Log.i(DEBUG_TAG, "Message:     " + mMessage);
		Log.i(DEBUG_TAG, "Location on server: " + mLocationOnServer);
		if (isOnServer()) {
			Log.i(DEBUG_TAG, "Data is on distant server");
		} else {
			Log.i(DEBUG_TAG, "Data no downloaded yet");
		}
		Log.i(DEBUG_TAG, "----------------------------- ");
	}

	public JSONObject getJSONObject()  {
		JSONObject object = new JSONObject();
		try {
			object.put("PhotoPath", mPhotoPath);
			if( mLocation != null ) {
				object.put("Latitude", mLocation.getLatitude());
				object.put("Longitude", mLocation.getLongitude());
			}
			else {
				object.put("Latitude", 0.0);
				object.put("Longitude", 0.0);
			}
			object.put("Rotation", mRotation);
			object.put("orientation", mOrientation);
			object.put("Comment", mComment);
			object.put("Message", mMessage);
			object.put("LocationOnServer", mLocationOnServer);
			return object;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;

	}

	@Override
	public String toString() {
		if( mPhotoPath == "" ) {
			return "";
		}
		
		File graffity_file = new File(mPhotoPath);
		InputStream graffity_is;
		try {
			graffity_is = new FileInputStream(graffity_file);
		} catch (FileNotFoundException e) {
			Log.i(DEBUG_TAG, "Can't read " + mPhotoPath);
			e.printStackTrace();
			return "";
		}
		ByteArrayOutputStream graffity_os = new ByteArrayOutputStream(1000);
		Bitmap graffity_bitmap = BitmapFactory.decodeStream(graffity_is, null, null);
		graffity_bitmap.compress(Bitmap.CompressFormat.JPEG, 90, graffity_os);  
		byte[] graffity_bytearray = graffity_os.toByteArray();
		String graffity_string = Base64.encodeToString(graffity_bytearray, Base64.DEFAULT);
		return graffity_string;
	}

	
}
