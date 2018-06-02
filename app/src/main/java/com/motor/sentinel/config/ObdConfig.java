package com.motor.sentinel.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import java.util.regex.Pattern;

public class ObdConfig {

  private SharedPreferences pref;

  public ObdConfig(Context appContext) {
    pref = PreferenceManager.getDefaultSharedPreferences(appContext);
  }

  public void saveMacAddress(String macAddress) {
    SharedPreferences.Editor editor = pref.edit();
    editor.clear();
    editor.putString(ObdConfigFragment.MAC_ADDRESS, macAddress);
    editor.apply();
  }

  /**
   * Mac addresses usually are 6 * 2 hex nibbles separated by colons,
   * and the separators can be any of : or - or . or none.
   * @return boolean
   */
  public boolean isMacAddress() {
    String macAddress=getMacAddress();
    if (macAddress.equals("")){
      return false;
    }
    Pattern patternMac = Pattern.compile("^([a-fA-F0-9]{2}[:\\.-]?){5}[a-fA-F0-9]{2}$");
    return (patternMac.matcher(macAddress).find());
  }

  public String getMacAddress() {
    return pref.getString(ObdConfigFragment.MAC_ADDRESS, "");
  }

  /** ----preference return value ---- */
  public String getVehicleId() {
    return pref.getString(ObdConfigFragment.VEHICLE_ID, "").trim();
  }

  /** @return String, depends on elm327 config, checks preferences.xml */
  public String getObdProtocol() {
    return pref.getString(ObdConfigFragment.OBD_PROTOCOL, "0");
  }

  /**
   * pre-defined unit: imperial(0), metric(1)
   *
   * @return true: unit is imperial, false: metric
   */
  public boolean isImperial() {
    return (pref.getString(ObdConfigFragment.IMPERIAL_METRIC, "0").equals("0")) ? true : false;
  }

  /**
   * saved unit: millis seconds
   *
   * @return millis seconds
   */
  public long getBluetoothTimeout() {
    String s =
        pref.getString(
                ObdConfigFragment.BLUETOOTH_TIMEOUT, ObdConfigFragment.DEFAULT_BLUETOOTH_TIMEOUT)
            .trim();
    long timeout = ObdConfigFragment.DEFAULT_BLUETOOTH_TIMEOUT_VALUE;

    try {
      timeout = Long.parseLong(s);
    } catch (Exception e) {
    }

    if (timeout <= 0) {
      timeout = ObdConfigFragment.DEFAULT_BLUETOOTH_TIMEOUT_VALUE;
    }

    return timeout;
  }

  /** @return true: bluetooth connecting secured, false: insecure */
  public boolean isSecure() {
    return (pref.getString(ObdConfigFragment.BLUETOOTH_SECURE, "0").equals("0")) ? true : false;
  }
}
