package project.tuyatag;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import project.tuyatag.R;

import project.location.LocationProvider;
import project.location.LocationUtils;
import android.location.Location;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64InputStream;
import android.util.Base64OutputStream;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class TakePictureActivity extends Activity implements LocationProvider.OnLocationChanged {
	
	private ImageView mImagePreview;
	private ImageButton mButtonLocation;
	private TextView mTextLocation;
	private ImageButton mButtonRefresh;
	private ImageButton mButtonTake;
	private EditText mEditComments;
	private ImageButton mButtonSend;
	private TextView mTextStatus;
	
	private Graffiti mGraffiti;
	private LocationProvider mLocationProvider;
	private Graffiti.Location mLastLocation;
	private Animation mRefreshAnimation;
	private Boolean mUpdateLocationRequested;
	private Boolean mTakingPicture;

	
	// Handle to SharedPreferences for this app
    private SharedPreferences mPrefs;

    // Handle to a SharedPreferences editor
    private SharedPreferences.Editor mPrefEditor;

    private static final String DEBUG_TAG = "Discovart/Activity";
    private static final String SHARED_PREFERENCES = "fr.irisa.discovart.SHARED_PREFERENCES";

	private static final int TAKE_PICTURE_REQUEST = 1;

	private static final String GRAFFITY_ALBUM = "graffity";
	private static final String GRAFFITY_IMAGE_PREFIX = "graffity_";
	private static final String GRAFFITY_IMAGE_SUFFIX = ".jpg";
	private static final String CAMERA_DIR = "/dcim/";

    /*
     * Initialize the Activity
     */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(DEBUG_TAG, "onCreate");

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tuyatag);
		
		mImagePreview = (ImageView) findViewById(R.id.image_preview);
		mButtonLocation = (ImageButton) findViewById(R.id.button_location);
		mTextLocation = (TextView) findViewById(R.id.text_location);
		mButtonRefresh = (ImageButton) findViewById(R.id.button_refresh);
		mButtonTake = (ImageButton) findViewById(R.id.button_take);
		mEditComments = (EditText) findViewById(R.id.edit_comments);
		mButtonSend = (ImageButton) findViewById(R.id.button_send);
		mTextStatus = (TextView) findViewById(R.id.text_status);
		//Toast.makeText(this,"bouton take picture", Toast.LENGTH_LONG).show();
	    mRefreshAnimation = AnimationUtils.loadAnimation(this, R.anim.clockwise_refresh);
	    mRefreshAnimation.setRepeatCount(Animation.INFINITE);


	    mGraffiti = null;
	    mLocationProvider = new LocationProvider(this);
	    mLastLocation = null;
	    mUpdateLocationRequested = false;
		mTakingPicture = false;

	    enableTake(true);
	    enableSend(false);
	    
	    // Open Shared Preferences
        mPrefs = getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);

        // Get an editor
        mPrefEditor = mPrefs.edit();

	    
		if( mPrefs.contains("graffity") ) {
			try {
				mGraffiti = (Graffiti) new ObjectInputStream(new Base64InputStream(
	                new ByteArrayInputStream(mPrefs.getString("graffity", "").getBytes()), 0)).readObject();
				mLastLocation = mGraffiti.getLocation();
				Log.d(DEBUG_TAG, "Restored graffity:");
				mGraffiti.issueLog();
			} catch (Exception e) {
				Log.d(DEBUG_TAG, "Unable to read serialized graffity");
				e.printStackTrace();
				mGraffiti = null;
			}
		}
	    if( mPrefs.contains("comments") ) {
	    	mEditComments.setText(mPrefs.getString("comments", ""));
	    }

	    mImagePreview.setOnClickListener(new OnClickListener() {	
			@Override
			public void onClick(View v) {
				if (mGraffiti == null) {
					return;
				}
				if (! isIntentAvailable(getApplicationContext(),Intent.ACTION_VIEW)) {
					return;
	    		}
				Intent intent = new Intent(Intent.ACTION_VIEW);
				File file = new File(mGraffiti.getPhotoPath());
				intent.setDataAndType(Uri.fromFile(file), "image/*");
				startActivity(intent);
			}
		});
	    
	    mButtonLocation.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if( mTextLocation.getText().toString() == "" ) {
					return;
				}
				if (! isIntentAvailable(getApplicationContext(),Intent.ACTION_VIEW)) {
					return;
	    		}
				Uri locationUri = Uri.parse("geo:"+Double.toString(mLastLocation.getLatitude())+","+Double.toString(mLastLocation.getLongitude()));
				Intent intent = new Intent(Intent.ACTION_VIEW, locationUri);
				startActivity(intent);
			}
		});
	    
	    mButtonRefresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(DEBUG_TAG, "Location update requested by click");
				updateLocation();
			}
		});
	    
	    mButtonTake.setOnClickListener(new OnClickListener() {
	        @Override
	        public void onClick(View v) {
	        	Log.i(DEBUG_TAG, "Taking a picture");
	        	takePicture();
	        }
	    });

	    mButtonSend.setOnClickListener(new OnClickListener() {
	        @Override
	        public void onClick(View v) {
	        	Log.i(DEBUG_TAG, "Sending the data");
	        	sendData();
	        }
	    });

	    mEditComments.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if( mGraffiti != null && mEditComments.getText() != null) {
	    			mGraffiti.setComment(mEditComments.getText().toString());
	    			mGraffiti.setOnServer(false);
	    			mTextStatus.setText("");
	    			enableSend(true);
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,	int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {}
		});
	    
	}

    /*
     * Called when the Activity is restarted, even before it becomes visible.
     */
	@Override
	protected void onStart() {
		Log.d(DEBUG_TAG, "onStart");
		super.onStart();
		
		if( ! mTakingPicture ) {
			/*
			 * Connect the client. Don't re-start any requests here;
			 * instead, wait for onResume()
			 */
			mLocationProvider.connect();
		}
	}
	
    /*
     * Called when the system detects that this Activity is now visible.
     */
	@Override
	protected void onResume() {
		Log.d(DEBUG_TAG, "onResume");
		super.onResume();

		if( ! mTakingPicture ) {
			startLocationUpdate();
			
			if( mLastLocation == null ) {
				updateLocation();
			}
		}
		
		updatePreview();
	}
	
	/*
     * Called when the Activity is going into the background.
     * Parts of the UI may be visible, but the Activity is inactive.
     */
	@Override
	protected void onPause() {
		Log.d(DEBUG_TAG, "onPause");

		if( ! mTakingPicture ) {
			stopLocationUpdate();
		}
		
		super.onPause();
	}
	
    /*
     * Called when the Activity is no longer visible at all.
     * Stop updates and disconnect.
     */
	@Override
	protected void onStop() {
		Log.d(DEBUG_TAG, "onStop");

		if( ! mTakingPicture ) {
			// After disconnect() is called, the client is considered "dead".
			mLocationProvider.disconnect();
		}
		
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		Log.d(DEBUG_TAG, "onDestroy");
		super.onDestroy();

		if( mGraffiti != null ) {
		    ByteArrayOutputStream out = new ByteArrayOutputStream();
		    try {
		        new ObjectOutputStream(out).writeObject(mGraffiti);
		        byte[] data = out.toByteArray();
		        out.close();

		        out = new ByteArrayOutputStream();
		        Base64OutputStream b64 = new Base64OutputStream(out, 0);
		        b64.write(data);
		        b64.close();
		        out.close();

		        mPrefEditor.putString("graffity", new String(out.toByteArray()));

		    } catch (IOException e) {
				Log.d(DEBUG_TAG, "Unable to serialize graffity");
		        e.printStackTrace();
		    }

		}
		if( mEditComments.getText() != null ) {
			mPrefEditor.putString("comments", mEditComments.getText().toString());
		}

		mPrefEditor.commit();

	}	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.discovart, menu);
        return super.onCreateOptionsMenu(menu);
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
		if (itemId == R.id.action_clean) {
			clean();
			return true;
		} else if (itemId == R.id.action_settings) {
			openSettings();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
    	
    }

    @Override
	public void onLocationUpdated( Location location ) {
    	if( location == null ) {
    		return;
    	}
    	
    	if( mUpdateLocationRequested ) {
    		mUpdateLocationRequested = false;
        	mLastLocation = new Graffiti.Location(location);
    		mTextLocation.setText(LocationUtils.getLatLng(this, location));
    		mButtonRefresh.clearAnimation();
    		
    		if( mGraffiti != null ) {
    			mGraffiti.setLocation(mLastLocation);
    		}
    		
    		Log.d(DEBUG_TAG, "Location updated");
    	}
    }
    
    /*
     * Clean current graffity, comments and saved preferences
     */
    private void clean() {
        mGraffiti = null;
        updateLocation();
        updatePreview();
        mEditComments.setText("");
        enableSend(false);
        
        mPrefEditor.clear();
        mPrefEditor.commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	switch (requestCode) {
    	case TAKE_PICTURE_REQUEST:
    		if (resultCode == RESULT_OK) {
    			if (mGraffiti == null ) {
    				Log.e(DEBUG_TAG, "Error: graffity should not be null at this point.");
    			}

    			// Add orientation
    			try {
    				ExifInterface exif = new ExifInterface(mGraffiti.getPhotoPath());
    				int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
    				mGraffiti.setOrientation(orientation);
    				switch( orientation ) {
    				case ExifInterface.ORIENTATION_NORMAL:
    					mGraffiti.setRotation(0);
    					break;
    				case ExifInterface.ORIENTATION_ROTATE_90:
    					mGraffiti.setRotation(90);
    					break;
    				case ExifInterface.ORIENTATION_ROTATE_180:
    					mGraffiti.setRotation(180);
    					break;
    				case ExifInterface.ORIENTATION_ROTATE_270:
    					mGraffiti.setRotation(270);
    					break;
    				case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
    				case ExifInterface.ORIENTATION_FLIP_VERTICAL:
    				case ExifInterface.ORIENTATION_TRANSPOSE:
    				case ExifInterface.ORIENTATION_TRANSVERSE:
    				case ExifInterface.ORIENTATION_UNDEFINED:
    					mGraffiti.setRotation(-1);
    				}
    			} catch (IOException e) {
    				mGraffiti.setRotation(-1);
    				mGraffiti.setOrientation(-1);
    			}

    			if (mEditComments.getText() != null) {
    				mGraffiti.setComment(mEditComments.getText().toString());
    			}

    			enableSend(true);
    			mTextStatus.setText("");
    		}
    		else {
    			mGraffiti = null;
    			enableSend(false);
    		}

			mTakingPicture = false;
    		updatePreview();
    		break;

    	case LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST :
    		/* The method onConnectionFailed() in LocationUpdateRemover and LocationUpdateRequester
    	     * may call startResolutionForResult() to start an Activity that handles Google
    	     * Play services problems. The result of this call returns here, to onActivityResult.
    	     */
    		switch (resultCode) {
    		// If Google Play services resolved the problem
    		case Activity.RESULT_OK:

    			// Log the result
    			Log.d(DEBUG_TAG, getString(R.string.resolved));

    			// Display the result
    			mTextStatus.setText(R.string.connected + " - " + R.string.resolved);
    			break;

    			// If any other result was returned by Google Play services
    		default:
    			// Log the result
    			Log.d(DEBUG_TAG, getString(R.string.no_resolution));

    			// Display the result
    			mTextStatus.setText(R.string.disconnected + " - " + R.string.no_resolution);

    			break;
    		}
    		break;
    	}
    }

	private File getAlbumDir() {
		File storageDir = null;

		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			storageDir = new File( Environment.getExternalStorageDirectory() + CAMERA_DIR + GRAFFITY_ALBUM );
			if (storageDir != null) {
				if (! storageDir.mkdirs()) {
					if (! storageDir.exists()){
						Log.d(DEBUG_TAG, "failed to create directory");
						return null;
					}
				}
			}
		} else {
			Log.v(DEBUG_TAG, "External storage is not mounted READ/WRITE.");
		}
		
		return storageDir;
	}

	@SuppressLint("SimpleDateFormat")
	private File createImageFile() throws IOException {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		Log.d(DEBUG_TAG, "timeStamp = " + timeStamp);
		//String timeStamp2 = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.US).format(new Date());
		//Log.d(DEBUG_TAG, "timeStamp2 = " + timeStamp2);
		String imageFileName = GRAFFITY_IMAGE_PREFIX + timeStamp;
		File albumF = getAlbumDir();
		File imageF = File.createTempFile(imageFileName, GRAFFITY_IMAGE_SUFFIX, albumF);
		return imageF;
	}

	private void takePicture() {
	    if (! isIntentAvailable(this, MediaStore.ACTION_IMAGE_CAPTURE)) {
	        AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        builder.setTitle(R.string.camera_unavailable);
	        builder.setIcon(android.R.drawable.ic_dialog_alert);
	        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
	        		@Override
					public void onClick(DialogInterface dialog, int id) {
	        		}
	        	});
	        builder.create().show();
	    	return;
	    }

		mTakingPicture = true;

		/*
		 * Create graffity and request location update,
		 * so graffity location will be updated on location update.
		 */
		mGraffiti = new Graffiti();
		updateLocation();
		
		File f = null;
		try {
			f = createImageFile();
		}
		catch(IOException e) {
			e.printStackTrace();
			mTextStatus.setText("Error: unable to create file to store graffity image.");
			return;
		}
		mGraffiti.setPhotoPath(f.getAbsolutePath());
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
		startActivityForResult(takePictureIntent, TAKE_PICTURE_REQUEST);
	}
    
	private void sendData() {
		if (mGraffiti.isOnServer() ) {
			// TODO: Toast;
			
			return;
		}

		// Check network
		ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo == null || ! networkInfo.isConnected()) {
	        AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        builder.setTitle(R.string.network_unavailable);
	        builder.setIcon(android.R.drawable.ic_dialog_alert);
	        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
	        		@Override
					public void onClick(DialogInterface dialog, int id) {
	        		}
	        	});
	        builder.create().show();
			return;
		}
		

		// create an async task to send the graffity
		Uploader upl = new Uploader(this, mGraffiti);

		upl.execute();
	}

	private void updatePreview() {
		/* There isn't enough memory to open up more than a couple camera photos */
		/* So pre-scale the target bitmap into which the file is decoded */

		/* Get the size of the image */
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		
		if( mGraffiti != null && (new File(mGraffiti.getPhotoPath())).exists() ) {
			BitmapFactory.decodeFile(mGraffiti.getPhotoPath(), bmOptions);
		}
		
		if( mGraffiti == null || ! (new File(mGraffiti.getPhotoPath())).exists() || bmOptions.outHeight == -1 ) {
			mImagePreview.setImageDrawable(null);
			//mImagePreview.setBackgroundResource(android.R.color.darker_gray);
			//mImagePreview.setAlpha(0.7f);
			mImagePreview.setBackgroundResource(R.color.preview_background_transparent);
			if( mLastLocation == null ) {
				mTextLocation.setText("");
			}
			else {
				mTextLocation.setText(LocationUtils.getLatLng(this, mLastLocation.getLatitude(), mLastLocation.getLongitude()));
			}
			return;
		}
		
		int photoW = bmOptions.outWidth;
		int photoH = bmOptions.outHeight;
		
		Log.d(DEBUG_TAG, "Bitmap size = " + photoH + "x" + photoW + " (rot = " + mGraffiti.getOrientation() + ")");

		
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		int targetH = displaymetrics.heightPixels;
		int targetW = displaymetrics.widthPixels;

		Log.d(DEBUG_TAG, "Screen size = " + targetH + "x" + targetW + " (rot = " + getWindowManager().getDefaultDisplay().getRotation() + ")" );

		// Reduce the target size according to the layout
		switch( getWindowManager().getDefaultDisplay().getRotation() ) {
		case Surface.ROTATION_0:
		case Surface.ROTATION_180:
			targetH /= 2;
			break;
		case Surface.ROTATION_90:
		case Surface.ROTATION_270:
			targetW /=2;
		}
		
		if( mGraffiti.getRotation() == 90 || mGraffiti.getRotation() == 270 ) {
			int tmp = photoH;
			photoH = photoW;
			photoW = tmp;
		}

		/* Figure out which way needs to be reduced less */
		int scaleFactor = 1;
		if ((targetW > 0) || (targetH > 0)) {
			scaleFactor = Math.max((int)Math.ceil((double)photoW/targetW), (int)Math.ceil((double)photoH/targetH));
		}
		
		/* Set bitmap options to scale the image decode target */
		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = scaleFactor;
		bmOptions.inPurgeable = true;

		/* Decode the JPEG file into a Bitmap */
		Bitmap bitmap = BitmapFactory.decodeFile(mGraffiti.getPhotoPath(), bmOptions);

		/* Associate the Bitmap to the ImageView */
		mImagePreview.setBackgroundResource(android.R.color.transparent);
		//mImagePreview.setAlpha(1.0f);

		// If the image is rotated, we need to scale the image precisely
		// because the ImageView scaling seems to be applied before rotation...
		photoH = bitmap.getHeight();
		photoW = bitmap.getWidth();

		// calculate the scale
		float scale = Math.max(((float) targetH) / photoH, ((float) targetW) / photoW);

		// create a matrix for the manipulation
		Matrix matrix = new Matrix();
		// resize the bit map
		matrix.postScale(scale, scale);
		// rotate the Bitmap
		matrix.postRotate(mGraffiti.getRotation());

		// recreate the new Bitmap
		Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, photoW, photoH, matrix, true);

		mImagePreview.setImageBitmap(resizedBitmap);

