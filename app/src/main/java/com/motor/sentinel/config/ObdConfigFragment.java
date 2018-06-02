package com.motor.sentinel.config;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.widget.Toast;

import com.motor.sentinel.R;

public class ObdConfigFragment
    extends PreferenceFragment
    implements SharedPreferences.OnSharedPreferenceChangeListener {

  public static final String VEHICLE_ID = "conf_vehicle_id";
  public static final String OBD_PROTOCOL = "conf_obd_protocols";
  public static final String IMPERIAL_METRIC = "conf_imperial_metric";
  public static final String OBD_UPDATE_PERIOD = "conf_obd_update_period";
  public static final String BLUETOOTH_TIMEOUT = "conf_bluetooth_timeout";
  public static final String BLUETOOTH_SECURE = "conf_bluetooth_secure";
  public static final String MAC_ADDRESS = "conf_mac_address";
  public static final String DEFAULT_BLUETOOTH_TIMEOUT = "300";  // millis seconds
  public static final long DEFAULT_BLUETOOTH_TIMEOUT_VALUE = 300L;
  private static final long MAX_TIMEOUT_VALUE = 1000L;  // 1000ms

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Load the preferences from an XML resource
    addPreferencesFromResource(R.xml.preferences);
    initialize();
  }

  @Override
  public void onResume() {
    super.onResume();
    getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
  }

  @Override
  public void onPause() {
    super.onPause();
    getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
  }


  /**
   *
   * @param sharedPreferences
   * @param key
   */
  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    if (key.equals(BLUETOOTH_TIMEOUT)) {
      String s = sharedPreferences.getString(key, DEFAULT_BLUETOOTH_TIMEOUT);
      long timeout = 0;
      try {
        timeout = Long.parseLong(s);
      } catch (Exception e) {
        Toast.makeText(getActivity(), R.string.conf_digit_required_maximum, Toast.LENGTH_LONG).show();
      }

      if ( (timeout <= 0) || (timeout > MAX_TIMEOUT_VALUE)) {
        timeout = ObdConfigFragment.DEFAULT_BLUETOOTH_TIMEOUT_VALUE;
      }
    }
  }


  private void initialize() {
    SharedPreferences pref = getPreferenceScreen().getSharedPreferences();
    SharedPreferences.Editor editor = pref.edit();

    editor.clear();
    editor.putString(VEHICLE_ID, pref.getString(VEHICLE_ID, "").trim());
    editor.apply();

    ListPreference obdProtocol = (ListPreference) findPreference(OBD_PROTOCOL);
    obdProtocol.setValue(pref.getString(OBD_PROTOCOL, "0"));

    ListPreference imperial = (ListPreference) findPreference(IMPERIAL_METRIC);
    imperial.setValue(pref.getString(IMPERIAL_METRIC, "0"));

    editor.clear();
    editor.putString(BLUETOOTH_TIMEOUT, pref.getString(BLUETOOTH_TIMEOUT, DEFAULT_BLUETOOTH_TIMEOUT).trim());
    editor.apply();

    ListPreference bluetoothSecure = (ListPreference) findPreference(BLUETOOTH_SECURE);
    bluetoothSecure.setValue(pref.getString(BLUETOOTH_SECURE, "0"));

  }
}
