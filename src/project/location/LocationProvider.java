package project.location;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.IntentSender;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import project.tuyatag.R;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class LocationProvider implements
	LocationListener,
	GooglePlayServicesClient.ConnectionCallbacks,
	GooglePlayServicesClient.OnConnectionFailedListener {
	
	public interface OnLocationChanged {
		public void onLocationUpdated(Location location);
	}
	
	Activity mContext;
	OnLocationChanged mCallback;
	
    // A request to connect to Location Services
    private LocationRequest mLocationRequest;

    // Stores the current instantiation of the location client in this object
    private LocationClient mLocationClient;

    // Whether updates should start in onConnected or not
    private Boolean mUpdateRequested;
    
    private static final String DEBUG_TAG = "Discovart/LocationProvider";

    /**
     * Constructor
     */
	public LocationProvider( Activity context ) {
		mContext = context;
		mCallback = (OnLocationChanged)context;
		
		// Create a new global location parameters object
		mLocationRequest = LocationRequest.create();
		
        // Set the update interval
        mLocationRequest.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);

        // Use high accuracy
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Set the interval ceiling to one minute
        mLocationRequest.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);

        // Create a new location client, using the enclosing class to handle callbacks.
        mLocationClient = new LocationClient(mContext, this, this);
        
        mUpdateRequested = false;
	}
	
	
    /*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle bundle) {
        Log.i(DEBUG_TAG, mContext.getString(R.string.connected));

        if( mUpdateRequested == true ) {
        	startPeriodicUpdates();
        }
    }

    /*
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
    @Override
    public void onDisconnected() {
    	Log.i(DEBUG_TAG, mContext.getString(R.string.disconnected));
    }

    /*
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult( mContext, LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

                /*
                * Thrown if Google Play services canceled the original
                * PendingIntent
                */
            } catch (IntentSender.SendIntentException e) {

                // Log the error
                e.printStackTrace();
            }
        } else {

            // If no resolution is available, display a dialog to the user with the error.
            showErrorDialog(connectionResult.getErrorCode());
        }
    }

    /**
     * Report location updates to the UI.
     *
     * @param location The updated location.
     */
    @Override
    public void onLocationChanged(Location location) {
    	//Log.i(DEBUG_TAG, mContext.getString(R.string.location_updated) + " = " + LocationUtils.getLatLng(mContext, location));
    	mCallback.onLocationUpdated( location );
    }

    /**
     * Connect the client
     */
    public void connect() {
    	mLocationClient.connect();
    }

    /**
     * Disconnect the client
     */
    public void disconnect() {
    	mLocationClient.disconnect();
    }
    
    /**
     * In response to a request to start updates, send a request
     * to Location Services
     */
    public void startPeriodicUpdates() {
		mUpdateRequested = true;

		if( servicesConnected() ) {
			if( ! mLocationClient.isConnected() ) {
				return;
			}
			mLocationClient.requestLocationUpdates(mLocationRequest, this);
			Log.i(DEBUG_TAG, mContext.getString(R.string.location_requested));
		}
    }

    /**
     * In response to a request to stop updates, send a request to
     * Location Services
     */
    public void stopPeriodicUpdates() {
    	mUpdateRequested = false;
    	
    	if( servicesConnected() ) {
			if( ! mLocationClient.isConnected() ) {
				return;
			}
    		mLocationClient.removeLocationUpdates(this);
    		Log.i(DEBUG_TAG, mContext.getString(R.string.location_updates_stopped));
    	}
    }
    
    /**
     * Verify that Google Play services is available before making a request.
     *
     * @return true if Google Play services is available, otherwise false
     */
    private boolean servicesConnected() {

        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d(DEBUG_TAG, mContext.getString(R.string.play_services_available));

            // Continue
            return true;
        // Google Play services was not available for some reason
        } else {
            // Display an error dialog
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, mContext, 0);
            if (dialog != null) {
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(dialog);
                errorFragment.show(mContext.getFragmentManager(), DEBUG_TAG);
            }
            return false;
        }
    }

//    public Location getLocation() {
//
//        // If Google Play Services is available
//        if (servicesConnected()) {
//        	return mLocationClient.getLastLocation();
//        }
//        return null;
//    }

    /**
     * Define a DialogFragment to display the error dialog generated in
     * showErrorDialog.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class ErrorDialogFragment extends DialogFragment {

        // Global field to contain the error dialog
        private Dialog mDialog;

        /**
         * Default constructor. Sets the dialog field to null
         */
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        /**
         * Set the dialog to display
         *
         * @param dialog An error dialog
         */
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        /*
         * This method must return a Dialog to the DialogFragment.
         */
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

    /**
     * Show a dialog returned by Google Play services for the
     * connection error code
     *
     * @param errorCode An error code returned from onConnectionFailed
     */
    private void showErrorDialog(int errorCode) {

        // Get the error dialog from Google Play services
        Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
            errorCode,
            mContext,
            LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

        // If Google Play services can provide an error dialog
        if (errorDialog != null) {

            // Create a new DialogFragment in which to show the error dialog
            ErrorDialogFragment errorFragment = new ErrorDialogFragment();

            // Set the dialog in the DialogFragment
            errorFragment.setDialog(errorDialog);

            // Show the error dialog in the DialogFragment
            errorFragment.show(mContext.getFragmentManager(), DEBUG_TAG);
        }
    }

}