//		// Code for using coordinates from EXIF if any
//		try {
//			ExifInterface exif = new ExifInterface(graffity_filename);
//			String latitude = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
//			String longitude = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
//			text_location.setText(latitude + " " + longitude);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		
		mTextLocation.setText(LocationUtils.getLatLng(this, mGraffiti.getLatitude(), mGraffiti.getLongitude()));
	}
	
	// to check if Intent is available
	private static boolean isIntentAvailable(Context context, String action) {
		final PackageManager packageManager = context.getPackageManager();
		final Intent intent = new Intent(action);
		List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	 }

	public void enableTake( Boolean b ) {
    	mButtonTake.setClickable(b);
    	mButtonTake.setEnabled(b);		
	}
	public void enableSend( Boolean b ) {
    	mButtonSend.setClickable(b);
    	mButtonSend.setEnabled(b);		
	}
	
	public void setStatus(String status ) {
		mTextStatus.setText(status);
	}
	
	private void startLocationUpdate() {
		mLocationProvider.startPeriodicUpdates();
	}
	
	private void stopLocationUpdate() {
		mLocationProvider.stopPeriodicUpdates();
	}
	
	private void updateLocation() {
		mButtonRefresh.startAnimation(mRefreshAnimation);
		mUpdateLocationRequested = true;
	}

	private void openSettings() {
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}
}
