package project.tuyatag;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import project.tuyatag.R;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;


// this class takes care of the uploading of the data on the server
// using a json based coding of the data

public class Uploader extends AsyncTask<String, String, Boolean> {
	private ProgressDialog mProgress;
	private final TakePictureActivity mContext;
	private Graffiti mGraffity;
	private String mStatus;
	
	private static final String DEBUG_TAG = "Discovart/Uploader";

	Uploader(TakePictureActivity takePictureActivity, Graffiti graffiti) {
		mGraffity = graffiti;
		mContext = takePictureActivity;
		mStatus = "";
	}

	@Override
	protected void onPreExecute() {
		Log.d(DEBUG_TAG, "onPreExecute");
	    super.onPreExecute();

		mProgress = new ProgressDialog(mContext);
		mProgress.setMessage(mContext.getResources().getString(R.string.uploading));
		mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mProgress.setCancelable(false);
		mProgress.show();
		
	}

	@Override
	protected Boolean doInBackground(String... args) {
		Log.i(DEBUG_TAG, "doInBackground");
		
		String url_get_location = mContext.getResources().getString(R.string.url_get_location);

		String url_send = mContext.getResources().getString(R.string.url_send_default);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		if( prefs.contains(SettingsActivity.KEY_PREF_SERVER) ) {
			if( prefs.getString(SettingsActivity.KEY_PREF_SERVER, "").equals(mContext.getResources().getString(R.string.pref_server_default)) ) {
				url_send = mContext.getResources().getString(R.string.url_send_default);
			}
			else if (prefs.getString(SettingsActivity.KEY_PREF_SERVER, "").equals(mContext.getResources().getString(R.string.pref_server_test)) ) {
				Log.i(DEBUG_TAG, "Using Test Sever");
				url_send = mContext.getResources().getString(R.string.url_send_test);
			} else if (prefs.getString(SettingsActivity.KEY_PREF_SERVER, "").equals(mContext.getResources().getString(R.string.pref_server_am)) ) {
				url_send = mContext.getResources().getString(R.string.url_send_am);
				Log.i(DEBUG_TAG, "Using AtlasMuseum Server");
			} else if (prefs.getString(SettingsActivity.KEY_PREF_SERVER, "").equals(mContext.getResources().getString(R.string.pref_server_refam)) ) {
				url_send = mContext.getResources().getString(R.string.url_send_refam);
				Log.i(DEBUG_TAG, "Using AtlasMuseum Reference Server");
			}
		}
		String server_location = null;

		
		HttpJson httpJ = new HttpJson();

		try {
			JSONObject json_data;
			JSONArray result = httpJ.getJSONFromUrl(url_get_location);
			if ((result != null) && (result.length() > 0)) {
				json_data = result.getJSONObject(0);
				server_location = json_data.getString("serverlocation");
				Log.i(DEBUG_TAG, "Server gave directory " + server_location );
			} else {
				mStatus = mContext.getResources().getString(R.string.get_server_location_failed);
				return false;
			}
		} catch (JSONException e) {
			mStatus = mContext.getResources().getString(R.string.http_request_failed);
			e.printStackTrace();
			return false;
		}

		mGraffity.setLocationOnServer(server_location);
		
		
		// create the json data structure to be sent...
		ArrayList<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("arg_passwd", "discovart&9!"));
		nvps.add(new BasicNameValuePair("arg_photopath", mGraffity.getPhotoPath()));
		nvps.add(new BasicNameValuePair("arg_latitude", Double.toString(mGraffity.getLatitude())));
		nvps.add(new BasicNameValuePair("arg_longitude", Double.toString(mGraffity.getLongitude())));
		nvps.add(new BasicNameValuePair("arg_rotation", Double.toString(mGraffity.getRotation())));
		nvps.add(new BasicNameValuePair("arg_orientation", Double.toString(mGraffity.getOrientation())));
		nvps.add(new BasicNameValuePair("arg_comment", mGraffity.getComment()));
		nvps.add(new BasicNameValuePair("arg_message", mGraffity.getMessage()));
		nvps.add(new BasicNameValuePair("arg_serverlocation", mGraffity.getLocationOnServer()));
		String image_string = mGraffity.toString();
		nvps.add(new BasicNameValuePair("arg_image", image_string));
		nvps.add(new BasicNameValuePair("arg_size_image", Integer.toString(image_string.length())));
		nvps.add( new BasicNameValuePair("arg_author", prefs.getString(SettingsActivity.KEY_PREF_AUTHOR, "")));
			
		try {
			JSONArray result = httpJ.getJSONFromUrl(url_send, nvps);
			if (result != null && result.length() > 0) {
				JSONObject json_data = result.getJSONObject(0);
				String server_status = json_data.getString("commandstatus");
				Log.i(DEBUG_TAG, "Feedback from server: " + server_status);
				if (! server_status.equals("ok")) {
					mStatus = mContext.getResources().getString(R.string.upload_failed, server_status);
					Log.w(DEBUG_TAG, mStatus);
					return false;
				}
			} else {
				mStatus = mContext.getResources().getString(R.string.upload_failed, mContext.getResources().getString(R.string.empty_result));
				Log.w(DEBUG_TAG, mStatus);
				return false;
			}
			return true;
		} catch (JSONException e) {
			mStatus = mContext.getResources().getString(R.string.http_request_failed);
			e.printStackTrace();
			return false;
		}
	}

	@Override
	protected void onProgressUpdate(String... progress) {
	}

	@Override
	protected void onPostExecute(Boolean result) {     
		Log.i(DEBUG_TAG, "onPostExecute");
		if( mProgress != null ) {
			mProgress.hide();
			mProgress.cancel();
		}
		if( result ) {
			mContext.setStatus(mContext.getResources().getString(R.string.graffity_sent_to, mGraffity.getLocationOnServer()));
			mContext.enableSend(false);
		}
		else {
			mContext.setStatus(mContext.getResources().getString(R.string.upload_failed, mStatus));
		}
	}
}

