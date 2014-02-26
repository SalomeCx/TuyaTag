package project.tuyatag;

import project.tuyatag.R;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;

public class SettingsActivity extends PreferenceActivity
	implements OnSharedPreferenceChangeListener {
	
	private static final String DEBUG_TAG = "Discovart/SettingsActivity";

	public static final String KEY_PREF_AUTHOR = "pref_author";
	public static final String KEY_PREF_SERVER = "pref_server";
	
    @SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        Preference pref;
        
        pref = findPreference(KEY_PREF_AUTHOR);
        pref.setSummary(sharedPreferences.getString(KEY_PREF_AUTHOR, ""));
        
        pref = findPreference(KEY_PREF_SERVER);
        pref.setSummary(sharedPreferences.getString(KEY_PREF_SERVER, getResources().getString(R.string.pref_server_default)));
        
    }

    @SuppressWarnings("deprecation")
	@Override
    protected void onResume() {
        super.onResume();
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @SuppressWarnings("deprecation")
	@Override
    protected void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @SuppressWarnings("deprecation")
    @Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    	Log.d(DEBUG_TAG, "onSharedPreferenceChanged(" + key + ")");
    	
    	if (key.equals(KEY_PREF_AUTHOR)) {
			Preference pref = findPreference(key);
            pref.setSummary(sharedPreferences.getString(key, ""));
        }
        if (key.equals(KEY_PREF_SERVER)) {
            
			Preference pref = findPreference(key);
            pref.setSummary(sharedPreferences.getString(key, ""));
        }
    }
}
