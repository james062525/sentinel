package com.motor.sentinel.connecting.bluetooth;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.widget.Toast;

import com.motor.sentinel.R;
import com.motor.sentinel.connecting.RequestCode;

/**
 * To enable bluetooth device
 */

public class RequestEnable implements RequestCode {

  /**
   *
   * @return false: if bluetooth device is not available
   *         true: to intent 'BluetoothAdapter.ACTION_REQUEST_ENABLE' if device is not enabled
   */
  public boolean enableIntent(Activity activity, Fragment fragment) {
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    if (bluetoothAdapter == null) {
      Toast.makeText(activity, R.string.no_bluetooth, Toast.LENGTH_LONG).show();
      return false;
    }

    try {
      if (!bluetoothAdapter.isEnabled()) {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        fragment.startActivityForResult(intent, REQUEST_ENABLE_BT);
      }
    } catch (Exception e) {}

    return true;
  }
}
